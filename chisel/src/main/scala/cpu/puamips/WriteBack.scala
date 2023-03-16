package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._

class WriteBack extends Module {
  val io = IO(new Bundle {
    // 来自EX的信息
    val ex_wd = Input(RegAddrBus)
    val ex_wreg = Input(Bool())
    val ex_wdata = Input(RegBus)
    val ex_hi = Input(RegBus)
    val ex_lo = Input(RegBus)
    val ex_whilo = Input(Bool())
    // 送到Regfile的信息
    val wb_wd = Output(RegAddrBus)
    val wb_wreg = Output(Bool())
    val wb_wdata = Output(RegBus)
    val wb_hi = Output(RegBus)
    val wb_lo = Output(RegBus)
    val wb_whilo = Output(Bool())
  })

  val wb_wregr = Reg(Bool())
  val wb_hir = Reg(RegBus)
  val wb_lor = Reg(RegBus)
  val wb_whilor = Reg(Bool())
  io.wb_wreg := wb_wregr
  io.wb_hi := wb_hir
  io.wb_lo := wb_lor
  io.wb_whilo := wb_whilor

  when(reset.asBool === RstEnable) {
    io.wb_wd := NOPRegAddr
    wb_wregr := WriteDisable
    io.wb_wdata := ZeroWord
    wb_hir := ZeroWord
    wb_lor := ZeroWord
    wb_whilor := ZeroWord
  }.otherwise {
    io.wb_wd := io.ex_wd
    wb_wregr := io.ex_wreg
    io.wb_wdata := io.ex_wdata
    wb_hir := io.ex_hi
    wb_lor := io.ex_lo
    wb_whilor := io.ex_whilo
  }
}
