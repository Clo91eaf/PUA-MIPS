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

class ExeMemInst0(implicit val config: CpuConfig) extends ExeMemInst1 {
  val cp0 = new Cp0Info()
  val mem = new Bundle {
    val en        = Bool()
    val ren       = Bool()
    val wen       = Bool()
    val inst_info = new InstInfo()
    val addr      = UInt(DATA_ADDR_WID.W)
    val wdata     = UInt(DATA_WID.W)
    val sel       = Vec(config.fuNum, Bool())
    val llbit     = Bool()
  }
}

class ExecuteUnitMemoryUnit(implicit val config: CpuConfig) extends Bundle {

  val inst0 = new ExeMemInst0()
  val inst1 = new ExeMemInst1()
}

class MemoryStage(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new Bundle {
      val allow_to_go = Bool()
      val clear       = Bool()
    })
    val executeUnit = Input(new ExecuteUnitMemoryUnit())
    val memoryUnit  = Output(new ExecuteUnitMemoryUnit())
  })
  val inst0 = RegInit(0.U.asTypeOf(new ExeMemInst0()))
  val inst1 = RegInit(0.U.asTypeOf(new ExeMemInst1()))

  when(io.ctrl.clear) {
    inst0 := 0.U.asTypeOf(new ExeMemInst0())
    inst1 := 0.U.asTypeOf(new ExeMemInst1())
  }.elsewhen(io.ctrl.allow_to_go) {
    inst0 := io.executeUnit.inst0
    inst1 := io.executeUnit.inst1
  }

  io.memoryUnit.inst0 := inst0
  io.memoryUnit.inst1 := inst1
}
