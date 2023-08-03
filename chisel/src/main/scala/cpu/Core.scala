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

class Core(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ext_int   = Input(UInt(6.W))
    val inst      = new Cache_ICache()
    val data      = new Cache_DCache()
    val debug     = new DEBUG()
    val statistic = if (!config.build) Some(new GlobalStatic()) else None
  })

  val ctrl           = Module(new Ctrl()).io
  val fetchUnit      = Module(new FetchUnit()).io
  val bpu            = Module(new BranchPredictorUnit()).io
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

  ctrl.instBuffer.has2insts := !(instBuffer.empty || instBuffer.almost_empty)
  ctrl.decoderUnit <> decoderUnit.ctrl
  ctrl.executeUnit <> executeUnit.ctrl
  ctrl.memoryUnit <> memoryUnit.ctrl
  ctrl.writeBackUnit <> writeBackUnit.ctrl
  ctrl.cacheCtrl.iCache_stall := io.inst.icache_stall
  ctrl.cacheCtrl.dCache_stall := io.data.dcache_stall

  fetchUnit.memory <> memoryUnit.fetchUnit
  fetchUnit.execute <> executeUnit.fetchUnit
  fetchUnit.decoder <> decoderUnit.fetchUnit
  fetchUnit.instBuffer.full   := instBuffer.full
  fetchUnit.iCache.inst_valid := io.inst.inst_valid
  io.inst.addr(0)             := fetchUnit.iCache.pc
  io.inst.addr(1)             := fetchUnit.iCache.pc_next
  io.inst.addr(2)             := fetchUnit.iCache.pc_next + 4.U
  io.inst.addr(3)             := fetchUnit.iCache.pc_next + 8.U

  bpu.decoder.ena               := ctrl.decoderUnit.allow_to_go
  bpu.decoder.op                := decoderUnit.bpu.decoded_inst0.op
  bpu.decoder.inst              := decoderUnit.bpu.decoded_inst0.inst
  bpu.decoder.pc                := decoderUnit.bpu.pc
  bpu.decoder.pc_plus4          := decoderUnit.bpu.pc + 4.U
  bpu.execute.pc                := executeUnit.bpu.pc
  bpu.execute.branch            := executeUnit.bpu.branch_inst
  bpu.execute.actual_take       := executeUnit.bpu.branch
  decoderUnit.bpu.branch_inst   := bpu.decoder.branch
  decoderUnit.bpu.pred_branch   := bpu.decoder.pred_take
  decoderUnit.bpu.branch_target := bpu.decoder.branch_target

  instBuffer.do_flush         := ctrl.decoderUnit.do_flush
  instBuffer.flush_delay_slot := ctrl.instBuffer.delay_slot_do_flush
  instBuffer.icache_stall     := io.inst.icache_stall
  instBuffer.jump_branch_inst := decoderUnit.instBuffer.jump_branch_inst
  instBuffer.delay_sel_rst := Mux(
    ctrl.executeUnit.branch,
    !(executeUnit.memoryStage.inst1.ex.bd || decoderUnit.executeStage.inst0.ex.bd),
    Mux(ctrl.decoderUnit.branch, !decoderUnit.instBuffer.inst(1).ready, false.B),
  )
  instBuffer.decoder_delay_rst := ctrl.decoderUnit.branch
  instBuffer.execute_delay_rst := ctrl.executeUnit.branch
  for (i <- 0 until config.decoderNum) {
    instBuffer.ren(i)                               := decoderUnit.instBuffer.inst(i).ready
    decoderUnit.instBuffer.inst(i).valid            := true.B
    decoderUnit.instBuffer.inst(i).bits.tlb_refill  := instBuffer.read(i).tlb.refill
    decoderUnit.instBuffer.inst(i).bits.tlb_invalid := instBuffer.read(i).tlb.invalid
    decoderUnit.instBuffer.inst(i).bits.pc          := instBuffer.read(i).addr
    decoderUnit.instBuffer.inst(i).bits.inst        := instBuffer.read(i).data
  }
  instBuffer.wen(0)               := io.inst.inst_valid(0)
  instBuffer.wen(1)               := io.inst.inst_valid(1)
  instBuffer.wen(2)               := io.inst.inst_valid(2)
  instBuffer.wen(3)               := io.inst.inst_valid(3)
  instBuffer.write(0).tlb.refill  := tlbL1I.tlb1.refill
  instBuffer.write(1).tlb.refill  := tlbL1I.tlb1.refill
  instBuffer.write(2).tlb.refill  := tlbL1I.tlb1.refill
  instBuffer.write(3).tlb.refill  := tlbL1I.tlb1.refill
  instBuffer.write(0).tlb.invalid := tlbL1I.tlb1.invalid
  instBuffer.write(1).tlb.invalid := tlbL1I.tlb1.invalid
  instBuffer.write(2).tlb.invalid := tlbL1I.tlb1.invalid
  instBuffer.write(3).tlb.invalid := tlbL1I.tlb1.invalid
  instBuffer.write(0).addr        := io.inst.addr(0)
  instBuffer.write(1).addr        := io.inst.addr(0) + 4.U
  instBuffer.write(2).addr        := io.inst.addr(0) + 8.U
  instBuffer.write(3).addr        := io.inst.addr(0) + 12.U
  instBuffer.write(0).data        := io.inst.inst(0)
  instBuffer.write(1).data        := io.inst.inst(1)
  instBuffer.write(2).data        := io.inst.inst(2)
  instBuffer.write(3).data        := io.inst.inst(3)

  decoderUnit.instBuffer.info.empty                 := instBuffer.empty
  decoderUnit.instBuffer.info.almost_empty          := instBuffer.almost_empty
  decoderUnit.instBuffer.info.inst0_is_in_delayslot := instBuffer.inst0_is_in_delayslot
  decoderUnit.regfile <> regfile.read
  for (i <- 0 until (config.fuNum)) {
    decoderUnit.forward(i).exe      := executeUnit.decoderUnit.forward(i).exe
    decoderUnit.forward(i).exe_rmem := executeUnit.decoderUnit.forward(i).exe_rmem
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
  memoryStage.ctrl.clear(0)    := ctrl.memoryUnit.do_flush
  memoryStage.ctrl.clear(1)    := ctrl.memoryUnit.do_flush

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
  writeBackStage.ctrl.clear(0)    := ctrl.writeBackUnit.do_flush
  writeBackStage.ctrl.clear(1)    := ctrl.writeBackUnit.do_flush

  writeBackUnit.writeBackStage <> writeBackStage.writeBackUnit
  writeBackUnit.ctrl <> ctrl.writeBackUnit
  regfile.write <> writeBackUnit.regfile

  io.debug <> writeBackUnit.debug

  io.inst.fence        := executeUnit.memoryStage.inst0.inst_info.ifence
  io.inst.fence_addr   := executeUnit.memoryStage.inst0.rd_info.wdata
  io.data.fence        := memoryUnit.writeBackStage.inst0.inst_info.dfence
  io.data.fence_addr   := memoryUnit.writeBackStage.inst0.rd_info.wdata
  io.data.execute_addr := executeUnit.memoryStage.inst0.mem.addr
  io.inst.req          := !instBuffer.full
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
