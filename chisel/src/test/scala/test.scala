// ANDtest.scala
package cpu.puamips

import chisel3.stage.ChiselGeneratorAnnotation

object testMain extends App {
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => new Decoder))
  )
}

