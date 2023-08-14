package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.CpuConfig
import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.decoder.RegWrite
import cpu.pipeline.memory.{ExecuteUnitMemoryUnit, Cp0Info}
import cpu.pipeline.fetch.ExecuteUnitBranchPredictor

class ExecuteUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl         = new ExecuteCtrl()
    val executeStage = Input(new DecoderUnitExecuteUnit())
    val cp0          = Flipped(new Cp0ExecuteUnit())
    val bpu          = new ExecuteUnitBranchPredictor()
    val fetchUnit = Output(new Bundle {
      val branch = Bool()
      val target = UInt(PC_WID.W)
    })
    val decoderUnit = new Bundle {
      val forward = Output(
        Vec(
          config.fuNum,
          new Bundle {
            val exe          = new RegWrite()
            val exe_mem_wreg = Bool()
          },
        ),
      )
      val inst0_bd = Input(Bool())
    }
    val memoryStage = Output(new ExecuteUnitMemoryUnit())

    val statistic = if (!config.build) Some(new BranchPredictorUnitStatistic()) else None
  })

  val fu            = Module(new Fu()).io
  val accessMemCtrl = Module(new ExeAccessMemCtrl()).io

  io.ctrl.inst(0).mem_wreg  := io.executeStage.inst0.inst_info.mem_wreg
  io.ctrl.inst(0).reg_waddr := io.executeStage.inst0.inst_info.reg_waddr
  io.ctrl.inst(1).mem_wreg  := io.executeStage.inst1.inst_info.mem_wreg
  io.ctrl.inst(1).reg_waddr := io.executeStage.inst1.inst_info.reg_waddr
  io.ctrl.branch := io.ctrl.allow_to_go &&
    (io.executeStage.inst0.jb_info.jump_regiser || fu.branch.pred_fail)

  io.cp0.in.mtc0_wdata := io.executeStage.inst0.src_info.src2_data
  io.cp0.in.inst_info(0) := Mux(
    !io.executeStage.inst0.ex.flush_req,
    io.executeStage.inst0.inst_info,
    0.U.asTypeOf(new InstInfo()),
  )
  io.cp0.in.inst_info(1) := io.executeStage.inst1.inst_info

  // input accessMemCtrl
  accessMemCtrl.inst(0).inst_info := io.executeStage.inst0.inst_info
  accessMemCtrl.inst(0).src_info  := io.executeStage.inst0.src_info
  accessMemCtrl.inst(0).ex.in     := io.executeStage.inst0.ex
  accessMemCtrl.inst(1).inst_info := io.executeStage.inst1.inst_info
  accessMemCtrl.inst(1).src_info  := io.executeStage.inst1.src_info
  accessMemCtrl.inst(1).ex.in     := io.executeStage.inst1.ex

  // input fu
  fu.ctrl <> io.ctrl.fu
  fu.inst(0).pc        := io.executeStage.inst0.pc
  fu.inst(0).hilo_wen  := io.executeStage.inst0.inst_info.whilo
  fu.inst(0).mul_en    := io.executeStage.inst0.inst_info.mul
  fu.inst(0).div_en    := io.executeStage.inst0.inst_info.div
  fu.inst(0).inst_info := io.executeStage.inst0.inst_info
  fu.inst(0).src_info  := io.executeStage.inst0.src_info
  fu.inst(0).ex.in :=
    Mux(io.executeStage.inst0.inst_info.fusel === FU_MEM, accessMemCtrl.inst(0).ex.out, io.executeStage.inst0.ex)
  fu.inst(1).pc         := io.executeStage.inst1.pc
  fu.inst(1).hilo_wen   := io.executeStage.inst1.inst_info.whilo
  fu.inst(1).mul_en     := io.executeStage.inst1.inst_info.mul
  fu.inst(1).div_en     := io.executeStage.inst1.inst_info.div
  fu.inst(1).inst_info  := io.executeStage.inst1.inst_info
  fu.inst(1).src_info   := io.executeStage.inst1.src_info
  fu.inst(1).ex.in      := io.executeStage.inst1.ex
  fu.cp0_rdata          := io.cp0.out.cp0_rdata
  fu.branch.pred_branch := io.executeStage.inst0.jb_info.pred_branch

  io.bpu.pc               := io.executeStage.inst0.pc
  io.bpu.update_pht_index := io.executeStage.inst0.jb_info.update_pht_index
  io.bpu.branch           := fu.branch.branch
  io.bpu.branch_inst      := io.executeStage.inst0.jb_info.branch_inst

  io.fetchUnit.branch := io.ctrl.allow_to_go &&
    (io.executeStage.inst0.jb_info.jump_regiser || fu.branch.pred_fail)
  io.fetchUnit.target := MuxCase(
    io.executeStage.inst0.pc + 4.U, // 默认顺序运行吧
    Seq(
      (fu.branch.pred_fail && fu.branch.branch) -> io.executeStage.inst0.jb_info.branch_target,
      (fu.branch.pred_fail && !fu.branch.branch) -> Mux(
        io.decoderUnit.inst0_bd || io.executeStage.inst1.ex.bd,
        io.executeStage.inst0.pc + 8.U,
        io.executeStage.inst0.pc + 4.U,
      ),
      (io.executeStage.inst0.jb_info.jump_regiser) -> io.executeStage.inst0.src_info.src1_data,
    ),
  )

  io.ctrl.fu_stall := fu.stall_req

  io.memoryStage.inst0.mem.en        := accessMemCtrl.mem.out.en
  io.memoryStage.inst0.mem.ren       := accessMemCtrl.mem.out.ren
  io.memoryStage.inst0.mem.wen       := accessMemCtrl.mem.out.wen
  io.memoryStage.inst0.mem.addr      := accessMemCtrl.mem.out.addr
  io.memoryStage.inst0.mem.wdata     := accessMemCtrl.mem.out.wdata
  io.memoryStage.inst0.mem.sel       := accessMemCtrl.inst.map(_.mem_sel)
  io.memoryStage.inst0.mem.inst_info := accessMemCtrl.mem.out.inst_info
  io.memoryStage.inst0.mem.llbit     := fu.llbit

  io.memoryStage.inst0.pc            := io.executeStage.inst0.pc
  io.memoryStage.inst0.inst_info     := io.executeStage.inst0.inst_info
  io.memoryStage.inst0.rd_info.wdata := fu.inst(0).result
  io.memoryStage.inst0.ex := Mux(
    io.executeStage.inst0.inst_info.fusel === FU_MEM,
    accessMemCtrl.inst(0).ex.out,
    fu.inst(0).ex.out,
  )
  io.memoryStage.inst0.cp0 := io.cp0.out.debug

  io.memoryStage.inst1.pc            := io.executeStage.inst1.pc
  io.memoryStage.inst1.inst_info     := io.executeStage.inst1.inst_info
  io.memoryStage.inst1.rd_info.wdata := fu.inst(1).result
  io.memoryStage.inst1.ex := Mux(
    io.executeStage.inst1.inst_info.fusel === FU_MEM,
    accessMemCtrl.inst(1).ex.out,
    fu.inst(1).ex.out,
  )

  io.decoderUnit.forward(0).exe.wen      := io.memoryStage.inst0.inst_info.reg_wen
  io.decoderUnit.forward(0).exe.waddr    := io.memoryStage.inst0.inst_info.reg_waddr
  io.decoderUnit.forward(0).exe.wdata    := io.memoryStage.inst0.rd_info.wdata
  io.decoderUnit.forward(0).exe_mem_wreg := io.memoryStage.inst0.inst_info.mem_wreg

  io.decoderUnit.forward(1).exe.wen      := io.memoryStage.inst1.inst_info.reg_wen
  io.decoderUnit.forward(1).exe.waddr    := io.memoryStage.inst1.inst_info.reg_waddr
  io.decoderUnit.forward(1).exe.wdata    := io.memoryStage.inst1.rd_info.wdata
  io.decoderUnit.forward(1).exe_mem_wreg := io.memoryStage.inst1.inst_info.mem_wreg

  // ===----------------------------------------------------------------===
  // statistic
  // ===----------------------------------------------------------------===
  if (!config.build) {
    io.statistic.get <> fu.statistic.get
  }
}
