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

class TlbL1I extends Module {
  val io = IO(new Bundle {
    val addr         = Input(UInt(32.W))
    val fence        = Input(Bool())
    val cpu_stall    = Input(Bool())
    val icache_stall = Input(Bool())
    val cache        = new Tlb_ICache()
    val tlb1         = Output(new Tlb1InfoI())
    val tlb2         = Flipped(new Tlb2Info())
  })
  val itlb          = RegInit(0.U.asTypeOf(new ITLB()))
  val vpn           = io.addr(31, 12)
  val direct_mapped = io.addr(31, 30) === 2.U(2.W)

  io.cache.uncached       := Mux(direct_mapped, io.addr(29), itlb.uncached)
  io.cache.translation_ok := direct_mapped || (itlb.vpn === vpn && itlb.valid)
  io.cache.hit            := io.tlb2.found && io.tlb2.entry.v(vpn(0))
  io.cache.tag            := Mux(direct_mapped, Cat(0.U(3.W), io.addr(28, 12)), itlb.ppn)
  io.cache.pa             := Cat(io.cache.tag, io.addr(11, 0))

  when(io.fence && !io.icache_stall && !io.cpu_stall) { itlb.valid := false.B }

  // * tlb1 * //
  val tlb1 = RegInit(0.U.asTypeOf(new Tlb1InfoI()))
  tlb1 <> io.tlb1

  io.tlb2.vpn2 := vpn(19, 1)

  when(io.cache.icache_is_tlb_fill) {
    when(io.tlb2.found) {
      when(io.tlb2.entry.v(vpn(0))) {
        itlb.vpn      := vpn
        itlb.ppn      := io.tlb2.entry.pfn(vpn(0))
        itlb.uncached := !io.tlb2.entry.c(vpn(0))
        itlb.valid    := true.B
      }.otherwise {
        tlb1.invalid := true.B
      }
    }.otherwise {
      tlb1.refill := true.B
    }
  }.elsewhen(io.cache.icache_is_save && !io.cpu_stall && !io.icache_stall) {
    tlb1.invalid := false.B
    tlb1.refill  := false.B
  }
}
