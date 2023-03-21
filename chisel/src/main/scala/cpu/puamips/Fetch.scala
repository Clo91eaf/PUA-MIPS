package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Fetch extends Module {
  val io = IO(new Bundle {
    val top = new Fetch_Top()
    val decoder = new Fetch_Decoder()
    val fromDecoder = Flipped(new Decoder_Fetch())
  })
  val pc = RegInit(REG_BUS_INIT)
  val ce = RegInit(CHIP_DISABLE) // 复位时指令存储器禁用

  ce := CHIP_ENABLE // 复位结束使能指令存储器

  when(ce === CHIP_DISABLE) {
    pc := 0.U
  }.elsewhen(io.fromDecoder.branch_flag === BRANCH) {
    pc := io.fromDecoder.branch_target_address
  }.otherwise {
    pc := pc + 4.U(REG_NUM.W)
  }
  
  io.top.pc := pc
  io.top.ce := ce
  io.decoder.pc := pc

  printf(p"fetch :pc 0x${Hexadecimal(pc)}\n")
}
