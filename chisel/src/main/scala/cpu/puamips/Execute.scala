package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import firrtl.FirrtlProtos.Firrtl.Statement.Memory

class Execute extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_Execute())
    val fromMemory = Flipped(new Memory_Execute())
    val fromWriteBack = Flipped(new WriteBack_Execute())
    val decoder = new Execute_Decoder()
    val memory = new Execute_Memory()
  })
  // input-decoder
  val pc = RegInit(REG_BUS_INIT)
  val aluop = RegInit(ALU_OP_BUS_INIT)
  val alusel = RegInit(ALU_SEL_BUS_INIT)
  val reg1 = RegInit(REG_BUS_INIT)
  val reg2 = RegInit(REG_BUS_INIT)
  val waddr = RegInit(REG_ADDR_BUS_INIT)
  val wen = RegInit(WRITE_DISABLE)
  val link_address = RegInit(REG_BUS_INIT)
  val inst = RegInit(REG_BUS_INIT)
  aluop := io.fromDecoder.aluop
  alusel := io.fromDecoder.alusel
  reg1 := io.fromDecoder.reg1
  reg2 := io.fromDecoder.reg2
  waddr := io.fromDecoder.waddr
  wen := io.fromDecoder.wen
  link_address := io.fromDecoder.link_addr
  inst := io.fromDecoder.inst

  // input-memory
  val whilo = RegInit(WRITE_DISABLE)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  whilo := io.fromMemory.whilo
  hi := io.fromMemory.hi
  lo := io.fromMemory.lo

  // input-write back
  hi := io.fromWriteBack.hi
  lo := io.fromWriteBack.lo
  whilo := io.fromWriteBack.whilo

  // output-decoder
  val wdata = RegInit(REG_BUS_INIT)
  io.decoder.wdata := wdata
  io.decoder.waddr := waddr
  io.decoder.wen := wen

  // output-memory
  io.memory.pc := pc
  io.memory.waddr := waddr
  io.memory.wen := wen
  io.memory.wdata := wdata
  io.memory.aluop := aluop
  io.memory.addr := reg1 + Util.signedExtend(inst(15, 0))
  io.memory.reg2 := reg2
  io.memory.hi := hi
  io.memory.lo := lo
  io.memory.whilo := whilo

  // 保存逻辑运算的结果
  val logicout = RegInit(REG_BUS_INIT) // 保存逻辑运算的结果
  val shiftres = RegInit(REG_BUS_INIT) // 保存移位操作运算的结果
  val moveres = RegInit(REG_BUS_INIT) // 保存移动操作运算的结果
  val arithmeticres = RegInit(REG_BUS_INIT) // 保存算术运算结果
  val HI = RegInit(REG_BUS_INIT)
  val LO = RegInit(REG_BUS_INIT)

  val ov_sum = Wire(Bool()) // 保存溢出情况
  val reg1_eq_reg2 = Wire(Bool()) // 第一个操作数是否等于第二个操作数
  val reg1_lt_reg2 = Wire(Bool()) // 第一个操作数是否小于第二个操作数
  val reg2_mux = Wire(REG_BUS) // 保存输入的第二个操作reg2的补码
  val reg1_not = Wire(REG_BUS) // 保存输入的第一个操作数reg1取反后的值
  val result_sum = Wire(REG_BUS) // 保存加法结果
  val opdata1_mult = Wire(REG_BUS) // 乘法操作中的被乘数
  val opdata2_mult = Wire(REG_BUS) // 乘法操作中的乘数
  val hilo_temp = Wire(DOUBLE_REG_BUS) // 临时保存乘法结果，宽度为64位
  val mulres = RegInit(DOUBLE_REG_BUS_INIT) // 保存乘法结果，宽度为64位

  // 根据aluop指示的运算子类型进行运算

  // LOGIC

  logicout := ZERO_WORD // default
  switch(aluop) {
    is(EXE_OR_OP) {
      logicout := reg1 | reg2
    }
    is(EXE_AND_OP) {
      logicout := reg1 & reg2
    }
    is(EXE_NOR_OP) {
      logicout := ~(reg1 | reg2)
    }
    is(EXE_XOR_OP) {
      logicout := reg1 ^ reg2
    }
  }

  // SHIFT

  shiftres := ZERO_WORD // default
  switch(aluop) {
    is(EXE_SLL_OP) {
      shiftres := reg2 << reg1(4, 0)
    }
    is(EXE_SRL_OP) {
      shiftres := reg2 >> reg1(4, 0)
    }
    is(EXE_SRA_OP) {
      shiftres := (reg2.asSInt >> reg1(4, 0)).asUInt
    }
  }
  // 第二个操作数
  reg2_mux := MuxCase(
    reg2,
    Seq(
      (aluop === EXE_SUB_OP) -> (-(reg2.asSInt)).asUInt,
      (aluop === EXE_SUBU_OP) -> (-(reg2.asSInt)).asUInt,
      (aluop === EXE_SLT_OP) -> (-(reg2.asSInt)).asUInt
    )
  )
  // 运算结果
  result_sum := reg1 + reg2_mux
  // 是否溢出
  ov_sum := Mux(((reg1 +& reg2_mux) === (reg1 + reg2)), false.B, true.B)
  // 操作数1是否小于操作数2
  reg1_lt_reg2 := reg1.asSInt < reg2.asSInt
  // 操作数1是否等于操作数2
  reg1_eq_reg2 := reg1 === reg2
  // 对操作数1取反
  reg1_not := ~reg1

  // @formatter:off
  arithmeticres := 
      Mux(aluop === EXE_SLT_OP || aluop === EXE_SLTU_OP, 
          reg1_lt_reg2, //比较运算
      Mux(aluop === EXE_ADD_OP || aluop === EXE_ADDU_OP || aluop === EXE_ADDI_OP || aluop === EXE_ADDIU_OP,
          result_sum, //加法运算
      Mux(aluop === EXE_SUB_OP || aluop === EXE_SUBU_OP,
          result_sum, //减法运算
      Mux(aluop === EXE_CLZ_OP, //计数运算clz
          (31 to 0 by -1).foldLeft(32.U) { (res, i) => Mux(reg1(i), res, i.U)},
      Mux(aluop === EXE_CLO_OP, //计数运算clo
          (31 to 0 by -1).foldLeft(32.U) { (res, i) =>Mux(!reg1(i), res, i.U)},
          ZERO_WORD // default
            )
          )
        )
      )
    )
  // @formatter:on

  // 被乘数
  opdata1_mult := MuxCase(
    reg1,
    Seq(
      (aluop === EXE_MUL_OP) -> (-(reg1.asSInt)).asUInt,
      (aluop === EXE_MULT_OP) -> (-(reg1.asSInt)).asUInt,
      (reg1(31) === 1.U) -> (-(reg1.asSInt)).asUInt
    )
  )
  // 乘数
  opdata2_mult := MuxCase(
    reg2,
    Seq(
      (aluop === EXE_MUL_OP) -> (-(reg2.asSInt)).asUInt,
      (aluop === EXE_MULT_OP) -> (-(reg2.asSInt)).asUInt,
      (reg2(31) === 1.U) -> (-(reg2.asSInt)).asUInt
    )
  )
  // 临时乘法结果
  hilo_temp := opdata1_mult * opdata2_mult

  // 对乘法结果修正(A*B）补=A补 * B补
  when((aluop === EXE_MULT_OP) || (aluop === EXE_MUL_OP)) {
    when(reg1(31) ^ reg2(31) === 1.U) {
      mulres := ~hilo_temp + 1.U
    }.otherwise {
      mulres := hilo_temp
    }
  }.otherwise {
    mulres := hilo_temp
  }

  // 得到最新的HI、LO寄存器的值，此处要解决指令数据相关问题
  HI := hi
  LO := lo

  // MFHI、MFLO、MOVN、MOVZ指令
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
  }

  // 根据alusel指示的运算类型，选择一个运算结果作为最终结果
  waddr := waddr
  when(
    ((aluop === EXE_ADD_OP) || (aluop === EXE_ADDI_OP) || (aluop === EXE_SUB_OP)) && (ov_sum === 1.U)
  ) {
    wen := WRITE_DISABLE
  }.otherwise {
    wen := wen
  }
  wdata := ZERO_WORD // default
  switch(alusel) {
    is(EXE_RES_LOGIC) { wdata := logicout } // 逻辑运算
    is(EXE_RES_SHIFT) { wdata := shiftres } // 移位运算
    is(EXE_RES_MOVE) { wdata := moveres } // 移动运算
    is(EXE_RES_ARITHMETIC) { wdata := arithmeticres } // 除乘法外简单算术操作指令
    is(EXE_RES_MUL) { wdata := mulres(31, 0) } // 乘法指令mul
    is(EXE_RES_JUMP_BRANCH) { wdata := link_address }
  }

  // MTHI和MTLO指令 乘法运算结果保存
  when((aluop === EXE_MULT_OP) || (aluop === EXE_MULTU_OP)) {
    whilo := WRITE_ENABLE
    hi := mulres(63, 32)
    lo := mulres(31, 0)
  }.elsewhen(aluop === EXE_MTHI_OP) {
    whilo := WRITE_ENABLE
    hi := reg1
    lo := LO
  }.elsewhen(aluop === EXE_MTLO_OP) {
    whilo := WRITE_ENABLE
    hi := HI
    lo := reg1
  }.otherwise {
    whilo := WRITE_DISABLE
    hi := ZERO_WORD
    lo := ZERO_WORD
  }

  // debug
  printf(p"execute :pc 0x${Hexadecimal(pc)}\n")
}
