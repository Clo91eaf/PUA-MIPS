package cache.memoryBanks.metaBanks

import chisel3._
import cache.CacheConfig

class TagValidBundle(implicit cacheConfig: CacheConfig) extends Bundle {
  val tag   = UInt(cacheConfig.tagWidth.W)
  val valid = Bool()
}
