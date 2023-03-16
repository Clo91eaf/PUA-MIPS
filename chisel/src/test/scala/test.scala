package cpu.openmips

import chisel3.stage.ChiselGeneratorAnnotation

object test extends App {
  val path = "generated/test"
  val s = "--target-dir"
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Ex))
  )
}
