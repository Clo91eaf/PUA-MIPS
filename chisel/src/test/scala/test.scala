package cpu

import chisel3.stage.ChiselGeneratorAnnotation

import cpu.CpuConfig
import cpu.pipeline.execute.Div
import cpu.pipeline.execute.Mul

object testMain extends App {
  implicit val config = new CpuConfig()
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => new Mul())),
  )
}
