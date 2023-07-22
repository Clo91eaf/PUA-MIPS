package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.decoder.{DecodedInst}
import cpu.CpuConfig

class IdExInst0 extends Bundle {
  val pc        = UInt(PC_WID.W)
  val inst_info = new DecodedInst()
  val ex        = new ExceptionInfo()
  val jb_info = new Bundle {
    val is_branch     = Bool()
    val pred_taken    = Bool()
    val jump_conflict = Bool()
    val branch_target = UInt(PC_WID.W)
  }
}

class IdExInst1 extends Bundle {
  val pc        = UInt(PC_WID.W)
  val inst_info = new DecodedInst()
  val ex        = new ExceptionInfo()
}

class ExecuteStage(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val clear       = Input(Vec(config.decoderNum, Bool()))
    val allow_to_go = Input(Vec(config.decoderNum, Bool()))
    val fromDecode = Input(new Bundle {
      val inst0 = new IdExInst0()
      val inst1 = new IdExInst1()

    })
    val toExecute = Output(new Bundle {
      val inst0 = new IdExInst0()
      val inst1 = new IdExInst1()
    })

  })

  val inst0_queue = Module(new Queue(new IdExInst0(), 1, pipe = true, hasFlush = true))
  val inst1_queue = Module(new Queue(new IdExInst1(), 1, pipe = true, hasFlush = true))

  inst0_queue.io.enq.valid := io.allow_to_go(0)
  inst1_queue.io.enq.valid := io.allow_to_go(1)
  inst0_queue.io.deq.ready := io.allow_to_go(0)
  inst1_queue.io.deq.ready := io.allow_to_go(1)

  inst0_queue.io.enq.bits := io.fromDecode.inst0
  inst1_queue.io.enq.bits := io.fromDecode.inst1

  io.toExecute.inst0 := inst0_queue.io.deq.bits
  io.toExecute.inst1 := inst1_queue.io.deq.bits

  inst0_queue.flush := io.clear(0) || reset.asBool()
  inst1_queue.flush := io.clear(1) || reset.asBool()

}
