package cpu

case class CpuConfig(
    val build: Boolean = true,              // 是否为build模式
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
