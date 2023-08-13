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
import mmu._
import chisel3.util.experimental.decode.decoder
import cpu.pipeline.fetch.InstFifo

class Core(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ext_int   = Input(UInt(6.W))
    val inst      = new Cache_ICache()
    val data      = new Cache_DCache()
    val debug     = new DEBUG()
    val statistic = if (!config.build) Some(new CPUStatistic()) else None
  })

  val ctrl           = Module(new Ctrl()).io
  val fetchUnit      = Module(new FetchUnit()).io
  val bpu            = Module(new BranchPredictorUnit()).io
  val instFifo       = Module(new InstFifo()).io
  val decoderUnit    = Module(new DecoderUnit()).io
  val regfile        = Module(new ARegFile()).io
  val executeStage   = Module(new ExecuteStage()).io
  val executeUnit    = Module(new ExecuteUnit()).io
  val cp0            = Module(new Cp0()).io
  val memoryStage    = Module(new MemoryStage()).io
  val memoryUnit     = Module(new MemoryUnit()).io
  val writeBackStage = Module(new WriteBackStage()).io
  val writeBackUnit  = Module(new WriteBackUnit()).io
  val tlbL1I         = Module(new TlbL1I()).io
  val tlbL1D         = Module(new TlbL1D()).io

  tlbL1I.addr         := fetchUnit.iCache.pc
  tlbL1I.fence        := executeUnit.executeStage.inst0.inst_info.tlbfence
  tlbL1I.cpu_stall    := !ctrl.fetchUnit.allow_to_go
  tlbL1I.icache_stall := io.inst.icache_stall
  tlbL1I.cache <> io.inst.tlb

  tlbL1D.addr         := memoryUnit.dataMemory.out.addr
  tlbL1D.fence        := memoryUnit.memoryStage.inst0.inst_info.tlbfence
  tlbL1D.cpu_stall    := !ctrl.memoryUnit.allow_to_go
  tlbL1D.dcache_stall := io.data.dcache_stall
  tlbL1D.mem_write    := memoryUnit.dataMemory.out.wen.orR
  tlbL1D.mem_en       := memoryUnit.dataMemory.out.en
  tlbL1D.cache <> io.data.tlb

  ctrl.instFifo.has2insts := !(instFifo.empty || instFifo.almost_empty)
  ctrl.decoderUnit <> decoderUnit.ctrl
  ctrl.executeUnit <> executeUnit.ctrl
  ctrl.memoryUnit <> memoryUnit.ctrl
  ctrl.writeBackUnit <> writeBackUnit.ctrl
  ctrl.cacheCtrl.iCache_stall := io.inst.icache_stall
  ctrl.cacheCtrl.dCache_stall := io.data.dcache_stall

  fetchUnit.memory <> memoryUnit.fetchUnit
  fetchUnit.execute <> executeUnit.fetchUnit
  fetchUnit.decoder <> decoderUnit.fetchUnit
  fetchUnit.instFifo.full     := instFifo.full
  fetchUnit.iCache.inst_valid := io.inst.inst_valid
  io.inst.addr(0)             := fetchUnit.iCache.pc
  io.inst.addr(1)             := fetchUnit.iCache.pc_next
  for (i <- 2 until config.instFetchNum) {
    io.inst.addr(i) := fetchUnit.iCache.pc_next + ((i - 1) * 4).U
  }

  bpu.decoder.ena                  := ctrl.decoderUnit.allow_to_go
  bpu.decoder.op                   := decoderUnit.bpu.decoded_inst0.op
  bpu.decoder.inst                 := decoderUnit.bpu.decoded_inst0.inst
  bpu.decoder.rs1                  := decoderUnit.bpu.decoded_inst0.reg1_raddr
  bpu.decoder.rs2                  := decoderUnit.bpu.decoded_inst0.reg2_raddr
  bpu.decoder.pc                   := decoderUnit.bpu.pc
  bpu.decoder.pc_plus4             := decoderUnit.bpu.pc + 4.U
  bpu.decoder.pht_index            := decoderUnit.bpu.pht_index
  decoderUnit.bpu.update_pht_index := bpu.decoder.update_pht_index
  bpu.execute <> executeUnit.bpu
  if (config.branchPredictor == "pesudo") {
    bpu.regfile.get <> regfile.bpu.get
  }
  decoderUnit.bpu.branch_inst   := bpu.decoder.branch_inst
  decoderUnit.bpu.pred_branch   := bpu.decoder.pred_branch
  decoderUnit.bpu.branch_target := bpu.decoder.branch_target

  instFifo.do_flush         := ctrl.decoderUnit.do_flush
  instFifo.flush_delay_slot := ctrl.instFifo.delay_slot_do_flush
  instFifo.icache_stall     := io.inst.icache_stall
  instFifo.jump_branch_inst := decoderUnit.instFifo.jump_branch_inst
  instFifo.delay_sel_flush := Mux(
    ctrl.executeUnit.branch,
    !(executeUnit.memoryStage.inst1.ex.bd || decoderUnit.executeStage.inst0.ex.bd),
    Mux(ctrl.decoderUnit.branch, !decoderUnit.instFifo.allow_to_go(1), false.B),
  )
  instFifo.decoder_delay_flush := ctrl.decoderUnit.branch
  instFifo.execute_delay_flush := ctrl.executeUnit.branch
  instFifo.ren <> decoderUnit.instFifo.allow_to_go
  decoderUnit.instFifo.inst <> instFifo.read

  for (i <- 0 until config.instFetchNum) {
    instFifo.write(i).pht_index   := bpu.instBuffer.pht_index(i)
    bpu.instBuffer.pc(i)          := instFifo.write(i).pc
    instFifo.wen(i)               := io.inst.inst_valid(i)
    instFifo.write(i).tlb.refill  := tlbL1I.tlb1.refill
    instFifo.write(i).tlb.invalid := tlbL1I.tlb1.invalid
    instFifo.write(i).pc          := io.inst.addr(0) + (i * 4).U
    instFifo.write(i).inst        := io.inst.inst(i)
  }

  decoderUnit.instFifo.info.empty                 := instFifo.empty
  decoderUnit.instFifo.info.almost_empty          := instFifo.almost_empty
  decoderUnit.instFifo.info.inst0_is_in_delayslot := instFifo.inst0_is_in_delayslot
  decoderUnit.regfile <> regfile.read
  for (i <- 0 until (config.fuNum)) {
    decoderUnit.forward(i).exe      := executeUnit.decoderUnit.forward(i).exe
    decoderUnit.forward(i).mem_wreg := executeUnit.decoderUnit.forward(i).exe_mem_wreg
    decoderUnit.forward(i).mem      := memoryUnit.decoderUnit(i)
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

  executeUnit.decoderUnit.inst0_bd := decoderUnit.executeStage.inst0.ex.bd
  executeUnit.executeStage <> executeStage.executeUnit
  executeUnit.cp0 <> cp0.executeUnit
  executeUnit.memoryStage <> memoryStage.executeUnit

  cp0.ctrl.exe_stall := !ctrl.executeUnit.allow_to_go
  cp0.ctrl.mem_stall := !ctrl.memoryUnit.allow_to_go
  cp0.tlb(0).vpn2    := tlbL1I.tlb2.vpn2
  cp0.tlb(1).vpn2    := tlbL1D.tlb2.vpn2
  cp0.ext_int        := io.ext_int
  tlbL1I.tlb2.found  := cp0.tlb(0).found
  tlbL1D.tlb2.found  := cp0.tlb(1).found
  tlbL1I.tlb2.entry  := cp0.tlb(0).info
  tlbL1D.tlb2.entry  := cp0.tlb(1).info

  memoryStage.ctrl.allow_to_go := ctrl.memoryUnit.allow_to_go
  memoryStage.ctrl.clear       := ctrl.memoryUnit.do_flush

  memoryUnit.memoryStage <> memoryStage.memoryUnit
  memoryUnit.cp0 <> cp0.memoryUnit
  memoryUnit.writeBackStage <> writeBackStage.memoryUnit

  memoryUnit.dataMemory.in.tlb <> tlbL1D.tlb1
  memoryUnit.dataMemory.in.rdata := io.data.rdata
  io.data.en                     := memoryUnit.dataMemory.out.en
  io.data.rlen                   := memoryUnit.dataMemory.out.rlen
  io.data.wen                    := memoryUnit.dataMemory.out.wen
  io.data.wdata                  := memoryUnit.dataMemory.out.wdata
  io.data.addr                   := memoryUnit.dataMemory.out.addr

  writeBackStage.memoryUnit <> memoryUnit.writeBackStage
  writeBackStage.ctrl.allow_to_go := ctrl.writeBackUnit.allow_to_go
  writeBackStage.ctrl.clear       := ctrl.writeBackUnit.do_flush

  writeBackUnit.writeBackStage <> writeBackStage.writeBackUnit
  writeBackUnit.ctrl <> ctrl.writeBackUnit
  regfile.write <> writeBackUnit.regfile

  io.debug <> writeBackUnit.debug

  io.inst.fence        := executeUnit.executeStage.inst0.inst_info.ifence
  io.inst.fence_addr   := executeUnit.executeStage.inst0.inst_info.mem_addr
  io.data.fence        := memoryUnit.memoryStage.inst0.inst_info.dfence
  io.data.fence_addr   := memoryUnit.memoryStage.inst0.inst_info.mem_addr
  io.data.execute_addr := executeUnit.memoryStage.inst0.mem.addr
  io.inst.req          := !instFifo.full
  io.inst.cpu_stall    := !ctrl.fetchUnit.allow_to_go
  io.data.cpu_stall    := !ctrl.memoryUnit.allow_to_go

  // ===----------------------------------------------------------------===
  // statistic
  // ===----------------------------------------------------------------===
  if (!config.build) {
    io.statistic.get.soc <> writeBackUnit.statistic.get
    io.statistic.get.bpu <> executeUnit.statistic.get
  }
}
