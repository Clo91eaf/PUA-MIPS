package cpu.puamips

import Const._
import chisel3._

class Fetch extends Module {
  val io = IO(new Bundle {
    val fromControl = Flipped(new Control_Fetch())
    val fromDecoder = Flipped(new Decoder_Fetch())
    val decoderStage = new Fetch_DecoderStage()
    val instMemory = new Fetch_InstMemory()
  })

  // input
  val stall = Wire(STALL_BUS)
  stall := io.fromControl.stall
  val branch_flag = Wire(Bool())
  branch_flag := io.fromDecoder.branch_flag
  val branch_target_address = Wire(REG_BUS)
  branch_target_address := io.fromDecoder.branch_target_address
  // output
  val pc = RegInit(PC_INIT)
  io.decoderStage.pc := pc
  io.instMemory.pc := pc
  val ce = RegInit(CHIP_DISABLE)
  io.instMemory.ce := ce

  when(ce === CHIP_DISABLE) {
    pc := PC_INIT
  }.elsewhen(stall(0) === NOT_STOP) {
    when(branch_flag === BRANCH) {
      pc := branch_target_address
    }.otherwise {
      pc := pc + 4.U
    }
  }

  ce := CHIP_ENABLE // 复位结束,使能指令存储器

  printf(p"fetch :pc 0x${Hexadecimal(pc)}\n")
}
