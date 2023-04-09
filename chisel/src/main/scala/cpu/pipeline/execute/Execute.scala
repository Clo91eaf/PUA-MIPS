package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.defines._

class Execute extends Module {
  val io = IO(new Bundle {
    val fromExecuteStage   = Flipped(new ExecuteStage_Execute())
    val fromMemoryStage    = Flipped(new MemoryStage_Execute())
    val fromDivider        = Flipped(new Divider_Execute())
    val fromHILO           = Flipped(new HILO_Execute())
    val fromMemory         = Flipped(new Memory_Execute())
    val fromWriteBackStage = Flipped(new WriteBackStage_Execute())
    val fromCP0            = Flipped(new CP0_Execute())

    val memoryStage = new Execute_MemoryStage()
    val decoder     = new Execute_Decoder()
    val divider     = new Execute_Divider()
    val control     = new Execute_Control()
    val cp0         = new Execute_CP0()
    val dataMemory  = new Execute_DataMemory()
  })

  // input
  val aluop_i           = Wire(ALU_OP_BUS)
  val alusel_i          = Wire(ALU_SEL_BUS)
  val reg1_i            = Wire(BUS)
  val reg2_i            = Wire(BUS)
  val wd_i              = Wire(ADDR_BUS)
  val wreg_i            = Wire(Bool())
  val inst_i            = Wire(BUS)
  val hi_i              = Wire(BUS)
  val lo_i              = Wire(BUS)
  val wb_hi_i           = Wire(BUS)
  val wb_lo_i           = Wire(BUS)
  val wb_whilo_i        = Wire(Bool())
  val mem_hi_i          = Wire(BUS)
  val mem_lo_i          = Wire(BUS)
  val mem_whilo_i       = Wire(Bool())
  val hilo_temp_i       = Wire(DOUBLE_BUS)
  val cnt_i             = Wire(CNT_BUS)
  val div_result_i      = Wire(DOUBLE_BUS)
  val div_ready_i       = Wire(Bool())
  val link_addr_i       = Wire(BUS)
  val is_in_delayslot_i = Wire(Bool())

  // input-execute stage
  aluop_i  := io.fromExecuteStage.aluop
  alusel_i := io.fromExecuteStage.alusel
  reg1_i   := io.fromExecuteStage.reg1
  reg2_i   := io.fromExecuteStage.reg2
  wd_i     := io.fromExecuteStage.reg_waddr
  wreg_i   := io.fromExecuteStage.reg_wen
  inst_i   := io.fromExecuteStage.inst

  // input-hilo
  hi_i := io.fromHILO.hi
  lo_i := io.fromHILO.lo

  // input-write back stage
  wb_hi_i    := io.fromWriteBackStage.hi
  wb_lo_i    := io.fromWriteBackStage.lo
  wb_whilo_i := io.fromWriteBackStage.whilo

  // input-memory
  mem_hi_i    := io.fromMemory.hi
  mem_lo_i    := io.fromMemory.lo
  mem_whilo_i := io.fromMemory.whilo

  // input-memory stage
  hilo_temp_i := io.fromMemoryStage.hilo
  cnt_i       := io.fromMemoryStage.cnt

  // input-divider
  div_result_i := io.fromDivider.result
  div_ready_i  := io.fromDivider.ready

  // input-execute stage
  link_addr_i       := io.fromExecuteStage.link_addr
  is_in_delayslot_i := io.fromExecuteStage.is_in_delayslot

  // output
  val pc                = Wire(BUS)
  val reg_waddr         = Wire(ADDR_BUS)
  val reg_wen           = Wire(Bool())
  val reg_wdata         = Wire(BUS)
  val hi                = Wire(BUS)
  val lo                = Wire(BUS)
  val whilo             = Wire(Bool())
  val hilo_temp_o       = Wire(DOUBLE_BUS)
  val cnt               = Wire(CNT_BUS)
  val div_opdata1       = Wire(BUS)
  val div_opdata2       = Wire(BUS)
  val div_start         = Wire(Bool())
  val signed_div        = Wire(Bool())
  val aluop             = Wire(ALU_OP_BUS)
  val mem_addr_temp     = Wire(BUS)
  val reg2              = Wire(BUS)
  val stallreq          = Wire(Bool())
  val cp0_raddr         = Wire(CP0_ADDR_BUS)
  val cp0_wen           = Wire(Bool())
  val cp0_waddr         = Wire(CP0_ADDR_BUS)
  val cp0_wdata         = Wire(BUS)
  val current_inst_addr = Wire(BUS)
  val is_in_delayslot   = Wire(Bool())
  val excepttype        = Wire(UInt(32.W))
  val mem_addr          = Wire(BUS)
  val mem_wsel          = Wire(DATA_MEMORY_SEL_BUS)
  val mem_wdata         = Wire(BUS)
  val mem_ce            = Wire(Bool())
  val mem_wen           = Wire(Bool())

  pc := io.fromExecuteStage.pc

  // output-memory stage
  io.memoryStage.pc := pc

  // output-decoder
  io.decoder.reg_waddr := reg_waddr

  // output-memory stage
  io.memoryStage.reg_waddr := reg_waddr

  // output-decoder
  io.decoder.reg_wen := reg_wen

  // output-memory stage
  io.memoryStage.reg_wen := reg_wen

  // output-decoder
  io.decoder.reg_wdata := reg_wdata

  // output-memory stage
  io.memoryStage.reg_wdata := reg_wdata
  io.memoryStage.hi        := hi
  io.memoryStage.lo        := lo
  io.memoryStage.whilo     := whilo
  io.memoryStage.hilo      := hilo_temp_o
  io.memoryStage.cnt       := cnt

  // output-divider
  io.divider.opdata1    := div_opdata1
  io.divider.opdata2    := div_opdata2
  io.divider.start      := div_start
  io.divider.signed_div := signed_div

  // output-memory stage
  io.memoryStage.aluop := aluop

  // output-decoder
  io.decoder.aluop := aluop

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
  io.memoryStage.except_type       := excepttype

  // output-data memory
  io.dataMemory.mem_addr  := mem_addr
  io.dataMemory.mem_wsel  := mem_wsel
  io.dataMemory.mem_wdata := mem_wdata
  io.dataMemory.mem_ce    := mem_ce
  io.dataMemory.mem_wen   := mem_wen & ~excepttype.orR()

  // io-finish

  // 保存逻辑运算的结果
  val logicout               = Wire(BUS)        // 保存逻辑运算的结果
  val shiftres               = Wire(BUS)        // 保存移位操作运算的结果
  val moveres                = Wire(BUS)        // 保存移动操作运算的结果
  val arithmeticres          = Wire(BUS)        // 保存算术运算结果
  val mulres                 = Wire(DOUBLE_BUS) // 保存乘法结果，宽度为64位
  val HI                     = Wire(BUS)
  val LO                     = Wire(BUS)
  val reg2_mux               = Wire(BUS)        // 保存输入的第二个操作reg2的补码
  val reg1_not               = Wire(BUS)        // 保存输入的第一个操作数reg1取反后的值
  val result_sum             = Wire(BUS)        // 保存加法结果
  val ov_sum                 = Wire(Bool())     // 保存溢出情况
  val reg1_eq_reg2           = Wire(Bool())     // 第一个操作数是否等于第二个操作数
  val reg1_lt_reg2           = Wire(Bool())     // 第一个操作数是否小于第二个操作数
  val opdata1_mult           = Wire(BUS)        // 乘法操作中的被乘数
  val opdata2_mult           = Wire(BUS)        // 乘法操作中的乘数
  val hilo_temp              = Wire(DOUBLE_BUS)
  val hilo_temp1             = Wire(DOUBLE_BUS)
  val stallreq_for_madd_msub = Wire(Bool())
  val stallreq_for_div       = Wire(Bool())
  val trapassert             = Wire(Bool())
  val ovassert               = Wire(Bool())

  // liphen
  cp0_raddr  := ZERO_WORD
  hilo_temp1 := ZERO_WORD
  //

  aluop := aluop_i // aluop传递到访存阶段，用于加载、存储指令
  mem_addr_temp := reg1_i + Util.signedExtend(
    inst_i(15, 0)
  ) // mem_addr传递到访存阶段，是加载、存储指令对应的存储器地址
  reg2 := reg2_i // 将两个操作数也传递到访存阶段，也是为记载、存储指令准备的

  excepttype := Cat(
    io.fromExecuteStage.except_type(31, 12),
    ovassert,
    trapassert,
    io.fromExecuteStage.except_type(9, 8),
    "h00".U(8.W)
  )
  is_in_delayslot := is_in_delayslot_i

  // input-execute stage
  current_inst_addr := io.fromExecuteStage.current_inst_addr

  // 根据aluop指示的运算子类型进行运算
  // LOGIC
  when(reset.asBool === RST_ENABLE) {
    logicout := ZERO_WORD
  }.otherwise {
    logicout := MuxLookup(
      aluop_i,
      ZERO_WORD,
    // @formatter:off
    Seq(
      EXE_OR_OP  -> (reg1_i | reg2_i),
      EXE_AND_OP -> (reg1_i & reg2_i),
      EXE_NOR_OP -> (~(reg1_i | reg2_i)),
      EXE_XOR_OP -> (reg1_i ^ reg2_i)
    )
    // @formatter:on
    )
  }

  // SHIFT
  when(reset.asBool === RST_ENABLE) {
    shiftres := ZERO_WORD
  }.otherwise {
    shiftres := MuxLookup(
      aluop_i,
      ZERO_WORD,
      Seq(
        EXE_SLL_OP -> (reg2_i << reg1_i(4, 0)),
        EXE_SRL_OP -> (reg2_i >> reg1_i(4, 0)),
        EXE_SRA_OP -> ((reg2_i.asSInt >> reg1_i(4, 0)).asUInt)
      )
    )
  }

  // 第二个操作数
  reg2_mux := Mux(
    ((aluop_i === EXE_SUB_OP) || (aluop_i === EXE_SUBU_OP) ||
      (aluop_i === EXE_SLT_OP) || (aluop_i === EXE_TLT_OP) ||
      (aluop_i === EXE_TLTI_OP) || (aluop_i === EXE_TGE_OP) ||
      (aluop_i === EXE_TGEI_OP)),
    ((~reg2_i) + 1.U),
    reg2_i
  )
  // 运算结果
  result_sum := reg1_i + reg2_mux
  // 是否溢出
  ov_sum := ((!reg1_i(31) && !reg2_mux(31)) && result_sum(31)) ||
    ((reg1_i(31) && reg2_mux(31)) && (!result_sum(31)))
  // 操作数1是否小于操作数2
  reg1_lt_reg2 := Mux(
    ((aluop_i === EXE_SLT_OP) || (aluop_i === EXE_TLT_OP) ||
      (aluop_i === EXE_TLTI_OP) || (aluop_i === EXE_TGE_OP) ||
      (aluop_i === EXE_TGEI_OP)),
    ((reg1_i(31) && !reg2_i(31)) ||
      (!reg1_i(31) && !reg2_i(31) && result_sum(31)) ||
      (reg1_i(31) && reg2_i(31) && result_sum(31))),
    (reg1_i < reg2_i)
  )
  // 操作数1是否等于操作数2
  reg1_eq_reg2 := DontCare
  // 对操作数1取反
  reg1_not := ~reg1_i

  when(reset.asBool === RST_ENABLE) {
    arithmeticres := ZERO_WORD
  }.otherwise {
  // @formatter:off
  arithmeticres := 
      Mux(aluop_i === EXE_SLT_OP || aluop_i === EXE_SLTU_OP, 
          reg1_lt_reg2, //比较运算
      Mux(aluop_i === EXE_ADD_OP || aluop_i === EXE_ADDU_OP || aluop_i === EXE_ADDI_OP || aluop_i === EXE_ADDIU_OP,
          result_sum, //加法运算
      Mux(aluop_i === EXE_SUB_OP || aluop_i === EXE_SUBU_OP,
          result_sum, //减法运算
      Mux(aluop_i === EXE_CLZ_OP, //计数运算clz
          (31 to 0 by -1).foldLeft(32.U) { (res, i) => Mux(reg1_i(i), res, i.U)},
      Mux(aluop_i === EXE_CLO_OP, //计数运算clo
          (31 to 0 by -1).foldLeft(32.U) { (res, i) =>Mux(reg1_not(i), res, i.U)},
          ZERO_WORD // default
            )
          )
        )
      )
    )
  // @formatter:on
  }

  when(reset.asBool === RST_ENABLE) {
    trapassert := TRAP_NOT_ASSERT
  }.otherwise {
    trapassert := TRAP_NOT_ASSERT
    switch(aluop_i) {
      is(EXE_TEQ_OP, EXE_TEQI_OP) {
        when(reg1_i === reg2_i) {
          trapassert := TRAP_ASSERT;
        }
      }
      is(EXE_TGE_OP, EXE_TGEI_OP, EXE_TGEIU_OP, EXE_TGEU_OP) {
        when(~reg1_lt_reg2) {
          trapassert := TRAP_ASSERT;
        }
      }
      is(EXE_TLT_OP, EXE_TLTI_OP, EXE_TLTIU_OP, EXE_TLTU_OP) {
        when(reg1_lt_reg2) {
          trapassert := TRAP_ASSERT;
        }
      }
      is(EXE_TNE_OP, EXE_TNEI_OP) {
        when(reg1_i =/= reg2_i) {
          trapassert := TRAP_ASSERT;
        }
      }
    }
  }

  // 被乘数
  opdata1_mult := MuxCase(
    reg1_i,
    Seq(
      (aluop_i === EXE_MUL_OP)  -> (~reg1_i + 1.U),
      (aluop_i === EXE_MULT_OP) -> (~reg1_i + 1.U),
      (reg1_i(31) === 1.U)      -> (~reg1_i + 1.U)
    )
  )
  // 乘数
  opdata2_mult := MuxCase(
    reg2_i,
    Seq(
      (aluop_i === EXE_MUL_OP)  -> (~reg2_i + 1.U),
      (aluop_i === EXE_MULT_OP) -> (~reg2_i + 1.U),
      (reg2_i(31) === 1.U)      -> (~reg2_i + 1.U)
    )
  )
  // 临时乘法结果
  hilo_temp := opdata1_mult * opdata2_mult

  // 对乘法结果修正(A*B）补=A补 * B补
  when(reset.asBool === RST_ENABLE) {
    mulres := ZERO_WORD
  }.elsewhen(
    (aluop_i === EXE_MULT_OP) ||
      (aluop_i === EXE_MUL_OP) ||
      (aluop_i === EXE_MADD_OP) ||
      (aluop_i === EXE_MSUB_OP)
  ) {
    when(reg1_i(31) ^ reg2_i(31) === 1.U) {
      mulres := ~hilo_temp + 1.U
    }.otherwise {
      mulres := hilo_temp
    }
  }.otherwise {
    mulres := hilo_temp
  }

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

  stallreq := stallreq_for_madd_msub || stallreq_for_div

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
    switch(aluop_i) {
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
  when(reset.asBool === RST_ENABLE) {
    stallreq_for_div := NOT_STOP
    div_opdata1      := ZERO_WORD
    div_opdata2      := ZERO_WORD
    div_start        := DIV_STOP
    signed_div       := NOT_SIGNED
  }.otherwise {
    stallreq_for_div := NOT_STOP
    div_opdata1      := ZERO_WORD
    div_opdata2      := ZERO_WORD
    div_start        := DIV_STOP
    signed_div       := NOT_SIGNED
    switch(aluop_i) {
      is(EXE_DIV_OP) {
        when(div_ready_i === DIV_RESULT_NOT_READY) {
          div_opdata1      := reg1_i
          div_opdata2      := reg2_i
          div_start        := DIV_START
          signed_div       := SIGNED
          stallreq_for_div := STOP
        }.elsewhen(div_ready_i === DIV_RESULT_READY) {
          div_opdata1      := reg1_i
          div_opdata2      := reg2_i
          div_start        := DIV_STOP
          signed_div       := SIGNED
          stallreq_for_div := NOT_STOP
        }.otherwise {
          div_opdata1      := ZERO_WORD
          div_opdata2      := ZERO_WORD
          div_start        := DIV_STOP
          signed_div       := NOT_SIGNED
          stallreq_for_div := NOT_STOP
        }
      }
      is(EXE_DIVU_OP) {
        when(div_ready_i === DIV_RESULT_NOT_READY) {
          div_opdata1      := reg1_i
          div_opdata2      := reg2_i
          div_start        := DIV_START
          signed_div       := NOT_SIGNED
          stallreq_for_div := STOP
        }.elsewhen(div_ready_i === DIV_RESULT_READY) {
          div_opdata1      := reg1_i
          div_opdata2      := reg2_i
          div_start        := DIV_STOP
          signed_div       := NOT_SIGNED
          stallreq_for_div := NOT_STOP
        }.otherwise {
          div_opdata1      := ZERO_WORD
          div_opdata2      := ZERO_WORD
          div_start        := DIV_STOP
          signed_div       := NOT_SIGNED
          stallreq_for_div := NOT_STOP
        }
      }
    }
  }

  // MFHI、MFLO、MOVN、MOVZ指令
  when(reset.asBool === RST_ENABLE) {
    moveres := ZERO_WORD
  }.otherwise {
    moveres := ZERO_WORD
    switch(aluop_i) {
      is(EXE_MFHI_OP) {
        moveres := HI
      }
      is(EXE_MFLO_OP) {
        moveres := LO
      }
      is(EXE_MOVZ_OP) {
        moveres := reg1_i
      }
      is(EXE_MOVN_OP) {
        moveres := reg1_i
      }
      is(EXE_MFC0_OP) {
        cp0_raddr := inst_i(15, 11)

        // input-c p0
        moveres := io.fromCP0.cp0_rdata
        when(
          io.fromMemory.cp0_wen === WRITE_ENABLE &&
            io.fromMemory.cp0_waddr === inst_i(15, 11)
        ) {

          // input-memory
          moveres := io.fromMemory.cp0_wdata
        }.elsewhen(
          io.fromWriteBackStage.cp0_wen === WRITE_ENABLE &&
            io.fromWriteBackStage.cp0_waddr === inst_i(15, 11)
        ) {

          // input-write back stage
          moveres := io.fromWriteBackStage.cp0_wdata
        }
      }
    }
  }

  // 根据alusel指示的运算类型，选择一个运算结果作为最终结果
  reg_waddr := wd_i
  when(
    ((aluop_i === EXE_ADD_OP) || (aluop_i === EXE_ADDI_OP) || (aluop_i === EXE_SUB_OP)) && (ov_sum === 1.U)
  ) {
    reg_wen  := WRITE_DISABLE
    ovassert := true.B
  }.otherwise {
    reg_wen  := wreg_i
    ovassert := false.B
  }
  reg_wdata := ZERO_WORD // default
  switch(alusel_i) {
    is(EXE_RES_LOGIC) { reg_wdata := logicout } // 逻辑运算
    is(EXE_RES_SHIFT) { reg_wdata := shiftres } // 移位运算
    is(EXE_RES_MOVE) { reg_wdata := moveres } // 移动运算
    is(EXE_RES_ARITHMETIC) { reg_wdata := arithmeticres } // 除乘法外简单算术操作指令
    is(EXE_RES_MUL) { reg_wdata := mulres(31, 0) } // 乘法指令mul
    is(EXE_RES_JUMP_BRANCH) { reg_wdata := link_addr_i }
  }

  // MTHI和MTLO指令 乘法运算结果保存
  when(reset.asBool === RST_ENABLE) {
    whilo := WRITE_DISABLE
    hi    := ZERO_WORD
    lo    := ZERO_WORD
  }.elsewhen((aluop_i === EXE_MULT_OP) || (aluop_i === EXE_MULTU_OP)) {
    whilo := WRITE_ENABLE
    hi    := mulres(63, 32)
    lo    := mulres(31, 0)
  }.elsewhen((aluop_i === EXE_MADD_OP) || (aluop_i === EXE_MADDU_OP)) {
    whilo := WRITE_ENABLE
    hi    := hilo_temp1(63, 32)
    lo    := hilo_temp1(31, 0)
  }.elsewhen((aluop_i === EXE_MSUB_OP) || (aluop_i === EXE_MSUBU_OP)) {
    whilo := WRITE_ENABLE
    hi    := hilo_temp1(63, 32)
    lo    := hilo_temp1(31, 0)
  }.elsewhen((aluop_i === EXE_DIV_OP) || (aluop_i === EXE_DIVU_OP)) {
    whilo := WRITE_ENABLE
    hi    := div_result_i(63, 32)
    lo    := div_result_i(31, 0)
  }.elsewhen(aluop_i === EXE_MTHI_OP) {
    whilo := WRITE_ENABLE
    hi    := reg1_i
    lo    := LO
  }.elsewhen(aluop_i === EXE_MTLO_OP) {
    whilo := WRITE_ENABLE
    hi    := HI
    lo    := reg1_i
  }.otherwise {
    whilo := WRITE_DISABLE
    hi    := ZERO_WORD
    lo    := ZERO_WORD
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_waddr := 0.U
    cp0_wen   := WRITE_DISABLE
    cp0_wdata := ZERO_WORD
  }.elsewhen(aluop_i === EXE_MTC0_OP) {
    cp0_waddr := inst_i(15, 11)
    cp0_wen   := WRITE_ENABLE
    cp0_wdata := reg1_i
  }.otherwise {
    cp0_waddr := 0.U
    cp0_wen   := WRITE_DISABLE
    cp0_wdata := ZERO_WORD
  }

  when(reset.asBool === RST_ENABLE) {
    mem_addr  := ZERO_WORD
    mem_wen   := WRITE_DISABLE
    mem_wsel  := "b0000".U
    mem_wdata := ZERO_WORD
    mem_ce    := CHIP_DISABLE
  }.otherwise {
    // input-memory stage
    mem_wen  := WRITE_DISABLE
    mem_addr := ZERO_WORD
    mem_wsel := "b1111".U
    mem_ce   := CHIP_DISABLE

    mem_wen := MuxLookup(
      aluop,
      WRITE_DISABLE,
      Seq(
        EXE_SB_OP  -> WRITE_ENABLE,
        EXE_SH_OP  -> WRITE_ENABLE,
        EXE_SW_OP  -> WRITE_ENABLE,
        EXE_SWL_OP -> WRITE_ENABLE,
        EXE_SWR_OP -> WRITE_ENABLE,
        EXE_SC_OP  -> WRITE_ENABLE
      )
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
        EXE_SWR_OP -> CHIP_ENABLE
        // EXE_SC_OP  -> Mux(LLbit, CHIP_ENABLE, CHIP_DISABLE)
      )
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
        EXE_SWR_OP -> Cat(mem_addr_temp(31, 2), 0.U(2.W))
        // EXE_SC_OP  -> Mux(LLbit, mem_addr_temp, ZERO_WORD)
      )
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
            "b11".U -> "b0001".U
          )
        ),
        EXE_LBU_OP -> MuxLookup(
          addrLowBit2,
          "b1111".U,
          Seq(
            "b00".U -> "b1000".U,
            "b01".U -> "b0100".U,
            "b10".U -> "b0010".U,
            "b11".U -> "b0001".U
          )
        ),
        EXE_LH_OP -> MuxLookup(
          addrLowBit2,
          "b1111".U,
          Seq(
            "b00".U -> "b1100".U,
            "b10".U -> "b0011".U
          )
        ),
        EXE_LHU_OP -> MuxLookup(
          addrLowBit2,
          "b1111".U,
          Seq(
            "b00".U -> "b1100".U,
            "b10".U -> "b0011".U
          )
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
            "b11".U -> "b0001".U
          )
        ),
        EXE_SH_OP -> MuxLookup(
          addrLowBit2,
          "b0000".U,
          Seq(
            "b00".U -> "b1100".U,
            "b10".U -> "b0011".U
          )
        ),
        EXE_SW_OP -> "b1111".U,
        EXE_SWL_OP -> MuxLookup(
          addrLowBit2,
          "b0000".U,
          Seq(
            "b00".U -> "b1111".U,
            "b01".U -> "b0111".U,
            "b10".U -> "b0011".U,
            "b11".U -> "b0001".U
          )
        ),
        EXE_SWR_OP -> MuxLookup(
          addrLowBit2,
          "b0000".U,
          Seq(
            "b00".U -> "b1000".U,
            "b01".U -> "b1100".U,
            "b10".U -> "b1110".U,
            "b11".U -> "b1111".U
          )
        ),
        EXE_SC_OP -> "b1111".U
      )
    ) // mem_wsel

    val zero32 = Wire(BUS)
    zero32 := 0.U(32.W)

    mem_wdata := MuxLookup(
      aluop,
      ZERO_WORD,
      Seq(
        EXE_SB_OP -> Fill(4, reg2_i(7, 0)),
        EXE_SH_OP -> Fill(2, reg2_i(15, 0)),
        EXE_SW_OP -> reg2_i,
        EXE_SWL_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> reg2_i,
            "b01".U -> Util.zeroExtend(reg2_i(31, 8)),
            "b10".U -> Util.zeroExtend(reg2_i(31, 16)),
            "b11".U -> Util.zeroExtend(reg2_i(31, 24))
          )
        ),
        EXE_SWR_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Cat(reg2_i(7, 0), zero32(23, 0)),
            "b01".U -> Cat(reg2_i(15, 0), zero32(15, 0)),
            "b10".U -> Cat(reg2_i(23, 0), zero32(7, 0)),
            "b11".U -> reg2_i
          )
        )
        // EXE_SC_OP -> Mux(LLbit, reg2_i, ZERO_WORD)
      )
    ) // mem_wdata
  }

  // debug
  // printf(p"execute :pc 0x${Hexadecimal(pc)}\n")
}
