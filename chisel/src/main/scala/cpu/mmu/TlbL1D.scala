package cpu.mmu

import chisel3._
import chisel3.util._
import cpu.defines._

class DTLB extends ITLB {
  val dirty = Bool()
}

class TlbL1D extends Module {
  val io = IO(new Bundle {
    val cache        = new Tlb_DCache()
    val fence        = Input(Bool())
    val cpu_stall    = Input(Bool())
    val dcache_stall = Input(Bool())
    val addr         = Input(UInt(32.W))

    val mem_en    = Input(Bool())
    val mem_write = Input(Bool())

    val tlb1 = Output(new Tlb1InfoD())
    val tlb2 = Flipped(new Tlb2Info())
  })
  val dtlb          = RegInit(0.U.asTypeOf(new DTLB()))
  val vpn           = io.addr(31, 12)
  val direct_mapped = io.addr(31, 30) === 2.U(2.W)

  io.cache.uncached       := Mux(direct_mapped, io.addr(29), dtlb.uncached)
  io.cache.translation_ok := direct_mapped || (dtlb.vpn === vpn && dtlb.valid && (!io.mem_write || dtlb.dirty))

  io.cache.tag     := Mux(direct_mapped, Cat(0.U(3.W), io.addr(28, 12)), dtlb.ppn)
  io.cache.pa      := Cat(io.cache.tag, io.addr(11, 0))
  io.cache.tlb1_ok := dtlb.vpn === vpn && dtlb.valid
  io.cache.hit     := io.cache.fill && io.tlb2.found && io.tlb2.entry.v(vpn(0))

  when(io.fence) { dtlb.valid := false.B }

  val tlb1 = RegInit(0.U.asTypeOf(new Tlb1InfoD()))
  io.tlb1 <> tlb1

  val tlb2 = RegInit(0.U.asTypeOf(new Bundle { val vpn2 = UInt(19.W) }))
  io.tlb2.vpn2 <> tlb2.vpn2

  when(io.cache.dcache_is_idle && !io.cache.fill && io.mem_en && !io.cache.translation_ok) {
    when(io.cache.tlb1_ok) {
      tlb1.modify := true.B
    }.otherwise {
      tlb2.vpn2 := vpn(19, 1)
    }
  }.elsewhen(io.cache.fill) {
    when(io.tlb2.found) {
      when(io.tlb2.entry.v(vpn(0))) {
        dtlb.vpn      := vpn
        dtlb.ppn      := io.tlb2.entry.pfn(vpn(0))
        dtlb.uncached := !io.tlb2.entry.c(vpn(0))
        dtlb.dirty    := io.tlb2.entry.d(vpn(0))
        dtlb.valid    := true.B
      }.otherwise {
        tlb1.invalid := true.B
      }
    }.otherwise {
      tlb1.refill := true.B
    }
  }.elsewhen(io.cache.dcache_is_save && !io.cpu_stall && !io.dcache_stall) {
    tlb1.invalid := false.B
    tlb1.refill  := false.B
    tlb1.modify  := false.B
  }
}
