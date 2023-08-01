package cpu

case class CpuConfig(
    val decoderNum: Int = 2,         // 同时访问寄存器的指令数
    val commitNum: Int = 2,          // 同时提交的指令数
    val fuNum: Int = 2,              // 功能单元数
    val hasMonitor: Boolean = false, // 是否有监控模块
    val instFetchNum: Int = 4,       // iCache取到的指令数量
    val build: Boolean = false,      // 是否为build模式
) {}
