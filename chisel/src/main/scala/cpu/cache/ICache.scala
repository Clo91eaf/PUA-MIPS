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
  // * addr organization * //
  // ======================================
  // |        tag         |  index |offset|
  // |31                12|11     6|5    0|
  // ======================================
  // |         offset           |
  // | bank index | bank offset |
  // | 5        3 | 2         0 |
  // ============================

  // * fsm * //
  val s_idle :: s_tlb_fill :: s_uncached :: s_replace :: s_save :: Nil = Enum(5)
  val state                                                            = RegInit(s_idle)

  // * nway * nset * //
  // * 64 bit for 2 inst * //
  // =========================================================
  // | valid | tag | data 0 | data 1 | ... | data 6 | data 7 |
  // | 1     | 20  |   64   |   64   | ... |  64    |  64    |
  // =========================================================
  // |       data      |
  // | inst 0 | inst 1 |
  // |   32   |   32   |
  // ===================

  val valid = RegInit(VecInit(Seq.fill(nset * nbank)(VecInit(Seq.fill(nway)(false.B)))))
  val data  = Wire(Vec(nway, Vec(ninst, UInt(32.W))))
  val tag   = Wire(Vec(nway, UInt(tagWidth.W)))

  // * should choose next addr * //
  val should_next_addr = (state === s_idle) || (state === s_save)

  val data_raddr = io.cpu.addr(should_next_addr)(indexWidth + offsetWidth - 1, bankOffsetWidth)
  val data_wstrb = RegInit(VecInit(Seq.fill(nway)(VecInit(Seq.fill(ninst)(0.U(4.W))))))

  val tag_raddr = io.cpu.addr(should_next_addr)(indexWidth + offsetWidth - 1, offsetWidth)
  val tag_wstrb = RegInit(VecInit(Seq.fill(nway)(false.B)))
  val tag_wdata = RegInit(0.U(tagWidth.W))

  // * lru * //
  val lru = RegInit(VecInit(Seq.fill(nset * nbank)(false.B)))

  // * l1_tlb * //
  val tlb = RegInit(0.U.asTypeOf(new Bundle {
    val vpn      = UInt(tagWidth.W)
    val ppn      = UInt(tagWidth.W)
    val uncached = Bool()
    val valid    = Bool()
  }))

  val direct_mapped = io.cpu.addr(0)(31, 30) === 2.U(2.W)
  val uncached      = Mux(direct_mapped, io.cpu.addr(0)(29), tlb.uncached)
  val inst_tag      = Mux(direct_mapped, Cat(0.U(bankOffsetWidth.W), io.cpu.addr(0)(28, 12)), tlb.ppn)
  val inst_vpn      = io.cpu.addr(0)(31, 12)
  val inst_pa       = Cat(inst_tag, io.cpu.addr(0)(11, 0))

  val translation_ok = direct_mapped || (tlb.vpn === inst_vpn && tlb.valid)

  val replace_line_addr = RegInit(0.U(6.W))

  val va_line_addr        = io.cpu.addr(0)(indexWidth + offsetWidth - 1, offsetWidth)
  val tag_compare_valid   = VecInit(Seq.tabulate(nway)(i => tag(i) === inst_tag && valid(va_line_addr)(i)))
  val cache_hit           = tag_compare_valid.contains(true.B)
  val cache_hit_available = cache_hit && translation_ok && !uncached

  val inst_valid = Wire(Vec(nway, Bool()))
  inst_valid(0) := cache_hit_available
  inst_valid(1) := cache_hit_available && !io.cpu.addr(0)(2)

  val i_cache_sel = tag_compare_valid(1)
  val fence_index = io.cpu.fence.addr(indexWidth + offsetWidth - 1, offsetWidth)

  val inst = VecInit(Seq.tabulate(nway)(i => Mux(io.cpu.addr(0)(2), data(i_cache_sel)(1), data(i_cache_sel)(i))))

  val saved = RegInit(VecInit(Seq.fill(nway)(0.U.asTypeOf(new Bundle {
    val inst  = UInt(32.W)
    val valid = Bool()
  }))))

  io.cpu.icache_stall := Mux(
    state === s_idle,
    (!cache_hit_available && io.cpu.req),
    state =/= s_save,
  )

  io.cpu.inst_valid(0) := Mux(state === s_idle, inst_valid(0), saved(0).valid) && io.cpu.req
  io.cpu.inst_valid(1) := Mux(state === s_idle, inst_valid(1), saved(1).valid) && io.cpu.req
  io.cpu.inst(0)       := Mux(state === s_idle, inst(0), saved(0).inst)
  io.cpu.inst(1)       := Mux(state === s_idle, inst(1), saved(1).inst)

  val axi_cnt = RegInit(0.U(5.W))

  // bank tag ram
  for { i <- 0 until nway; j <- 0 until ninst } {
    val bank = Module(new SimpleDualPortRam(nset * nbank, 32, byteAddressable = true))
    bank.io.ren   := true.B
    bank.io.raddr := data_raddr
    data(i)(j)    := bank.io.rdata

    bank.io.wen   := data_wstrb(i)(j).orR
    bank.io.waddr := Cat(replace_line_addr, axi_cnt(3, 1))
    bank.io.wdata := Mux(j.U === axi_cnt(0), io.axi.r.bits.data, 0.U)
    bank.io.wstrb := data_wstrb(i)(j)
  }

  // tag
  for { i <- 0 until nway } {
    val tag_bram = Module(new SimpleDualPortRam(nset, tagWidth, false))
    tag_bram.io.ren   := true.B
    tag_bram.io.raddr := tag_raddr
    tag(i)            := tag_bram.io.rdata

    tag_bram.io.wen   := tag_wstrb(i).orR
    tag_bram.io.waddr := replace_line_addr
    tag_bram.io.wdata := tag_wdata
    tag_bram.io.wstrb := tag_wstrb(i)
  }

  when(io.cpu.fence.tlb && !io.cpu.icache_stall && !io.cpu.cpu_stall) { tlb.valid := false.B }
  when(io.cpu.fence.value && !io.cpu.icache_stall && !io.cpu.cpu_stall) {
    valid(fence_index) := VecInit(Seq.fill(2)(false.B))
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

          replace_line_addr                      := va_line_addr
          data_wstrb(lru(va_line_addr))(0)       := 0xf.U
          data_wstrb(lru(va_line_addr))(1)       := 0x0.U
          tag_wstrb(lru(va_line_addr))           := true.B
          tag_wdata                              := inst_tag
          valid(va_line_addr)(lru(va_line_addr)) := true.B
          axi_cnt                                := 0.U
        }.elsewhen(!io.cpu.icache_stall) {
          lru(va_line_addr) := ~i_cache_sel
          when(io.cpu.cpu_stall) {
            state          := s_save
            saved(1).inst  := data(1)(0)
            saved(0).valid := inst_valid(0)
            saved(1).valid := inst_valid(1)
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
      }.elsewhen(io.axi.r.fire) {
        when(!io.axi.r.bits.last) {
          axi_cnt                          := axi_cnt + 1.U
          data_wstrb(lru(va_line_addr))(0) := ~data_wstrb(lru(va_line_addr))(0)
          data_wstrb(lru(va_line_addr))(1) := ~data_wstrb(lru(va_line_addr))(1)
        }.otherwise {
          rready                        := false.B
          data_wstrb(lru(va_line_addr)) := 0.U.asTypeOf(Vec(ninst, UInt(4.W)))
          tag_wstrb(lru(va_line_addr))  := 0.U
        }
      }.elsewhen(!io.axi.r.ready) {
        state := s_idle
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
