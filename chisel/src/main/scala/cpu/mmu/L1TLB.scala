package cpu.mmu

import chisel3._
import chisel3.util._
import cpu.defines._

class ITLB extends Bundle {
  val vpn      = UInt(20.W)
  val ppn      = UInt(20.W)
  val uncached = Bool()
  val valid    = Bool()
}

class DTLB extends ITLB {
  val dirty = Bool()
}

class L1TLBI extends Module {
  val io = IO(new Bundle {
    val fence              = Input(Bool())
    val cpu_stall          = Input(Bool())
    val icache_stall       = Input(Bool())
    val addr               = Input(UInt(32.W))
    val icache_is_tlb_fill = Input(Bool())
    val icache_is_save     = Input(Bool())
    val uncached           = Output(Bool())
    val tlb1 = new Bundle {
      val invalid = Output(Bool())
      val refill  = Output(Bool())
    }
    val tlb2 = new Bundle {
      val vpn2  = Output(UInt(19.W))
      val found = Input(Bool())
      val entry = Input(new TlbEntry())
    }
    val translation_ok = Output(Bool())
    val hit            = Output(Bool())
    val inst_tag       = Output(UInt(20.W))
    val inst_pa        = Output(UInt(32.W))
  })
  val itlb          = RegInit(0.U.asTypeOf(new ITLB()))
  val inst_vpn      = io.addr(31, 12)
  val direct_mapped = io.addr(31, 30) === 2.U(2.W)

  io.uncached       := Mux(direct_mapped, io.addr(29), itlb.uncached)
  io.translation_ok := direct_mapped || (itlb.vpn === inst_vpn && itlb.valid)

  when(io.icache_is_tlb_fill) {
    itlb.vpn      := inst_vpn
    itlb.ppn      := io.tlb2.entry.pfn(inst_vpn(0))
    itlb.uncached := !io.tlb2.entry.c(inst_vpn(0))
    itlb.valid    := true.B
  }

  io.inst_tag := Mux(direct_mapped, Cat(0.U(3.W), io.addr(28, 12)), itlb.ppn)
  io.inst_pa  := Cat(io.inst_tag, io.addr(11, 0))

  io.tlb2.vpn2 := inst_vpn(19, 1)
  io.hit       := io.tlb2.found && io.tlb2.entry.v(inst_vpn(0))
  when(io.fence && !io.icache_stall && !io.cpu_stall) { itlb.valid := false.B }

  // * tlb1 * //
  val tlb1 = RegInit(0.U.asTypeOf(new Bundle {
    val invalid = Bool()
    val refill  = Bool()
  }))

  tlb1 <> io.tlb1

  when(io.icache_is_tlb_fill) {
    when(io.tlb2.found && !io.hit) {
      tlb1.invalid := true.B
    }.elsewhen(!io.tlb2.found) {
      tlb1.refill := true.B
    }
  }.elsewhen(io.icache_is_save) {
    when(!io.cpu_stall && !io.icache_stall) {
      tlb1.invalid := false.B
      tlb1.refill  := false.B
    }
  }
}

