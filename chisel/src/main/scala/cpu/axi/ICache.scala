// * Cache 设计借鉴了nscscc2021 cqu的cdim * //
package cache

import chisel3._
import chisel3.util._
import memoryBanks.metaBanks._
import memoryBanks.SimpleDualPortRam

class ICache(cacheConfig: CacheConfig) extends Module {
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
    val cpu = Flipped(new Cache_ICache())
    val axi = new ICache_AXIInterface()
  })
  // addr organization
  // ======================================
  // |        tag         |  index |offset|
  // |31                12|11     6|5    0|
  // ======================================

  // ============================
  // |         offset           |
  // | bank index | bank offset |
  // | 5        3 | 2         0 |
  // ============================

  // * meta * //
  val meta = RegInit(VecInit(Seq.fill(nset * nbank)(0.U.asTypeOf(new Bundle {
    val valid = Vec(nway, Bool())
    val lru   = Bool()
  }))))

  // * l1_tlb * //
  val tlb = RegInit(0.U.asTypeOf(new Bundle {
    val vpn      = UInt(tagWidth.W)
    val ppn      = UInt(tagWidth.W)
    val uncached = Bool()
    val valid    = Bool()
  }))

  val s_idle :: s_tlb_fill :: s_uncached :: s_replace :: s_save :: Nil = Enum(5)
  val state                                                            = RegInit(s_idle)

  val direct_mapped = io.cpu.addr(0)(31, 30) === 2.U(2.W)
  val uncached      = Mux(direct_mapped, io.cpu.addr(0)(29), tlb.uncached)
  val inst_tag      = Mux(direct_mapped, Cat(0.U(bankOffsetWidth.W), io.cpu.addr(0)(28, 12)), tlb.ppn)
  val inst_vpn      = io.cpu.addr(0)(31, 12)
  val inst_pa       = Cat(inst_tag, io.cpu.addr(0)(11, 0))

  val translation_ok = direct_mapped || (tlb.vpn === inst_vpn && tlb.valid)

  val replace_line_addr     = RegInit(0.U(6.W))
  val bram_addr_choose_next = !((state =/= s_idle) && (state =/= s_save))

  val bram_word_addr =
    io.cpu.addr(bram_addr_choose_next)(indexWidth + offsetWidth - 1, bankOffsetWidth)
  val bram_line_addr = io.cpu.addr(bram_addr_choose_next)(indexWidth + offsetWidth - 1, offsetWidth)
  val cache_data     = Wire(Vec(nway, UInt(bankWidthBits.W)))
  val cache_tag      = Wire(Vec(nway, UInt(tagWidth.W)))

  val data_wen      = RegInit(VecInit(Seq.fill(nway)(0.U(bankWidth.W))))
  val tag_wen       = RegInit(VecInit(Seq.fill(nway)(false.B)))
  val tag_ram_wdata = RegInit(0.U(tagWidth.W))

  val tag_compare_valid   = Wire(Vec(nway, Bool()))
  val cache_hit           = tag_compare_valid.contains(true.B)
  val cache_hit_available = cache_hit && translation_ok && !uncached

  val cache_inst_ok = Wire(Vec(nway, Bool()))
  cache_inst_ok(0) := cache_hit_available
  cache_inst_ok(1) := cache_hit_available && !io.cpu.addr(0)(2)

  val i_cache_sel = tag_compare_valid(1)

  val va_line_addr = io.cpu.addr(0)(indexWidth + offsetWidth - 1, offsetWidth)
  val fence_index  = io.cpu.fence.addr(indexWidth + offsetWidth - 1, offsetWidth)

  val cache_inst = Wire(Vec(nway, UInt(32.W)))
  cache_inst(0) := Mux(
    io.cpu.addr(0)(2),
    cache_data(i_cache_sel)(63, 32),
    cache_data(i_cache_sel)(31, 0),
  )
  cache_inst(1) := cache_data(i_cache_sel)(63, 32)

  val saved = RegInit(
    VecInit(Seq.fill(nway)(0.U.asTypeOf(new Bundle {
      val inst  = UInt(32.W)
      val valid = Bool()
    }))),
  )

  io.cpu.icache_stall := Mux(
    state === s_idle,
    (!cache_hit_available && io.cpu.req),
    state =/= s_save,
  )

  io.cpu.inst_valid(0) := Mux(state === s_idle, cache_inst_ok(0), saved(0).valid) && io.cpu.req
  io.cpu.inst_valid(1) := Mux(state === s_idle, cache_inst_ok(1), saved(1).valid) && io.cpu.req
  io.cpu.inst(0)       := Mux(state === s_idle, cache_inst(0), saved(0).inst)
  io.cpu.inst(1)       := Mux(state === s_idle, cache_inst(1), saved(1).inst)

  val axi_cnt = RegInit(0.U(5.W))

  // bank tag ram
  val bank_ram = Module(new Bank(byteAddressable = true))
  for { i <- 0 until nway } {
    bank_ram.io.way(i).r.addr := bram_word_addr
    bank_ram.io.way(i).w.en   := data_wen(i)
    bank_ram.io.way(i).w.addr := Cat(replace_line_addr, axi_cnt(3, 1))
    bank_ram.io.way(i).w.data := Mux(
      axi_cnt(0),
      Cat(io.axi.r.bits.data, 0.U(32.W)),
      Cat(0.U(32.W), io.axi.r.bits.data),
    )
    cache_data(i) := bank_ram.io.way(i).r.data
  }

  val tag_ram = Module(new Tag())
  for { i <- 0 until nway } {
    tag_ram.io.way(i).r.addr := bram_line_addr
    tag_ram.io.way(i).w.en   := tag_wen(i)
    tag_ram.io.way(i).w.addr := replace_line_addr
    tag_ram.io.way(i).w.data := tag_ram_wdata
    cache_tag(i)             := tag_ram.io.way(i).r.data
    tag_compare_valid(i)     := cache_tag(i) === inst_tag && meta(va_line_addr).valid(i)
  }

  when(io.cpu.fence.tlb && !io.cpu.icache_stall && !io.cpu.cpu_stall) { tlb.valid := false.B }
  when(io.cpu.fence.value && !io.cpu.icache_stall && !io.cpu.cpu_stall) {
    meta(fence_index).valid(0) := false.B
    meta(fence_index).valid(1) := false.B
  }

  val ar      = RegInit(0.U.asTypeOf(new AR()))
  val arvalid = RegInit(false.B)
  ar <> io.axi.ar.bits
  arvalid <> io.axi.ar.valid

  val r      = RegInit(0.U.asTypeOf(new R()))
  val rready = RegInit(false.B)
  r <> io.axi.r.bits
  rready <> io.axi.r.ready

  val tlb1 = RegInit(0.U.asTypeOf(new Bundle {
    val refill  = Bool()
    val invalid = Bool()
  }))
  tlb1 <> io.cpu.tlb1

  val tlb2 = RegInit(0.U.asTypeOf(new Bundle {
    val vpn = UInt(tagWidth.W)
  }))
  io.cpu.tlb2.vpn <> tlb2.vpn

  io.cpu.tlb2.vpn := 0.U
  switch(state) {
    is(s_idle) {
      when(io.cpu.req) {
        when(!translation_ok) {
          state    := s_tlb_fill
          tlb2.vpn := inst_vpn
        }.elsewhen(uncached) {
          state   := s_uncached
          ar.addr := inst_pa
          ar.len  := 0.U(bankWidth.W)
          ar.size := 2.U(bankOffsetWidth.W)
          arvalid := true.B
        }.elsewhen(!cache_hit) {
          state   := s_replace
          ar.addr := Cat(inst_pa(31, 6), 0.U(6.W))
          ar.len  := 15.U(bankWidth.W)
          ar.size := 2.U(bankOffsetWidth.W)
          arvalid := true.B

          replace_line_addr                                := va_line_addr
          data_wen(meta(va_line_addr).lru)                 := 0x0f.U
          tag_wen(meta(va_line_addr).lru)                  := true.B
          tag_ram_wdata                                    := inst_tag
          meta(va_line_addr).valid(meta(va_line_addr).lru) := true.B
          axi_cnt                                          := 0.U
        }.elsewhen(!io.cpu.icache_stall) {
          meta(va_line_addr).lru := ~i_cache_sel
          when(io.cpu.cpu_stall) {
            state          := s_save
            saved(1).inst  := cache_data(1)
            saved(0).valid := cache_inst_ok(0)
            saved(1).valid := cache_inst_ok(1)
          }
        }
      }
    }
    is(s_tlb_fill) {
      when(
        io.cpu.tlb2.found && (inst_vpn(12) && io.cpu.tlb2.entry.V1 || !inst_vpn(
          12,
        ) && io.cpu.tlb2.entry.V0),
      ) {
        state        := s_idle
        tlb.vpn      := io.cpu.tlb2.vpn
        tlb.ppn      := Mux(inst_vpn(12), io.cpu.tlb2.entry.PFN1, io.cpu.tlb2.entry.PFN0)
        tlb.uncached := Mux(inst_vpn(12), io.cpu.tlb2.entry.C1, io.cpu.tlb2.entry.C0)
        tlb.valid    := true.B
      }.otherwise {
        state          := s_save
        tlb1.invalid   := true.B
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
      }.otherwise {
        when(io.axi.r.fire) {
          when(!io.axi.r.bits.last) {
            axi_cnt                          := axi_cnt + 1.U
            data_wen(meta(va_line_addr).lru) := ~data_wen(meta(va_line_addr).lru)
          }.otherwise {
            rready                           := false.B
            data_wen(meta(va_line_addr).lru) := 0.U
            tag_wen(meta(va_line_addr).lru)  := 0.U
          }
        }.elsewhen(!io.axi.r.ready) {
          state := s_idle
        }
      }
    }
    is(s_save) {
      when(!io.cpu.cpu_stall && !io.cpu.icache_stall) {
        state          := s_idle
        tlb1.invalid   := false.B
        tlb1.refill    := false.B
        saved(0).valid := false.B
        saved(1).valid := false.B
      }
    }
  }
}
