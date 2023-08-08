package cache

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.CpuConfig

class Cache(implicit config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val inst = Flipped(new Cache_ICache())
    val data = Flipped(new Cache_DCache())
    val axi  = new AXI()
    val statistic = if (!config.build) Some(new CacheStatistic()) else None
  })
  implicit val iCacheConfig = CacheConfig(nset = 64, nbank = 4, bankWidth = 16)
  implicit val dCacheConfig = CacheConfig(nset = 128, bankWidth = 4)

  val icache        = Module(new ICache(iCacheConfig))
  val dcache        = Module(new DCache(dCacheConfig))
  val axi_interface = Module(new CacheAXIInterface())

  icache.io.axi <> axi_interface.io.icache
  dcache.io.axi <> axi_interface.io.dcache

  io.inst <> icache.io.cpu
  io.data <> dcache.io.cpu
  io.axi <> axi_interface.io.axi

  // ===----------------------------------------------------------------===
  // statistic
  // ===----------------------------------------------------------------===
  if (!config.build) {
    io.statistic.get.icache <> icache.io.statistic.get
    io.statistic.get.dcache <> dcache.io.statistic.get
  }
}
