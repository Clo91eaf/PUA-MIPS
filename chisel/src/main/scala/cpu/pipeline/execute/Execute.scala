package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.defines._

class Execute extends Module {
  val io = IO(new Bundle {
    val fromAlu            = Flipped(new ALU_Execute())
    val fromMul            = Flipped(new Mul_Execute())
    val fromDiv            = Flipped(new Div_Execute())
    val fromMov            = Flipped(new Mov_Execute())
    val fromDataMemory     = Flipped(new DataMemory_Execute())
    val fromExecuteStage   = Flipped(new ExecuteStage_Execute())
    val fromMemoryStage    = Flipped(new MemoryStage_Execute())
    val fromMemory         = Flipped(new Memory_Execute())
    val fromHILO           = Flipped(new HILO_Execute())
    val fromWriteBackStage = Flipped(new WriteBackStage_Execute())

    val alu          = new Execute_ALU()
    val mul          = new Execute_Mul()
    val div          = new Execute_Div()
    val mov          = new Execute_Mov()
    val decoder      = new Execute_Decoder()
    val memoryStage  = new Execute_MemoryStage()
    val dataMemory   = new Execute_DataMemory()
    val executeStage = new Execute_ExecuteStage()
  })
  // input-execute stage
  val pc        = io.fromExecuteStage.pc
  val aluop     = io.fromExecuteStage.aluop
  val alusel    = io.fromExecuteStage.alusel
  val reg1      = io.fromExecuteStage.reg1
  val reg2      = io.fromExecuteStage.reg2
  val reg_waddr = io.fromExecuteStage.reg_waddr
  val wreg_i    = io.fromExecuteStage.reg_wen
  val inst      = io.fromExecuteStage.inst
  val es_valid  = io.fromExecuteStage.valid
  val cp0_addr  = io.fromExecuteStage.cp0_addr
  val bd        = io.fromExecuteStage.bd

  // input-hilo
  val hi_i = io.fromHILO.hi
  val lo_i = io.fromHILO.lo

  // input-write back stage
  val wb_hi_i    = io.fromWriteBackStage.hi
  val wb_lo_i    = io.fromWriteBackStage.lo
  val wb_whilo_i = io.fromWriteBackStage.whilo

  // input-memory
  val mem_hi_i    = io.fromMemory.hi
  val mem_lo_i    = io.fromMemory.lo
  val mem_whilo_i = io.fromMemory.whilo

  // input-memory stage
  val hilo_temp_i = io.fromMemoryStage.hilo
  val cnt_i       = io.fromMemoryStage.cnt

  // input-execute stage
  val link_addr       = io.fromExecuteStage.link_addr
  val is_in_delayslot = io.fromExecuteStage.is_in_delayslot

  // output
  val reg_wen        = Wire(REG_WRITE_BUS)
  val reg_wdata      = Wire(BUS)
  val hi             = Wire(BUS)
  val lo             = Wire(BUS)
  val whilo          = Wire(Bool())
  val hilo_temp_o    = Wire(DOUBLE_BUS)
  val cnt            = Wire(CNT_BUS)
  val mem_addr_temp  = Wire(BUS)
  val stallreq       = Wire(Bool())
  val allowin        = Wire(Bool())
  val es_to_ms_valid = Wire(Bool())
  val blk_valid      = Wire(Bool())
  val es_fwd_valid   = Wire(Bool())
  val badvaddr       = Wire(BUS)
  val excode         = Wire(UInt(5.W))
  val ex             = Wire(Bool())
  val no_store       = Wire(Bool())

  // data sram
  val addr_ok_r         = RegInit(false.B)
  val mem_we            = WireInit(WRITE_DISABLE)
  val mem_re            = WireInit(WRITE_DISABLE)
  val addrLowBit2       = mem_addr_temp(1, 0)
  val data_sram_req     = es_valid && !addr_ok_r && (mem_we || mem_re) && !no_store
  val data_sram_wr      = mem_we
  val data_sram_size    = WireInit(0.U(2.W))
  val data_sram_addr    = Wire(BUS)
  val data_sram_wstrb   = Wire(UInt(4.W))
  val data_sram_wdata   = Wire(BUS)
  val data_buff         = RegInit(BUS_INIT)
  val data_buff_valid   = RegInit(false.B)
  val addr_ok           = data_sram_req && io.fromDataMemory.addr_ok || addr_ok_r
  val data_sram_data_ok = io.fromDataMemory.data_ok && io.fromMemory.inst_unable
  val data_ok           = Wire(Bool())
  val data              = Wire(BUS)

  // mem_we
  switch(aluop) {
    is(EXE_SB_OP, EXE_SH_OP, EXE_SW_OP, EXE_SWL_OP, EXE_SWR_OP, EXE_SC_OP) {
      mem_we := WRITE_ENABLE
    }
  }

  // mem_re
  switch(aluop) {
    is(EXE_LB_OP, EXE_LBU_OP, EXE_LH_OP, EXE_LHU_OP, EXE_LW_OP, EXE_LL_OP, EXE_LWL_OP, EXE_LWR_OP) {
      mem_re := CHIP_ENABLE
    }
  }

  // size
  switch(aluop) {
    is(EXE_LW_OP, EXE_SW_OP) {
      data_sram_size := 2.U
    }
    is(EXE_LH_OP, EXE_LHU_OP, EXE_SH_OP) {
      data_sram_size := 1.U
    }
    is(EXE_LB_OP, EXE_LBU_OP, EXE_SB_OP) {
      data_sram_size := 0.U
    }
    is(EXE_LWL_OP, EXE_SWL_OP) {
      data_sram_size := Mux(addrLowBit2 === 3.U, 2.U, addrLowBit2)
    }
    is(EXE_LWR_OP, EXE_SWR_OP) {
      data_sram_size := Mux(addrLowBit2 === 0.U, 2.U, 3.U - addrLowBit2)
    }
  }

  // addr
  data_sram_addr := Mux(
    (aluop === EXE_LWL_OP || aluop === EXE_SWL_OP),
    Cat(addrLowBit2, 0.U(2.W)),
    mem_addr_temp,
  )

  // wstrb
  data_sram_wstrb := MuxLookup(
    aluop,
    "b1111".U, // default SW,SC
    Seq(
      EXE_SB_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b0001".U,
          "b01".U -> "b0010".U,
          "b10".U -> "b0100".U,
          "b11".U -> "b1000".U,
        ),
      ),
      EXE_SH_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b0011".U,
          "b10".U -> "b1100".U,
        ),
      ),
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b11".U -> "b1111".U,
          "b10".U -> "b0111".U,
          "b01".U -> "b0011".U,
          "b00".U -> "b0001".U,
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b11".U -> "b1000".U,
          "b10".U -> "b1100".U,
          "b01".U -> "b1110".U,
          "b00".U -> "b1111".U,
        ),
      ),
    ),
  )

  // wdata
  data_sram_wdata := MuxLookup(
    aluop,
    BUS_INIT,
    Seq(
      EXE_SB_OP -> Fill(4, reg2(7, 0)),
      EXE_SH_OP -> Fill(2, reg2(15, 0)),
      EXE_SW_OP -> reg2,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Cat(0.U(8.W), reg2(31, 24)),
          "b01".U -> Cat(0.U(16.W), reg2(31, 16)),
          "b10".U -> Cat(0.U(24.W), reg2(31, 8)),
          "b11".U -> reg2,
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> reg2,
          "b01".U -> Cat(reg2(23, 0), 0.U(8.W)),
          "b10".U -> Cat(reg2(15, 0), 0.U(16.W)),
          "b11".U -> Cat(reg2(7, 0), 0.U(24.W)),
        ),
      ),
      // EXE_SC_OP -> Mux(LLbit, data, ZERO_WORD)
    ),
  )
  // output-memory stage
  io.memoryStage.pc       := pc
  io.memoryStage.ex       := ex
  io.memoryStage.bd       := bd
  io.memoryStage.badvaddr := badvaddr
  io.memoryStage.cp0_addr := cp0_addr
  io.memoryStage.excode   := excode

  // output-decoder
  io.decoder.reg_wen      := reg_wen
  io.decoder.reg_waddr    := reg_waddr
  io.decoder.reg_wdata    := reg_wdata
  io.decoder.aluop        := aluop
  io.decoder.blk_valid    := blk_valid
  io.decoder.allowin      := allowin
  io.decoder.inst_is_mfc0 := io.fromExecuteStage.valid && (aluop === EXE_MFC0_OP)
  io.decoder.es_fwd_valid := es_fwd_valid

  // output-memory stage
  io.memoryStage.reg_wen         := reg_wen
  io.memoryStage.reg_waddr       := reg_waddr
  io.memoryStage.reg_wdata       := reg_wdata
  io.memoryStage.hi              := hi
  io.memoryStage.lo              := lo
  io.memoryStage.whilo           := whilo
  io.memoryStage.hilo            := hilo_temp_o
  io.memoryStage.cnt             := cnt
  io.memoryStage.aluop           := aluop
  io.memoryStage.reg2            := reg2
  io.memoryStage.valid           := es_to_ms_valid
  io.memoryStage.is_in_delayslot := is_in_delayslot
  io.memoryStage.mem_addr        := mem_addr_temp
  io.memoryStage.data_ok         := data_ok
  io.memoryStage.data            := data
  io.memoryStage.wait_mem        := es_valid && addr_ok

  // output-execute stage
  io.executeStage.allowin := allowin

  // output-data memory
  io.dataMemory.aluop       := aluop
  io.dataMemory.addrLowBit2 := addrLowBit2
  io.dataMemory.req         := data_sram_req
  io.dataMemory.wr          := data_sram_wr
  io.dataMemory.size        := data_sram_size
  io.dataMemory.addr        := data_sram_addr
  io.dataMemory.wdata       := data_sram_wdata
  io.dataMemory.wstrb       := data_sram_wstrb
  io.dataMemory.waiting     := es_valid && addr_ok && !data_ok

  when(data_sram_req && io.fromDataMemory.addr_ok && !io.fromMemory.allowin) {
    addr_ok_r := true.B
  }.elsewhen(io.fromMemory.allowin) {
    addr_ok_r := false.B
  }
  when(io.fromMemory.allowin || no_store) {
    data_buff_valid := false.B
    data_buff       := BUS_INIT
  }.elsewhen(addr_ok && data_sram_data_ok && !io.fromMemory.allowin) {
    data_buff_valid := true.B
    data_buff       := io.fromDataMemory.rdata
  }

  data_ok := data_buff_valid || (addr_ok && data_sram_data_ok)
  data    := Mux(data_buff_valid, data_buff, io.fromDataMemory.rdata)

  // io-finish

  no_store := io.fromMemory.ex | io.fromWriteBackStage.ex |
    ex | io.fromMemory.eret | io.fromWriteBackStage.eret

  val ready_go = Wire(Bool())
  val load_op  = Wire(Bool())

  when(
    aluop === EXE_LB_OP || aluop === EXE_LBU_OP ||
      aluop === EXE_LH_OP || aluop === EXE_LHU_OP ||
      aluop === EXE_LW_OP || aluop === EXE_LWR_OP ||
      aluop === EXE_LWL_OP || aluop === EXE_LL_OP ||
      aluop === EXE_SC_OP,
  ) {
    load_op := true.B
  }.otherwise {
    load_op := false.B
  }

  val ws_not_eret_ex = !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  blk_valid := es_valid && load_op && ws_not_eret_ex

  ready_go       := Mux((mem_we || mem_re), addr_ok || ex, true.B)
  allowin        := !es_valid || ready_go && io.fromMemory.allowin
  es_to_ms_valid := es_valid && ready_go && ws_not_eret_ex

  es_fwd_valid := es_valid

  // 保存逻辑运算的结果
  val alures                 = Wire(BUS)
  val moveres                = Wire(BUS)        // 保存移动操作运算的结果
  val mulres                 = Wire(DOUBLE_BUS) // 保存乘法结果，宽度为64位
  val HI                     = Wire(BUS)
  val LO                     = Wire(BUS)
  val hilo_temp1             = Wire(DOUBLE_BUS)
  val stallreq_for_madd_msub = Wire(Bool())
  val trapassert             = Wire(Bool())
  val ovassert               = Wire(Bool())

  // liphen
  hilo_temp1 := ZERO_WORD
  //

  mem_addr_temp := reg1 + Util.signedExtend(
    inst(15, 0),
  ) // mem_addr传递到访存阶段，是加载、存储指令对应的存储器地址

  // 根据aluop指示的运算子类型进行运算
  io.alu.op  := aluop
  io.alu.in1 := reg1
  io.alu.in2 := reg2
  alures     := io.fromAlu.out
  trapassert := io.fromAlu.trap
  ovassert   := io.fromAlu.ov

  io.mul.op  := aluop
  io.mul.in1 := reg1
  io.mul.in2 := reg2
  mulres     := io.fromMul.out

  io.div.op       := aluop
  io.div.divisor  := reg1
  io.div.dividend := reg2
  val quotient  = io.fromDiv.quotient
  val remainder = io.fromDiv.remainder

  io.mov.op   := aluop
  io.mov.in   := reg1
  io.mov.inst := inst
  io.mov.hi   := HI
  io.mov.lo   := LO
  moveres     := io.fromMov.out
  // 得到最新的HI、LO寄存器的值，此处要解决指令数据相关问题
  when(reset.asBool === RST_ENABLE) {
    HI := ZERO_WORD
    LO := ZERO_WORD
  }.elsewhen(mem_whilo_i === WRITE_ENABLE) {
    HI := mem_hi_i
    LO := mem_lo_i
  }.elsewhen(wb_whilo_i === WRITE_ENABLE) {
    HI := wb_hi_i
    LO := wb_lo_i
  }.otherwise {
    HI := hi_i
    LO := lo_i
  }

  stallreq := stallreq_for_madd_msub

  // MADD、MADDU、MSUB、MSUBU指令
  // default
  when(reset.asBool === RST_ENABLE) {
    hilo_temp_o            := ZERO_WORD
    cnt                    := 0.U
    stallreq_for_madd_msub := NOT_STOP
  }.otherwise {
    // default
    hilo_temp_o            := ZERO_WORD
    cnt                    := 0.U
    stallreq_for_madd_msub := NOT_STOP
    switch(aluop) {
      is(EXE_MADD_OP, EXE_MADDU_OP) {
        when(cnt_i === 0.U) {
          hilo_temp_o            := mulres
          cnt                    := 1.U
          stallreq_for_madd_msub := STOP
          hilo_temp1             := ZERO_WORD
        }.elsewhen(cnt_i === 1.U) {
          hilo_temp_o            := ZERO_WORD
          cnt                    := 2.U
          hilo_temp1             := hilo_temp_i + Cat(HI, LO)
          stallreq_for_madd_msub := NOT_STOP
        }
      }
      is(EXE_MSUB_OP, EXE_MSUBU_OP) {
        when(cnt_i === 0.U) {
          hilo_temp_o            := ~mulres + 1.U
          cnt                    := 1.U
          stallreq_for_madd_msub := STOP
        }.elsewhen(cnt_i === 1.U) {
          hilo_temp_o            := ZERO_WORD
          cnt                    := 2.U
          hilo_temp1             := hilo_temp_i + Cat(HI, LO)
          stallreq_for_madd_msub := NOT_STOP
        }
      }
    }
  }

  // 根据alusel指示的运算类型，选择一个运算结果作为最终结果
  reg_wen := Mux(ovassert, REG_WRITE_DISABLE, wreg_i)
  reg_wdata := MuxLookup(
    alusel,
    ZERO_WORD,
    Seq(
      EXE_RES_NOP         -> alures,
      EXE_RES_ALU         -> alures,
      EXE_RES_MOV         -> moveres,
      EXE_RES_MUL         -> mulres(31, 0),
      EXE_RES_JUMP_BRANCH -> link_addr,
    ),
  )

  // MTHI和MTLO指令 乘法运算结果保存
  when(reset.asBool === RST_ENABLE) {
    whilo := WRITE_DISABLE
    hi    := ZERO_WORD
    lo    := ZERO_WORD
  }.elsewhen((aluop === EXE_MULT_OP) || (aluop === EXE_MULTU_OP)) {
    whilo := WRITE_ENABLE
    hi    := mulres(63, 32)
    lo    := mulres(31, 0)
  }.elsewhen((aluop === EXE_MADD_OP) || (aluop === EXE_MADDU_OP)) {
    whilo := WRITE_ENABLE
    hi    := hilo_temp1(63, 32)
    lo    := hilo_temp1(31, 0)
  }.elsewhen((aluop === EXE_MSUB_OP) || (aluop === EXE_MSUBU_OP)) {
    whilo := WRITE_ENABLE
    hi    := hilo_temp1(63, 32)
    lo    := hilo_temp1(31, 0)
  }.elsewhen((aluop === EXE_DIV_OP) || (aluop === EXE_DIVU_OP)) {
    whilo := WRITE_ENABLE
    hi    := remainder
    lo    := quotient
  }.elsewhen(aluop === EXE_MTHI_OP) {
    whilo := WRITE_ENABLE
    hi    := reg1
    lo    := LO
  }.elsewhen(aluop === EXE_MTLO_OP) {
    whilo := WRITE_ENABLE
    hi    := HI
    lo    := reg1
  }.otherwise {
    whilo := WRITE_DISABLE
    hi    := ZERO_WORD
    lo    := ZERO_WORD
  }

  val overflow_ex = io.fromExecuteStage.overflow_inst && io.fromAlu.ov
  val st_addr     = addrLowBit2
  val load_ex = ((aluop === EXE_LW_OP) && (st_addr =/= 0.U)) ||
    ((aluop === EXE_LH_OP || aluop === EXE_LHU_OP) && st_addr(0) =/= 0.U)
  val store_ex = (aluop === EXE_SW_OP && (st_addr =/= 0.U)) ||
    (aluop === EXE_SH_OP && (st_addr(0) =/= 0.U))
  val mem_ex = load_ex || store_ex

  ex := (overflow_ex | mem_ex | io.fromExecuteStage.ds_to_es_ex) & es_valid
  badvaddr := Mux(
    io.fromExecuteStage.fs_to_ds_ex,
    io.fromExecuteStage.badvaddr,
    mem_addr_temp,
  )
  excode := MuxCase(
    io.fromExecuteStage.excode,
    Seq(
      io.fromExecuteStage.ds_to_es_ex -> io.fromExecuteStage.excode,
      overflow_ex                     -> EX_OV,
      load_ex                         -> EX_ADEL,
      store_ex                        -> EX_ADES,
    ),
  )
  // debug
  // printf(p"execute :pc 0x${Hexadecimal(pc)}\n")
}
