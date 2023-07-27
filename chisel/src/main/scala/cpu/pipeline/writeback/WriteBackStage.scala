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
  val inst0 = RegInit(0.U.asTypeOf(new MemWbInst0()))
  val inst1 = RegInit(0.U.asTypeOf(new MemWbInst1()))

  when(io.ctrl.clear(0)) {
    inst0 := 0.U.asTypeOf(new MemWbInst0())
  }.elsewhen(io.ctrl.allow_to_go) {
    inst0 := io.memoryUnit.inst0
  }

  when(io.ctrl.clear(1)) {
    inst1 := 0.U.asTypeOf(new MemWbInst1())
  }.elsewhen(io.ctrl.allow_to_go) {
    inst1 := io.memoryUnit.inst1
  }

  io.writeBackUnit.inst0 := inst0
  io.writeBackUnit.inst1 := inst1
}
