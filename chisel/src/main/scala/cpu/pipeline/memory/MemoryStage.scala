package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class Cp0Info extends Bundle {
  val cp0_count  = UInt(DATA_WID.W)
  val cp0_random = UInt(DATA_WID.W)
  val cp0_cause  = UInt(DATA_WID.W)
}

class ExeMemInst1 extends Bundle {
  val pc        = UInt(PC_WID.W)
  val inst_info = new InstInfo()
  val rd_info   = new RdInfo()
  val ex        = new ExceptionInfo()
}

class ExeMemInst0 extends ExeMemInst1 {
  val cp0 = new Cp0Info()
}

class ExecuteUnitMemoryUnit(implicit val config: CpuConfig) extends Bundle {
  val mem = new Bundle {
    val en        = Bool()
    val ren       = Bool()
    val wen       = Bool()
    val inst_info = new InstInfo()
    val addr      = UInt(DATA_ADDR_WID.W)
    val wdata     = UInt(DATA_WID.W)
    val sel       = Vec(config.fuNum, Bool())
  }
  val inst0 = new ExeMemInst0()
  val inst1 = new ExeMemInst1()
}

class MemoryStage(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new Bundle {
      val allow_to_go = Bool()
      val clear       = Vec(config.decoderNum, Bool())
    })
    val executeUnit = Input(new ExecuteUnitMemoryUnit())
    val memoryUnit  = Output(new ExecuteUnitMemoryUnit())
  })
  val inst0_queue = Module(new Queue(new ExeMemInst0(), 1, pipe = true, hasFlush = true))
  val inst1_queue = Module(new Queue(new ExeMemInst1(), 1, pipe = true, hasFlush = true))

  inst0_queue.io.enq.valid := io.ctrl.allow_to_go
  inst1_queue.io.enq.valid := io.ctrl.allow_to_go
  inst0_queue.io.deq.ready := io.ctrl.allow_to_go
  inst1_queue.io.deq.ready := io.ctrl.allow_to_go

  inst0_queue.io.enq.bits := io.executeUnit.inst0
  inst1_queue.io.enq.bits := io.executeUnit.inst1

  io.memoryUnit.inst0 := inst0_queue.io.deq.bits
  io.memoryUnit.inst1 := inst1_queue.io.deq.bits

  inst0_queue.flush := io.ctrl.clear(0) || reset.asBool()
  inst1_queue.flush := io.ctrl.clear(1) || reset.asBool()
}
