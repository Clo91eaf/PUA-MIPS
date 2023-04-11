package cpu.pipeline.execute

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class ExecuteStage extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_ExecuteStage())
    val fromExecute = Flipped(new Execute_ExecuteStage())

    val decoder = new ExecuteStage_Decoder()
    val execute = new ExecuteStage_Execute()
  })
  // input

  // output
  val aluop              = RegInit(ALU_OP_BUS_INIT)
  val alusel             = RegInit(ALU_SEL_BUS_INIT)
  val inst               = RegInit(BUS_INIT)
  val ex_is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  val is_in_delayslot    = RegInit(NOT_IN_DELAY_SLOT)
  val link_addr          = RegInit(BUS_INIT)
  val reg1               = RegInit(BUS_INIT)
  val reg2               = RegInit(BUS_INIT)
  val reg_waddr          = RegInit(ADDR_BUS_INIT)
  val reg_wen            = RegInit(WRITE_DISABLE)
  val current_inst_addr  = RegInit(BUS_INIT)
  val except_type        = RegInit(0.U(32.W))
  val pc                 = RegInit(INST_ADDR_BUS_INIT)
  val es_valid           = RegInit(false.B)

  // output-execute
  io.execute.aluop             := aluop
  io.execute.alusel            := alusel
  io.execute.inst              := inst
  io.execute.is_in_delayslot   := ex_is_in_delayslot
  io.execute.link_addr         := link_addr
  io.execute.reg1              := reg1
  io.execute.reg2              := reg2
  io.execute.reg_waddr         := reg_waddr
  io.execute.reg_wen           := reg_wen
  io.execute.current_inst_addr := current_inst_addr
  io.execute.except_type       := except_type
  io.execute.pc                := pc
  io.execute.valid             := es_valid

  // output-decoder
  io.decoder.is_in_delayslot := is_in_delayslot

  when(io.fromExecute.allowin) {
    es_valid := io.fromDecoder.valid
  }

  when(io.fromDecoder.valid && io.fromExecute.allowin) {
    aluop              := io.fromDecoder.aluop
    alusel             := io.fromDecoder.alusel
    reg1               := io.fromDecoder.reg1
    reg2               := io.fromDecoder.reg2
    reg_waddr          := io.fromDecoder.reg_waddr
    reg_wen            := io.fromDecoder.reg_wen
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
