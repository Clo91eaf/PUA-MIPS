package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val fromMemory = Flipped(new Memory_WriteBackStage())
    val fromCP0    = Flipped(new CP0_WriteBackStage())
    val fromTLB    = Flipped(new TLB_WriteBackStage())
    val ext_int    = Input(UInt(6.W))

    val decoder = new WriteBackStage_Decoder()
    val regFile = new WriteBackStage_RegFile()
    val execute = new WriteBackStage_Execute()
    val memory  = new WriteBackStage_Memory()
    val mov     = new WriteBackStage_Mov()
    val hilo    = new WriteBackStage_HILO()
    val cp0     = new WriteBackStage_CP0()
    val ctrl    = new WriteBackStage_Ctrl()
    val tlb     = new WriteBackStage_TLB()
    val instMMU = new WriteBackStage_MMU()
    val dataMMU = new WriteBackStage_MMU()
    val debug   = new DEBUG()
  })
  // input
  val ws_pc              = RegInit(BUS_INIT)
  val ws_reg_waddr       = RegInit(ADDR_BUS_INIT)
  val ws_reg_wen         = RegInit(REG_WRITE_DISABLE)
  val ws_reg_wdata       = RegInit(BUS_INIT)
  val ws_hi              = RegInit(BUS_INIT)
  val ws_lo              = RegInit(BUS_INIT)
  val ws_whilo           = RegInit(WRITE_DISABLE)
  val ws_valid           = RegInit(false.B)
  val ws_inst_is_mtc0    = RegInit(false.B)
  val ws_inst_is_mfc0    = RegInit(false.B)
  val ws_inst_is_eret    = RegInit(false.B)
  val ws_inst_is_syscall = RegInit(false.B)
  val ws_bd              = RegInit(false.B)
  val ws_badvaddr        = RegInit(BUS_INIT)
  val ws_cp0_addr        = RegInit(0.U(8.W))
  val ws_excode          = RegInit(0.U(5.W))
  val ws_ex              = RegInit(false.B)
  val ext_int            = io.ext_int
  val ws_inst_is_tlbp    = RegInit(false.B)
  val ws_inst_is_tlbr    = RegInit(false.B)
  val ws_inst_is_tlbwi   = RegInit(false.B)
  val ws_tlb_refill      = RegInit(false.B)
  val ws_after_tlb       = RegInit(false.B)
  val ws_s1_found        = RegInit(false.B)
  val ws_s1_index        = RegInit(0.U(log2Ceil(TLB_NUM).W))
  // input-cp0
  val cp0_rdata  = io.fromCP0.cp0_rdata
  val cp0_epc    = io.fromCP0.cp0_epc
  val cp0_status = io.fromCP0.cp0_status
  val cp0_cause  = io.fromCP0.cp0_cause

  // output
  val allowin = Wire(Bool())

  val inst_is_mfc0 = Wire(Bool())
  val cp0_we       = Wire(Bool())
  val cp0_wdata    = Wire(UInt(32.W))
  val excode       = ws_excode
  val badvaddr     = ws_badvaddr
  val eret         = ws_valid && ws_inst_is_eret
  val ex           = ws_valid && ws_ex

  val ex_tlb_refill_entry =
    (ws_excode === EX_TLBL || ws_excode === EX_TLBS) && ws_tlb_refill && ws_valid

  val do_flush = ex
  val flush_pc = MuxCase(
    EX_ENTRY,
    Seq(
      ws_after_tlb        -> ws_pc,
      ws_inst_is_eret     -> cp0_epc,
      ex_tlb_refill_entry -> EX_TLB_REFILL_ENTRY,
    ),
  )

  // output-ctrl
  io.ctrl.ex       := ex
  io.ctrl.do_flush := do_flush
  io.ctrl.flush_pc := flush_pc

  // output-mmu
  io.dataMMU.cp0_entryhi := io.fromCP0.cp0_entryhi
  io.instMMU.cp0_entryhi := io.fromCP0.cp0_entryhi

  // output-reg file
  io.regFile.reg_waddr := ws_reg_waddr
  io.regFile.reg_wen   := ws_reg_wen & Fill(4, ws_valid & ~ws_ex)
  io.regFile.reg_wdata := Mux(ws_inst_is_mfc0, cp0_rdata, ws_reg_wdata)

  // output-hilo
  io.hilo.hi    := ws_hi
  io.hilo.lo    := ws_lo
  io.hilo.whilo := ws_whilo & ws_valid

  // output-decoder
  io.decoder.inst_is_mfc0 := inst_is_mfc0
  io.decoder.reg_waddr    := ws_reg_waddr
  io.decoder.cp0_cause    := cp0_cause
  io.decoder.cp0_status   := cp0_status

  // output-execute
  io.execute.hi    := ws_hi
  io.execute.lo    := ws_lo
  io.execute.whilo := ws_whilo && ws_valid

  // output-mov
  io.mov.cp0_wen   := cp0_we
  io.mov.cp0_waddr := ws_cp0_addr
  io.mov.cp0_wdata := cp0_wdata
  io.mov.cp0_rdata := cp0_rdata

  // output-memory
  io.memory.allowin := allowin

  // output-debug
  io.debug.pc    := ws_pc
  io.debug.waddr := io.regFile.reg_waddr
  io.debug.wen   := io.regFile.reg_wen
  io.debug.wdata := io.regFile.reg_wdata

  io.debug.cp0_count  := io.fromCP0.cp0_count
  io.debug.cp0_cause  := io.fromCP0.cp0_cause
  io.debug.cp0_random := io.fromCP0.cp0_random

  io.debug.int    := ex && !ws_inst_is_eret && !ws_after_tlb
  io.debug.commit := ws_valid & ~ws_ex

  // output-cp0
  io.cp0.wb_ex       := ex && !ws_inst_is_eret && !ws_after_tlb
  io.cp0.wb_bd       := ws_bd
  io.cp0.eret_flush  := eret
  io.cp0.wb_excode   := ws_excode
  io.cp0.wb_pc       := ws_pc
  io.cp0.wb_badvaddr := ws_badvaddr
  io.cp0.ext_int_in  := ext_int
  io.cp0.cp0_addr    := ws_cp0_addr
  io.cp0.mtc0_we     := cp0_we
  io.cp0.cp0_wdata   := cp0_wdata
  io.cp0.tlbp        := ws_inst_is_tlbp
  io.cp0.tlbr        := ws_inst_is_tlbr
  io.cp0.tlbwi       := ws_inst_is_tlbwi
  io.cp0.s1_found    := ws_s1_found
  io.cp0.s1_index    := ws_s1_index
  io.cp0.r_vpn2      := io.fromTLB.r_vpn2
  io.cp0.r_asid      := io.fromTLB.r_asid
  io.cp0.r_g         := io.fromTLB.r_g
  io.cp0.r_pfn0      := io.fromTLB.r_pfn0
  io.cp0.r_c0        := io.fromTLB.r_c0
  io.cp0.r_d0        := io.fromTLB.r_d0
  io.cp0.r_v0        := io.fromTLB.r_v0
  io.cp0.r_pfn1      := io.fromTLB.r_pfn1
  io.cp0.r_c1        := io.fromTLB.r_c1
  io.cp0.r_d1        := io.fromTLB.r_d1
  io.cp0.r_v1        := io.fromTLB.r_v1

  // output-tlb
  io.tlb.we      := ws_inst_is_tlbwi && ws_valid
  io.tlb.r_index := io.fromCP0.cp0_index(3, 0)

  // ENTRYHI
  io.tlb.w_index := io.fromCP0.cp0_index(3, 0)
  io.tlb.w_vpn2  := io.fromCP0.cp0_entryhi(31, 13)
  io.tlb.w_asid  := io.fromCP0.cp0_entryhi(7, 0)
  io.tlb.w_g     := io.fromCP0.cp0_entrylo0(0) & io.fromCP0.cp0_entrylo1(0)
  // ENTRYLO0
  io.tlb.w_pfn0 := io.fromCP0.cp0_entrylo0(25, 6)
  io.tlb.w_c0   := io.fromCP0.cp0_entrylo0(5, 3)
  io.tlb.w_d0   := io.fromCP0.cp0_entrylo0(2)
  io.tlb.w_v0   := io.fromCP0.cp0_entrylo0(1)
  // ENTRYLO1
  io.tlb.w_pfn1 := io.fromCP0.cp0_entrylo1(25, 6)
  io.tlb.w_c1   := io.fromCP0.cp0_entrylo1(5, 3)
  io.tlb.w_d1   := io.fromCP0.cp0_entrylo1(2)
  io.tlb.w_v1   := io.fromCP0.cp0_entrylo1(1)

  // io-finish

  val ready_go = true.B
  allowin := !ws_valid || ready_go

  when(allowin) {
    ws_valid := io.fromMemory.valid
  }

  when(io.fromMemory.valid && allowin) {
    // input-memory
    ws_reg_waddr       := io.fromMemory.reg_waddr
    ws_reg_wen         := io.fromMemory.reg_wen
    ws_reg_wdata       := io.fromMemory.reg_wdata
    ws_hi              := io.fromMemory.hi
    ws_lo              := io.fromMemory.lo
    ws_whilo           := io.fromMemory.whilo
    ws_pc              := io.fromMemory.pc
    ws_inst_is_mfc0    := io.fromMemory.inst_is_mfc0
    ws_inst_is_mtc0    := io.fromMemory.inst_is_mtc0
    ws_inst_is_syscall := io.fromMemory.inst_is_syscall
    ws_inst_is_eret    := io.fromMemory.inst_is_eret
    ws_excode          := io.fromMemory.excode
    ws_badvaddr        := io.fromMemory.badvaddr
    ws_cp0_addr        := io.fromMemory.cp0_addr
    ws_ex              := io.fromMemory.ex
    ws_bd              := io.fromMemory.bd
    ws_inst_is_tlbp    := io.fromMemory.inst_is_tlbp
    ws_inst_is_tlbr    := io.fromMemory.inst_is_tlbr
    ws_inst_is_tlbwi   := io.fromMemory.inst_is_tlbwi
    ws_tlb_refill      := io.fromMemory.tlb_refill
    ws_after_tlb       := io.fromMemory.after_tlb
    ws_s1_found        := io.fromMemory.s1_found
    ws_s1_index        := io.fromMemory.s1_index
  }

  inst_is_mfc0 := ws_valid && ws_inst_is_mfc0

  cp0_we    := ws_valid && ws_inst_is_mtc0 && !ws_ex
  cp0_wdata := ws_reg_wdata

}
