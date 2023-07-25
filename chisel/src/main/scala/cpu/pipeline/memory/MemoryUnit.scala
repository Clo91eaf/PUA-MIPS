package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.writeback.MemoryUnitWriteBackUnit

class MemoryUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl           = new MemoryCtrl()
    val memoryUnit     = Input(new ExecuteUnitMemoryUnit())
    val writeBackStage = Output(new MemoryUnitWriteBackUnit())
  })
}
