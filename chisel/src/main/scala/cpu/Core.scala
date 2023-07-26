package cpu

import chisel3._
import chisel3.util._
import chisel3.internal.DontCareBinding

import defines._
import defines.Const._
import pipeline.decoder._
import pipeline.execute._
import pipeline.memory._
import pipeline.writeback._
import ctrl._

class Core(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
  })

  val ctrl    = Module(new Ctrl()).io
  val cp0     = Module(new Cp0()).io
  val regfile = Module(new ARegFile()).io

  val decoderUnit    = Module(new DecoderUnit()).io
  val executeStage   = Module(new ExecuteStage()).io
  val executeUnit    = Module(new ExecuteUnit()).io
  val memoryStage    = Module(new MemoryStage()).io
  val memoryUnit     = Module(new MemoryUnit()).io
  val writeBackStage = Module(new WriteBackStage()).io

  ctrl.decoderUnit <> decoderUnit.ctrl
  ctrl.executeUnit <> executeStage.ctrl
  ctrl.memoryUnit <> memoryUnit.ctrl
  ctrl.writeBackUnit <> writeBackStage.ctrl

}
