package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.{CpuConfig, BranchPredictorConfig}
import cpu.pipeline.execute.DecoderUnitExecuteUnit
import cpu.pipeline.fetch.BufferUnit

class InstFifoDecoderUnit(implicit val config: CpuConfig) extends Bundle {
  val allow_to_go = Output(Vec(config.decoderNum, Bool()))
  val inst        = Input(Vec(config.decoderNum, new BufferUnit()))
  val info = Input(new Bundle {
    val inst0_is_in_delayslot = Bool()
    val empty                 = Bool()
    val almost_empty          = Bool()
  })

  val jump_branch_inst = Output(Bool())
}

class DataForwardToDecoderUnit extends Bundle {
  val exe      = new RegWrite()
  val mem_wreg = Bool()
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
    val instFifo = new InstFifoDecoderUnit()
    val regfile  = Vec(config.decoderNum, new Src12Read())
    val forward  = Input(Vec(config.fuNum, new DataForwardToDecoderUnit()))
    val cp0      = Input(new Cp0DecoderUnit())
    // 输出
    val fetchUnit = new Bundle {
      val branch = Output(Bool())
      val target = Output(UInt(PC_WID.W))
    }
    val bpu = new Bundle {
      val bpuConfig      = new BranchPredictorConfig()
      val pc             = Output(UInt(PC_WID.W))
      val decoded_inst0  = Output(new InstInfo())
      val id_allow_to_go = Output(Bool())
      val pht_index      = Output(UInt(bpuConfig.phtDepth.W))

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
  issue.instFifo    := io.instFifo.info

  jumpCtrl.in.allow_to_go   := io.ctrl.allow_to_go
  jumpCtrl.in.decoded_inst0 := decoder(0).io.out
  jumpCtrl.in.forward       := io.forward
  jumpCtrl.in.pc            := io.instFifo.inst(0).pc
  jumpCtrl.in.reg1_data     := io.regfile(0).src1.rdata

  val jump_branch_inst0 = jumpCtrl.out.jump_inst || io.bpu.branch_inst
  val inst0_branch      = jumpCtrl.out.jump || io.bpu.pred_branch

  io.fetchUnit.branch := inst0_branch
  io.fetchUnit.target := Mux(io.bpu.pred_branch, io.bpu.branch_target, jumpCtrl.out.jump_target)

  io.instFifo.allow_to_go(0)   := io.ctrl.allow_to_go
  io.instFifo.allow_to_go(1)   := issue.inst1.allow_to_go
  io.instFifo.jump_branch_inst := jump_branch_inst0

  io.bpu.id_allow_to_go := io.ctrl.allow_to_go
  io.bpu.pc             := io.instFifo.inst(0).pc
  io.bpu.decoded_inst0  := decoder(0).io.out
  io.bpu.pht_index      := io.instFifo.inst(0).pht_index

  io.ctrl.inst0.src1.ren   := decoder(0).io.out.reg1_ren
  io.ctrl.inst0.src1.raddr := decoder(0).io.out.reg1_raddr
  io.ctrl.inst0.src2.ren   := decoder(0).io.out.reg2_ren
  io.ctrl.inst0.src2.raddr := decoder(0).io.out.reg2_raddr
  io.ctrl.branch           := inst0_branch

  val pc          = io.instFifo.inst.map(_.pc)
  val inst        = io.instFifo.inst.map(_.inst)
  val inst_info   = decoder.map(_.io.out)
  val tlb_refill  = io.instFifo.inst.map(_.tlb.refill)
  val tlb_invalid = io.instFifo.inst.map(_.tlb.invalid)
  val interrupt   = io.cp0.intterupt_allowed && (io.cp0.cause_ip & io.cp0.status_im).orR() && !io.instFifo.info.empty

  for (i <- 0 until (config.decoderNum)) {
    decoder(i).io.in.inst      := inst(i)
    issue.decodeInst(i)        := inst_info(i)
    issue.execute(i).mem_wreg  := io.forward(i).mem_wreg
    issue.execute(i).reg_waddr := io.forward(i).exe.waddr
  }

  io.executeStage.inst0.pc        := pc(0)
  io.executeStage.inst0.inst_info := inst_info(0)
  io.executeStage.inst0.inst_info.reg_wen := MuxLookup(
    inst_info(0).op,
    inst_info(0).reg_wen,
    Seq(
      EXE_MOVN -> (io.executeStage.inst0.src_info.src2_data =/= 0.U),
      EXE_MOVZ -> (io.executeStage.inst0.src_info.src2_data === 0.U),
    ),
  )
  io.executeStage.inst0.inst_info.mem_addr :=
    io.executeStage.inst0.src_info.src1_data + Util.signedExtend(io.executeStage.inst0.inst_info.inst(15, 0))
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
  io.executeStage.inst0.ex.bd         := io.instFifo.info.inst0_is_in_delayslot
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
  io.executeStage.inst1.inst_info.reg_wen := MuxLookup(
    inst_info(1).op,
    inst_info(1).reg_wen,
    Seq(
      EXE_MOVN -> (io.executeStage.inst1.src_info.src2_data =/= 0.U),
      EXE_MOVZ -> (io.executeStage.inst1.src_info.src2_data === 0.U),
    ),
  )
  io.executeStage.inst1.inst_info.mem_addr :=
    io.executeStage.inst1.src_info.src1_data + Util.signedExtend(io.executeStage.inst1.inst_info.inst(15, 0))
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
