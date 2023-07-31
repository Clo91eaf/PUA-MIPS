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

class Core(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
    val inst    = new Cache_ICache()
    val data    = new Cache_DCache()
    val debug   = new DEBUG()
  })

  val ctrl           = Module(new Ctrl()).io
  val fetchUnit      = Module(new FetchUnit()).io
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
  ctrl.cacheCtrl.iCache_stall := io.inst.icache_stall
  ctrl.cacheCtrl.dCache_stall := io.data.dstall

  fetchUnit.memory <> memoryUnit.fetchUnit
  fetchUnit.execute <> executeUnit.fetchUnit
  fetchUnit.decoder <> decoderUnit.fetchUnit
  fetchUnit.instBuffer.full   := instBuffer.full
  fetchUnit.iCache.inst_valid := io.inst.inst_valid
  io.inst.addr(0)             := fetchUnit.iCache.pc
  io.inst.addr(1)             := fetchUnit.iCache.pc_next
  io.inst.addr(2)             := fetchUnit.iCache.pc_next + 4.U
  io.inst.addr(3)             := fetchUnit.iCache.pc_next + 8.U

  bpu.enaD                      := ctrl.decoderUnit.allow_to_go
  bpu.instrD                    := decoderUnit.bpu.decoded_inst0.inst
  bpu.pcD                       := decoderUnit.bpu.pc
  bpu.pc_plus4D                 := decoderUnit.bpu.pc + 4.U
  bpu.pcE                       := executeUnit.bpu.pc
  bpu.branchE                   := executeUnit.bpu.branch_inst
  bpu.actual_takeE              := executeUnit.bpu.branch
  decoderUnit.bpu.branch_inst   := bpu.branchD
  decoderUnit.bpu.pred_branch   := bpu.pred_takeD
  decoderUnit.bpu.branch_target := bpu.branch_targetD

  instBuffer.fifo_rst         := reset.asBool || ctrl.decoderUnit.do_flush
  instBuffer.flush_delay_slot := ctrl.instBuffer.delay_slot_do_flush
  instBuffer.D_ena            := ctrl.decoderUnit.allow_to_go
  instBuffer.i_stall          := io.inst.icache_stall
  instBuffer.jump_branch_inst := decoderUnit.instBuffer.jump_branch_inst
  instBuffer.delay_sel_rst := Mux(
    ctrl.executeUnit.branch,
    !(executeUnit.memoryStage.inst1.ex.bd || decoderUnit.executeStage.inst0.ex.bd),
    Mux(
      ctrl.decoderUnit.branch,
      !decoderUnit.instBuffer.inst(1).ready,
      false.B,
    ),
  )
  instBuffer.D_delay_rst := ctrl.decoderUnit.branch
  instBuffer.E_delay_rst := ctrl.executeUnit.branch
  for (i <- 0 until config.decoderNum) {
    instBuffer.read_en(i)                           := decoderUnit.instBuffer.inst(i).ready
    decoderUnit.instBuffer.inst(i).valid            := true.B
    decoderUnit.instBuffer.inst(i).bits.tlb_refill  := instBuffer.read(i).tlb.refill
    decoderUnit.instBuffer.inst(i).bits.tlb_invalid := instBuffer.read(i).tlb.invalid
    decoderUnit.instBuffer.inst(i).bits.pc          := instBuffer.read(i).addr
    decoderUnit.instBuffer.inst(i).bits.inst        := instBuffer.read(i).data
  }
  instBuffer.write_en(0)          := io.inst.inst_valid(0)
  instBuffer.write_en(1)          := io.inst.inst_valid(1)
  instBuffer.write_en(2)          := io.inst.inst_valid(2)
  instBuffer.write_en(3)          := io.inst.inst_valid(3)
  instBuffer.write(0).tlb.refill  := io.inst.tlb1.refill
  instBuffer.write(1).tlb.refill  := io.inst.tlb1.refill
  instBuffer.write(2).tlb.refill  := io.inst.tlb1.refill
  instBuffer.write(3).tlb.refill  := io.inst.tlb1.refill
  instBuffer.write(0).tlb.invalid := io.inst.tlb1.invalid
  instBuffer.write(1).tlb.invalid := io.inst.tlb1.invalid
  instBuffer.write(2).tlb.invalid := io.inst.tlb1.invalid
  instBuffer.write(3).tlb.invalid := io.inst.tlb1.invalid
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
  cp0.tlb(0).vpn2    := io.inst.tlb2.vpn2
  cp0.tlb(1).vpn2    := io.data.tlb2.vpn2
  cp0.ext_int        := io.ext_int
  io.inst.tlb2.found := cp0.tlb(0).found
  io.data.tlb2.found := cp0.tlb(1).found
  io.inst.tlb2.entry := cp0.tlb(0).info
  io.data.tlb2.entry := cp0.tlb(1).info

  memoryStage.ctrl.allow_to_go := ctrl.memoryUnit.allow_to_go
  memoryStage.ctrl.clear(0)    := ctrl.memoryUnit.do_flush
  memoryStage.ctrl.clear(1)    := ctrl.memoryUnit.do_flush

  memoryUnit.memoryStage <> memoryStage.memoryUnit
  memoryUnit.cp0 <> cp0.memoryUnit
  memoryUnit.writeBackStage <> writeBackStage.memoryUnit

  memoryUnit.dataMemory.in.tlb <> io.data.tlb1
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

  io.inst.fence.value := executeUnit.memoryStage.inst0.inst_info
    .inst(16) === 0.U && executeUnit.memoryStage.inst0.inst_info.op === EXE_CACHE
  io.inst.fence.addr := executeUnit.memoryStage.inst0.rd_info.wdata
  io.data.M_fence_d := memoryUnit.writeBackStage.inst0.inst_info
    .inst(16) === 1.U && memoryUnit.writeBackStage.inst0.inst_info.op === EXE_CACHE
  io.data.M_fence_addr := memoryUnit.writeBackStage.inst0.rd_info.wdata
  io.inst.fence.tlb    := VecInit(EXE_MTC0, EXE_TLBWI, EXE_TLBWR).contains(executeUnit.executeStage.inst0.inst_info.op)
  io.data.fence_tlb    := VecInit(EXE_MTC0, EXE_TLBWI, EXE_TLBWR).contains(memoryUnit.memoryStage.inst0.inst_info.op)
  io.data.E_mem_va     := executeUnit.memoryStage.inst0.mem.addr
  io.inst.req          := !(reset.asBool || instBuffer.full)
  io.inst.cpu_stall    := !ctrl.fetchUnit.allow_to_go
  io.data.stallM       := !ctrl.memoryUnit.allow_to_go
}
