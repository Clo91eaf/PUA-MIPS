package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class DivBlackBox extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val clk = Input(Clock())
    val rst = Input(Reset())

    val signed_div_i = Input(Bool())
    val start_i      = Input(Bool())
    val annul_i      = Input(Bool())
    val opdata1_i    = Input(UInt(32.W))
    val opdata2_i    = Input(UInt(32.W))

    val result_o = Output(UInt(64.W))
    val ready_o  = Output(Bool())
  })
  addResource("/blackbox/div.sv")
}

class Div extends Module {
  val io = IO(new Bundle {
    val src1   = Input(UInt(DATA_WID.W))
    val src2   = Input(UInt(DATA_WID.W))
    val signed = Input(Bool())
    val start  = Input(Bool())
    val annul  = Input(Bool())

    val ready  = Output(Bool())
    val result = Output(UInt(HILO_WID.W))
  })

  val div = Module(new DivBlackBox()).io
  div.clk          := clock
  div.rst          := reset
  div.signed_div_i := io.signed
  div.start_i      := io.start
  div.annul_i      := io.annul
  div.opdata1_i    := io.src1
  div.opdata2_i    := io.src2
  io.ready         := div.ready_o
  io.result        := div.result_o
}
