package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.memory.Cp0Info

class MemWbInst1 extends Bundle {
  val pc        = UInt(PC_WID.W)
  val inst_info = new InstInfo()
  val rd_info   = new RdInfo()
  val ex        = new ExceptionInfo()
}
class MemWbInst0 extends MemWbInst1 {
  val cp0 = new Cp0Info()
  val int = Bool()
}

class MemoryUnitWriteBackUnit extends Bundle {
  val inst0 = new MemWbInst0()
  val inst1 = new MemWbInst1()
}
class WriteBackStage(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new Bundle {
      val allow_to_go = Bool()
      val clear       = Vec(config.decoderNum, Bool())
    })
    val memoryUnit    = Input(new MemoryUnitWriteBackUnit())
    val writeBackUnit = Output(new MemoryUnitWriteBackUnit())
  })
  val inst0_queue = Module(new Queue(new MemWbInst0(), 1, pipe = true, hasFlush = true))
  val inst1_queue = Module(new Queue(new MemWbInst1(), 1, pipe = true, hasFlush = true))

  inst0_queue.io.enq.valid := io.ctrl.allow_to_go
  inst1_queue.io.enq.valid := io.ctrl.allow_to_go
  inst0_queue.io.deq.ready := io.ctrl.allow_to_go
  inst1_queue.io.deq.ready := io.ctrl.allow_to_go

  inst0_queue.io.enq.bits := io.memoryUnit.inst0
  inst1_queue.io.enq.bits := io.memoryUnit.inst1

  io.writeBackUnit.inst0 := inst0_queue.io.deq.bits
  io.writeBackUnit.inst1 := inst1_queue.io.deq.bits

  inst0_queue.flush := io.ctrl.clear(0) || reset.asBool()
  inst1_queue.flush := io.ctrl.clear(1) || reset.asBool()
}
