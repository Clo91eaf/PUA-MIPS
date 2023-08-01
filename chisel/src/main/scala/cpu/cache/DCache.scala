// * Cache 设计借鉴了nscscc2021 cqu的cdim * //
package cache

import chisel3._
import chisel3.util._
import memoryBanks.metaBanks._
import memoryBanks.SimpleDualPortRam
import cpu.defines._
import cpu.mmu._

class WriteBufferUnit extends Bundle {
  val data = UInt(32.W)
  val addr = UInt(32.W)
  val strb = UInt(4.W)
  val size = UInt(2.W)
}

class DCache(cacheConfig: CacheConfig) extends Module {
  implicit val config      = cacheConfig
  val nway: Int            = cacheConfig.nway
  val nset: Int            = cacheConfig.nset
  val nbank: Int           = cacheConfig.nbank
  val ninst: Int           = 2
  val bankOffsetWidth: Int = cacheConfig.bankOffsetWidth
  val bankWidth: Int       = cacheConfig.bankWidth
  val bankWidthBits: Int   = cacheConfig.bankWidthBits
  val tagWidth: Int        = cacheConfig.tagWidth
  val indexWidth: Int      = cacheConfig.indexWidth
  val offsetWidth: Int     = cacheConfig.offsetWidth
  val io = IO(new Bundle {
    val cpu = Flipped(new Cache_DCache())
    val axi = new DCache_AXIInterface()
  })
  // * cpu io * //
  val stallM       = io.cpu.stallM
  val E_mem_va     = io.cpu.E_mem_va
  val M_mem_va     = io.cpu.addr
  val M_fence_addr = io.cpu.M_fence_addr
  val M_fence_d    = io.cpu.M_fence_d
  val M_mem_en     = io.cpu.en
  val M_mem_write  = io.cpu.wen.orR
  val M_wmask      = io.cpu.wen
  val M_mem_size   = io.cpu.rlen
  val M_wdata      = io.cpu.wdata

  val s_idle :: s_tlb_fill :: s_uncached :: s_writeback :: s_replace :: s_save :: Nil = Enum(6)
  val state                                                                           = RegInit(s_idle)

  // * l1_tlb * //
  io.cpu.tlb.dcahce_is_tlb_fill := state === s_tlb_fill
  io.cpu.tlb.dcache_is_idle     := state === s_idle
  io.cpu.tlb.dcache_is_save     := state === s_save

  // * valid dirty * //
  val valid = RegInit(VecInit(Seq.fill(nset)(VecInit(Seq.fill(nway)(false.B)))))
  val dirty = RegInit(VecInit(Seq.fill(nset)(VecInit(Seq.fill(nway)(false.B)))))
  val lru   = RegInit(VecInit(Seq.fill(nset)(0.U(1.W))))

  val tag_wen          = RegInit(VecInit(Seq.fill(nway)(false.B)))
  val bram_replace_wea = RegInit(VecInit(Seq.fill(nway)(0.U(4.W))))

  val data_wstrb = Wire(Vec(nway, UInt(4.W)))

  val tag_wdata = RegInit(0.U(tagWidth.W))

  val bram_addr_choose = (state =/= s_idle) && (state =/= s_save)

  val write_buffer = Module(new Queue(new WriteBufferUnit(), 4))
  write_buffer.io.enq.valid := false.B
  write_buffer.io.enq.bits  := 0.U.asTypeOf(new WriteBufferUnit())
  write_buffer.io.deq.ready := false.B

  // replace and fence control
  val fence_line_addr         = M_fence_addr(11, 6)
  val axi_wcnt                = RegInit(0.U(4.W))
  val bram_replace_addr       = RegInit(0.U(10.W))
  val bram_read_ready_addr    = RegInit(0.U(10.W))
  val bram_replace_write_addr = RegInit(0.U(10.W))
  val bram_replace_cnt        = RegInit(0.U(5.W))
  val bram_r_buffer           = RegInit(VecInit(Seq.fill(16)(0.U(32.W))))
  val bram_use_replace_addr   = RegInit(false.B)
  val bram_data_valid         = RegInit(false.B)
  val fence_working           = RegInit(false.B)
  val replace_working         = RegInit(false.B)
  val ar_handshake            = RegInit(false.B)
  val aw_handshake            = RegInit(false.B)
  val replace_writeback       = RegInit(false.B)
  val fence_way               = dirty(fence_line_addr)(1)

  val data_raddr = Mux(
    bram_use_replace_addr,
    bram_replace_addr,
    Mux(bram_addr_choose, M_mem_va(11, 2), E_mem_va(11, 2)),
  )
  val tag_raddr = Mux(
    bram_use_replace_addr,
    bram_replace_addr(9, 4),
    Mux(bram_addr_choose, M_mem_va(11, 6), E_mem_va(11, 6)),
  )
  val data_waddr = Mux(bram_use_replace_addr, bram_replace_write_addr, M_mem_va(11, 2))

  val data_bram_wdata_sel = state === s_replace
  val data_wdata          = Mux(data_bram_wdata_sel, io.axi.r.bits.data, M_wdata)

  val cache_data = Wire(Vec(nway, UInt(32.W)))
  val cache_tag  = Wire(Vec(nway, UInt(tagWidth.W)))

  val tag_compare_valid = Wire(Vec(nway, Bool()))
  val cache_hit         = tag_compare_valid.contains(true.B)

  val mmio_read_stall  = io.cpu.tlb.uncached && !M_mem_write
  val mmio_write_stall = io.cpu.tlb.uncached && M_mem_write && !write_buffer.io.enq.ready
  val cached_stall     = !io.cpu.tlb.uncached && !cache_hit
  val tlb_stall        = !io.cpu.tlb.translation_ok

  // Note, when 2 > 2, we should mux one hot from tag_compare_valid
  val d_cache_sel = tag_compare_valid(1)

  val pa_line_addr = M_mem_va(11, 6)

  io.cpu.dstall := Mux(
    state === s_idle,
    Mux(M_mem_en, (cached_stall || mmio_read_stall || mmio_write_stall || tlb_stall), M_fence_d),
    state =/= s_save,
  )

  val saved_rdata = RegInit(0.U(32.W))

  // forward last stored data in data bram
  val last_line_addr     = RegInit(0.U(10.W))
  val last_wea           = RegInit(VecInit(Seq.fill(nway)(0.U(32.W))))
  val last_wdata         = RegInit(0.U(32.W))
  val cache_data_forward = Wire(Vec(nway, UInt(32.W)))

  io.cpu.rdata := Mux(state === s_save, saved_rdata, cache_data_forward(d_cache_sel))

  // bank tagv ram
  for { i <- 0 until nway } {
    val bank_ram = Module(new SimpleDualPortRam(nset * nbank, bankWidthBits, byteAddressable = true))
    bank_ram.io.ren   := true.B
    bank_ram.io.raddr := data_raddr
    cache_data(i)     := bank_ram.io.rdata

    bank_ram.io.wen   := data_wstrb(i).orR
    bank_ram.io.waddr := data_waddr
    bank_ram.io.wdata := data_wdata
    bank_ram.io.wstrb := data_wstrb(i)

    val tag_ram = Module(new SimpleDualPortRam(nset, tagWidth, byteAddressable = false))
    tag_ram.io.ren   := true.B
    tag_ram.io.raddr := tag_raddr
    cache_tag(i)     := tag_ram.io.rdata

    tag_ram.io.wen   := tag_wen(i).orR
    tag_ram.io.wstrb := tag_wen(i)
    tag_ram.io.waddr := bram_replace_addr(9, 4)
    tag_ram.io.wdata := tag_wdata

    tag_compare_valid(i) := cache_tag(i) === io.cpu.tlb.tag && valid(pa_line_addr)(i) && io.cpu.tlb.translation_ok
    cache_data_forward(i) := Mux(
      last_line_addr === M_mem_va(11, 2),
      ((last_wea(i) & last_wdata) | (cache_data(i) & (~last_wea(i)))),
      cache_data(i),
    )

    data_wstrb(i) := Mux(
      tag_compare_valid(i) && M_mem_en && M_mem_write && !io.cpu.tlb.uncached && state === s_idle,
      M_wmask,
      bram_replace_wea(i),
    )

    last_wea(i) := Cat(
      Fill(8, data_wstrb(i)(3)),
      Fill(8, data_wstrb(i)(2)),
      Fill(8, data_wstrb(i)(1)),
      Fill(8, data_wstrb(i)(0)),
    )
  }

  last_line_addr := data_waddr
  last_wdata     := data_wdata

  val write_buffer_axi_busy = RegInit(false.B)

  val ar      = RegInit(0.U.asTypeOf(new AR()))
  val arvalid = RegInit(false.B)
  io.axi.ar.bits <> ar
  io.axi.ar.valid := arvalid
  val rready = RegInit(false.B)
  io.axi.r.ready := rready
  val aw      = RegInit(0.U.asTypeOf(new AW()))
  val awvalid = RegInit(false.B)
  io.axi.aw.bits <> aw
  io.axi.aw.valid := awvalid
  val w      = RegInit(0.U.asTypeOf(new W()))
  val wvalid = RegInit(false.B)
  io.axi.w.bits <> w
  io.axi.w.valid := wvalid

  io.axi.b.ready := true.B

  val current_mmio_write_saved = RegInit(false.B)

  // write buffer
  when(write_buffer_axi_busy) { // To implement SC memory ordering, when store buffer busy, axi is unseable.
    when(io.axi.aw.fire) {
      awvalid := false.B
    }
    when(io.axi.w.fire) {
      wvalid := false.B
      w.last := false.B
    }
    when(io.axi.b.fire) {
      write_buffer_axi_busy := false.B
    }
  }.elsewhen(write_buffer.io.deq.valid) {
    write_buffer.io.deq.ready := write_buffer.io.deq.valid
    when(write_buffer.io.deq.fire) {
      aw.addr := write_buffer.io.deq.bits.addr
      aw.size := Cat(0.U(1.W), write_buffer.io.deq.bits.size)
      w.data  := write_buffer.io.deq.bits.data
      w.strb  := write_buffer.io.deq.bits.strb
    }
    aw.len                := 0.U
    awvalid               := true.B
    w.last                := true.B
    wvalid                := true.B
    write_buffer_axi_busy := true.B
  }

  switch(state) {
    is(s_idle) {
      when(M_mem_en) {
        when(!io.cpu.tlb.translation_ok) {
          when(io.cpu.tlb.tlb1_ok) {
            state := s_save
          }.otherwise {
            state := s_tlb_fill
          }
        }.elsewhen(io.cpu.tlb.uncached) {
          when(M_mem_write) {
            when(write_buffer.io.enq.ready && !current_mmio_write_saved) {
              write_buffer.io.enq.valid := true.B
              write_buffer.io.enq.bits.addr := Mux(
                M_mem_size === 2.U,
                Cat(io.cpu.tlb.pa(31, 2), 0.U(2.W)),
                io.cpu.tlb.pa,
              )
              write_buffer.io.enq.bits.size := M_mem_size
              write_buffer.io.enq.bits.strb := M_wmask
              write_buffer.io.enq.bits.data := M_wdata

              current_mmio_write_saved := true.B
            }
            when(!io.cpu.dstall && !stallM) {
              current_mmio_write_saved := false.B
            }
          }.elsewhen(!(write_buffer.io.deq.valid || write_buffer_axi_busy)) {
            ar.addr := Mux(M_mem_size === 2.U, Cat(io.cpu.tlb.pa(31, 2), 0.U(2.W)), io.cpu.tlb.pa)
            ar.len  := 0.U
            ar.size := Cat(0.U(1.W), M_mem_size)
            arvalid := true.B
            state   := s_uncached
            rready  := true.B
          } // when store buffer busy, read will stop at s_idle but stall pipeline.
        }.otherwise {
          when(!cache_hit) {
            state                   := s_replace
            axi_wcnt                := 0.U
            bram_replace_addr       := Cat(pa_line_addr, 0.U(4.W))
            bram_read_ready_addr    := Cat(pa_line_addr, 0.U(4.W))
            bram_replace_write_addr := Cat(pa_line_addr, 0.U(4.W))
            bram_replace_cnt        := 0.U
            bram_use_replace_addr   := true.B
            bram_data_valid         := 0.U
            replace_writeback       := dirty(pa_line_addr)(lru(pa_line_addr))
          }.otherwise {
            when(!io.cpu.dstall) {
              // update lru and mark dirty
              lru(pa_line_addr) := ~d_cache_sel
              when(M_mem_write) {
                dirty(pa_line_addr)(d_cache_sel) := true.B
              }
              when(stallM) {
                saved_rdata := cache_data_forward(d_cache_sel)
                state       := s_save
              }
            }
          }
        }
      }.elsewhen(M_fence_d) {
        when(dirty(fence_line_addr).contains(true.B)) {
          when(!(write_buffer.io.deq.valid || write_buffer_axi_busy)) {
            state                 := s_writeback
            axi_wcnt              := 0.U
            bram_replace_addr     := Cat(M_fence_addr(11, 6), 0.U(4.W))
            bram_read_ready_addr  := Cat(M_fence_addr(11, 6), 0.U(4.W))
            bram_replace_cnt      := 0.U
            bram_use_replace_addr := true.B
            bram_data_valid       := 0.U
          }
        }.otherwise {
          when(valid(fence_line_addr).contains(true.B)) {
            valid(fence_line_addr)(0) := false.B
            valid(fence_line_addr)(1) := false.B
          }
          state := s_save
        }
      }
    }
    is(s_tlb_fill) {
      when(io.cpu.tlb.hit) {
        state := s_idle
      }.otherwise {
        state := s_save
      }
    }
    is(s_uncached) {
      when(arvalid && io.axi.ar.ready) {
        arvalid := false.B
      }
      when(io.axi.r.valid) {
        saved_rdata := io.axi.r.bits.data
        state       := s_save
      }
    }
    is(s_writeback) { // CACHE Instruction
      when(fence_working) {
        when(bram_replace_addr(3, 0) =/= 15.U) {
          bram_replace_addr := bram_replace_addr + 1.U
        }
        bram_read_ready_addr                      := bram_replace_addr
        bram_r_buffer(bram_read_ready_addr(3, 0)) := cache_data(fence_way)
        when(!aw_handshake) {
          aw.addr      := Cat(cache_tag(fence_way), fence_line_addr, 0.U(6.W))
          aw.len       := 15.U
          aw.size      := 2.U(3.W)
          awvalid      := true.B
          w.data       := cache_data(fence_way)
          w.strb       := 15.U
          w.last       := false.B
          wvalid       := true.B
          aw_handshake := true.B
        }
        when(io.axi.aw.fire) {
          awvalid := false.B
        }
        when(io.axi.w.fire) {
          when(w.last) {
            wvalid := false.B
          }.otherwise {
            w.data := Mux(
              ((axi_wcnt + 1.U) === bram_read_ready_addr(3, 0)),
              cache_data(fence_way),
              bram_r_buffer(axi_wcnt + 1.U),
            )
            axi_wcnt := axi_wcnt + 1.U
            when(axi_wcnt + 1.U === 15.U) {
              w.last := true.B
            }
          }
        }
        when(io.axi.b.valid) {
          dirty(fence_line_addr)(fence_way) := false.B
          fence_working                     := false.B
          bram_use_replace_addr             := false.B
          state                             := s_idle
        }
      }.otherwise {
        aw_handshake      := false.B
        fence_working     := true.B
        bram_replace_addr := bram_replace_addr + 1.U
      }
    }
    is(s_replace) {
      when(!(write_buffer.io.deq.valid || write_buffer_axi_busy)) {
        when(replace_working) {
          when(replace_writeback) {
            when(bram_replace_addr(3, 0) =/= 15.U) {
              bram_replace_addr := bram_replace_addr + 1.U
            }
            bram_read_ready_addr                      := bram_replace_addr
            bram_r_buffer(bram_read_ready_addr(3, 0)) := cache_data(lru(pa_line_addr))
            when(!aw_handshake) {
              aw.addr      := Cat(cache_tag(lru(pa_line_addr)), pa_line_addr, 0.U(6.W))
              aw.len       := 15.U
              aw.size      := 2.U(3.W)
              awvalid      := true.B
              w.data       := cache_data(lru(pa_line_addr))
              w.strb       := 15.U
              w.last       := false.B
              wvalid       := true.B
              aw_handshake := true.B
            }
            when(io.axi.aw.fire) {
              awvalid := false.B
            }
            when(io.axi.w.fire) {
              when(w.last) {
                wvalid := false.B
              }.otherwise {
                w.data := Mux(
                  ((axi_wcnt + 1.U) === bram_read_ready_addr(3, 0)),
                  cache_data(lru(pa_line_addr)),
                  bram_r_buffer(axi_wcnt + 1.U),
                )
                axi_wcnt := axi_wcnt + 1.U
                when(axi_wcnt + 1.U === 15.U) {
                  w.last := true.B
                }
              }
            }
            when(io.axi.b.valid) {
              dirty(pa_line_addr)(lru(pa_line_addr)) := false.B
              replace_writeback                      := false.B
            }
          }
          // at here, cache line is writeable from axi read.
          when(!ar_handshake) {
            ar.addr                             := Cat(io.cpu.tlb.pa(31, 6), 0.U(6.W))
            ar.len                              := 15.U
            ar.size                             := 2.U(3.W)
            arvalid                             := true.B
            rready                              := true.B
            ar_handshake                        := true.B
            bram_replace_wea(lru(pa_line_addr)) := 15.U
            tag_wen(lru(pa_line_addr))          := true.B
            tag_wdata                           := io.cpu.tlb.pa(31, 12)
          }
          when(io.axi.ar.fire) {
            tag_wen(lru(pa_line_addr)) := false.B
            arvalid                    := false.B
          }
          when(io.axi.r.fire) {
            when(io.axi.r.bits.last) {
              rready                              := false.B
              bram_replace_wea(lru(pa_line_addr)) := 0.U
            }.otherwise {
              bram_replace_write_addr := bram_replace_write_addr + 1.U
            }
          }
          when(
            (!replace_writeback || (io.axi.b.valid)) && ((ar_handshake && io.axi.r.valid && io.axi.r.bits.last) || (ar_handshake && !rready)),
          ) {
            bram_use_replace_addr                  := 0.U
            valid(pa_line_addr)(lru(pa_line_addr)) := true.B
          }
          when(!bram_use_replace_addr) {
            replace_working := false.B
            state           := s_idle
          }
        }.otherwise {
          ar_handshake      := false.B
          aw_handshake      := false.B
          replace_working   := true.B
          bram_replace_addr := bram_replace_addr + 1.U
          // transfer (addr + 1), and receive (addr).
        }
      }
    }
    is(s_save) {
      when(!io.cpu.dstall && !stallM) {
        state := s_idle
      }
    }
  }
}
