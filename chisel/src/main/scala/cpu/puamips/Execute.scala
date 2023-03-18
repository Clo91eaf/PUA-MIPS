package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import scala.annotation.switch

class Execute extends Module {
  val io = IO(new Bundle {
    val decoder = Flipped(new Decoder_Execute())
    val writeBack = Flipped(new WriteBack_Execute())
    val memory = new Execute_Memory()

    // // 译码模块传来的信息
    // val hi = Input(RegBus)
    // val lo = Input(RegBus)

    // // 运算完毕后
    // val hi_o = Output(RegBus)
    // val lo_o = Output(RegBus)
    // val whilo_o = Output(Bool())
  })
  // input-decoder
  val aluop = RegInit(ALU_OP_BUS_INIT)
  val alusel = RegInit(ALU_SEL_BUS_INIT)
  val reg1 = RegInit(RegBusInit)
  val reg2 = RegInit(RegBusInit)
  val wd = RegInit(RegAddrBusInit)
  val wreg = RegInit(false.B)
  aluop  := io.decoder.aluop  
  alusel := io.decoder.alusel 
  reg1   := io.decoder.reg1   
  reg2   := io.decoder.reg2   
  wd    := io.decoder.wd   
  wreg   := io.decoder.wreg   

  // input-write back
  val hi = Reg(RegBus)
  val lo = Reg(RegBus)
  val whilo = Reg(Bool())
  hi := io.writeBack.hi
  lo := io.writeBack.lo
  whilo := io.writeBack.whilo

  // output-memory
  val wdata = RegInit(RegBusInit)
  io.memory.wd := wd
  io.memory.wreg := wreg
  io.memory.wdata := wdata



//保存逻辑运算的结果
  val logicout = Reg(RegBus)
  val shiftres = Reg(RegBus)
  val moveres = Reg(RegBus)
  val HI = Reg(RegBus)
  val LO = Reg(RegBus)

// 根据aluop指示的运算子类型进行运算

//LOGIC
  when(reset.asBool === RstEnable) {
    logicout := ZeroWord
  }.otherwise {
    logicout := ZeroWord // default
    switch(aluop) {
      is(EXE_OR_OP) {
        logicout := reg1 | reg2
      }
      is(EXE_AND_OP) {
        logicout := reg1 & reg2
      }
      is(EXE_NOR_OP) {
        logicout := ~(reg1 | reg2)
      }
      is(EXE_XOR_OP) {
        logicout := reg1 ^ reg2
      }
    }
  }

// SHIFT
  when(reset.asBool === RstEnable) {
    shiftres := ZeroWord
  }.otherwise {
    shiftres := ZeroWord // default
    switch(aluop) {
      is(EXE_SLL_OP) {
        shiftres := reg2 << reg1(4, 0)
      }
      is(EXE_SRL_OP) {
        shiftres := reg2 >> reg1(4, 0)
      }
      is(EXE_SRA_OP) {
        shiftres := (reg2.asSInt >> reg1(4, 0)).asUInt
      }
    }
  }

  // 得到最新的HI、LO寄存器的值，此处要解决指令数据相关问题
  when(reset.asBool === RstEnable) {
    HI := ZeroWord
    LO := ZeroWord
  }.otherwise {
    HI := hi
    LO := lo
  }

//MFHI、MFLO、MOVN、MOVZ指令
  when(reset.asBool === RstEnable) {
    moveres := ZeroWord
  }.otherwise {
    moveres := ZeroWord
    switch(aluop) {
      is(EXE_MFHI_OP) {
        moveres := HI
      }
      is(EXE_MFLO_OP) {
        moveres := LO
      }
      is(EXE_MOVZ_OP) {
        moveres := reg1
      }
      is(EXE_MOVN_OP) {
        moveres := reg1
      }
    }
  }

//根据alusel指示的运算类型，选择一个运算结果作为最终结果
  wd := wd
  wreg := wreg
  wdata := ZeroWord // default
  switch(alusel) {
    is(EXE_RES_LOGIC) { wdata := logicout }
    is(EXE_RES_SHIFT) { wdata := shiftres }
    is(EXE_RES_MOVE) { wdata := moveres }
  }

//MTHI和MTLO指令
  when(reset.asBool === RstEnable) {
    whilo := WriteDisable
    hi := ZeroWord
    lo := ZeroWord
  }.elsewhen(aluop === EXE_MTHI_OP) {
    whilo := WriteEnable
    hi := reg1
    lo := LO
  }.elsewhen(aluop === EXE_MTLO_OP) {
    whilo := WriteEnable
    hi := HI
    lo := reg1
  }.otherwise {
    whilo := WriteDisable
    hi := ZeroWord
    lo := ZeroWord
  }
}
