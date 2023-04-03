package cpu.puamips

import chisel3.stage.ChiselGeneratorAnnotation
import java.util.ResourceBundle.Control

object testMain extends App {
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => new Execute))
  )
}

