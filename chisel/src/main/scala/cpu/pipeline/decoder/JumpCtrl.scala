package cpu.pipeline.decoder

import chisel3._
import chisel3.util._

import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class JumpCtrl(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new Bundle {
      val allow_to_go   = Bool()
      val pc            = UInt(PC_WID.W)
      val decoded_inst0 = new InstInfo()
      val reg1_data     = UInt(DATA_WID.W)
      val forward       = Vec(config.fuNum, new DataForwardToDecoderUnit())
    })
    val out = Output(new Bundle {
      val inst_is_jump  = Bool()
      val jump_conflict = Bool()
      val jump_flag     = Bool()
      val jump_target   = UInt(PC_WID.W)
    })
  })

  val op         = io.in.decoded_inst0.op
  val inst_is_j  = VecInit(EXE_J, EXE_JAL).contains(op)
  val inst_is_jr = VecInit(EXE_JR, EXE_JALR).contains(op)
  io.out.inst_is_jump := inst_is_j || inst_is_jr
  io.out.jump_flag    := io.in.allow_to_go && (inst_is_j || inst_is_jr && !io.out.jump_conflict)
  io.out.jump_conflict := inst_is_jr &&
    ((io.in.forward(0).exe.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(0).exe.waddr) ||
      (io.in.forward(1).exe.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(1).exe.waddr) ||
      (io.in.forward(0).mem.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(0).mem.waddr) ||
      (io.in.forward(1).mem.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(1).mem.waddr))
  val pc_plus_4 = io.in.pc + 4.U(PC_WID.W)
  io.out.jump_target := Mux(
    inst_is_j,
    Cat(pc_plus_4(31, 28), io.in.decoded_inst0.inst(25, 0), 0.U(2.W)),
    io.in.reg1_data,
  )
}
