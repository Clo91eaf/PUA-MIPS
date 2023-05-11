package cpu

import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline._
import cpu.pipeline.fetch._
import cpu.pipeline.decoder._
import cpu.pipeline.execute._
import cpu.pipeline.memory._
import cpu.pipeline.writeback._
import cpu.mmu._
import cpu.ctrl._
import cpu.axi._

import chisel3.stage.ChiselGeneratorAnnotation

object testMain extends App {
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => new WriteBackStage))
  )
}
