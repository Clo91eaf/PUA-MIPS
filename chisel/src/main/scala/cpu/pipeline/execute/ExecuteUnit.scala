package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.CpuConfig
import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.memory.ExecuteUnitMemoryUnit

class ExecuteUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl         = new ExecuteCtrl()
    val executeStage = Input(new DecoderUnitExecuteUnit())

    val memoryStage = Output(new ExecuteUnitMemoryUnit())
  })
}
