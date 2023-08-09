package cpu

import chisel3.util._

case class CpuConfig(
    val build: Boolean = true,               // 是否为build模式
    val hasCommitBuffer: Boolean = false,    // 是否有提交缓存
    val decoderNum: Int = 2,                 // 同时访问寄存器的指令数
    val commitNum: Int = 2,                  // 同时提交的指令数
    val fuNum: Int = 2,                      // 功能单元数
    val instFetchNum: Int = 4,               // iCache取到的指令数量
    val instBufferDepth: Int = 16,           // 指令缓存深度
    val writeBufferDepth: Int = 16,          // 写缓存深度
    val mulClockNum: Int = 3,                // 乘法器的时钟周期数
    val divClockNum: Int = 8,                // 除法器的时钟周期数
    val branchPredictor: String = "adaptive",// adaptive, pesudo, global
)

case class BranchPredictorConfig(
    val bhtDepth: Int = 8,
    val phtDepth: Int = 10,
)

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
  val ninst           = 4                                // TODO:改成可随意修改的参数
  require(isPow2(nset))
  require(isPow2(nway))
  require(isPow2(nbank))
  require(isPow2(bankWidth))
  require(
    tagWidth + indexWidth + bankIndexWidth + bankOffsetWidth == 32,
    "basic request calculation",
  )
}
