// * Cache 设计借鉴了nscscc2021 cqu的cdim * //
package cache

import chisel3._
import chisel3.util._
import memoryBanks.metaBanks._
import memoryBanks.SimpleDualPortRam
import cpu.defines._
import cpu.mmu._

class ICache(cacheConfig: CacheConfig) extends Module {
  implicit val config      = cacheConfig
  val nway: Int            = cacheConfig.nway
  val nset: Int            = cacheConfig.nset
  val nbank: Int           = cacheConfig.nbank
  val ninst: Int           = 4
  val bankOffsetWidth: Int = cacheConfig.bankOffsetWidth
  val bankWidth: Int       = cacheConfig.bankWidth
  val bankWidthBits: Int   = cacheConfig.bankWidthBits
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
  // | 5        4 | 3         0 |
  // ============================

  // * fsm * //
  val s_idle :: s_tlb_fill :: s_uncached :: s_replace :: s_save :: Nil = Enum(5)
  val state                                                            = RegInit(s_idle)

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
  val tag  = Wire(Vec(nway, UInt(tagWidth.W)))

  // * should choose next addr * //
  val should_next_addr = (state === s_idle) || (state === s_save)

  val data_raddr = io.cpu.addr(should_next_addr)(indexWidth + offsetWidth - 1, bankOffsetWidth)
  val data_wstrb = RegInit(VecInit(Seq.fill(nway)(VecInit(Seq.fill(ninst)(0.U(4.W))))))

  val tag_raddr = io.cpu.addr(should_next_addr)(indexWidth + offsetWidth - 1, offsetWidth)
  val tag_wstrb = RegInit(VecInit(Seq.fill(nway)(false.B)))
  val tag_wdata = RegInit(0.U(tagWidth.W))

  // * lru * //
  val lru = RegInit(VecInit(Seq.fill(nset * nbank)(false.B)))

  // * itlb * //
  val l1tlb = Module(new TlbL1I())
  l1tlb.io.addr := io.cpu.addr(0)
  l1tlb.io.tlb1 <> io.cpu.tlb1
  l1tlb.io.tlb2 <> io.cpu.tlb2
  l1tlb.io.icache_is_tlb_fill := (state === s_tlb_fill)
  l1tlb.io.icache_is_save     := (state === s_save)
  l1tlb.io.fence              := io.cpu.fence.tlb
  l1tlb.io.cpu_stall          := io.cpu.cpu_stall
  l1tlb.io.icache_stall       := io.cpu.icache_stall

  // * fence * //
  val fence_index = io.cpu.fence.addr(indexWidth + offsetWidth - 1, offsetWidth)
  when(io.cpu.fence.value && !io.cpu.icache_stall && !io.cpu.cpu_stall) {
    valid(fence_index) := VecInit(Seq.fill(ninst)(false.B))
  }

  // * replace set * //
  val rset = RegInit(0.U(6.W))

  // * virtual set * //
  val vset = io.cpu.addr(0)(indexWidth + offsetWidth - 1, offsetWidth)

  // * cache hit * //
  val tag_compare_valid   = VecInit(Seq.tabulate(nway)(i => tag(i) === l1tlb.io.tag && valid(vset)(i)))
  val cache_hit           = tag_compare_valid.contains(true.B)
  val cache_hit_available = cache_hit && l1tlb.io.translation_ok && !l1tlb.io.uncached

  val inst_valid = Wire(Vec(ninst, Bool()))
  inst_valid(0) := cache_hit_available
  (1 until ninst).foreach(i => inst_valid(i) := cache_hit_available && !io.cpu.addr(0)(log2Ceil(ninst) + 1))

  val sel = tag_compare_valid(1)

  val inst = VecInit(Seq.tabulate(ninst)(i => Mux(!io.cpu.addr(0)(2), data(sel)(0), data(sel)(i))))

  val saved = RegInit(VecInit(Seq.fill(ninst)(0.U.asTypeOf(new Bundle {
    val inst  = UInt(32.W)
    val valid = Bool()
  }))))

  val axi_cnt = Counter(32)

  // bank tag ram
  for { i <- 0 until nway; j <- 0 until ninst } {
    val bank = Module(new SimpleDualPortRam(nset * nbank, 32, byteAddressable = true))
    bank.io.ren   := true.B
    bank.io.raddr := data_raddr
    data(i)(j)    := bank.io.rdata

    bank.io.wen   := data_wstrb(i)(j).orR
    bank.io.waddr := Cat(rset, axi_cnt.value(3, log2Ceil(ninst)))
    bank.io.wdata := Mux(j.U === axi_cnt.value(log2Ceil(ninst) - 1, 0), io.axi.r.bits.data, 0.U)
    bank.io.wstrb := data_wstrb(i)(j)
  }

  for { i <- 0 until ninst } {
    io.cpu.inst_valid(i) := Mux(state === s_idle, inst_valid(i), saved(i).valid) && io.cpu.req
    io.cpu.inst(i)       := Mux(state === s_idle, inst(i), saved(i).inst)
  }

  for { i <- 0 until nway } {
    val tag_bram = Module(new SimpleDualPortRam(nset, tagWidth, false))
    tag_bram.io.ren   := true.B
    tag_bram.io.raddr := tag_raddr
    tag(i)            := tag_bram.io.rdata

    tag_bram.io.wen   := tag_wstrb(i).orR
    tag_bram.io.waddr := rset
    tag_bram.io.wdata := tag_wdata
    tag_bram.io.wstrb := tag_wstrb(i)
  }

  io.cpu.icache_stall := Mux(state === s_idle, (!cache_hit_available && io.cpu.req), state =/= s_save)

  val ar      = RegInit(0.U.asTypeOf(new AR()))
  val arvalid = RegInit(false.B)
  ar <> io.axi.ar.bits
  arvalid <> io.axi.ar.valid

  val r      = RegInit(0.U.asTypeOf(new R()))
  val rready = RegInit(false.B)
  r <> io.axi.r.bits
  rready <> io.axi.r.ready

  switch(state) {
    is(s_idle) {
      when(io.cpu.req) {
        when(!l1tlb.io.translation_ok) {
          state := s_tlb_fill
        }.elsewhen(l1tlb.io.uncached) {
          state   := s_uncached
          ar.addr := l1tlb.io.pa
          // * 4 inst per bank and 4 bank per set * //
          ar.len  := 0.U(log2Ceil((nbank * bankWidth) / 4).W)
          ar.size := 2.U(bankOffsetWidth.W)
          arvalid := true.B
        }.elsewhen(!cache_hit) {
          state   := s_replace
          ar.addr := Cat(l1tlb.io.pa(31, 6), 0.U(6.W))
          ar.len  := 15.U(log2Ceil((nbank * bankWidth) / 4).W)
          ar.size := 2.U(bankOffsetWidth.W)
          arvalid := true.B

          rset                     := vset
          data_wstrb(lru(vset))(0) := 0xf.U
          data_wstrb(lru(vset))(1) := 0x0.U
          data_wstrb(lru(vset))(2) := 0x0.U
          data_wstrb(lru(vset))(3) := 0x0.U
          tag_wstrb(lru(vset))     := true.B
          tag_wdata                := l1tlb.io.tag
          valid(vset)(lru(vset))   := true.B
          axi_cnt.reset()
        }.elsewhen(!io.cpu.icache_stall) {
          lru(vset) := ~sel
          when(io.cpu.cpu_stall) {
            state         := s_save
            saved(1).inst := data(1)(0)
            (0 until ninst).foreach(i => saved(i).valid := inst_valid(i))
          }
        }
      }
    }
    is(s_tlb_fill) {
      when(l1tlb.io.hit) {
        state := s_idle
      }.otherwise {
        state          := s_save
        saved(0).inst  := 0.U
        saved(0).valid := true.B
      }
    }
    is(s_uncached) {
      when(io.axi.ar.valid) {
        when(io.axi.ar.ready) {
          arvalid := false.B
          rready  := true.B
        }
      }.elsewhen(io.axi.r.fire) {
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
        when(!io.axi.r.bits.last) {
          axi_cnt.inc()
          (0 until ninst).foreach(i => data_wstrb(lru(vset))(i) := ~data_wstrb(lru(vset))(i))
        }.otherwise {
          rready                := false.B
          data_wstrb(lru(vset)) := 0.U.asTypeOf(Vec(ninst, UInt(4.W)))
          tag_wstrb(lru(vset))  := 0.U
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
