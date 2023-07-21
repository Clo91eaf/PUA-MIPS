package cpu

case class CpuConfig(
    val decoderNum: Int = 2, // 同时访问寄存器的指令数
    val commitNum: Int = 2, // 同时提交的指令数
    val fuNum: Int = 2,     // 功能单元数
) {}
