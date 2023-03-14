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

//保存逻辑运算的结果
  val logicout = Reg(RegBus)
  val shiftres = Reg(RegBus)

// 根据aluop_i指示的运算子类型进行运算

//LOGIC
  when(reset.asBool === RstEnable) {
    logicout := ZeroWord
  }.otherwise {
    io.aluop_i match {
      case EXE_OR_OP => {
        logicout := io.reg1_i | io.reg2_i
      }
      case EXE_AND_OP => {
        logicout := io.reg1_i & io.reg2_i
      }
      case EXE_NOR_OP => {
        logicout := ~(io.reg1_i | io.reg2_i)
      }
      case EXE_XOR_OP => {
        logicout := io.reg1_i ^ io.reg2_i
      }
      case _ => {
        logicout := ZeroWord
      }
    }
  }

// SHIFT
  when(reset.asBool === RstEnable) {
    shiftres := ZeroWord
  }.otherwise {
    io.aluop_i match {
      case EXE_SLL_OP => {
        shiftres := io.reg2_i << io.reg1_i(4, 0)
      }
      case EXE_SRL_OP => {
        shiftres := io.reg2_i >> io.reg1_i(4, 0)
      }
      case EXE_SRA_OP => {
        shiftres := io.reg2_i.asSInt >> io.reg1_i(4, 0)
      }
      case _ => {
        shiftres := ZeroWord
      }
    }
  }

  wd_or := io.wd_i
  wreg_or := io.wreg_i

  io.alusel_i match {
    case EXE_RES_LOGIC => wdata_or := logicout
    case EXE_RES_SHIFT => wdata_or := shiftres
    case _             => wdata_or := ZeroWord
  }

}
