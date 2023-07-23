package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class MulBlackBox extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk   = Input(Clock())
    val rst   = Input(Reset())
    val a     = Input(UInt(32.W))
    val b     = Input(UInt(32.W))
    val sign  = Input(Bool())
    val start = Input(Bool())

    val result = Output(UInt(64.W))
    val ready  = Output(Bool())
  })
  addResource("/blackbox/mul.sv")
}

class Mul extends Module {
  val io = IO(new Bundle {
    val src1   = Input(UInt(DATA_WID.W))
    val src2   = Input(UInt(DATA_WID.W))
    val signed = Input(Bool())
    val start  = Input(Bool())

    val result = Output(UInt(HILO_WID.W))
    val ready  = Output(Bool())
  })
  val mul = Module(new MulBlackBox()).io
  mul.clk   := clock
  mul.rst   := reset
  mul.a     := io.src1
  mul.b     := io.src2
  mul.sign  := io.signed
  mul.start := io.start
  io.result := mul.result
  io.ready  := mul.ready
}
