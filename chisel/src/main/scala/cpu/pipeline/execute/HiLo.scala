package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines.Const._

import cpu.defines._
class HiLo extends Module {
  val io = IO(new Bundle {
    val wen   = Input(Bool())
    val wdata = Input(UInt(HILO_WID.W))
    val rdata = Output(UInt(HILO_WID.W))
  })
  // output
  val hilo = RegInit(0.U(HILO_WID.W))

  when(io.wen) {
    hilo := io.wdata
  }

  io.rdata := hilo
}
