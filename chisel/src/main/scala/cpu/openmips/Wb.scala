package cpu.openmips

import chisel3._
import chisel3.util._
import cpu.openmips.Constants._

class Wb extends Module {
  val io = IO(new Bundle {
    // From EX stage
    val ex_wd = Input(RegAddrBus)
    val ex_wreg = Input(Bool())
    val ex_wdata = Input(RegBus)

    // To Regfile
    val wb_wd = Output(RegAddrBus)
    val wb_wreg = Output(Bool())
    val wb_wdata = Output(RegBus)
  })

  val wb_wregr = Reg(Bool())
  io.wb_wreg := wb_wregr

  when(reset.asBool === RstEnable) {
    io.wb_wd := NOPRegAddr
    wb_wregr := WriteDisable
    io.wb_wdata := ZeroWord
  }.otherwise {
    io.wb_wd := io.ex_wd
    wb_wregr := io.ex_wreg
    io.wb_wdata := io.ex_wdata
  }
}
