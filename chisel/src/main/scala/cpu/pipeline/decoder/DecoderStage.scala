package cpu.pipeline.decoder

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class DecoderStage extends Module {
  val io = IO(new Bundle {
    val fromControl    = Flipped(new Control_DecoderStage())
    val fromFetchStage = Flipped(new FetchStage_DecoderStage())
    val decoder        = new DecoderStage_Decoder()
  })
  // input-control
  val stall = Wire(STALL_BUS)
  stall := io.fromControl.stall

  // output-decoder
  val pc = RegInit(INST_ADDR_BUS_INIT)
  io.decoder.pc := pc

  when(io.fromControl.flush) {
    pc := ZERO_WORD
  }.elsewhen(stall(1) === STOP && stall(2) === NOT_STOP) {
    pc := pc
  }.elsewhen(stall(1) === NOT_STOP) {
    pc := io.fromFetchStage.pc
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
