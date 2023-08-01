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
      val jump_inst     = Bool()
      val jump_register = Bool()
      val jump          = Bool()
      val jump_target   = UInt(PC_WID.W)
    })
  })

  val op                 = io.in.decoded_inst0.op
  val jump_inst          = VecInit(EXE_J, EXE_JAL).contains(op)
  val jump_register_inst = VecInit(EXE_JR, EXE_JALR).contains(op)
  io.out.jump_inst := jump_inst || jump_register_inst
  io.out.jump      := io.in.allow_to_go && (jump_inst || jump_register_inst && !io.out.jump_register)
  io.out.jump_register := jump_register_inst &&
    ((io.in.forward(0).exe.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(0).exe.waddr) ||
      (io.in.forward(1).exe.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(1).exe.waddr) ||
      (io.in.forward(0).mem.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(0).mem.waddr) ||
      (io.in.forward(1).mem.wen && io.in.decoded_inst0.reg1_raddr === io.in.forward(1).mem.waddr))
  val pc_plus_4 = io.in.pc + 4.U(PC_WID.W)
  io.out.jump_target := Mux(
    jump_inst,
    Cat(pc_plus_4(31, 28), io.in.decoded_inst0.inst(25, 0), 0.U(2.W)),
    io.in.reg1_data,
  )
}
