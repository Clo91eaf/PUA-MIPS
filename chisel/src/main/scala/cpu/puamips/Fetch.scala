package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Fetch extends Module {
  val io = IO(new Bundle {
    val instMemory = new Fetch_InstMemory()
    val decoder = new Fetch_Decoder()
  })
  val pc = RegInit(REG_BUS_INIT)
  val ce = RegInit(CHIP_ENABLE)

  when(ce === CHIP_DISABLE) {
    pc := REG_BUS_INIT
  }.otherwise {
    pc := pc + 4.U(REG_NUM.W)
  }
  io.instMemory.pc := pc
  io.instMemory.ce := ce
  io.decoder.pc := pc
}
