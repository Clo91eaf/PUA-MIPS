package cpu

import chisel3._
import chisel3.util._
import chisel3.internal.DontCareBinding

import defines._
import defines.Const._
import pipeline.fetch._
import pipeline.decoder._
import pipeline.execute._
import pipeline.memory._
import pipeline.writeback._
import ctrl._
import cpu.pipeline.fetch.FetchStage

class Core(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ext_int           = Input(UInt(6.W))
    val i_stall           = Input(Bool())
    val stallF            = Output(Bool())
    val inst_sram_en      = Output(Bool())
    val F_pc              = Output(UInt(32.W))
    val F_pc_next         = Output(UInt(32.W))
    val inst_data_ok1     = Input(Bool())
    val inst_data_ok2     = Input(Bool())
    val inst_rdata1       = Input(UInt(32.W))
    val inst_rdata2       = Input(UInt(32.W))
    val inst_tlb_refill   = Input(Bool())
    val inst_tlb_invalid  = Input(Bool())
    val fence_iE          = Output(Bool())
    val fence_addrE       = Output(UInt(32.W))
    val fence_dM          = Output(Bool())
    val fence_addrM       = Output(UInt(32.W))
    val fence_tlbE        = Output(Bool())
    val itlb_vpn2         = Input(UInt(19.W))
    val itlb_found        = Output(Bool())
    val itlb_entry        = Output(new TlbEntry())
    val d_stall           = Input(Bool())
    val stallM            = Output(Bool())
    val mem_read_enE      = Output(Bool())
    val mem_write_enE     = Output(Bool())
    val E_mem_va          = Output(UInt(32.W))
    val mem_addrE         = Output(UInt(32.W)) // TODO: delete
    val data_sram_rdataM  = Input(UInt(32.W))
    val data_sram_enM     = Output(Bool())
    val data_sram_rlenM   = Output(UInt(2.W))
    val data_sram_wenM    = Output(UInt(4.W))
    val M_mem_va          = Output(UInt(32.W))
    val data_sram_addrM   = Output(UInt(32.W)) // TODO: delete
    val data_sram_wdataM  = Output(UInt(32.W))
    val dtlb_vpn2         = Input(UInt(19.W))
    val dtlb_found        = Output(Bool())
    val dtlb_entry        = Output(new TlbEntry())
    val fence_tlbM        = Output(Bool())
    val data_tlb_refill   = Input(Bool())
    val data_tlb_invalid  = Input(Bool())
    val data_tlb_mod      = Input(Bool())
    val debug_wb_pc       = Output(UInt(32.W))
    val debug_wb_rf_wen   = Output(UInt(4.W))
    val debug_wb_rf_wnum  = Output(UInt(5.W))
    val debug_wb_rf_wdata = Output(UInt(32.W))
    val debug_cp0_count   = Output(UInt(32.W))
    val debug_cp0_random  = Output(UInt(32.W))
    val debug_cp0_cause   = Output(UInt(32.W))
    val debug_int         = Output(Bool())
    val debug_commit      = Output(Bool())
  })

  val ctrl           = Module(new Ctrl()).io
  val fetchUnit      = Module(new FetchStage()).io
  val bpu            = Module(new BPU()).io
  val instBuffer     = Module(new InstBuffer()).io
  val decoderUnit    = Module(new DecoderUnit()).io
  val regfile        = Module(new ARegFile()).io
  val executeStage   = Module(new ExecuteStage()).io
  val executeUnit    = Module(new ExecuteUnit()).io
  val cp0            = Module(new Cp0()).io
  val memoryStage    = Module(new MemoryStage()).io
  val memoryUnit     = Module(new MemoryUnit()).io
  val writeBackStage = Module(new WriteBackStage()).io
  val writeBackUnit  = Module(new WriteBackUnit()).io

  ctrl.decoderUnit <> decoderUnit.ctrl
  ctrl.executeUnit <> executeUnit.ctrl
  ctrl.memoryUnit <> memoryUnit.ctrl
  ctrl.writeBackUnit <> writeBackUnit.ctrl
  ctrl.cacheCtrl.iCache_stall := io.i_stall
  ctrl.cacheCtrl.dCache_stall := io.d_stall

  fetchUnit.memory.ex       := memoryUnit.fetchUnit.mtc0.flush
  fetchUnit.memory.ex_pc    := memoryUnit.fetchUnit.mtc0.flush_pc
  fetchUnit.memory.flush    := memoryUnit.fetchUnit.flush
  fetchUnit.memory.flush_pc := memoryUnit.fetchUnit.flush_pc
  fetchUnit.execute <> executeUnit.fetchStage
  fetchUnit.decoder <> decoderUnit.fetchUnit
  fetchUnit.instBuffer.full      := instBuffer.full
  fetchUnit.iCache.inst_valid(0) := io.inst_data_ok1
  fetchUnit.iCache.inst_valid(1) := io.inst_data_ok2
  io.F_pc                        := fetchUnit.iCache.pc
  io.F_pc_next                   := fetchUnit.iCache.pc_next

  bpu.enaD                         := ctrl.decoderUnit.allow_to_go
  bpu.instrD                       := decoderUnit.bpu.decoded_inst0.inst
  bpu.pcD                          := decoderUnit.bpu.pc
  bpu.pc_plus4D                    := decoderUnit.bpu.pc + 4.U
  bpu.pcE                          := executeUnit.bpu.pc
  bpu.branchE                      := executeUnit.bpu.inst0_is_branch
  bpu.actual_takeE                 := executeUnit.bpu.branch_flag
  decoderUnit.bpu.inst_is_branch   := bpu.branchD
  decoderUnit.bpu.pred_branch_flag := bpu.pred_takeD
  decoderUnit.bpu.branch_target    := bpu.branch_targetD

  instBuffer.fifo_rst         := reset.asBool || ctrl.decoderUnit.do_flush
  instBuffer.flush_delay_slot := ctrl.instBuffer.delay_slot_do_flush
  instBuffer.D_ena            := ctrl.decoderUnit.allow_to_go
  instBuffer.i_stall          := io.i_stall
  instBuffer.master_is_branch := decoderUnit.instBuffer.inst0_is_jb
  instBuffer.delay_sel_rst := Mux(
    ctrl.executeUnit.branch_flag,
    !(executeUnit.executeStage.inst1.ex.bd || executeUnit.executeStage.inst0.ex.bd),
    Mux(
      ctrl.decoderUnit.branch_flag,
      !decoderUnit.instBuffer.inst(1).ready,
      0.U,
    ),
  )
  instBuffer.D_delay_rst := ctrl.decoderUnit.branch_flag
  instBuffer.E_delay_rst := ctrl.executeUnit.branch_flag
  for (i <- 0 until config.decoderNum) {
    instBuffer.read_en(i)                           := decoderUnit.instBuffer.inst(i).ready
    decoderUnit.instBuffer.inst(i).valid            := true.B
    decoderUnit.instBuffer.inst(i).bits.tlb_refill  := instBuffer.read(i).tlb.refill
    decoderUnit.instBuffer.inst(i).bits.tlb_invalid := instBuffer.read(i).tlb.invalid
    decoderUnit.instBuffer.inst(i).bits.pc          := instBuffer.read(i).addr
    decoderUnit.instBuffer.inst(i).bits.inst        := instBuffer.read(i).data
  }
  instBuffer.write_en(0)          := io.inst_data_ok1
  instBuffer.write_en(1)          := io.inst_data_ok2
  instBuffer.write(0).tlb.refill  := io.inst_tlb_refill
  instBuffer.write(1).tlb.refill  := io.inst_tlb_refill
  instBuffer.write(0).tlb.invalid := io.inst_tlb_invalid
  instBuffer.write(1).tlb.invalid := io.inst_tlb_invalid
  instBuffer.write(0).addr        := io.F_pc
  instBuffer.write(1).addr        := io.F_pc + 4.U
  instBuffer.write(0).data        := io.inst_rdata1
  instBuffer.write(1).data        := io.inst_rdata2

  decoderUnit.instBuffer.info.empty                 := instBuffer.empty
  decoderUnit.instBuffer.info.almost_empty          := instBuffer.almost_empty
  decoderUnit.instBuffer.info.inst0_is_in_delayslot := instBuffer.master_is_in_delayslot_o
  decoderUnit.regfile <> regfile.read
  for (i <- 0 until (config.fuNum)) {
    decoderUnit.forward(i).exe         := executeUnit.decoderUnit(i).exe
    decoderUnit.forward(i).exe_mem_ren := executeUnit.decoderUnit(i).exe_mem_ren
    decoderUnit.forward(i).mem         := memoryUnit.decoderUnit(i)
  }
  decoderUnit.cp0 <> cp0.decoderUnit
  decoderUnit.executeStage <> executeStage.decoderUnit

  executeStage.ctrl.clear(0) := ctrl.memoryUnit.flush_req ||
    !decoderUnit.executeStage.inst0.ex.bd && ctrl.executeUnit.do_flush && ctrl.executeUnit.allow_to_go ||
    !ctrl.decoderUnit.allow_to_go && ctrl.executeUnit.allow_to_go
  executeStage.ctrl.clear(1) := ctrl.memoryUnit.flush_req ||
    (ctrl.executeUnit.do_flush && decoderUnit.executeStage.inst1.allow_to_go) ||
    (ctrl.executeUnit.allow_to_go && !decoderUnit.executeStage.inst1.allow_to_go)
  executeStage.ctrl.inst0_allow_to_go := ctrl.executeUnit.allow_to_go

  executeUnit.executeStage <> executeStage.executeUnit
  executeUnit.cp0 <> cp0.executeUnit
  executeUnit.memoryStage <> memoryStage.executeUnit
  executeUnit.memoryUnit <> memoryUnit.executeUnit

  cp0.ctrl.exe_stall := !ctrl.executeUnit.allow_to_go
  cp0.ctrl.mem_stall := !ctrl.memoryUnit.allow_to_go
  cp0.tlb(0).vpn2    := io.itlb_vpn2
  cp0.tlb(1).vpn2    := io.dtlb_vpn2
  cp0.ext_int        := io.ext_int
  io.itlb_found      := cp0.tlb(0).found
  io.dtlb_found      := cp0.tlb(1).found
  io.itlb_entry      := cp0.tlb(0).info
  io.dtlb_entry      := cp0.tlb(1).info

  memoryStage.ctrl.allow_to_go := ctrl.memoryUnit.allow_to_go
  memoryStage.ctrl.clear(0)    := ctrl.memoryUnit.do_flush
  memoryStage.ctrl.clear(1)    := ctrl.memoryUnit.do_flush

  memoryUnit.memoryStage <> memoryStage.memoryUnit
  memoryUnit.cp0 <> cp0.memoryUnit
  memoryUnit.writeBackStage <> writeBackStage.memoryUnit
  memoryUnit.dataMemory.in.tlb_invalid := io.data_tlb_invalid
  memoryUnit.dataMemory.in.tlb_refill  := io.data_tlb_refill
  memoryUnit.dataMemory.in.tlb_modify  := io.data_tlb_mod
  memoryUnit.dataMemory.in.rdata       := io.data_sram_rdataM
  io.data_sram_enM                     := memoryUnit.dataMemory.out.en
  io.data_sram_rlenM                   := memoryUnit.dataMemory.out.rlen
  io.data_sram_wenM                    := memoryUnit.dataMemory.out.wen
  io.data_sram_wdataM                  := memoryUnit.dataMemory.out.wdata
  io.data_sram_addrM                   := memoryUnit.dataMemory.out.addr
  io.data_sram_wdataM                  := memoryUnit.dataMemory.out.wdata

  writeBackStage.memoryUnit <> memoryUnit.writeBackStage
  writeBackStage.ctrl.allow_to_go := ctrl.writeBackUnit.allow_to_go
  writeBackStage.ctrl.clear(0)    := ctrl.writeBackUnit.do_flush
  writeBackStage.ctrl.clear(1)    := ctrl.writeBackUnit.do_flush

  writeBackUnit.writeBackStage <> writeBackStage.writeBackUnit
  writeBackUnit.ctrl <> ctrl.writeBackUnit
  regfile.write <> writeBackUnit.regfile

  io.debug_commit      := writeBackUnit.debug.debug_commit
  io.debug_int         := writeBackUnit.debug.debug_int
  io.debug_wb_pc       := writeBackUnit.debug.debug_wb_pc
  io.debug_wb_rf_wen   := writeBackUnit.debug.debug_wb_rf_wen
  io.debug_wb_rf_wnum  := writeBackUnit.debug.debug_wb_rf_wnum
  io.debug_wb_rf_wdata := writeBackUnit.debug.debug_wb_rf_wdata
  io.debug_cp0_count   := writeBackUnit.debug.debug_cp0_count
  io.debug_cp0_random  := writeBackUnit.debug.debug_cp0_random
  io.debug_cp0_cause   := writeBackUnit.debug.debug_cp0_cause

  io.fence_iE      := executeUnit.memoryStage.inst0.inst_info.inst(16) === 0.U
  io.fence_addrE   := executeUnit.memoryStage.inst0.rd_info.wdata
  io.fence_dM      := memoryUnit.writeBackStage.inst0.inst_info.inst(16) === 1.U
  io.fence_addrM   := memoryUnit.writeBackStage.inst0.rd_info.wdata
  io.fence_tlbE    := VecInit(EXE_MTC0, EXE_TLBWI, EXE_TLBWR).contains(executeUnit.executeStage.inst0.inst_info.op)
  io.fence_tlbM    := VecInit(EXE_MTC0, EXE_TLBWI, EXE_TLBWR).contains(memoryUnit.memoryStage.inst0.inst_info.op)
  io.mem_addrE     := executeUnit.memoryStage.inst0.mem.addr
  io.E_mem_va      := executeUnit.memoryStage.inst0.mem.addr
  io.M_mem_va      := memoryUnit.memoryStage.inst0.mem.addr
  io.inst_sram_en  := !(reset.asBool || instBuffer.full)
  io.stallF        := !ctrl.fetchUnit.allow_to_go
  io.stallM        := !ctrl.memoryUnit.allow_to_go
  io.mem_read_enE  := executeUnit.memoryStage.inst0.mem.ren
  io.mem_write_enE := executeUnit.memoryStage.inst0.mem.wen

}
