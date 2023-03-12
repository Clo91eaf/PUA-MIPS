package cpu.ori
import cpu.ori.Constants._

import chisel3.util._
import chisel3._

class PC_reg extends Module {
  val io = IO(new Bundle {
    val pc = Output(InstAddrBus)
    val ce = Output(Bool())
  })

  val pcr = Reg(InstAddrBus)
  val cer = Reg(Bool())

  io.pc := pcr
  io.ce := cer

  when(reset.asBool === RstEnable) {
    cer := ChipDisable
  }.otherwise {
    cer := ChipEnable
  }
  when(cer === ChipDisable) {
    pcr := 0.U
  }.otherwise {
    pcr := pcr + "h4".U
  }
}
