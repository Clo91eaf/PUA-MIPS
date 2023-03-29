package cpu.puamips

import Const._
import chisel3._
import chisel3.util._

class Control extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_Control())
    val fromExecute = Flipped(new Execute_Control())

    val fetch = new Control_Fetch()
    val decoderStage = new Control_DecoderStage()
    val executeStage = new Control_ExecuteStage()
    val memoryStage = new Control_MemoryStage()
    val writeBackStage = new Control_WriteBackStage()
  })
  val stall = RegInit(STALL_BUS_INIT)
  io.fetch.stall := stall
  io.decoderStage.stall := stall
  io.executeStage.stall := stall
  io.memoryStage.stall := stall
  io.writeBackStage.stall := stall

  when(io.fromExecute.stallreq === STOP) {
    stall := "b001111".U
  }.elsewhen(io.fromDecoder.stallreq === STOP) {
    stall := "b000111".U
  }.otherwise {
    stall := "b000000".U
  }

  // debug
  printf(p"control :stall 0x${Hexadecimal(stall)}\n")
}
