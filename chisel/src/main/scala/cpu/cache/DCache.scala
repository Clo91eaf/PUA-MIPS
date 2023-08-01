// * Cache 设计借鉴了nscscc2021 cqu的cdim * //
package cache

import chisel3._
import chisel3.util._
import memory._
import cpu.defines._
import cpu.CpuConfig

class WriteBufferUnit extends Bundle {
  val data = UInt(32.W)
  val addr = UInt(32.W)
  val strb = UInt(4.W)
  val size = UInt(2.W)
}

class DCache(
    cacheConfig: CacheConfig,
    writeBufferDepth: Int = 16,
)(implicit cpuConfig: CpuConfig)
    extends Module {
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
  // * fsm * //
  val s_idle :: s_tlb_fill :: s_uncached :: s_writeback :: s_replace :: s_save :: Nil = Enum(6)
  val state                                                                           = RegInit(s_idle)

  io.cpu.tlb.dcache_is_idle     := state === s_idle
  io.cpu.tlb.dcahce_is_tlb_fill := state === s_tlb_fill
  io.cpu.tlb.dcache_is_save     := state === s_save

  // * valid dirty * //
  val valid = RegInit(VecInit(Seq.fill(nset)(VecInit(Seq.fill(nway)(false.B)))))
  val dirty = RegInit(VecInit(Seq.fill(nset)(VecInit(Seq.fill(nway)(false.B)))))
  val lru   = RegInit(VecInit(Seq.fill(nset)(0.U(1.W))))

  val should_next_addr = (state === s_idle) || (state === s_save)

  val write_buffer = Module(new Queue(new WriteBufferUnit(), writeBufferDepth))
  write_buffer.io.enq.valid := false.B
  write_buffer.io.enq.bits  := 0.U.asTypeOf(new WriteBufferUnit())
  write_buffer.io.deq.ready := false.B

  val axi_cnt         = Counter(16)
  val read_ready_addr = RegInit(0.U(10.W))

  // replace and fence control
  val replace = RegInit(0.U.asTypeOf(new Bundle {
    val use        = Bool()
    val addr       = UInt(10.W)
    val write_addr = UInt(10.W)
    val wstrb      = Vec(nway, UInt(4.W))
    val working    = Bool()
    val writeback  = Bool()
  }))

  val fset = io.cpu.fence_addr(11, 6)
  val fence = RegInit(0.U.asTypeOf(new Bundle {
    val working = Bool()
  }))

  val read_buffer  = RegInit(VecInit(Seq.fill(16)(0.U(32.W))))
  val ar_handshake = RegInit(false.B)
  val aw_handshake = RegInit(false.B)

  val data_raddr = Mux(replace.use, replace.addr, Mux(should_next_addr, io.cpu.execute_addr(11, 2), io.cpu.addr(11, 2)))
  val data_wstrb = Wire(Vec(nway, UInt(4.W)))
  val data_waddr = Mux(replace.use, replace.write_addr, io.cpu.addr(11, 2))
  val data_wdata = Mux(state === s_replace, io.axi.r.bits.data, io.cpu.wdata)

  val tag_raddr =
    Mux(replace.use, replace.addr(9, 4), Mux(should_next_addr, io.cpu.execute_addr(11, 6), io.cpu.addr(11, 6)))
  val tag_wstrb = RegInit(VecInit(Seq.fill(nway)(false.B)))
  val tag_wdata = RegInit(0.U(tagWidth.W))

  val data = Wire(Vec(nway, UInt(32.W)))
  val tag  = Wire(Vec(nway, UInt(tagWidth.W)))

  val tag_compare_valid = Wire(Vec(nway, Bool()))
  val cache_hit         = tag_compare_valid.contains(true.B)

  val mmio_read_stall  = io.cpu.tlb.uncached && !io.cpu.wen.orR
  val mmio_write_stall = io.cpu.tlb.uncached && io.cpu.wen.orR && !write_buffer.io.enq.ready
  val cached_stall     = !io.cpu.tlb.uncached && !cache_hit

  val sel = tag_compare_valid(1)

  // * physical set * //
  val pset = io.cpu.addr(11, 6)

  io.cpu.dcache_stall := Mux(
    state === s_idle,
    Mux(io.cpu.en, (cached_stall || mmio_read_stall || mmio_write_stall || !io.cpu.tlb.translation_ok), io.cpu.fence),
    state =/= s_save,
  )

  val saved_rdata = RegInit(0.U(32.W))

  // forward last stored data in data bram
  val last_waddr         = RegNext(data_waddr)
  val last_wstrb         = RegInit(VecInit(Seq.fill(nway)(0.U(32.W))))
  val last_wdata         = RegNext(data_wdata)
  val cache_data_forward = Wire(Vec(nway, UInt(32.W)))

  io.cpu.rdata := Mux(state === s_save, saved_rdata, cache_data_forward(sel))

  // bank tagv ram
  for { i <- 0 until nway } {
    val bank_ram = Module(new SimpleDualPortRam(nset * nbank, bankWidthBits, byteAddressable = true))
    bank_ram.io.ren   := true.B
    bank_ram.io.raddr := data_raddr
    data(i)           := bank_ram.io.rdata

    bank_ram.io.wen   := data_wstrb(i).orR
    bank_ram.io.waddr := data_waddr
    bank_ram.io.wdata := data_wdata
    bank_ram.io.wstrb := data_wstrb(i)

    val tag_ram = Module(new SimpleDualPortRam(nset, tagWidth, byteAddressable = false))
    tag_ram.io.ren   := true.B
    tag_ram.io.raddr := tag_raddr
    tag(i)           := tag_ram.io.rdata

    tag_ram.io.wen   := tag_wstrb(i).orR
    tag_ram.io.waddr := replace.addr(9, 4)
    tag_ram.io.wdata := tag_wdata
    tag_ram.io.wstrb := tag_wstrb(i)

    tag_compare_valid(i) := tag(i) === io.cpu.tlb.tag && valid(pset)(i) && io.cpu.tlb.translation_ok
    cache_data_forward(i) := Mux(
      last_waddr === io.cpu.addr(11, 2),
      ((last_wstrb(i) & last_wdata) | (data(i) & (~last_wstrb(i)))),
      data(i),
    )

    data_wstrb(i) := Mux(
      tag_compare_valid(i) && io.cpu.en && io.cpu.wen.orR && !io.cpu.tlb.uncached && state === s_idle,
      io.cpu.wen,
      replace.wstrb(i),
    )

    last_wstrb(i) := Cat(
      Fill(8, data_wstrb(i)(3)),
      Fill(8, data_wstrb(i)(2)),
      Fill(8, data_wstrb(i)(1)),
      Fill(8, data_wstrb(i)(0)),
    )
  }
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
      when(io.cpu.en) {
        when(!io.cpu.tlb.translation_ok) {
          when(io.cpu.tlb.tlb1_ok) {
            state := s_save
          }.otherwise {
            state := s_tlb_fill
          }
        }.elsewhen(io.cpu.tlb.uncached) {
          when(io.cpu.wen.orR) {
            when(write_buffer.io.enq.ready && !current_mmio_write_saved) {
              write_buffer.io.enq.valid := true.B
              write_buffer.io.enq.bits.addr := Mux(
                io.cpu.rlen === 2.U,
                Cat(io.cpu.tlb.pa(31, 2), 0.U(2.W)),
                io.cpu.tlb.pa,
              )
              write_buffer.io.enq.bits.size := io.cpu.rlen
              write_buffer.io.enq.bits.strb := io.cpu.wen
              write_buffer.io.enq.bits.data := io.cpu.wdata

              current_mmio_write_saved := true.B
            }
            when(!io.cpu.dcache_stall && !io.cpu.cpu_stall) {
              current_mmio_write_saved := false.B
            }
          }.elsewhen(!(write_buffer.io.deq.valid || write_buffer_axi_busy)) {
            ar.addr := Mux(io.cpu.rlen === 2.U, Cat(io.cpu.tlb.pa(31, 2), 0.U(2.W)), io.cpu.tlb.pa)
            ar.len  := 0.U
            ar.size := Cat(0.U(1.W), io.cpu.rlen)
            arvalid := true.B
            state   := s_uncached
            rready  := true.B
          } // when store buffer busy, read will stop at s_idle but stall pipeline.
        }.otherwise {
          when(!cache_hit) {
            state := s_replace
            axi_cnt.reset()
            replace.addr       := Cat(pset, 0.U(4.W))
            read_ready_addr    := Cat(pset, 0.U(4.W))
            replace.write_addr := Cat(pset, 0.U(4.W))
            replace.use        := true.B
            replace.writeback  := dirty(pset)(lru(pset))
          }.otherwise {
            when(!io.cpu.dcache_stall) {
              // update lru and mark dirty
              lru(pset) := ~sel
              when(io.cpu.wen.orR) {
                dirty(pset)(sel) := true.B
              }
              when(io.cpu.cpu_stall) {
                saved_rdata := cache_data_forward(sel)
                state       := s_save
              }
            }
          }
        }
      }.elsewhen(io.cpu.fence) {
        when(dirty(fset).contains(true.B)) {
          when(!(write_buffer.io.deq.valid || write_buffer_axi_busy)) {
            state := s_writeback
            axi_cnt.reset()
            replace.addr    := Cat(fset, 0.U(4.W))
            read_ready_addr := Cat(fset, 0.U(4.W))
            replace.use     := true.B
          }
        }.otherwise {
          when(valid(fset).contains(true.B)) {
            valid(fset)(0) := false.B
            valid(fset)(1) := false.B
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
    is(s_writeback) {
      when(fence.working) {
        when(replace.addr(3, 0) =/= 15.U) {
          replace.addr := replace.addr + 1.U
        }
        read_ready_addr                    := replace.addr
        read_buffer(read_ready_addr(3, 0)) := data(dirty(fset)(1))
        when(!aw_handshake) {
          aw.addr      := Cat(tag(dirty(fset)(1)), fset, 0.U(6.W))
          aw.len       := 15.U
          aw.size      := 2.U(3.W)
          awvalid      := true.B
          w.data       := data(dirty(fset)(1))
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
              ((axi_cnt.value + 1.U) === read_ready_addr(3, 0)),
              data(dirty(fset)(1)),
              read_buffer(axi_cnt.value + 1.U),
            )
            axi_cnt.inc()
            when(axi_cnt.value + 1.U === 15.U) {
              w.last := true.B
            }
          }
        }
        when(io.axi.b.valid) {
          dirty(fset)(dirty(fset)(1)) := false.B
          fence.working               := false.B
          replace.use                 := false.B
          state                       := s_idle
        }
      }.otherwise {
        aw_handshake  := false.B
        fence.working := true.B
        replace.addr  := replace.addr + 1.U
      }
    }
    is(s_replace) {
      when(!(write_buffer.io.deq.valid || write_buffer_axi_busy)) {
        when(replace.working) {
          when(replace.writeback) {
            when(replace.addr(3, 0) =/= 15.U) {
              replace.addr := replace.addr + 1.U
            }
            read_ready_addr                    := replace.addr
            read_buffer(read_ready_addr(3, 0)) := data(lru(pset))
            when(!aw_handshake) {
              aw.addr      := Cat(tag(lru(pset)), pset, 0.U(6.W))
              aw.len       := 15.U
              aw.size      := 2.U(3.W)
              awvalid      := true.B
              w.data       := data(lru(pset))
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
                  ((axi_cnt.value + 1.U) === read_ready_addr(3, 0)),
                  data(lru(pset)),
                  read_buffer(axi_cnt.value + 1.U),
                )
                axi_cnt.inc()
                when(axi_cnt.value + 1.U === 15.U) {
                  w.last := true.B
                }
              }
            }
            when(io.axi.b.valid) {
              dirty(pset)(lru(pset)) := false.B
              replace.writeback      := false.B
            }
          }
          // at here, cache line is writeable from axi read.
          when(!ar_handshake) {
            ar.addr                  := Cat(io.cpu.tlb.pa(31, 6), 0.U(6.W))
            ar.len                   := 15.U
            ar.size                  := 2.U(3.W)
            arvalid                  := true.B
            rready                   := true.B
            ar_handshake             := true.B
            replace.wstrb(lru(pset)) := 15.U
            tag_wstrb(lru(pset))     := true.B
            tag_wdata                := io.cpu.tlb.pa(31, 12)
          }
          when(io.axi.ar.fire) {
            tag_wstrb(lru(pset)) := false.B
            arvalid              := false.B
          }
          when(io.axi.r.fire) {
            when(io.axi.r.bits.last) {
              rready                   := false.B
              replace.wstrb(lru(pset)) := 0.U
            }.otherwise {
              replace.write_addr := replace.write_addr + 1.U
            }
          }
          when(
            (!replace.writeback || io.axi.b.valid) && ((ar_handshake && io.axi.r.valid && io.axi.r.bits.last) || (ar_handshake && !rready)),
          ) {
            replace.use            := 0.U
            valid(pset)(lru(pset)) := true.B
          }
          when(!replace.use) {
            replace.working := false.B
            state           := s_idle
          }
        }.otherwise {
          ar_handshake    := false.B
          aw_handshake    := false.B
          replace.working := true.B
          replace.addr    := replace.addr + 1.U
        }
      }
    }
    is(s_save) {
      when(!io.cpu.dcache_stall && !io.cpu.cpu_stall) {
        state := s_idle
      }
    }
  }
}
