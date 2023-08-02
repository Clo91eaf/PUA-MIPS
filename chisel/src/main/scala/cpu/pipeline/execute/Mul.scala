package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Mul extends Module {
  val io = IO(new Bundle {
    val src1   = Input(UInt(DATA_WID.W))
    val src2   = Input(UInt(DATA_WID.W))
    val signed = Input(Bool())
    val start  = Input(Bool())

    val result = Output(UInt(HILO_WID.W))
  })
  when(io.start) {
    io.result := Mux(io.signed, (io.src1.asSInt * io.src2.asSInt).asUInt, io.src1 * io.src2)
  }.otherwise {
    io.result := 0.U
  }
}
