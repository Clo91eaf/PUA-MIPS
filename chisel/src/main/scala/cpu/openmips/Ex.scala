package cpu.openmips

import chisel3._
import chisel3.util._
import cpu.openmips.Constants._

class Ex extends Module {
  val io = IO(new Bundle {
    // 译码模块传来的信息
    val aluop_i = Input(AluOpBus)
    val alusel_i = Input(AluSelBus)
    val reg1_i = Input(RegBus)
    val reg2_i = Input(RegBus)
    val wd_i = Input(RegAddrBus)
    val wreg_i = Input(Bool())

    // 运算完毕后的结果
    val wd_o = Output(RegAddrBus)
    val wreg_o = Output(Bool())
    val wdata_o = Output(RegBus)
  })

  val wd_or = Reg(RegAddrBus)
  val wreg_or = Reg(Bool())
  val wdata_or = Reg(RegBus)

  io.wd_o := wd_or
  io.wreg_o := wreg_or
  io.wdata_o := wdata_or

  val logicout = Reg(RegBus)

  when(reset.asBool === RstEnable) {
    logicout := ZeroWord
  }.otherwise {
    io.aluop_i match {
      case EXE_OR_OP => {
        logicout := io.reg1_i | io.reg2_i
      }
      case _ => {
        logicout := ZeroWord
      }
    }
  }

  wd_or := io.wd_i
  wreg_or := io.wreg_i

  io.alusel_i match {
    case EXE_RES_LOGIC => wdata_or := logicout
    case _             => wdata_or := ZeroWord
  }

}
