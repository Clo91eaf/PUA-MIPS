package cpu.pipeline

import cpu.defines.Const._
import chisel3._

class DecoderStage extends Module {
  val io = IO(new Bundle {
    val fromControl = Flipped(new Control_DecoderStage())
    val fromFetch = Flipped(new Fetch_DecoderStage())
    val decoder = new DecoderStage_Decoder()
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
    pc := io.fromFetch.pc
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
