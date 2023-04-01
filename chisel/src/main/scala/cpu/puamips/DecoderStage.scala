package cpu.puamips

import Const._
import chisel3._

class DecoderStage extends Module {
  val io = IO(new Bundle {
    val fromControl = Flipped(new Control_DecoderStage())
    val fromFetch = Flipped(new Fetch_DecoderStage())
    val fromInstMemory = Flipped(new InstMemory_DecoderStage())
    val decoder = new DecoderStage_Decoder()
  })
  // input
  val stall = Wire(STALL_BUS)
  stall := io.fromControl.stall
  // io.fromFetch.pc
  // io.fromInstMemory.inst

  // output
  val pc = RegInit(INST_ADDR_BUS_INIT)
  io.decoder.pc := pc
  val inst = RegInit(INST_BUS_INIT)
  io.decoder.inst := inst

  when(stall(1) === STOP && stall(2) === NOT_STOP) {
    pc := pc
    inst := ZERO_WORD
  }.elsewhen(stall(1) === NOT_STOP) {
    pc := io.fromFetch.pc
    inst := io.fromInstMemory.inst
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
