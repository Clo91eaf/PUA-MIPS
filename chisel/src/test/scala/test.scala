package cpu

import chisel3.stage.ChiselGeneratorAnnotation

import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.decoder._
import cpu.CpuConfig

object testMain extends App {
  implicit val config = new CpuConfig()
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => new ForwardCtrl())),
  )
}
