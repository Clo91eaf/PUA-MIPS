package cache

import chisel3._
import chisel3.util._
import chisel3.internal.firrtl.MemPortDirection

// cpu to icache
class TLB_Entry extends Bundle {
  val G    = Bool()
  val V0   = Bool()
  val V1   = Bool()
  val D0   = Bool()
  val D1   = Bool()
  val C0   = Bool()
  val C1   = Bool()
  val PFN0 = UInt(20.W)
  val PFN1 = UInt(20.W)
  val VPN2 = UInt(19.W)
  val ASID = UInt(8.W)
}

class Cache_ICache(
    ninst: Int = 2,
) extends Bundle {
  // read inst request from cpu
  val req  = Output(Bool())
  val addr = Output(Vec(ninst, UInt(32.W))) // virtual address and next virtual address

  // read inst result
  val inst       = Input(Vec(ninst, UInt(32.W)))
  val inst_valid = Input(Vec(ninst, Bool()))

  // control
  val cpu_stall    = Output(Bool())
  val icache_stall = Input(Bool())

  // l1 tlb
  val tlb1 = Input(new Bundle {
    val refill  = Bool()
    val invalid = Bool()
  })

  // l2 tlb
  val tlb2 = new Bundle {
    val vpn   = Input(UInt(19.W))
    val found = Output(Bool())
    val entry = Output(new TLB_Entry())
  }

  val fence = Output(new Bundle {
    val value = Bool()
    val addr  = UInt(32.W)
    val tlb   = Bool()
  })
}

// cpu to dcache
class Cache_DCache extends Bundle {
  val stallM       = Output(Bool())
  val dstall       = Input(Bool())
  val E_mem_va     = Output(UInt(32.W))
  val M_mem_va     = Output(UInt(32.W))
  val M_fence_addr = Output(UInt(32.W))
  val M_fence_d    = Output(Bool())
  val M_mem_en     = Output(Bool())
  val M_mem_write  = Output(Bool())
  val M_wmask      = Output(UInt(4.W))
  val M_mem_size   = Output(UInt(2.W))
  val M_wdata      = Output(UInt(32.W))
  val M_rdata      = Input(UInt(32.W))

  // to l2 tlb
  val tlb = new Bundle {
    val vpn2  = Input(UInt(19.W))
    val found = Output(Bool())
    val entry = Output(new TLB_Entry())
  }
  val fence_tlb = Output(Bool())
  // M_tlb_except
  val data_tlb_refill  = Input(Bool())
  val data_tlb_invalid = Input(Bool())
  val data_tlb_mod     = Input(Bool())
}

// axi
// master

class AR extends Bundle {
  val addr = UInt(32.W)
  val len  = UInt(8.W)
  val size = UInt(3.W)
}

class R extends Bundle {
  val data = UInt(32.W)
  val last = Bool()
}

class AW extends Bundle {
  val addr = UInt(32.W)
  val len  = UInt(8.W)
  val size = UInt(3.W)
}

class W extends Bundle {
  val data = UInt(32.W)
  val strb = UInt(4.W)
  val last = Bool()
}

class ICache_AXIInterface extends Bundle {
  val ar = Decoupled(new AR())
  val r  = Flipped(Decoupled(new R()))
}

class DCache_AXIInterface extends ICache_AXIInterface {
  val aw = Decoupled(new AW())

  val w = Decoupled(new W())

  val b = Flipped(Decoupled())
}

class Cache_AXIInterface extends Bundle {
  // axi read channel
  val icache = new ICache_AXIInterface()
  val dcache = new DCache_AXIInterface()
}

// AXI read address channel
class AXI_AR extends Bundle {
  val id    = UInt(4.W)  // transaction ID
  val addr  = UInt(32.W) // address
  val len   = UInt(8.W)  // burst length
  val size  = UInt(3.W)  // transfer size
  val burst = UInt(2.W)  // burst type
  val lock  = UInt(2.W)  // lock type
  val cache = UInt(4.W)  // cache type
  val prot  = UInt(3.W)  // protection type
}

// AXI read data channel
class AXI_R extends Bundle {
  val id   = UInt(4.W)  // transaction ID
  val data = UInt(32.W) // read data
  val resp = UInt(2.W)  // response type
  val last = Bool()     // last beat of burst
}

// AXI write address channel
class AXI_AW extends Bundle {
  val id    = UInt(4.W)  // transaction ID
  val addr  = UInt(32.W) // address
  val len   = UInt(8.W)  // burst length
  val size  = UInt(3.W)  // transfer size
  val burst = UInt(2.W)  // burst type
  val lock  = UInt(2.W)  // lock type
  val cache = UInt(4.W)  // cache type
  val prot  = UInt(3.W)  // protection type
}

// AXI write data channel
class AXI_W extends Bundle {
  val id   = UInt(4.W)  // transaction ID
  val data = UInt(32.W) // write data
  val strb = UInt(4.W)  // byte enable
  val last = Bool()     // last beat of burst
}

// AXI write response channel
class AXI_B extends Bundle {
  val id   = UInt(4.W) // transaction ID
  val resp = UInt(2.W) // response type
}

// AXI interface
class AXI extends Bundle {
  val ar = Decoupled(new AXI_AR())         // read address channel
  val r  = Flipped(Decoupled(new AXI_R())) // read data channel
  val aw = Decoupled(new AXI_AW())         // write address channel
  val w  = Decoupled(new AXI_W())          // write data channel
  val b  = Flipped(Decoupled(new AXI_B())) // write response channel
}
