// * Cache 设计借鉴了nscscc2021 cqu的cdim * //
package cache

import chisel3._
import chisel3.util._
import memory._
import cpu.defines._
import cpu.CpuConfig

class ICache(cacheConfig: CacheConfig)(implicit cpuConfig: CpuConfig) extends Module {
  implicit val config      = cacheConfig
  val nway: Int            = cacheConfig.nway
  val nset: Int            = cacheConfig.nset
  val nbank: Int           = cacheConfig.nbank
  val ninst: Int           = cacheConfig.ninst
  val bankOffsetWidth: Int = cacheConfig.bankOffsetWidth
  val bankWidth: Int       = cacheConfig.bankWidth
  val tagWidth: Int        = cacheConfig.tagWidth
  val indexWidth: Int      = cacheConfig.indexWidth
  val offsetWidth: Int     = cacheConfig.offsetWidth
  val io = IO(new Bundle {
    val cpu = Flipped(new Cache_ICache(ninst))
    val axi = new ICache_AXIInterface()
  })
  require(isPow2(ninst), "ninst must be power of 2")
  // * addr organization * //
  // ======================================
  // |        tag         |  index |offset|
  // |31                12|11     6|5    0|
  // ======================================
  // |         offset           |
  // | bank index | bank offset |
  // | 5        4 | 3         2 |
  // ============================

  val tlb_fill = RegInit(false.B)
  // * fsm * //
  val s_idle :: s_uncached :: s_replace :: s_save :: Nil = Enum(4)
  val state                                              = RegInit(s_idle)

  // * nway * nset * //
  // * 128 bit for 4 inst * //
  // =========================================================
  // | valid | tag |  bank 0 | bank 1  |  bank 2 | bank 3 |
  // | 1     | 20  |   128   |   128   |   128   |  128   |
  // =========================================================
  // |                bank               |
  // | inst 0 | inst 1 | inst 2 | inst 3 |
  // |   32   |   32   |   32   |   32   |
  // =====================================
  val valid = RegInit(VecInit(Seq.fill(nset * nbank)(VecInit(Seq.fill(ninst)(false.B)))))

  val data = Wire(Vec(nway, Vec(ninst, UInt(32.W))))
  val tag  = RegInit(VecInit(Seq.fill(nway)(UInt(tagWidth.W))))

  // * should choose next addr * //
  val should_next_addr = (state === s_idle && !tlb_fill) || (state === s_save)

  val data_raddr = io.cpu.addr(should_next_addr)(indexWidth + offsetWidth - 1, bankOffsetWidth)
  val data_wstrb = RegInit(VecInit(Seq.fill(nway)(VecInit(Seq.fill(ninst)(0.U(4.W))))))

  val tag_raddr = io.cpu.addr(should_next_addr)(indexWidth + offsetWidth - 1, offsetWidth)
  val tag_wstrb = RegInit(VecInit(Seq.fill(nway)(false.B)))
  val tag_wdata = RegInit(0.U(tagWidth.W))

  // * lru * //
  val lru = RegInit(VecInit(Seq.fill(nset * nbank)(false.B)))

  // * itlb * //
  when(tlb_fill) { tlb_fill := false.B }
  io.cpu.tlb.fill           := tlb_fill
  io.cpu.tlb.icache_is_save := (state === s_save)

  // * fence * //
  val fence_index = io.cpu.fence_addr(indexWidth + offsetWidth - 1, offsetWidth)
  when(io.cpu.fence && !io.cpu.icache_stall && !io.cpu.cpu_stall) {
    valid(fence_index) := VecInit(Seq.fill(ninst)(false.B))
  }

  // * replace set * //
  val rset = RegInit(0.U(6.W))

  // * virtual set * //
  val vset = io.cpu.addr(0)(indexWidth + offsetWidth - 1, offsetWidth)

  // * cache hit * //
  val tag_compare_valid   = VecInit(Seq.tabulate(nway)(i => tag(i) === io.cpu.tlb.tag && valid(vset)(i)))
  val cache_hit           = tag_compare_valid.contains(true.B)
  val cache_hit_available = cache_hit && io.cpu.tlb.translation_ok && !io.cpu.tlb.uncached
  val sel                 = tag_compare_valid(1)

  val bank_offset = io.cpu.addr(0)(log2Ceil(ninst) + 1, 2)
  val inst       = VecInit(Seq.tabulate(ninst)(i => Mux(i.U <= (3.U - bank_offset), data(sel)(i.U + bank_offset), 0.U)))
  val inst_valid = VecInit(Seq.tabulate(ninst)(i => cache_hit_available && i.U <= (3.U - bank_offset)))

  val saved = RegInit(VecInit(Seq.fill(ninst)(0.U.asTypeOf(new Bundle {
    val inst  = UInt(32.W)
    val valid = Bool()
  }))))

  val axi_cnt = Counter(cacheConfig.burstSize)

  // bank tag ram
  for { i <- 0 until nway; j <- 0 until ninst } {
    val bank = Module(new SimpleDualPortRam(nset * nbank, 32, byteAddressable = true))
    bank.io.ren   := true.B
    bank.io.raddr := data_raddr
    data(i)(j)    := bank.io.rdata

    bank.io.wen   := data_wstrb(i)(j).orR
    bank.io.waddr := Cat(rset, axi_cnt.value(log2Ceil(cacheConfig.burstSize) - 1, log2Ceil(ninst)))
    bank.io.wdata := Mux(j.U === axi_cnt.value(log2Ceil(ninst) - 1, 0), io.axi.r.bits.data, 0.U)
    bank.io.wstrb := data_wstrb(i)(j)
  }

  for { i <- 0 until ninst } {
    io.cpu.inst_valid(i) := Mux(state === s_idle && !tlb_fill, inst_valid(i), saved(i).valid) && io.cpu.req
    io.cpu.inst(i)       := Mux(state === s_idle && !tlb_fill, inst(i), saved(i).inst)
  }

  for { i <- 0 until nway } {
    val tag_bram = Module(new LUTRam(nset, tagWidth))
    tag_bram.io.raddr := tag_raddr
    tag(i)            := tag_bram.io.rdata

    tag_bram.io.wen   := tag_wstrb(i)
    tag_bram.io.waddr := rset
    tag_bram.io.wdata := tag_wdata
  }

  io.cpu.icache_stall := Mux(state === s_idle && !tlb_fill, (!cache_hit_available && io.cpu.req), state =/= s_save)

  val ar      = RegInit(0.U.asTypeOf(new AR()))
  val arvalid = RegInit(false.B)
  ar <> io.axi.ar.bits
  arvalid <> io.axi.ar.valid

  val r      = RegInit(0.U.asTypeOf(new R()))
  val rready = RegInit(false.B)
  r <> io.axi.r.bits
  rready <> io.axi.r.ready

  when(tlb_fill === true.B) {
    tlb_fill := false.B
  }

  switch(state) {
    is(s_idle) {
      when(tlb_fill) {
        when(!io.cpu.tlb.hit) {
          state          := s_save
          saved(0).inst  := 0.U
          saved(0).valid := true.B
        }
      }.elsewhen(io.cpu.req) {
        when(!io.cpu.tlb.translation_ok) {
          tlb_fill := true.B
        }.elsewhen(io.cpu.tlb.uncached) {
          state   := s_uncached
          ar.addr := io.cpu.tlb.pa
          ar.len  := 0.U(log2Ceil((nbank * bankWidth) / 4).W)
          ar.size := 2.U(bankOffsetWidth.W)
          arvalid := true.B
        }.elsewhen(!cache_hit) {
          state   := s_replace
          ar.addr := Cat(io.cpu.tlb.pa(31, 6), 0.U(6.W))
          ar.len  := 15.U(log2Ceil((nbank * bankWidth) / 4).W)
          ar.size := 2.U(bankOffsetWidth.W)
          arvalid := true.B

          rset := vset
          (0 until ninst).foreach(i => data_wstrb(lru(vset))(i) := Mux(i.U === 0.U, 0xf.U, 0x0.U))
          tag_wstrb(lru(vset))   := true.B
          tag_wdata              := io.cpu.tlb.tag
          valid(vset)(lru(vset)) := true.B
          axi_cnt.reset()
        }.elsewhen(!io.cpu.icache_stall) {
          lru(vset) := ~sel
          when(io.cpu.cpu_stall) {
            state := s_save
            (1 until ninst).foreach(i => saved(i).inst := data(sel)(i))
            (0 until ninst).foreach(i => saved(i).valid := inst_valid(i))
          }
        }
      }
    }
    is(s_uncached) {
      when(io.axi.ar.valid) {
        when(io.axi.ar.ready) {
          arvalid := false.B
          rready  := true.B
        }
      }.elsewhen(io.axi.r.fire) {
        // * uncached not support burst transport * //
        state          := s_save
        saved(0).inst  := io.axi.r.bits.data
        saved(0).valid := true.B
        rready         := false.B
      }
    }
    is(s_replace) {
      when(io.axi.ar.valid) {
        when(io.axi.ar.ready) {
          arvalid := false.B
          rready  := true.B
        }
      }.elsewhen(io.axi.r.fire) {
        // * burst transport * //
        when(!io.axi.r.bits.last) {
          axi_cnt.inc()
          data_wstrb(lru(vset))(0) := data_wstrb(lru(vset))(ninst - 1)
          (1 until ninst).foreach(i => data_wstrb(lru(vset))(i) := data_wstrb(lru(vset))(i - 1))
        }.otherwise {
          rready                := false.B
          data_wstrb(lru(vset)) := 0.U.asTypeOf(Vec(ninst, UInt(4.W)))
          tag_wstrb(lru(vset))  := false.B
        }
      }.elsewhen(!io.axi.r.ready) {
        state := s_idle
      }
    }
    is(s_save) {
      when(!io.cpu.cpu_stall && !io.cpu.icache_stall) {
        state := s_idle
        (0 until ninst).foreach(i => saved(i).valid := false.B)
      }
    }
  }
}
