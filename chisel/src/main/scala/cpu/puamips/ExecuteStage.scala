package cpu.puamips

import Const._
import chisel3._

class ExecuteStage extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_ExecuteStage())
    val fromControl = Flipped(new Control_ExecuteStage())

    val decoder = new ExecuteStage_Decoder()
    val execute = new ExecuteStage_Execute()
  })
  // input
  val stall = Wire(STALL_BUS)
  stall := io.fromControl.stall

  // out put
  val aluop = RegInit(ALU_OP_BUS_INIT)
  io.execute.aluop := aluop
  val alusel = RegInit(ALU_SEL_BUS_INIT)
  io.execute.alusel := alusel
  val inst = RegInit(REG_BUS_INIT)
  io.execute.inst := inst
  val ex_is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  io.execute.is_in_delayslot := ex_is_in_delayslot
  val is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  io.decoder.is_in_delayslot := is_in_delayslot
  val link_addr = RegInit(REG_BUS_INIT)
  io.execute.link_addr := link_addr
  val reg1 = RegInit(REG_BUS_INIT)
  io.execute.reg1 := reg1
  val reg2 = RegInit(REG_BUS_INIT)
  io.execute.reg2 := reg2
  val wd = RegInit(REG_ADDR_BUS_INIT)
  io.execute.wd := wd
  val wreg = RegInit(WRITE_DISABLE)
  io.execute.wreg := wreg
  val current_inst_addr = RegInit(REG_BUS_INIT)
  io.execute.current_inst_addr := current_inst_addr
  val excepttype = RegInit(0.U(32.W))
  io.execute.excepttype := excepttype
  val pc = RegInit(INST_ADDR_BUS_INIT)
  io.execute.pc := pc

  when(io.fromControl.flush) {
    aluop := EXE_NOP_OP
    alusel := EXE_RES_NOP
    reg1 := ZERO_WORD
    reg2 := ZERO_WORD
    wd := NOP_REG_ADDR
    wreg := WRITE_DISABLE
    excepttype := ZERO_WORD
    link_addr := ZERO_WORD
    inst := ZERO_WORD
    is_in_delayslot := NOT_IN_DELAY_SLOT
    current_inst_addr := ZERO_WORD
    is_in_delayslot := NOT_IN_DELAY_SLOT
  }.elsewhen(stall(2) === STOP && stall(3) === NOT_STOP) {
    aluop := EXE_NOP_OP
    alusel := EXE_RES_NOP
    reg1 := ZERO_WORD
    reg2 := ZERO_WORD
    wd := NOP_REG_ADDR
    wreg := WRITE_DISABLE
    link_addr := ZERO_WORD
    ex_is_in_delayslot := NOT_IN_DELAY_SLOT
    inst := ZERO_WORD
    excepttype := ZERO_WORD
    current_inst_addr := ZERO_WORD
    pc := pc
  }.elsewhen(stall(2) === NOT_STOP) {
    aluop := io.fromDecoder.aluop
    alusel := io.fromDecoder.alusel
    reg1 := io.fromDecoder.reg1
    reg2 := io.fromDecoder.reg2
    wd := io.fromDecoder.wd
    wreg := io.fromDecoder.wreg
    link_addr := io.fromDecoder.link_addr
    ex_is_in_delayslot := io.fromDecoder.is_in_delayslot
    is_in_delayslot := io.fromDecoder.next_inst_in_delayslot
    inst := io.fromDecoder.inst
    excepttype := io.fromDecoder.excepttype
    current_inst_addr := io.fromDecoder.current_inst_addr
    pc := io.fromDecoder.pc
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
