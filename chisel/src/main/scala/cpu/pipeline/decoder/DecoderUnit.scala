package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.execute.DecoderUnitExecuteStage

class InstBufferDecoderUnit(implicit val config: CpuConfig) extends Bundle {
  val inst = Vec(
    config.decoderNum,
    Decoupled(new Bundle {
      val pc   = UInt(PC_WID.W)
      val inst = UInt(INST_WID.W)
    }),
  )
  val info = new Bundle {
    val empty        = Bool()
    val almost_empty = Bool()
  }
}

class DataForwardToDecoderUnit extends Bundle {
  val exe         = new RegWrite()
  val exe_mem_ren = Bool()
  val mem         = new RegWrite()
}

class DecoderUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    // 输入
    val instBuffer = Flipped(new InstBufferDecoderUnit())
    val regfile    = Vec(config.decoderNum, new Src12Read())
    val forward    = Input(Vec(config.fuNum, new DataForwardToDecoderUnit()))
    // 输出
    val fetchUnit = new Bundle {
      val jb_target = Output(UInt(PC_WID.W))
    }
    val bpu = new Bundle {
      val pc             = Output(UInt(PC_WID.W))
      val decoded_inst0  = Output(new DecodedInst())
      val id_allow_to_go = Output(Bool())

      val inst_is_branch   = Input(Bool())
      val pred_branch_flag = Input(Bool())
      val branch_target    = Input(UInt(PC_WID.W))
    }
    val executeStage = Output(new DecoderUnitExecuteStage())
    val ctrl         = new DecoderUnitCtrl()
  })

  val issue    = Module(new Issue()).io
  val decoder  = Seq.fill(config.decoderNum)(Module(new Decoder()))
  val jumpCtrl = Module(new JumpCtrl()).io

  issue.allow_to_go := io.ctrl.allow_to_go
  issue.instBuffer  := io.instBuffer.info

  jumpCtrl.in.allow_to_go   := io.ctrl.allow_to_go
  jumpCtrl.in.decoded_inst0 := decoder(0).io.out
  jumpCtrl.in.forward       := io.forward
  jumpCtrl.in.pc            := io.instBuffer.inst(0).bits.pc
  jumpCtrl.in.reg1_data     := io.regfile(0).src1.rdata

  val inst0_is_jb   = jumpCtrl.out.inst_is_jump || io.bpu.inst_is_branch
  val inst0_jb_flag = jumpCtrl.out.jump_flag || io.bpu.pred_branch_flag
  io.fetchUnit.jb_target := Mux(io.bpu.pred_branch_flag, io.bpu.branch_target, jumpCtrl.out.jump_target)
  io.executeStage.inst0.jb_info.jump_conflict := jumpCtrl.out.jump_conflict

  io.instBuffer.inst(0).ready := io.ctrl.allow_to_go
  io.instBuffer.inst(1).ready := issue.inst1.allow_to_go

  io.bpu.id_allow_to_go := io.ctrl.allow_to_go
  io.bpu.pc             := io.instBuffer.inst(0).bits.pc
  io.bpu.decoded_inst0  := decoder(0).io.out

  io.ctrl.inst0.src1.ren   := decoder(0).io.out.reg1_ren
  io.ctrl.inst0.src1.raddr := decoder(0).io.out.reg1_raddr
  io.ctrl.inst0.src2.ren   := decoder(0).io.out.reg2_ren
  io.ctrl.inst0.src2.raddr := decoder(0).io.out.reg2_raddr

  for (i <- 0 until (config.decoderNum)) {

    val pc   = io.instBuffer.inst(i).bits.pc
    val inst = io.instBuffer.inst(i).bits.inst
    decoder(i).io.in.inst := inst
    val decoded_inst = decoder(i).io.out

    issue.decodeInst(i)           := decoded_inst
    issue.execute.inst(i).mem_ren := io.forward(i).exe_mem_ren
  }
}
