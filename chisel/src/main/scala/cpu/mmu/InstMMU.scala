package cpu.mmu

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class InstMMU extends Module {
  val io = IO(new Bundle {
    val fromPreFetchStage  = Flipped(new PreFetchStage_InstMMU())
    val fromWriteBackStage = Flipped(new WriteBackStage_MMU())
    val fromTLB            = Flipped(new TLB_MMU())

    val preFetchStage = new MMU_Common()
    val instMemory    = new MMU_Sram()
    val tlb           = new MMU_TLB()
  })
  // input
  val vaddr       = io.fromPreFetchStage.vaddr
  val inst_tlbp   = false.B
  val cp0_entryhi = io.fromWriteBackStage.cp0_entryhi
  val tlb_found   = io.fromTLB.tlb_found
  val tlb_pfn     = io.fromTLB.tlb_pfn
  val tlb_c       = io.fromTLB.tlb_c
  val tlb_d       = io.fromTLB.tlb_d
  val tlb_v       = io.fromTLB.tlb_v
  // output
  val paddr        = Wire(UInt(32.W))
  val tlb_refill   = Wire(Bool())
  val tlb_invalid  = Wire(Bool())
  val tlb_modified = Wire(Bool())
  val tlb_vpn2     = Wire(UInt(19.W))
  val tlb_odd_page = Wire(Bool())
  val tlb_asid     = Wire(UInt(8.W))
  io.instMemory.paddr           := paddr
  io.preFetchStage.tlb_refill   := tlb_refill
  io.preFetchStage.tlb_invalid  := tlb_invalid
  io.preFetchStage.tlb_modified := tlb_modified
  io.tlb.tlb_vpn2               := tlb_vpn2
  io.tlb.tlb_odd_page           := tlb_odd_page
  io.tlb.tlb_asid               := tlb_asid
  // io-finish
  val unmapped = Wire(Bool())
  unmapped := vaddr(31) && !vaddr(30)

  tlb_vpn2     := Mux(inst_tlbp, cp0_entryhi(31, 13), vaddr(31, 13))
  tlb_odd_page := vaddr(12)
  tlb_asid     := cp0_entryhi(7, 0)

  paddr := Mux(
    unmapped,
    Cat(0.U(3.W), vaddr(28, 0)),
    Cat(tlb_pfn, vaddr(11, 0)),
  )

  tlb_refill   := !unmapped && !tlb_found
  tlb_invalid  := !unmapped && tlb_found && !tlb_v
  tlb_modified := !unmapped && tlb_found && tlb_v && !tlb_d

}