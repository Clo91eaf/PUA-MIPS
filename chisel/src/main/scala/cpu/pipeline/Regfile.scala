package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines.Const._

class Regfile extends Module {
  val io = IO(new Bundle {
    val fromDecoder        = Flipped(new Decoder_RegFile())
    val fromWriteBackStage = Flipped(new WriteBackStage_RegFile())
    val decoder            = new RegFile_Decoder()
  })
  // input-decoder
  val ren1    = Wire(Bool())
  val raddr1 = Wire(ADDR_BUS)
  val ren2    = Wire(Bool())
  val raddr2 = Wire(ADDR_BUS)
  ren1    := io.fromDecoder.reg1_ren
  ren2    := io.fromDecoder.reg2_ren
  raddr1 := io.fromDecoder.reg1_raddr
  raddr2 := io.fromDecoder.reg2_raddr

  // input-write back
  val wen   = Wire(Bool())
  val waddr = Wire(ADDR_BUS)
  val wdata = Wire(BUS)
  wen   := io.fromWriteBackStage.reg_wen
  wdata := io.fromWriteBackStage.reg_wdata
  waddr := io.fromWriteBackStage.reg_waddr

  // output-decoder
  val rdata1 = Wire(BUS)
  val rdata2 = Wire(BUS)
  io.decoder.reg1_data := rdata1
  io.decoder.reg2_data := rdata2

  // 定义32个32位寄存器
  val regs = RegInit(VecInit(Seq.fill(REG_NUM)(BUS_INIT)))

  when(reset.asBool === RST_DISABLE) {
    when(wen === WRITE_ENABLE && waddr =/= 0.U) {
      regs(waddr) := wdata
    }
  }

  when(reset.asBool === RST_ENABLE) {
    rdata1 := ZERO_WORD
  }.elsewhen(raddr1 === 0.U) {
    rdata1 := ZERO_WORD
  }.elsewhen(
    (raddr1 === waddr) && (wen === WRITE_ENABLE)
      && (ren1 === READ_ENABLE)
  ) {
    rdata1 := wdata
  }.elsewhen(ren1 === READ_ENABLE) {
    rdata1 := regs(raddr1)
  }.otherwise {
    rdata1 := ZERO_WORD
  }

  when(reset.asBool === RST_ENABLE) {
    rdata2 := ZERO_WORD
  }.elsewhen(raddr2 === 0.U) {
    rdata2 := ZERO_WORD
  }.elsewhen(
    (raddr2 === waddr) && (wen === WRITE_ENABLE)
      && (ren2 === READ_ENABLE)
  ) {
    rdata2 := wdata
  }.elsewhen(ren2 === READ_ENABLE) {
    rdata2 := regs(raddr2)
  }.otherwise {
    rdata2 := ZERO_WORD
  }

  // debug
  // when(wen === WRITE_ENABLE) {
  //   printf(
  //     p"regfile :waddr 0x${Hexadecimal(waddr)}, wdata 0x${Hexadecimal(wdata)}\n"
  //   )
  // }.otherwise {
  //   printf(
  //     p"regfile :raddr1 0x${Hexadecimal(raddr1)}, rdata1 0x${Hexadecimal(rdata1)}\n"
  //   )
  //   printf(
  //     p"regfile :raddr2 0x${Hexadecimal(raddr2)}, rdata2 0x${Hexadecimal(rdata2)}\n"
  //   )
  // }

}
