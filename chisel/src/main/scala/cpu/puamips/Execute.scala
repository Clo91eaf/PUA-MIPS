package cpu.puamips

import chisel3._
import chisel3.util._
import Const._

class Execute extends Module {
  val io = IO(new Bundle {
    val fromExecuteStage = Flipped(new ExecuteStage_Execute())
    val fromMemoryStage = Flipped(new MemoryStage_Execute())
    val fromDivider = Flipped(new Divider_Execute())
    val fromHILO = Flipped(new HILO_Execute())
    val fromMemory = Flipped(new Memory_Execute())
    val fromWriteBackStage = Flipped(new WriteBackStage_Execute())
    val fromCP0 = Flipped(new CP0_Execute())

    val memoryStage = new Execute_MemoryStage()
    val decoder = new Execute_Decoder()
    val divider = new Execute_Divider()
    val control = new Execute_Control()
    val cp0 = new Execute_CP0()
  })

  // input
  val aluop_i = Wire(ALU_OP_BUS)
  aluop_i := io.fromExecuteStage.aluop
  val alusel_i = Wire(ALU_SEL_BUS)
  alusel_i := io.fromExecuteStage.alusel
  val reg1_i = Wire(BUS)
  reg1_i := io.fromExecuteStage.reg1
  val reg2_i = Wire(BUS)
  reg2_i := io.fromExecuteStage.reg2
  val wd_i = Wire(ADDR_BUS)
  wd_i := io.fromExecuteStage.waddr
  val wreg_i = Wire(Bool())
  wreg_i := io.fromExecuteStage.wen
  val inst_i = Wire(BUS)
  inst_i := io.fromExecuteStage.inst
  val hi_i = Wire(BUS)
  hi_i := io.fromHILO.hi
  val lo_i = Wire(BUS)
  lo_i := io.fromHILO.lo
  val wb_hi_i = Wire(BUS)
  wb_hi_i := io.fromWriteBackStage.hi
  val wb_lo_i = Wire(BUS)
  wb_lo_i := io.fromWriteBackStage.lo
  val wb_whilo_i = Wire(Bool())
  wb_whilo_i := io.fromWriteBackStage.whilo
  val mem_hi_i = Wire(BUS)
  mem_hi_i := io.fromMemory.hi
  val mem_lo_i = Wire(BUS)
  mem_lo_i := io.fromMemory.lo
  val mem_whilo_i = Wire(Bool())
  mem_whilo_i := io.fromMemory.whilo
  val hilo_temp_i = Wire(DOUBLE_BUS)
  hilo_temp_i := io.fromMemoryStage.hilo
  val cnt_i = Wire(CNT_BUS)
  cnt_i := io.fromMemoryStage.cnt
  val div_result_i = Wire(DOUBLE_BUS)
  div_result_i := io.fromDivider.result
  val div_ready_i = Wire(Bool())
  div_ready_i := io.fromDivider.ready
  val link_addr_i = Wire(BUS)
  link_addr_i := io.fromExecuteStage.link_addr
  val is_in_delayslot_i = Wire(Bool())
  is_in_delayslot_i := io.fromExecuteStage.is_in_delayslot

  // output
  val pc = Wire(BUS)
  pc := io.fromExecuteStage.pc
  io.memoryStage.pc := pc
  val waddr = Wire(ADDR_BUS)
  io.decoder.waddr := waddr
  io.memoryStage.waddr := waddr
  val wen = Wire(Bool())
  io.decoder.wen := wen
  io.memoryStage.wen := wen
  val wdata = Wire(BUS)
  io.decoder.wdata := wdata
  io.memoryStage.wdata := wdata
  val hi = Wire(BUS)
  io.memoryStage.hi := hi
  val lo = Wire(BUS)
  io.memoryStage.lo := lo
  val whilo = Wire(Bool())
  io.memoryStage.whilo := whilo
  val hilo_temp_o = Wire(DOUBLE_BUS)
  io.memoryStage.hilo := hilo_temp_o
  val cnt = Wire(CNT_BUS)
  io.memoryStage.cnt := cnt
  val div_opdata1 = Wire(BUS)
  io.divider.opdata1 := div_opdata1
  val div_opdata2 = Wire(BUS)
  io.divider.opdata2 := div_opdata2
  val div_start = Wire(Bool())
  io.divider.start := div_start
  val signed_div = Wire(Bool())
  io.divider.signed_div := signed_div
  val aluop = Wire(ALU_OP_BUS)
  io.memoryStage.aluop := aluop
  io.decoder.aluop := aluop
  val mem_addr = Wire(BUS)
  io.memoryStage.addr := mem_addr
  val reg2 = Wire(BUS)
  io.memoryStage.reg2 := reg2
  val stallreq = Wire(Bool())
  io.control.stallreq := stallreq
  val cp0_raddr = Wire(CP0_ADDR_BUS)
  io.cp0.cp0_raddr := cp0_raddr
  val cp0_wen = Wire(Bool())
  io.memoryStage.cp0_wen := cp0_wen
  val cp0_waddr = Wire(CP0_ADDR_BUS)
  io.memoryStage.cp0_waddr := cp0_waddr
  val cp0_data = Wire(BUS)
  io.memoryStage.cp0_data := cp0_data
  val current_inst_addr = Wire(BUS)
  io.memoryStage.current_inst_addr := current_inst_addr
  val is_in_delayslot = Wire(Bool())
  io.memoryStage.is_in_delayslot := is_in_delayslot
  val excepttype = Wire(UInt(32.W))
  io.memoryStage.excepttype := excepttype

  // 保存逻辑运算的结果
  val logicout = Wire(BUS) // 保存逻辑运算的结果
  val shiftres = Wire(BUS) // 保存移位操作运算的结果
  val moveres = Wire(BUS) // 保存移动操作运算的结果
  val arithmeticres = Wire(BUS) // 保存算术运算结果
  val mulres = Wire(DOUBLE_BUS) // 保存乘法结果，宽度为64位
  val HI = Wire(BUS)
  val LO = Wire(BUS)
  val reg2_mux = Wire(BUS) // 保存输入的第二个操作reg2的补码
  val reg1_not = Wire(BUS) // 保存输入的第一个操作数reg1取反后的值
  val result_sum = Wire(BUS) // 保存加法结果
  val ov_sum = Wire(Bool()) // 保存溢出情况
  val reg1_eq_reg2 = Wire(Bool()) // 第一个操作数是否等于第二个操作数
  val reg1_lt_reg2 = Wire(Bool()) // 第一个操作数是否小于第二个操作数
  val opdata1_mult = Wire(BUS) // 乘法操作中的被乘数
  val opdata2_mult = Wire(BUS) // 乘法操作中的乘数
  val hilo_temp = Wire(DOUBLE_BUS)
  val hilo_temp1 = Wire(DOUBLE_BUS)
  val stallreq_for_madd_msub = Wire(Bool())
  val stallreq_for_div = Wire(Bool())
  val trapassert = Wire(Bool())
  val ovassert = Wire(Bool())

  // liphen
  cp0_raddr := ZERO_WORD
  hilo_temp1 := ZERO_WORD
  //

  // aluop传递到访存阶段，用于加载、存储指令
  aluop := aluop_i
  // mem_addr传递到访存阶段，是加载、存储指令对应的存储器地址
  mem_addr := reg1_i + Util.signedExtend(inst_i(15, 0))
  // 将两个操作数也传递到访存阶段，也是为记载、存储指令准备的
  reg2 := reg2_i

  excepttype := Cat(
    io.fromExecuteStage.excepttype(31, 12),
    ovassert,
    trapassert,
    io.fromExecuteStage.excepttype(9, 8),
    "h00".U(8.W)
  )
  is_in_delayslot := is_in_delayslot_i
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
      (aluop_i === EXE_MUL_OP) -> (~reg1_i + 1.U),
      (aluop_i === EXE_MULT_OP) -> (~reg1_i + 1.U),
      (reg1_i(31) === 1.U) -> (~reg1_i + 1.U)
    )
  )
  // 乘数
  opdata2_mult := MuxCase(
    reg2_i,
    Seq(
      (aluop_i === EXE_MUL_OP) -> (~reg2_i + 1.U),
      (aluop_i === EXE_MULT_OP) -> (~reg2_i + 1.U),
      (reg2_i(31) === 1.U) -> (~reg2_i + 1.U)
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
    hilo_temp_o := ZERO_WORD
    cnt := 0.U
    stallreq_for_madd_msub := NOT_STOP
  }.otherwise {
    // default
    hilo_temp_o := ZERO_WORD
    cnt := 0.U
    stallreq_for_madd_msub := NOT_STOP
    switch(aluop_i) {
      is(EXE_MADD_OP, EXE_MADDU_OP) {
        when(cnt_i === 0.U) {
          hilo_temp_o := mulres
          cnt := 1.U
          stallreq_for_madd_msub := STOP
          hilo_temp1 := ZERO_WORD
        }.elsewhen(cnt_i === 1.U) {
          hilo_temp_o := ZERO_WORD
          cnt := 2.U
          hilo_temp1 := hilo_temp_i + Cat(HI, LO)
          stallreq_for_madd_msub := NOT_STOP
        }
      }
      is(EXE_MSUB_OP, EXE_MSUBU_OP) {
        when(cnt_i === 0.U) {
          hilo_temp_o := ~mulres + 1.U
          cnt := 1.U
          stallreq_for_madd_msub := STOP
        }.elsewhen(cnt_i === 1.U) {
          hilo_temp_o := ZERO_WORD
          cnt := 2.U
          hilo_temp1 := hilo_temp_i + Cat(HI, LO)
          stallreq_for_madd_msub := NOT_STOP
        }
      }
    }
  }

  // DIV、DIVU指令
  when(reset.asBool === RST_ENABLE) {
    stallreq_for_div := NOT_STOP
    div_opdata1 := ZERO_WORD
    div_opdata2 := ZERO_WORD
    div_start := DIV_STOP
    signed_div := NOT_SIGNED
  }.otherwise {
    stallreq_for_div := NOT_STOP
    div_opdata1 := ZERO_WORD
    div_opdata2 := ZERO_WORD
    div_start := DIV_STOP
    signed_div := NOT_SIGNED
    switch(aluop_i) {
      is(EXE_DIV_OP) {
        when(div_ready_i === DIV_RESULT_NOT_READY) {
          div_opdata1 := reg1_i
          div_opdata2 := reg2_i
          div_start := DIV_START
          signed_div := SIGNED
          stallreq_for_div := STOP
        }.elsewhen(div_ready_i === DIV_RESULT_READY) {
          div_opdata1 := reg1_i
          div_opdata2 := reg2_i
          div_start := DIV_STOP
          signed_div := SIGNED
          stallreq_for_div := NOT_STOP
        }.otherwise {
          div_opdata1 := ZERO_WORD
          div_opdata2 := ZERO_WORD
          div_start := DIV_STOP
          signed_div := NOT_SIGNED
          stallreq_for_div := NOT_STOP
        }
      }
      is(EXE_DIVU_OP) {
        when(div_ready_i === DIV_RESULT_NOT_READY) {
          div_opdata1 := reg1_i
          div_opdata2 := reg2_i
          div_start := DIV_START
          signed_div := NOT_SIGNED
          stallreq_for_div := STOP
        }.elsewhen(div_ready_i === DIV_RESULT_READY) {
          div_opdata1 := reg1_i
          div_opdata2 := reg2_i
          div_start := DIV_STOP
          signed_div := NOT_SIGNED
          stallreq_for_div := NOT_STOP
        }.otherwise {
          div_opdata1 := ZERO_WORD
          div_opdata2 := ZERO_WORD
          div_start := DIV_STOP
          signed_div := NOT_SIGNED
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
        moveres := io.fromCP0.cp0_data
        when(
          io.fromMemory.cp0_wen === WRITE_ENABLE &&
            io.fromMemory.cp0_waddr === inst_i(15, 11)
        ) {
          moveres := io.fromMemory.cp0_data
        }.elsewhen(
          io.fromWriteBackStage.cp0_wen === WRITE_ENABLE &&
            io.fromWriteBackStage.cp0_waddr === inst_i(15, 11)
        ) {
          moveres := io.fromWriteBackStage.cp0_data
        }
      }
    }
  }

  // 根据alusel指示的运算类型，选择一个运算结果作为最终结果
  waddr := wd_i
  when(
    ((aluop_i === EXE_ADD_OP) || (aluop_i === EXE_ADDI_OP) || (aluop_i === EXE_SUB_OP)) && (ov_sum === 1.U)
  ) {
    wen := WRITE_DISABLE
    ovassert := true.B
  }.otherwise {
    wen := wreg_i
    ovassert := false.B
  }
  wdata := ZERO_WORD // default
  switch(alusel_i) {
    is(EXE_RES_LOGIC) { wdata := logicout } // 逻辑运算
    is(EXE_RES_SHIFT) { wdata := shiftres } // 移位运算
    is(EXE_RES_MOVE) { wdata := moveres } // 移动运算
    is(EXE_RES_ARITHMETIC) { wdata := arithmeticres } // 除乘法外简单算术操作指令
    is(EXE_RES_MUL) { wdata := mulres(31, 0) } // 乘法指令mul
    is(EXE_RES_JUMP_BRANCH) { wdata := link_addr_i }
  }

  // MTHI和MTLO指令 乘法运算结果保存
  when(reset.asBool === RST_ENABLE) {
    whilo := WRITE_DISABLE
    hi := ZERO_WORD
    lo := ZERO_WORD
  }.elsewhen((aluop_i === EXE_MULT_OP) || (aluop_i === EXE_MULTU_OP)) {
    whilo := WRITE_ENABLE
    hi := mulres(63, 32)
    lo := mulres(31, 0)
  }.elsewhen((aluop_i === EXE_MADD_OP) || (aluop_i === EXE_MADDU_OP)) {
    whilo := WRITE_ENABLE
    hi := hilo_temp1(63, 32)
    lo := hilo_temp1(31, 0)
  }.elsewhen((aluop_i === EXE_MSUB_OP) || (aluop_i === EXE_MSUBU_OP)) {
    whilo := WRITE_ENABLE
    hi := hilo_temp1(63, 32)
    lo := hilo_temp1(31, 0)
  }.elsewhen((aluop_i === EXE_DIV_OP) || (aluop_i === EXE_DIVU_OP)) {
    whilo := WRITE_ENABLE
    hi := div_result_i(63, 32)
    lo := div_result_i(31, 0)
  }.elsewhen(aluop_i === EXE_MTHI_OP) {
    whilo := WRITE_ENABLE
    hi := reg1_i
    lo := LO
  }.elsewhen(aluop_i === EXE_MTLO_OP) {
    whilo := WRITE_ENABLE
    hi := HI
    lo := reg1_i
  }.otherwise {
    whilo := WRITE_DISABLE
    hi := ZERO_WORD
    lo := ZERO_WORD
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_waddr := 0.U
    cp0_wen := WRITE_DISABLE
    cp0_data := ZERO_WORD
  }.elsewhen(aluop_i === EXE_MTC0_OP) {
    cp0_waddr := inst_i(15, 11)
    cp0_wen := WRITE_ENABLE
    cp0_data := reg1_i
  }.otherwise {
    cp0_waddr := 0.U
    cp0_wen := WRITE_DISABLE
    cp0_data := ZERO_WORD
  }

  // debug
  // printf(p"execute :pc 0x${Hexadecimal(pc)}\n")
}
