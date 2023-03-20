package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._

class Regfile extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_RegFile())
    val fromWriteBack = Flipped(new WriteBack_RegFile())
    val decoder = new RegFile_Decoder()
  })
  // input-decoder
  val re1 = RegInit(false.B)
  val raddr1 = RegInit(REG_ADDR_BUS_INIT)
  val re2 = RegInit(false.B)
  val raddr2 = RegInit(REG_ADDR_BUS_INIT)
  re1 := io.fromDecoder.reg1_read
  re2 := io.fromDecoder.reg2_read
  raddr1 := io.fromDecoder.reg1_addr
  raddr1 := io.fromDecoder.reg2_addr

  // input-write back
  val we = RegInit(false.B)
  val waddr = RegInit(REG_ADDR_BUS_INIT)
  val wdata = RegInit(REG_BUS_INIT)
  we := io.fromWriteBack.wreg
  wdata := io.fromWriteBack.wdata
  waddr := io.fromWriteBack.wd

  // output-decoder
  val rdata1 = RegInit(REG_BUS_INIT)
  val rdata2 = RegInit(REG_BUS_INIT)
  io.decoder.rdata1 := rdata1
  io.decoder.rdata2 := rdata2

  // 定义32个32位寄存器
  val regs = RegInit(VecInit(Seq.fill(REG_NUM)(REG_BUS_INIT)))

  when(reset.asBool === RST_DISABLE) {
    when(we === WRITE_ENABLE && waddr =/= 0.U) {
      regs(waddr) := wdata
    }
  }
  when(reset.asBool === RST_DISABLE) {
    rdata1 := ZERO_WORD
  }.elsewhen(raddr1 === 0.U) {
    rdata1 := ZERO_WORD
  }.elsewhen(re1 === READ_ENABLE) {
    rdata1 := regs(raddr1)
  }.otherwise {
    rdata1 := ZERO_WORD
  }

  when(reset.asBool === RST_ENABLE) {
    rdata2 := ZERO_WORD
  }.elsewhen(raddr2 === 0.U) {
    rdata2 := ZERO_WORD
  }.elsewhen(re2 === READ_ENABLE) {
    rdata2 := regs(raddr2)
  }.otherwise {
    rdata2 := ZERO_WORD
  }

  // debug
  when(we === WRITE_ENABLE) {
    printf(p"regfile :waddr 0x${Hexadecimal(waddr)}, wdata 0x${Hexadecimal(wdata)}\n")
  }.otherwise {
    printf(p"regfile :raddr1 0x${Hexadecimal(raddr1)}, rdata1 0x${Hexadecimal(rdata1)}\n")
    printf(p"regfile :raddr2 0x${Hexadecimal(raddr2)}, rdata2 0x${Hexadecimal(rdata2)}\n")
  }

}
