package cpu.openmips

import chisel3.stage.ChiselGeneratorAnnotation

object testMain extends App {
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated/openmips"),
    Seq(ChiselGeneratorAnnotation(() => new Sopc))
  )
}
