package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.defines._

class Execute extends Module {
  val io = IO(new Bundle {
    val fromAlu            = Flipped(new ALU_Execute())
    val fromMul            = Flipped(new Mul_Execute())
    val fromExecuteStage   = Flipped(new ExecuteStage_Execute())
    val fromMemoryStage    = Flipped(new MemoryStage_Execute())
    val fromDiv            = Flipped(new Div_Execute())
    val fromHILO           = Flipped(new HILO_Execute())
    val fromMemory         = Flipped(new Memory_Execute())
    val fromWriteBackStage = Flipped(new WriteBackStage_Execute())
    val fromCP0            = Flipped(new CP0_Execute())

    val alu         = new Execute_ALU()
    val mul         = new Execute_Mul()
    val memoryStage = new Execute_MemoryStage()
    val decoder     = new Execute_Decoder()
    val div         = new Execute_Div()
    val control     = new Execute_Control()
    val cp0         = new Execute_CP0()
    val dataMemory  = new Execute_DataMemory()
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

  // input-divider
  // val div_result_i = io.fromDiv.result
  // val div_ready_i  = io.fromDiv.ready

  // input-execute stage
  val link_addr       = io.fromExecuteStage.link_addr
  val is_in_delayslot = io.fromExecuteStage.is_in_delayslot

  // output
  val reg_wen           = Wire(Bool())
  val reg_wdata         = Wire(BUS)
  val hi                = Wire(BUS)
  val lo                = Wire(BUS)
  val whilo             = Wire(Bool())
  val hilo_temp_o       = Wire(DOUBLE_BUS)
  val cnt               = Wire(CNT_BUS)
  val mem_addr_temp     = Wire(BUS)
  val stallreq          = Wire(Bool())
  val cp0_raddr         = Wire(CP0_ADDR_BUS)
  val cp0_wen           = Wire(Bool())
  val cp0_waddr         = Wire(CP0_ADDR_BUS)
  val cp0_wdata         = Wire(BUS)
  val current_inst_addr = Wire(BUS)
  val except_type       = Wire(UInt(32.W))
  val mem_addr          = Wire(BUS)
  val mem_wsel          = Wire(DATA_MEMORY_SEL_BUS)
  val mem_wdata         = Wire(BUS)
  val mem_ce            = Wire(Bool())
  val mem_wen           = Wire(Bool())

  // output-memory stage
  io.memoryStage.pc := pc

  // output-decoder
  io.decoder.reg_wen   := reg_wen
  io.decoder.reg_waddr := reg_waddr
  io.decoder.reg_wdata := reg_wdata
  io.decoder.aluop     := aluop

  // output-memory stage
  io.memoryStage.reg_wen   := reg_wen
  io.memoryStage.reg_waddr := reg_waddr
  io.memoryStage.reg_wdata := reg_wdata
  io.memoryStage.hi        := hi
  io.memoryStage.lo        := lo
  io.memoryStage.whilo     := whilo
  io.memoryStage.hilo      := hilo_temp_o
  io.memoryStage.cnt       := cnt

  // output-divider
  // io.div.opdata1    :=divisor
  // io.div.opdata2    :=dividend
  // io.div.start      := div_start
  // io.div.signed_div := signed_div

  // output-memory stage
  io.memoryStage.aluop := aluop

  // output-memory stage
  io.memoryStage.mem_addr := mem_addr
  io.memoryStage.reg2     := reg2

  // output-control
  io.control.stallreq := stallreq

  // output-cp0
  io.cp0.cp0_raddr := cp0_raddr

  // output-memory stage
  io.memoryStage.cp0_wen           := cp0_wen
  io.memoryStage.cp0_waddr         := cp0_waddr
  io.memoryStage.cp0_wdata         := cp0_wdata
  io.memoryStage.current_inst_addr := current_inst_addr
  io.memoryStage.is_in_delayslot   := is_in_delayslot
  io.memoryStage.except_type       := except_type

  // output-data memory
  io.dataMemory.mem_addr  := mem_addr
  io.dataMemory.mem_wsel  := mem_wsel
  io.dataMemory.mem_wdata := mem_wdata
  io.dataMemory.mem_ce    := mem_ce
  io.dataMemory.mem_wen   := mem_wen & ~except_type.orR()

  // io-finish

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
  cp0_raddr  := ZERO_WORD
  hilo_temp1 := ZERO_WORD
  //

  mem_addr_temp := reg1 + Util.signedExtend(
    inst(15, 0),
  ) // mem_addr传递到访存阶段，是加载、存储指令对应的存储器地址

  except_type := Cat(
    io.fromExecuteStage.except_type(31, 12),
    ovassert,
    trapassert,
    io.fromExecuteStage.except_type(9, 8),
    "h00".U(8.W),
  )

  // input-execute stage
  current_inst_addr := io.fromExecuteStage.current_inst_addr

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

  // DIV、DIVU指令
  // when(reset.asBool === RST_ENABLE) {
  //   stallreq_for_div := NOT_STOP
  //   divisor      := ZERO_WORD
  //   dividend      := ZERO_WORD
  //   div_start        := DIV_STOP
  //   signed_div       := NOT_SIGNED
  // }.otherwise {
  //   stallreq_for_div := NOT_STOP
  //   divisor      := ZERO_WORD
  //   dividend      := ZERO_WORD
  //   div_start        := DIV_STOP
  //   signed_div       := NOT_SIGNED
  //   switch(aluop) {
  //     is(EXE_DIV_OP) {
  //       when(div_ready_i === DIV_RESULT_NOT_READY) {
  //         divisor      := reg1
  //         dividend      := reg2
  //         div_start        := DIV_START
  //         signed_div       := SIGNED
  //         stallreq_for_div := STOP
  //       }.elsewhen(div_ready_i === DIV_RESULT_READY) {
  //         divisor      := reg1
  //         dividend      := reg2
  //         div_start        := DIV_STOP
  //         signed_div       := SIGNED
  //         stallreq_for_div := NOT_STOP
  //       }.otherwise {
  //         divisor      := ZERO_WORD
  //         dividend      := ZERO_WORD
  //         div_start        := DIV_STOP
  //         signed_div       := NOT_SIGNED
  //         stallreq_for_div := NOT_STOP
  //       }
  //     }
  //     is(EXE_DIVU_OP) {
  //       when(div_ready_i === DIV_RESULT_NOT_READY) {
  //         divisor      := reg1
  //         dividend      := reg2
  //         div_start        := DIV_START
  //         signed_div       := NOT_SIGNED
  //         stallreq_for_div := STOP
  //       }.elsewhen(div_ready_i === DIV_RESULT_READY) {
  //         divisor      := reg1
  //         dividend      := reg2
  //         div_start        := DIV_STOP
  //         signed_div       := NOT_SIGNED
  //         stallreq_for_div := NOT_STOP
  //       }.otherwise {
  //         divisor      := ZERO_WORD
  //         dividend      := ZERO_WORD
  //         div_start        := DIV_STOP
  //         signed_div       := NOT_SIGNED
  //         stallreq_for_div := NOT_STOP
  //       }
  //     }
  //   }
  // }

  // MFHI、MFLO、MOVN、MOVZ指令
  when(reset.asBool === RST_ENABLE) {
    moveres := ZERO_WORD
  }.otherwise {
    moveres := ZERO_WORD
    switch(aluop) {
      is(EXE_MFHI_OP) {
        moveres := HI
      }
      is(EXE_MFLO_OP) {
        moveres := LO
      }
      is(EXE_MOVZ_OP) {
        moveres := reg1
      }
      is(EXE_MOVN_OP) {
        moveres := reg1
      }
      is(EXE_MFC0_OP) {
        cp0_raddr := inst(15, 11)

        // input-c p0
        moveres := io.fromCP0.cp0_rdata
        when(
          io.fromMemory.cp0_wen === WRITE_ENABLE &&
            io.fromMemory.cp0_waddr === inst(15, 11),
        ) {
          moveres := io.fromMemory.cp0_wdata
        }.elsewhen(
          io.fromWriteBackStage.cp0_wen === WRITE_ENABLE &&
            io.fromWriteBackStage.cp0_waddr === inst(15, 11),
        ) {
          moveres := io.fromWriteBackStage.cp0_wdata
        }
      }
    }
  }

  // 根据alusel指示的运算类型，选择一个运算结果作为最终结果
  reg_wen   := Mux(ovassert, WRITE_DISABLE, wreg_i)
  reg_wdata := ZERO_WORD // default
  switch(alusel) {
    is(EXE_RES_LOGIC) { reg_wdata := alures } // 逻辑运算
    is(EXE_RES_SHIFT) { reg_wdata := alures } // 移位运算
    is(EXE_RES_MOVE) { reg_wdata := moveres } // 移动运算
    is(EXE_RES_ARITHMETIC) { reg_wdata := alures } // 除乘法外简单算术操作指令
    is(EXE_RES_MUL) { reg_wdata := mulres(31, 0) } // 乘法指令mul
    is(EXE_RES_JUMP_BRANCH) { reg_wdata := link_addr }
  }

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

  when(reset.asBool === RST_ENABLE) {
    cp0_waddr := 0.U
    cp0_wen   := WRITE_DISABLE
    cp0_wdata := ZERO_WORD
  }.elsewhen(aluop === EXE_MTC0_OP) {
    cp0_waddr := inst(15, 11)
    cp0_wen   := WRITE_ENABLE
    cp0_wdata := reg1
  }.otherwise {
    cp0_waddr := 0.U
    cp0_wen   := WRITE_DISABLE
    cp0_wdata := ZERO_WORD
  }

  // input-memory stage
  mem_wen := MuxLookup(
    aluop,
    WRITE_DISABLE,
    Seq(
      EXE_SB_OP  -> WRITE_ENABLE,
      EXE_SH_OP  -> WRITE_ENABLE,
      EXE_SW_OP  -> WRITE_ENABLE,
      EXE_SWL_OP -> WRITE_ENABLE,
      EXE_SWR_OP -> WRITE_ENABLE,
      EXE_SC_OP  -> WRITE_ENABLE,
    ),
  )
  mem_ce := MuxLookup(
    aluop,
    CHIP_DISABLE,
    Seq(
      EXE_LB_OP  -> CHIP_ENABLE,
      EXE_LBU_OP -> CHIP_ENABLE,
      EXE_LH_OP  -> CHIP_ENABLE,
      EXE_LHU_OP -> CHIP_ENABLE,
      EXE_LW_OP  -> CHIP_ENABLE,
      EXE_LWL_OP -> CHIP_ENABLE,
      EXE_LWL_OP -> CHIP_ENABLE,
      EXE_LL_OP  -> CHIP_ENABLE,
      EXE_SB_OP  -> CHIP_ENABLE,
      EXE_SH_OP  -> CHIP_ENABLE,
      EXE_SW_OP  -> CHIP_ENABLE,
      EXE_SWL_OP -> CHIP_ENABLE,
      EXE_SWR_OP -> CHIP_ENABLE,
      // EXE_SC_OP  -> Mux(LLbit, CHIP_ENABLE, CHIP_DISABLE)
    ),
  ) // mem_ce
  mem_addr := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_LB_OP  -> mem_addr_temp,
      EXE_LBU_OP -> mem_addr_temp,
      EXE_LH_OP  -> mem_addr_temp,
      EXE_LHU_OP -> mem_addr_temp,
      EXE_LW_OP  -> mem_addr_temp,
      EXE_LWL_OP -> Cat(mem_addr_temp(31, 2), 0.U(2.W)),
      EXE_LWR_OP -> Cat(mem_addr_temp(31, 2), 0.U(2.W)),
      EXE_LL_OP  -> mem_addr_temp,
      EXE_SB_OP  -> mem_addr_temp,
      EXE_SH_OP  -> mem_addr_temp,
      EXE_SW_OP  -> mem_addr_temp,
      EXE_SWL_OP -> Cat(mem_addr_temp(31, 2), 0.U(2.W)),
      EXE_SWR_OP -> Cat(mem_addr_temp(31, 2), 0.U(2.W)),
      // EXE_SC_OP  -> Mux(LLbit, mem_addr_temp, ZERO_WORD)
    ),
  ) // mem_addr

  val addrLowBit2 = mem_addr_temp(1, 0)
  mem_wsel := MuxLookup(
    aluop,
    "b1111".U,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U,
        ),
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U,
        ),
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U,
        ),
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U,
        ),
      ),
      EXE_LW_OP  -> "b1111".U,
      EXE_LWL_OP -> "b1111".U,
      EXE_LWR_OP -> "b1111".U,
      EXE_LL_OP  -> "b1111".U,
      EXE_SB_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U,
        ),
      ),
      EXE_SH_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U,
        ),
      ),
      EXE_SW_OP -> "b1111".U,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1111".U,
          "b01".U -> "b0111".U,
          "b10".U -> "b0011".U,
          "b11".U -> "b0001".U,
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b1100".U,
          "b10".U -> "b1110".U,
          "b11".U -> "b1111".U,
        ),
      ),
      EXE_SC_OP -> "b1111".U,
    ),
  ) // mem_wsel

  val zero32 = Wire(BUS)
  zero32 := 0.U(32.W)

  mem_wdata := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_SB_OP -> Fill(4, reg2(7, 0)),
      EXE_SH_OP -> Fill(2, reg2(15, 0)),
      EXE_SW_OP -> reg2,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> reg2,
          "b01".U -> Util.zeroExtend(reg2(31, 8)),
          "b10".U -> Util.zeroExtend(reg2(31, 16)),
          "b11".U -> Util.zeroExtend(reg2(31, 24)),
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Cat(reg2(7, 0), zero32(23, 0)),
          "b01".U -> Cat(reg2(15, 0), zero32(15, 0)),
          "b10".U -> Cat(reg2(23, 0), zero32(7, 0)),
          "b11".U -> reg2,
        ),
      ),
      // EXE_SC_OP -> Mux(LLbit, reg2, ZERO_WORD)
    ),
  ) // mem_wdata

  // debug
  // printf(p"execute :pc 0x${Hexadecimal(pc)}\n")
}
