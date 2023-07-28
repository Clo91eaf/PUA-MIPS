package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.decoder.RegWrite
import cpu.CpuConfig

class WriteBackUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl           = new WriteBackCtrl()
    val writeBackStage = Input(new MemoryUnitWriteBackUnit())
    val regfile        = Output(Vec(config.commitNum, new RegWrite()))
    val debug          = new DEBUG()
  })

  io.regfile(0).wen :=
    io.writeBackStage.inst0.inst_info.reg_wen && io.ctrl.allow_to_go && !io.writeBackStage.inst0.ex.flush_req
  io.regfile(0).waddr := io.writeBackStage.inst0.inst_info.reg_waddr
  io.regfile(0).wdata := io.writeBackStage.inst0.rd_info.wdata

  io.regfile(1).wen :=
    io.writeBackStage.inst1.inst_info.reg_wen && io.ctrl.allow_to_go && !io.writeBackStage.inst0.ex.flush_req && !io.writeBackStage.inst1.ex.flush_req
  io.regfile(1).waddr := io.writeBackStage.inst1.inst_info.reg_waddr
  io.regfile(1).wdata := io.writeBackStage.inst1.rd_info.wdata

  io.debug.wb_pc := Mux(
    clock.asBool,
    io.writeBackStage.inst0.pc,
    Mux(io.writeBackStage.inst0.ex.flush_req, 0.U, io.writeBackStage.inst1.pc),
  )
  io.debug.wb_rf_wen := Mux(
    reset.asBool,
    0.U,
    Mux(
      clock.asBool,
      Fill(4, io.regfile(0).wen),
      Fill(4, io.regfile(1).wen),
    ),
  )
  io.debug.wb_rf_wnum := Mux(
    clock.asBool,
    io.regfile(0).waddr,
    io.regfile(1).waddr,
  )
  io.debug.wb_rf_wdata := Mux(
    clock.asBool,
    io.regfile(0).wdata,
    io.regfile(1).wdata,
  )
  io.debug.cp0_cause  := io.writeBackStage.inst0.cp0.cp0_cause
  io.debug.cp0_count  := io.writeBackStage.inst0.cp0.cp0_count
  io.debug.cp0_random := io.writeBackStage.inst0.cp0.cp0_random
  io.debug.int        := io.writeBackStage.inst0.ex.excode === EX_INT
  io.debug.commit     := io.ctrl.allow_to_go
}
