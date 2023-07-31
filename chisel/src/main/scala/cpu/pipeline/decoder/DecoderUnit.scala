package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.execute.DecoderUnitExecuteUnit

class InstBufferDecoderUnit(implicit val config: CpuConfig) extends Bundle {
  val inst = Flipped(
    Vec(
      config.decoderNum,
      Decoupled(new Bundle {
        val pc          = UInt(PC_WID.W)
        val inst        = UInt(INST_WID.W)
        val tlb_refill  = Bool()
        val tlb_invalid = Bool()
      }),
    ),
  )
  val info = Input(new Bundle {
    val inst0_is_in_delayslot = Bool()
    val empty                 = Bool()
    val almost_empty          = Bool()
  })

  val jump_branch_inst = Output(Bool())
}

class DataForwardToDecoderUnit extends Bundle {
  val exe      = new RegWrite()
  val exe_rmem = Bool()
  val mem      = new RegWrite()
}

class Cp0DecoderUnit extends Bundle {
  val access_allowed    = Bool()
  val kernel_mode       = Bool()
  val intterupt_allowed = Bool()
  val cause_ip          = UInt(8.W)
  val status_im         = UInt(8.W)
}

class DecoderUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    // 输入
    val instBuffer = new InstBufferDecoderUnit()
    val regfile    = Vec(config.decoderNum, new Src12Read())
    val forward    = Input(Vec(config.fuNum, new DataForwardToDecoderUnit()))
    val cp0        = Input(new Cp0DecoderUnit())
    // 输出
    val fetchUnit = new Bundle {
      val branch = Output(Bool())
      val target = Output(UInt(PC_WID.W))
    }
    val bpu = new Bundle {
      val pc             = Output(UInt(PC_WID.W))
      val decoded_inst0  = Output(new InstInfo())
      val id_allow_to_go = Output(Bool())

      val branch_inst   = Input(Bool())
      val pred_branch   = Input(Bool())
      val branch_target = Input(UInt(PC_WID.W))
    }
    val executeStage = Output(new DecoderUnitExecuteUnit())
    val ctrl         = new DecoderUnitCtrl()
  })

  val issue       = Module(new Issue()).io
  val decoder     = Seq.fill(config.decoderNum)(Module(new Decoder()))
  val jumpCtrl    = Module(new JumpCtrl()).io
  val forwardCtrl = Module(new ForwardCtrl()).io

  io.regfile(0).src1.raddr := decoder(0).io.out.reg1_raddr
  io.regfile(0).src2.raddr := decoder(0).io.out.reg2_raddr
  io.regfile(1).src1.raddr := decoder(1).io.out.reg1_raddr
  io.regfile(1).src2.raddr := decoder(1).io.out.reg2_raddr

  forwardCtrl.in.forward := io.forward
  forwardCtrl.in.regfile := io.regfile // TODO:这里的连接可能有问题

  issue.allow_to_go := io.ctrl.allow_to_go
  issue.instBuffer  := io.instBuffer.info

  jumpCtrl.in.allow_to_go   := io.ctrl.allow_to_go
  jumpCtrl.in.decoded_inst0 := decoder(0).io.out
  jumpCtrl.in.forward       := io.forward
  jumpCtrl.in.pc            := io.instBuffer.inst(0).bits.pc
  jumpCtrl.in.reg1_data     := io.regfile(0).src1.rdata

  val inst0_is_jb   = jumpCtrl.out.inst_is_jump || io.bpu.branch_inst
  val inst0_jb_flag = jumpCtrl.out.jump_flag || io.bpu.pred_branch

  io.fetchUnit.branch := inst0_jb_flag
  io.fetchUnit.target := Mux(io.bpu.pred_branch, io.bpu.branch_target, jumpCtrl.out.jump_target)

  io.instBuffer.inst(0).ready := io.ctrl.allow_to_go
  io.instBuffer.inst(1).ready := issue.inst1.allow_to_go
  io.instBuffer.jump_branch_inst   := inst0_is_jb

  io.bpu.id_allow_to_go := io.ctrl.allow_to_go
  io.bpu.pc             := io.instBuffer.inst(0).bits.pc
  io.bpu.decoded_inst0  := decoder(0).io.out

  io.ctrl.inst0.src1.ren   := decoder(0).io.out.reg1_ren
  io.ctrl.inst0.src1.raddr := decoder(0).io.out.reg1_raddr
  io.ctrl.inst0.src2.ren   := decoder(0).io.out.reg2_ren
  io.ctrl.inst0.src2.raddr := decoder(0).io.out.reg2_raddr
  io.ctrl.branch      := inst0_jb_flag

  val pc          = io.instBuffer.inst.map(_.bits.pc)
  val inst        = io.instBuffer.inst.map(_.bits.inst)
  val inst_info   = decoder.map(_.io.out)
  val tlb_refill  = io.instBuffer.inst.map(_.bits.tlb_refill)
  val tlb_invalid = io.instBuffer.inst.map(_.bits.tlb_invalid)
  val interrupt   = io.cp0.intterupt_allowed && (io.cp0.cause_ip & io.cp0.status_im).orR() && !io.instBuffer.info.empty

  for (i <- 0 until (config.decoderNum)) {
    decoder(i).io.in.inst      := inst(i)
    issue.decodeInst(i)        := inst_info(i)
    issue.execute(i).mem_ren   := io.forward(i).exe_rmem
    issue.execute(i).reg_waddr := io.forward(i).exe.waddr
  }

  io.executeStage.inst0.pc        := pc(0)
  io.executeStage.inst0.inst_info := inst_info(0)
  io.executeStage.inst0.src_info.src1_data := Mux(
    inst_info(0).reg1_ren,
    forwardCtrl.out.inst(0).src1.rdata,
    decoder(0).io.out.imm32,
  )
  io.executeStage.inst0.src_info.src2_data := Mux(
    inst_info(0).reg2_ren,
    forwardCtrl.out.inst(0).src2.rdata,
    decoder(0).io.out.imm32,
  )
  io.executeStage.inst0.ex.flush_req :=
    io.executeStage.inst0.ex.excode =/= EX_NO ||
      io.executeStage.inst0.ex.tlb_refill ||
      io.executeStage.inst0.ex.eret
  io.executeStage.inst0.ex.tlb_refill := tlb_refill(0)
  io.executeStage.inst0.ex.eret       := inst_info(0).op === EXE_ERET
  io.executeStage.inst0.ex.badvaddr   := pc(0)
  io.executeStage.inst0.ex.bd         := io.instBuffer.info.inst0_is_in_delayslot
  val inst0_ex_cpu =
    !io.cp0.access_allowed && VecInit(EXE_MFC0, EXE_MTC0, EXE_TLBR, EXE_TLBWI, EXE_TLBWR, EXE_TLBP, EXE_ERET, EXE_WAIT)
      .contains(inst_info(0).op)
  io.executeStage.inst0.ex.excode := MuxCase(
    EX_NO,
    Seq(
      interrupt                                                 -> EX_INT,
      (tlb_refill(0) || tlb_invalid(0))                         -> EX_TLBL,
      (pc(0)(1, 0).orR() || (pc(0)(31) && !io.cp0.kernel_mode)) -> EX_ADEL,
      (inst_info(0).inst_valid === INST_INVALID)                -> EX_RI,
      (inst_info(0).op === EXE_SYSCALL)                         -> EX_SYS,
      (inst_info(0).op === EXE_BREAK)                           -> EX_BP,
      (inst0_ex_cpu)                                            -> EX_CPU,
    ),
  )
  io.executeStage.inst0.jb_info.jump_regiser  := jumpCtrl.out.jump_register
  io.executeStage.inst0.jb_info.branch_inst   := io.bpu.branch_inst
  io.executeStage.inst0.jb_info.pred_branch   := io.bpu.pred_branch
  io.executeStage.inst0.jb_info.branch_target := io.bpu.branch_target

  io.executeStage.inst1.allow_to_go := issue.inst1.allow_to_go
  io.executeStage.inst1.pc          := pc(1)
  io.executeStage.inst1.inst_info   := inst_info(1)
  io.executeStage.inst1.src_info.src1_data := Mux(
    inst_info(1).reg1_ren,
    forwardCtrl.out.inst(1).src1.rdata,
    decoder(1).io.out.imm32,
  )
  io.executeStage.inst1.src_info.src2_data := Mux(
    inst_info(1).reg2_ren,
    forwardCtrl.out.inst(1).src2.rdata,
    decoder(1).io.out.imm32,
  )
  io.executeStage.inst1.ex.flush_req := io.executeStage.inst1.ex.excode =/= EX_NO || io.executeStage.inst1.ex.tlb_refill
  io.executeStage.inst1.ex.tlb_refill := tlb_refill(1)
  io.executeStage.inst1.ex.eret       := inst_info(1).op === EXE_ERET
  io.executeStage.inst1.ex.badvaddr   := pc(1)
  io.executeStage.inst1.ex.bd         := issue.inst1.is_in_delayslot
  val inst1_ex_cpu =
    !io.cp0.access_allowed && VecInit(EXE_MFC0, EXE_MTC0, EXE_TLBR, EXE_TLBWI, EXE_TLBWR, EXE_TLBP, EXE_ERET, EXE_WAIT)
      .contains(inst_info(1).op)
  io.executeStage.inst1.ex.excode := MuxCase(
    EX_NO,
    Seq(
      (tlb_refill(1) || tlb_invalid(1))                         -> EX_TLBL,
      (pc(1)(1, 0).orR() || (pc(1)(31) && !io.cp0.kernel_mode)) -> EX_ADEL,
      (inst_info(1).inst_valid === INST_INVALID)                -> EX_RI,
      (inst_info(1).op === EXE_SYSCALL)                         -> EX_SYS,
      (inst_info(1).op === EXE_BREAK)                           -> EX_BP,
      (inst1_ex_cpu)                                            -> EX_CPU,
    ),
  )
}
