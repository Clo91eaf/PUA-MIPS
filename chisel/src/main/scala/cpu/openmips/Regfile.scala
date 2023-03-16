package cpu.openmips

import chisel3._
import chisel3.util._
import cpu.openmips.Constants._

class Regfile extends Module {
  val io = IO(new Bundle {
    // 输入端口
    val we = Input(Bool())
    val waddr = Input(RegAddrBus)
    val wdata = Input(RegBus)
    val re1 = Input(Bool())
    val raddr1 = Input(RegAddrBus)
    val re2 = Input(Bool())
    val raddr2 = Input(RegAddrBus)
    // 输出端口
    val rdata1 = Output(RegBus)
    val rdata2 = Output(RegBus)
  })

  val rdata1r = Reg(RegBus)
  val rdata2r = Reg(RegBus)

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
