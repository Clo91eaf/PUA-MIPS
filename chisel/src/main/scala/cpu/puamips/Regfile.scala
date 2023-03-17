package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._

class Regfile extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_RegFile())
    val decoder = new RegFile_Decoder()
 })
  // 输入端口
  val we = RegInit(false.B)
  val waddr = RegInit(RegAddrBusInit)
  val wdata = RegInit(RegBusInit)
  val re1 = RegInit(false.B)
  val raddr1 = RegInit(RegAddrBusInit)
  val re2 = RegInit(false.B)
  val raddr2 = RegInit(RegAddrBusInit)
  // 输出端口
  val rdata1 = RegInit(RegBusInit)
  val rdata2 = RegInit(RegBusInit)
 
  val rdata1r = RegInit(RegBusInit)
  val rdata2r = RegInit(RegBusInit)

  io.rdata1 := rdata1r
  io.rdata2 := rdata2r

  // 定义32个32位寄存器
  val regs = RegInit(VecInit(Seq.fill(RegNum)(RegBusInit)))

  when(reset.asBool === RstDisable) {
    when(io.we === WriteEnable && io.waddr =/= 0.U) {
      regs(io.waddr) := io.wdata
    }
  }
  when(reset.asBool === RstDisable) {
    rdata1r := ZeroWord
  }.elsewhen(io.raddr1 === 0.U) {
    rdata1r := ZeroWord
  }.elsewhen(io.re1 === ReadEnable) {
    rdata1r := regs(io.raddr1)
  }.otherwise {
    rdata1r := ZeroWord
  }

  when(reset.asBool === RstEnable) {
    rdata2r := ZeroWord
  }.elsewhen(io.raddr2 === 0.U) {
    rdata2r := ZeroWord
  }.elsewhen(io.re2 === ReadEnable) {
    rdata2r := regs(io.raddr2)
  }.otherwise {
    rdata2r := ZeroWord
  }
}
