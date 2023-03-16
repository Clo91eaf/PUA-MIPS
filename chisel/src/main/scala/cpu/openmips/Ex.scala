package cpu.openmips

import chisel3._
import chisel3.util._
import cpu.openmips.Constants._
import scala.annotation.switch

class Ex extends Module {
  val io = IO(new Bundle {
    // 译码模块传来的信息
    val aluop_i = Input(AluOpBus)
    val alusel_i = Input(AluSelBus)
    val reg1_i = Input(RegBus)
    val reg2_i = Input(RegBus)
    val wd_i = Input(RegAddrBus)
    val wreg_i = Input(Bool())

    val hi_i = Input(RegBus)
    val lo_i = Input(RegBus)

    // 运算完毕后的结果
    val wd_o = Output(RegAddrBus)
    val wreg_o = Output(Bool())
    val wdata_o = Output(RegBus)

    // 运算完毕后
    val hi_o = Output(RegBus)
    val lo_o = Output(RegBus)
    val whilo_o = Output(Bool())
  })

  val wd_or = Reg(RegAddrBus)
  val wreg_or = Reg(Bool())
  val wdata_or = Reg(RegBus)
  val hi_or = Reg(RegBus)
  val lo_or = Reg(RegBus)
  val whilo_or = Reg(Bool())

  io.wd_o := wd_or
  io.wreg_o := wreg_or
  io.wdata_o := wdata_or
  io.hi_o := hi_or
  io.lo_o := lo_or
  io.whilo_o := whilo_or

//保存逻辑运算的结果
  val logicout = Reg(RegBus)
  val shiftres = Reg(RegBus)
  val moveres = Reg(RegBus)
  val HI = Reg(RegBus)
  val LO = Reg(RegBus)

// 根据aluop_i指示的运算子类型进行运算

//LOGIC
  when(reset.asBool === RstEnable) {
    logicout := ZeroWord
  }.otherwise {
    logicout := ZeroWord // default
    switch(io.aluop_i) {
      is(EXE_OR_OP) {
        logicout := io.reg1_i | io.reg2_i
      }
      is(EXE_AND_OP) {
        logicout := io.reg1_i & io.reg2_i
      }
      is(EXE_NOR_OP) {
        logicout := ~(io.reg1_i | io.reg2_i)
      }
      is(EXE_XOR_OP) {
        logicout := io.reg1_i ^ io.reg2_i
      }
    }
  }

// SHIFT
  when(reset.asBool === RstEnable) {
    shiftres := ZeroWord
  }.otherwise {
    shiftres := ZeroWord // default
    switch(io.aluop_i) {
      is(EXE_SLL_OP) {
        shiftres := io.reg2_i << io.reg1_i(4, 0)
      }
      is(EXE_SRL_OP) {
        shiftres := io.reg2_i >> io.reg1_i(4, 0)
      }
      is(EXE_SRA_OP) {
        shiftres := (io.reg2_i.asSInt >> io.reg1_i(4, 0)).asUInt
      }
    }
  }

  // 得到最新的HI、LO寄存器的值，此处要解决指令数据相关问题
  when(reset.asBool === RstEnable) {
    HI := ZeroWord
    LO := ZeroWord
  }.otherwise {
    HI := io.hi_i
    LO := io.lo_i
  }

//MFHI、MFLO、MOVN、MOVZ指令
  when(reset.asBool === RstEnable) {
    moveres := ZeroWord
  }.otherwise {
    moveres := ZeroWord
    switch(io.aluop_i) {
      is(EXE_MFHI_OP) {
        moveres := HI
      }
      is(EXE_MFLO_OP) {
        moveres := LO
      }
      is(EXE_MOVZ_OP) {
        moveres := io.reg1_i
      }
      is(EXE_MOVN_OP) {
        moveres := io.reg1_i
      }
    }
  }

//根据alusel_i指示的运算类型，选择一个运算结果作为最终结果
  wd_or := io.wd_i
  wreg_or := io.wreg_i
  wdata_or := ZeroWord // default
  switch(io.alusel_i) {
    is(EXE_RES_LOGIC) { wdata_or := logicout }
    is(EXE_RES_SHIFT) { wdata_or := shiftres }
    is(EXE_RES_MOVE) { wdata_or := moveres }
  }

//MTHI和MTLO指令
  when(reset.asBool === RstEnable) {
    whilo_or := WriteDisable
    hi_or := ZeroWord
    lo_or := ZeroWord
  }.elsewhen(io.aluop_i === EXE_MTHI_OP) {
    whilo_or := WriteEnable
    hi_or := io.reg1_i
    lo_or := LO
  }.elsewhen(io.aluop_i === EXE_MTLO_OP) {
    whilo_or := WriteEnable
    hi_or := HI
    lo_or := io.reg1_i
  }.otherwise {
    whilo_or := WriteDisable
    hi_or := ZeroWord
    lo_or := ZeroWord
  }
}
