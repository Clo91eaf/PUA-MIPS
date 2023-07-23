package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class ExeMemInst0 extends Bundle{

}

class ExeMemInst1 extends Bundle{

}

class MemoryStage(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new Bundle {
      val allow_to_go = Bool()
      val clear       = Vec(config.decoderNum, Bool())
    })
    val executeUnit = new Bundle {}
    val memoryUnit  = new Bundle {}
  })
  val inst0_queue = Module(new Queue(new ExeMemInst0(), 1, pipe = true, hasFlush = true))
  val inst1_queue = Module(new Queue(new ExeMemInst1(), 1, pipe = true, hasFlush = true))
}
