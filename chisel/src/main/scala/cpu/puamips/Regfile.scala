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
  val raddr1 = RegInit(RegAddrBusInit)
  val re2 = RegInit(false.B)
  val raddr2 = RegInit(RegAddrBusInit)
  re1 := io.fromDecoder.reg1_read
  re2 := io.fromDecoder.reg2_read
  raddr1 := io.fromDecoder.reg1_addr
  raddr1 := io.fromDecoder.reg2_addr

  // input-write back
  val we = RegInit(false.B)
  val waddr = RegInit(RegAddrBusInit)
  val wdata = RegInit(RegBusInit)
  we := io.fromWriteBack.wreg
  wdata := io.fromWriteBack.wdata
  waddr := io.fromWriteBack.wd

  // output-decoder
  val rdata1 = RegInit(RegBusInit)
  val rdata2 = RegInit(RegBusInit)
  io.decoder.rdata1 := rdata1
  io.decoder.rdata2 := rdata2

  // 定义32个32位寄存器
  val regs = RegInit(VecInit(Seq.fill(RegNum)(RegBusInit)))

  when(reset.asBool === RstDisable) {
    when(we === WriteEnable && waddr =/= 0.U) {
      regs(waddr) := wdata
    }
  }
  when(reset.asBool === RstDisable) {
    rdata1 := ZeroWord
  }.elsewhen(raddr1 === 0.U) {
    rdata1 := ZeroWord
  }.elsewhen(re1 === ReadEnable) {
    rdata1 := regs(raddr1)
  }.otherwise {
    rdata1 := ZeroWord
  }

  when(reset.asBool === RstEnable) {
    rdata2 := ZeroWord
  }.elsewhen(raddr2 === 0.U) {
    rdata2 := ZeroWord
  }.elsewhen(re2 === ReadEnable) {
    rdata2 := regs(raddr2)
  }.otherwise {
    rdata2 := ZeroWord
  }
}
