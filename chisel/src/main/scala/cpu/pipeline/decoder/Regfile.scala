package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Regfile extends Module {
  val io = IO(new Bundle {
    val fromDecoder        = Flipped(new Decoder_RegFile())
    val fromWriteBackStage = Flipped(new WriteBackStage_RegFile())
    val decoder            = new RegFile_Decoder()
  })
  // input-decoder
  val ren1   = Wire(Bool())
  val raddr1 = Wire(ADDR_BUS)
  val ren2   = Wire(Bool())
  val raddr2 = Wire(ADDR_BUS)
  ren1   := io.fromDecoder.reg1_ren
  ren2   := io.fromDecoder.reg2_ren
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

  // write
  when(wen === WRITE_ENABLE && waddr.orR) {
    regs(waddr) := wdata
  }

  // read and read after write 
  rdata1 := Mux(
    (wen === WRITE_ENABLE && waddr === raddr1 && waddr.orR),
    wdata,
    regs(raddr1),
  )
  rdata2 := Mux(
    (wen === WRITE_ENABLE && waddr === raddr2 && waddr.orR),
    wdata,
    regs(raddr2),
  )
}
