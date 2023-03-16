package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._

class HILO_reg extends Module {
  val io = IO(new Bundle {

    // 写端口
    val we = Input(Bool())
    val hi_i = Input(RegBus)
    val lo_i = Input(RegBus)

    // 读端口
    val hi_o = Output(RegBus)
    val lo_o = Output(RegBus)
  })

  val hi_or = Reg(RegBus)
  val lo_or = Reg(RegBus)

  io.hi_o := hi_or
  io.lo_o := lo_or

  when(reset.asBool === RstEnable) {
    hi_or := ZeroWord
    lo_or := ZeroWord
  }.elsewhen(io.we === WriteEnable) {
    hi_or := io.hi_i
    lo_or := io.lo_i
  }

}
