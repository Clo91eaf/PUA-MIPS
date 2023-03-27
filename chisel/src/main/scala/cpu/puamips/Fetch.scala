package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Fetch extends Module {
  val io = IO(new Bundle {
    val fromInstMemory = Flipped(new InstMemory_Fetch())
    val fromDecoder = Flipped(new Decoder_Fetch())
    val instMemory = new Fetch_InstMemory()
    val decoder = new Fetch_Decoder()
  })
  // input-inst memory
  val inst = RegInit(REG_BUS_INIT) // 复位时指令存储器禁用
  inst := io.fromInstMemory.inst
  
  // output-inst memory
  val pc = RegInit(REG_BUS_INIT)
  val ce = RegInit(CHIP_DISABLE) // 复位时指令存储器禁用
  io.instMemory.pc := pc
  io.instMemory.ce := ce

  // output-decoder
  io.decoder.pc := pc
  io.decoder.inst := inst

  ce := CHIP_ENABLE // 复位结束使能指令存储器

  when(ce === CHIP_DISABLE) {
    pc := 0.U
  }.elsewhen(io.fromDecoder.branch_flag === BRANCH) {
    pc := io.fromDecoder.branch_target_address
  }.otherwise {
    pc := pc + 4.U(REG_NUM.W)
  }
  
  io.decoder.pc := pc

  printf(p"fetch :pc 0x${Hexadecimal(pc)}\n")
}
