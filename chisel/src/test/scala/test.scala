package cpu

import chisel3.stage.ChiselGeneratorAnnotation

import cpu.CpuConfig
import cpu.pipeline.execute._
import cpu.pipeline.memory.DataMemoryAccess
import cpu.pipeline.memory.MemoryUnit
import cpu.pipeline.writeback.WriteBackUnit

object testMain extends App {
  implicit val config = new CpuConfig()
  (new chisel3.stage.ChiselStage).execute(
    Array("--target-dir", "generated"),
    Seq(ChiselGeneratorAnnotation(() => new WriteBackUnit)),
  )
}
