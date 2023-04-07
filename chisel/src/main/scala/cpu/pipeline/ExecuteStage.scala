package cpu.pipeline

import cpu.defines.Const._
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

  // input-control
  stall := io.fromControl.stall

  // output
  val aluop              = RegInit(ALU_OP_BUS_INIT)
  val alusel             = RegInit(ALU_SEL_BUS_INIT)
  val inst               = RegInit(BUS_INIT)
  val ex_is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  val is_in_delayslot    = RegInit(NOT_IN_DELAY_SLOT)
  val link_addr          = RegInit(BUS_INIT)
  val reg1               = RegInit(BUS_INIT)
  val reg2               = RegInit(BUS_INIT)
  val waddr              = RegInit(ADDR_BUS_INIT)
  val wen                = RegInit(WRITE_DISABLE)
  val current_inst_addr  = RegInit(BUS_INIT)
  val except_type        = RegInit(0.U(32.W))
  val pc                 = RegInit(INST_ADDR_BUS_INIT)

  // output-execute
  io.execute.aluop           := aluop
  io.execute.alusel          := alusel
  io.execute.inst            := inst
  io.execute.is_in_delayslot := ex_is_in_delayslot

  // output-decoder
  io.decoder.is_in_delayslot := is_in_delayslot

  // output-execute
  io.execute.link_addr         := link_addr
  io.execute.reg1              := reg1
  io.execute.reg2              := reg2
  io.execute.waddr             := waddr
  io.execute.wen               := wen
  io.execute.current_inst_addr := current_inst_addr
  io.execute.except_type       := except_type
  io.execute.pc                := pc

  when(io.fromControl.flush) {
    aluop             := EXE_NOP_OP
    alusel            := EXE_RES_NOP
    reg1              := ZERO_WORD
    reg2              := ZERO_WORD
    waddr             := NOP_REG_ADDR
    wen               := WRITE_DISABLE
    except_type       := ZERO_WORD
    link_addr         := ZERO_WORD
    inst              := ZERO_WORD
    is_in_delayslot   := NOT_IN_DELAY_SLOT
    current_inst_addr := ZERO_WORD
    is_in_delayslot   := NOT_IN_DELAY_SLOT
    pc                := ZERO_WORD
  }.elsewhen(stall(2) === STOP && stall(3) === NOT_STOP) {
    aluop              := EXE_NOP_OP
    alusel             := EXE_RES_NOP
    reg1               := ZERO_WORD
    reg2               := ZERO_WORD
    waddr              := NOP_REG_ADDR
    wen                := WRITE_DISABLE
    link_addr          := ZERO_WORD
    ex_is_in_delayslot := NOT_IN_DELAY_SLOT
    inst               := ZERO_WORD
    except_type        := ZERO_WORD
    current_inst_addr  := ZERO_WORD
    pc                 := pc
  }.elsewhen(stall(2) === NOT_STOP) {
    aluop              := io.fromDecoder.aluop
    alusel             := io.fromDecoder.alusel
    reg1               := io.fromDecoder.reg1
    reg2               := io.fromDecoder.reg2
    waddr              := io.fromDecoder.waddr
    wen                := io.fromDecoder.wen
    link_addr          := io.fromDecoder.link_addr
    ex_is_in_delayslot := io.fromDecoder.is_in_delayslot
    is_in_delayslot    := io.fromDecoder.next_inst_in_delayslot
    inst               := io.fromDecoder.inst
    except_type        := io.fromDecoder.except_type
    current_inst_addr  := io.fromDecoder.current_inst_addr
    pc                 := io.fromDecoder.pc
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
