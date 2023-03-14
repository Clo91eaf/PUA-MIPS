package cpu.openmips

import chisel3.stage.ChiselGeneratorAnnotation

object testMain extends App {
  val path = "generated/openmips"
  val s = "--target-dir"
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Ex))
  )
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Id))
  )
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Inst_rom))
  )
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new OpenMips))
  )
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new PC_reg))
  )
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Regfile))
  )
  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Sopc))
  )

  (new chisel3.stage.ChiselStage).execute(
    Array(s, path),
    Seq(ChiselGeneratorAnnotation(() => new Wb))
  )
}
