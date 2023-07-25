package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.CpuConfig
import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.decoder.RegWrite
import cpu.pipeline.memory.{ExecuteUnitMemoryUnit, Cp0Info}

class ExecuteUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl         = new ExecuteCtrl()
    val executeStage = Input(new DecoderUnitExecuteUnit())
    val cp0 = new Bundle {
      val in = Input(new Bundle {
        val rdata = UInt(DATA_WID.W)
        val debug = new Cp0Info()
      })
      val out = Output(new Bundle {
        val inst_info  = new InstInfo()
        val mtc0_wdata = UInt(DATA_WID.W)
      })
    }
    val bpu = Output(new Bundle {
      val pc              = UInt(PC_WID.W)
      val branch_flag     = Bool()
      val inst0_is_branch = Bool()
    })
    val fetchStage = Output(new Bundle {
      val branch_flag   = Bool()
      val branch_target = UInt(PC_WID.W)
    })
    val decoderUnit = Output(
      Vec(
        config.fuNum,
        new Bundle {
          val exe         = new RegWrite()
          val exe_mem_ren = Bool()
        },
      ),
    )
    val memoryStage = Output(new ExecuteUnitMemoryUnit())
    val memoryUnit = Input(new Bundle {
      val mem = new Bundle {
        val sel   = Vec(config.fuNum, Bool())
        val rdata = UInt(DATA_WID.W)
      }
    })
  })

  val fu            = Module(new Fu()).io
  val accessMemCtrl = Module(new ExeAccessMemCtrl()).io

  io.ctrl.inst(0).mem_ren := io.executeStage.inst0.inst_info.fusel === FU_MEM &&
    io.executeStage.inst0.inst_info.reg_wen
  io.ctrl.inst(0).reg_waddr := io.executeStage.inst0.inst_info.reg_waddr
  io.ctrl.inst(1).mem_ren := io.executeStage.inst1.inst_info.fusel === FU_MEM &&
    io.executeStage.inst1.inst_info.reg_wen
  io.ctrl.inst(1).reg_waddr := io.executeStage.inst1.inst_info.reg_waddr
  io.ctrl.branch_flag := io.ctrl.allow_to_go &&
    (io.executeStage.inst0.jb_info.jump_regiser_conflict || fu.branch.pred_fail)

  io.cp0.out.mtc0_wdata := io.executeStage.inst0.src_info.src2_data
  io.cp0.out.inst_info := Mux(
    !io.executeStage.inst0.ex.flush_req,
    io.executeStage.inst0.inst_info,
    0.U.asTypeOf(new InstInfo()),
  )

  // input accessMemCtrl
  accessMemCtrl.mem.in.sel        := io.memoryUnit.mem.sel
  accessMemCtrl.mem.in.rdata      := io.memoryUnit.mem.rdata
  accessMemCtrl.inst(0).inst_info := io.executeStage.inst0.inst_info
  accessMemCtrl.inst(0).src_info  := io.executeStage.inst0.src_info
  accessMemCtrl.inst(0).ex.in     := io.executeStage.inst0.ex
  accessMemCtrl.inst(1).inst_info := io.executeStage.inst1.inst_info
  accessMemCtrl.inst(1).src_info  := io.executeStage.inst1.src_info
  accessMemCtrl.inst(1).ex.in     := io.executeStage.inst1.ex

  // input fu
  fu.ctrl.allow_to_go        := io.ctrl.allow_to_go
  fu.ctrl.do_flush           := io.ctrl.do_flush
  fu.inst(0).pc              := io.executeStage.inst0.pc
  fu.inst(0).hilo_wen        := VecInit(FU_MUL, FU_DIV, FU_MTHILO).contains(io.executeStage.inst0.inst_info.fusel)
  fu.inst(0).mul_en          := io.executeStage.inst0.inst_info.fusel === FU_MUL
  fu.inst(0).div_en          := io.executeStage.inst0.inst_info.fusel === FU_DIV
  fu.inst(0).inst_info       := io.executeStage.inst0.inst_info
  fu.inst(0).src_info        := io.executeStage.inst0.src_info
  fu.inst(0).ex.in           := io.executeStage.inst0.ex
  fu.inst(1).pc              := io.executeStage.inst1.pc
  fu.inst(1).hilo_wen        := VecInit(FU_MUL, FU_DIV, FU_MTHILO).contains(io.executeStage.inst1.inst_info.fusel)
  fu.inst(1).mul_en          := io.executeStage.inst1.inst_info.fusel === FU_MUL
  fu.inst(1).div_en          := io.executeStage.inst1.inst_info.fusel === FU_DIV
  fu.inst(1).inst_info       := io.executeStage.inst1.inst_info
  fu.inst(1).src_info        := io.executeStage.inst1.src_info
  fu.inst(1).ex.in           := io.executeStage.inst1.ex
  fu.cp0_rdata               := io.cp0.in.rdata
  fu.branch.pred_branch_flag := io.executeStage.inst0.jb_info.pred_branch_flag

  io.bpu.pc              := io.executeStage.inst0.pc
  io.bpu.branch_flag     := fu.branch.branch_flag
  io.bpu.inst0_is_branch := io.executeStage.inst0.inst_info.fusel === FU_BR

  io.fetchStage.branch_flag := io.ctrl.allow_to_go &&
    (io.executeStage.inst0.jb_info.jump_regiser_conflict || fu.branch.pred_fail)
  io.fetchStage.branch_target := MuxCase(
    io.executeStage.inst0.pc + 4.U, // 默认顺序运行吧
    Seq(
      (fu.branch.pred_fail && fu.branch.branch_flag) -> io.executeStage.inst0.jb_info.branch_target,
      (fu.branch.pred_fail && !fu.branch.branch_flag) -> Mux(
        io.executeStage.inst0.ex.bd || io.executeStage.inst1.ex.bd,
        io.executeStage.inst0.pc + 8.U,
        io.executeStage.inst0.pc + 4.U,
      ),
      (io.executeStage.inst0.jb_info.jump_regiser_conflict) -> io.executeStage.inst0.src_info.src1_data,
    ),
  )

  io.ctrl.fu_stall := fu.stall_req

  io.memoryStage.mem.en        := accessMemCtrl.mem.out.en
  io.memoryStage.mem.ren       := accessMemCtrl.mem.out.ren
  io.memoryStage.mem.wen       := accessMemCtrl.mem.out.wen
  io.memoryStage.mem.addr      := accessMemCtrl.mem.out.addr
  io.memoryStage.mem.wdata     := accessMemCtrl.mem.out.wdata
  io.memoryStage.mem.sel       := accessMemCtrl.inst.map(_.mem_sel)
  io.memoryStage.mem.inst_info := accessMemCtrl.mem.out.inst_info

  io.memoryStage.inst0.pc        := io.executeStage.inst0.pc
  io.memoryStage.inst0.inst_info := io.executeStage.inst0.inst_info
  io.memoryStage.inst0.inst_info.reg_wen := MuxLookup(
    io.executeStage.inst0.inst_info.op,
    io.executeStage.inst0.inst_info.reg_wen,
    Seq(
      EXE_MOVN -> (io.executeStage.inst0.src_info.src2_data =/= 0.U),
      EXE_MOVZ -> (io.executeStage.inst0.src_info.src2_data === 0.U),
    ),
  )
  io.memoryStage.inst0.rd_info.wdata := Mux(
    io.executeStage.inst0.inst_info.fusel === FU_MEM,
    accessMemCtrl.inst(0).mem_rdata,
    fu.inst(0).result,
  )
  io.memoryStage.inst0.ex := Mux(
    io.executeStage.inst0.inst_info.fusel === FU_MEM,
    accessMemCtrl.inst(0).ex.out,
    fu.inst(0).ex.out,
  )
  io.memoryStage.inst0.cp0_info := io.cp0.in.debug

  io.memoryStage.inst1.pc        := io.executeStage.inst1.pc
  io.memoryStage.inst1.inst_info := io.executeStage.inst1.inst_info
  io.memoryStage.inst1.inst_info.reg_wen := MuxLookup(
    io.executeStage.inst1.inst_info.op,
    io.executeStage.inst1.inst_info.reg_wen,
    Seq(
      EXE_MOVN -> (io.executeStage.inst1.src_info.src2_data =/= 0.U),
      EXE_MOVZ -> (io.executeStage.inst1.src_info.src2_data === 0.U),
    ),
  )
  io.memoryStage.inst1.rd_info.wdata := Mux(
    io.executeStage.inst1.inst_info.fusel === FU_MEM,
    accessMemCtrl.inst(1).mem_rdata,
    fu.inst(1).result,
  )
  io.memoryStage.inst1.ex := Mux(
    io.executeStage.inst1.inst_info.fusel === FU_MEM,
    accessMemCtrl.inst(1).ex.out,
    fu.inst(1).ex.out,
  )

  io.decoderUnit(0).exe.wen   := io.memoryStage.inst0.inst_info.reg_wen
  io.decoderUnit(0).exe.waddr := io.memoryStage.inst0.inst_info.reg_waddr
  io.decoderUnit(0).exe.wdata := io.memoryStage.inst0.rd_info.wdata
  io.decoderUnit(0).exe_mem_ren := io.memoryStage.inst0.inst_info.fusel === FU_MEM &&
    io.memoryStage.inst0.inst_info.reg_wen

  io.decoderUnit(1).exe.wen   := io.memoryStage.inst1.inst_info.reg_wen
  io.decoderUnit(1).exe.waddr := io.memoryStage.inst1.inst_info.reg_waddr
  io.decoderUnit(1).exe.wdata := io.memoryStage.inst1.rd_info.wdata
  io.decoderUnit(1).exe_mem_ren := io.memoryStage.inst1.inst_info.fusel === FU_MEM &&
    io.memoryStage.inst1.inst_info.reg_wen

}
