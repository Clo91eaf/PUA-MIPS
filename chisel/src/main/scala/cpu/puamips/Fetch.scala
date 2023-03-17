package cpu.puamips 

import chisel3._

class Fetch extends Module {
  val io = IO(new Bundle {
    val instMemory = new Fetch_InstMemory()
    val decoder = new Fetch_Decoder()
  })
  val pc = RegInit(RegBusInit)
  val ce = Reg(Bool())

  when(reset.asBool() === RstEnable) {
    ce := ChipDisable
  }.otherwise {
    ce := ChipEnable
  }

  when(ce === ChipDisable) {
    pc := RegBusInit
  }.otherwise{
    pc := pc + 4.U(RegNum.W)
  }

  io.instMemory.pc := pc
  io.instMemory.ce := ce
  io.decoder.pc := pc
}