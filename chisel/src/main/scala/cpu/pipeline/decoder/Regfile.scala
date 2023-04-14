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
  val wen   = Wire(REG_WRITE_BUS)
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
  when(wen.orR && waddr.orR) {
    regs(waddr) := Cat(
      Mux(wen(3), wdata(31, 24), regs(waddr)(31, 24)),
      Mux(wen(2), wdata(23, 16), regs(waddr)(23, 16)),
      Mux(wen(1), wdata(15, 8), regs(waddr)(15, 8)),
      Mux(wen(0), wdata(7, 0), regs(waddr)(7, 0)),
    )
  }

  val rdata1_value = Wire(Vec(4, UInt(8.W)))
  val rdata2_value = Wire(Vec(4, UInt(8.W)))

  // 解决wb的数据前递问题
  rdata1 := rdata1_value.asUInt
  for (i <- 0 until 4) {
    when(
      wen(i) && waddr === raddr1 && waddr.orR,
    ) {
      rdata1_value(i) := wdata(i * 8 + 7, i * 8)
    }.otherwise {
      rdata1_value(i) := regs(raddr1)(i * 8 + 7, i * 8)
    }
  }

  rdata2 := rdata2_value.asUInt
  for (i <- 0 until 4) {
    when(
      wen(i) && waddr === raddr2 && waddr.orR,
    ) {
      rdata2_value(i) := wdata(i * 8 + 7, i * 8)
    }.otherwise {
      rdata2_value(i) := regs(raddr2)(i * 8 + 7, i * 8)
    }
  }
}
