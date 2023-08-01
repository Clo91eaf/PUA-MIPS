package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class SignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    // 除数
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(DATA_WID.W))
    // 被除数
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(DATA_WID.W))
    // 结果
    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(HILO_WID.W))
  })
}

class UnsignedDiv extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val aclk = Input(Clock())
    // 除数
    val s_axis_divisor_tvalid = Input(Bool())
    val s_axis_divisor_tready = Output(Bool())
    val s_axis_divisor_tdata  = Input(UInt(DATA_WID.W))
    // 被除数
    val s_axis_dividend_tvalid = Input(Bool())
    val s_axis_dividend_tready = Output(Bool())
    val s_axis_dividend_tdata  = Input(UInt(DATA_WID.W))
    // 结果
    val m_axis_dout_tvalid = Output(Bool())
    val m_axis_dout_tdata  = Output(UInt(HILO_WID.W))
  })
}

class Div extends Module {
  val io = IO(new Bundle {
    val src1        = Input(UInt(DATA_WID.W))
    val src2        = Input(UInt(DATA_WID.W))
    val signed      = Input(Bool())
    val start       = Input(Bool())
    val allow_to_go = Input(Bool())

    val ready  = Output(Bool())
    val result = Output(UInt(HILO_WID.W))
  })

  val signedDiv   = Module(new SignedDiv()).io
  val unsignedDiv = Module(new UnsignedDiv()).io

  signedDiv.aclk   := clock
  unsignedDiv.aclk := clock

  // 0为被除数，1为除数
  val unsignedDiv_sent = Seq.fill(2)(RegInit(false.B))
  val signedDiv_sent   = Seq.fill(2)(RegInit(false.B))

  when(unsignedDiv.s_axis_dividend_tready && unsignedDiv.s_axis_dividend_tvalid) {
    unsignedDiv_sent(0) := true.B
  }
  when(unsignedDiv.s_axis_divisor_tready && unsignedDiv.s_axis_divisor_tvalid) {
    unsignedDiv_sent(1) := true.B
  }

  when(signedDiv.s_axis_dividend_tready && signedDiv.s_axis_dividend_tvalid) {
    signedDiv_sent(0) := true.B
  }
  when(signedDiv.s_axis_divisor_tready && signedDiv.s_axis_divisor_tvalid) {
    signedDiv_sent(1) := true.B
  }

  when(io.allow_to_go) {
    unsignedDiv_sent(0) := false.B
    unsignedDiv_sent(1) := false.B
    signedDiv_sent(0)   := false.B
    signedDiv_sent(1)   := false.B
  }
  // 被除数和除数的valid信号
  signedDiv.s_axis_dividend_tvalid := io.start && !signedDiv_sent(0) && io.signed
  signedDiv.s_axis_divisor_tvalid  := io.start && !signedDiv_sent(1) && io.signed

  unsignedDiv.s_axis_dividend_tvalid := io.start && !unsignedDiv_sent(0) && !io.signed
  unsignedDiv.s_axis_divisor_tvalid  := io.start && !unsignedDiv_sent(1) && !io.signed

  // 被除数和除数的值
  signedDiv.s_axis_dividend_tdata := io.src1
  signedDiv.s_axis_divisor_tdata  := io.src2

  unsignedDiv.s_axis_dividend_tdata := io.src1
  unsignedDiv.s_axis_divisor_tdata  := io.src2

  io.ready  := Mux(io.signed, signedDiv.m_axis_dout_tvalid, unsignedDiv.m_axis_dout_tvalid)
  io.result := Mux(io.signed, signedDiv.m_axis_dout_tdata, unsignedDiv.m_axis_dout_tdata)

}
