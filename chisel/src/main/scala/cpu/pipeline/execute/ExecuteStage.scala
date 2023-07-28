package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class IdExeInst0 extends Bundle {
  val pc        = UInt(PC_WID.W)
  val inst_info = new InstInfo()
  val src_info  = new SrcInfo()
  val ex        = new ExceptionInfo()
  val jb_info = new Bundle {
    // jump ctrl
    val jump_regiser_conflict = Bool()
    // bpu
    val is_branch        = Bool()
    val pred_branch_flag = Bool()
    val branch_target    = UInt(PC_WID.W)
  }
}

class IdExeInst1 extends Bundle {
  val allow_to_go = Bool()
  val pc          = UInt(PC_WID.W)
  val inst_info   = new InstInfo()
  val src_info    = new SrcInfo()
  val ex          = new ExceptionInfo()
}

class DecoderUnitExecuteUnit extends Bundle {
  val inst0 = new IdExeInst0()
  val inst1 = new IdExeInst1()
}

class ExecuteStage(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new Bundle {
      val inst0_allow_to_go = Bool()
      val clear             = Vec(config.decoderNum, Bool())
    })
    val decoderUnit = Input(new DecoderUnitExecuteUnit())
    val executeUnit = Output(new DecoderUnitExecuteUnit())
  })

  val inst0 = RegInit(0.U.asTypeOf(new IdExeInst0()))
  val inst1 = RegInit(0.U.asTypeOf(new IdExeInst1()))

  when(io.ctrl.clear(0)) {
    inst0 := 0.U.asTypeOf(new IdExeInst0())
  }.elsewhen(io.ctrl.inst0_allow_to_go) {
    inst0 := io.decoderUnit.inst0
  }

  when(io.ctrl.clear(1)) {
    inst1 := 0.U.asTypeOf(new IdExeInst1())
  }.elsewhen(io.decoderUnit.inst1.allow_to_go) {
    inst1 := io.decoderUnit.inst1
  }

  io.executeUnit.inst0 := inst0
  io.executeUnit.inst1 := inst1
}
