package cpu.pipeline.execute

import chisel3._
import chisel3.util._

class LLbit extends Module {
  val io = IO(new Bundle {
    val do_flush = Input(Bool())
    val wen      = Input(Bool())
    val wdata    = Input(Bool())

    val rdata = Output(Bool())
  })
  val llbit = RegInit(false.B)

  when(io.do_flush) {
    llbit := false.B
  }.elsewhen(io.wen) {
    llbit := io.wdata
  }

  io.rdata := llbit
}
