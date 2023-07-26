package cache

import chisel3._
import chisel3.util._

class CacheAXIInterface extends Module {
  val io = IO(new Bundle {
    val icache = Flipped(new ICache_AXIInterface())
    val dcache = Flipped(new DCache_AXIInterface())
    val axi    = new AXI()
  })
  val ar_sel          = Wire(Bool())
  val ar_sel_lock     = RegInit(false.B)
  val ar_sel_lock_val = RegInit(false.B)
  when(io.axi.ar.valid) {
    when(io.axi.ar.ready) {
      ar_sel_lock := false.B
    }.otherwise {
      ar_sel_lock     := true.B
      ar_sel_lock_val := ar_sel
    }
  }

  ar_sel := Mux(ar_sel_lock, ar_sel_lock_val, !io.icache.ar.valid && io.dcache.ar.valid)
  val r_sel = io.axi.r.bits.id(0)

  // ===----------------------------------------------------------------===
  // dcache
  // ===----------------------------------------------------------------===
  io.dcache.ar.ready    := io.axi.ar.ready && ar_sel
  io.dcache.r.bits.data := Mux(r_sel, io.axi.r.bits.data, 0.U)
  io.dcache.r.bits.last := Mux(r_sel, io.axi.r.bits.last, 0.U)
  io.dcache.r.valid     := Mux(r_sel, io.axi.r.valid, 0.U)

  io.dcache.aw.ready := io.axi.aw.ready
  io.dcache.w.ready  := io.axi.w.ready
  io.dcache.b.valid  := io.axi.b.valid

  // ===----------------------------------------------------------------===
  // icache
  // ===----------------------------------------------------------------===
  io.icache.ar.ready    := io.axi.ar.ready && !ar_sel
  io.icache.r.bits.data := Mux(!r_sel, io.axi.r.bits.data, 0.U)
  io.icache.r.bits.last := Mux(!r_sel, io.axi.r.bits.last, 0.U)
  io.icache.r.valid     := Mux(!r_sel, io.axi.r.valid, 0.U)

  // ===----------------------------------------------------------------===
  // axi
  // ===----------------------------------------------------------------===
  io.axi.ar.bits.id    := ar_sel
  io.axi.ar.bits.addr  := Mux(ar_sel, io.dcache.ar.bits.addr, io.icache.ar.bits.addr)
  io.axi.ar.bits.len   := Mux(ar_sel, io.dcache.ar.bits.len, io.icache.ar.bits.len)
  io.axi.ar.bits.size  := Mux(ar_sel, io.dcache.ar.bits.size, io.icache.ar.bits.size)
  io.axi.ar.bits.burst := 1.U
  io.axi.ar.bits.lock  := 0.U
  io.axi.ar.bits.cache := 0.U
  io.axi.ar.bits.prot  := 0.U
  io.axi.ar.valid      := Mux(ar_sel, io.dcache.ar.valid, io.icache.ar.valid)

  io.axi.r.ready := Mux(~r_sel, io.icache.r.ready, io.dcache.r.ready)

  io.axi.aw.bits.id    := 0.U
  io.axi.aw.bits.addr  := io.dcache.aw.bits.addr
  io.axi.aw.bits.len   := io.dcache.aw.bits.len
  io.axi.aw.bits.size  := io.dcache.aw.bits.size
  io.axi.aw.bits.burst := 1.U
  io.axi.aw.bits.lock  := 0.U
  io.axi.aw.bits.cache := 0.U
  io.axi.aw.bits.prot  := 0.U
  io.axi.aw.valid      := io.dcache.aw.valid

  io.axi.w.bits.id   := 0.U
  io.axi.w.bits.data := io.dcache.w.bits.data
  io.axi.w.bits.strb := io.dcache.w.bits.strb
  io.axi.w.bits.last := io.dcache.w.bits.last
  io.axi.w.valid     := io.dcache.w.valid

  io.axi.b.ready := io.dcache.b.ready
}
