package cache

import chisel3.util._

case class CacheConfig(
    nway: Int = 2,
    nbank: Int = 8,
    nset: Int,
    bankWidth: Int, // bytes per bank
) {
  val indexWidth      = log2Ceil(nset)                   // 6
  val bankIndexWidth  = log2Ceil(nbank)                  // 3
  val bankOffsetWidth = log2Ceil(bankWidth)              // 3
  val offsetWidth     = bankIndexWidth + bankOffsetWidth // 6
  val tagWidth        = 32 - indexWidth - offsetWidth    // 20
  val tagvWidth       = tagWidth + 1                     // 21
  val bankWidthBits   = bankWidth * 8                    // 64
  val burstSize       = 16
  val ninst           = 4
  require(isPow2(nset))
  require(isPow2(nway))
  require(isPow2(nbank))
  require(isPow2(bankWidth))
  require(
    tagWidth + indexWidth + bankIndexWidth + bankOffsetWidth == 32,
    "basic request calculation",
  )
}
