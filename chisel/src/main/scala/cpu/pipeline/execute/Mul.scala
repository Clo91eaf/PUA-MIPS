package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class SignedMul extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val CLK = Input(Clock())
    val CE  = Input(Bool())
    val A   = Input(UInt((DATA_WID + 1).W))
    val B   = Input(UInt((DATA_WID + 1).W))

    val P = Output(UInt((HILO_WID + 2).W))
  })
}

class Mul(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val src1        = Input(UInt(DATA_WID.W))
    val src2        = Input(UInt(DATA_WID.W))
    val signed      = Input(Bool())
    val start       = Input(Bool())
    val allow_to_go = Input(Bool())

    val ready  = Output(Bool())
    val result = Output(UInt(HILO_WID.W))
  })

  if (config.build) {
    val signedMul = Module(new SignedMul()).io
    val cnt       = RegInit(0.U(log2Ceil(config.mulClockNum).W))

    cnt := MuxCase(
      cnt,
      Seq(
        (io.start && !io.ready) -> (cnt + 1.U),
        io.allow_to_go          -> 0.U,
      ),
    )

    signedMul.CLK := clock
    signedMul.CE  := io.start
    when(io.signed) {
      signedMul.A := Cat(io.src1(DATA_WID - 1), io.src1)
      signedMul.B := Cat(io.src2(DATA_WID - 1), io.src2)
    }.otherwise {
      signedMul.A := Cat(0.U(1.W), io.src1)
      signedMul.B := Cat(0.U(1.W), io.src2)
    }
    io.ready  := cnt >= config.mulClockNum.U
    io.result := signedMul.P(HILO_WID - 1, 0)
  } else {
    when(io.start) {
      io.result := Mux(io.signed, (io.src1.asSInt * io.src2.asSInt).asUInt, io.src1 * io.src2)
    }.otherwise {
      io.result := 0.U
    }
  }
}
