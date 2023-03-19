package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import scala.annotation.switch
import firrtl.FirrtlProtos.Firrtl.Statement.Memory

class Execute extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_Execute())
    val fromMemory = Flipped(new Memory_Execute())
    val fromWriteBack = Flipped(new WriteBack_Execute())
    val decoder = new Execute_Decoder()
    val memory = new Execute_Memory()
  })
  // input-decoder
  val aluop = RegInit(ALU_OP_BUS_INIT)
  val alusel = RegInit(ALU_SEL_BUS_INIT)
  val reg1 = RegInit(REG_BUS_INIT)
  val reg2 = RegInit(REG_BUS_INIT)
  val wd = RegInit(REG_ADDR_BUS_INIT)
  val wreg = RegInit(false.B)
  aluop := io.fromDecoder.aluop
  alusel := io.fromDecoder.alusel
  reg1 := io.fromDecoder.reg1
  reg2 := io.fromDecoder.reg2
  wd := io.fromDecoder.wd
  wreg := io.fromDecoder.wreg

  // input-memory
  val whilo = RegInit(false.B)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  whilo := io.fromMemory.whilo
  hi := io.fromMemory.hi
  lo := io.fromMemory.lo

  // input-write back
  hi := io.fromWriteBack.hi
  lo := io.fromWriteBack.lo
  whilo := io.fromWriteBack.whilo

  // output-decoder
  val wdata = Output(REG_BUS)
  io.decoder.wdata := wdata
  io.decoder.wd := wd
  io.decoder.wreg := wreg

  // output-memory
  io.memory.wd := wd
  io.memory.wreg := wreg
  io.memory.wdata := wdata

//保存逻辑运算的结果
  val logicout = Reg(REG_BUS)
  val shiftres = Reg(REG_BUS)
  val moveres = Reg(REG_BUS)
  val HI = Reg(REG_BUS)
  val LO = Reg(REG_BUS)

// 根据aluop指示的运算子类型进行运算

//LOGIC
  when(reset.asBool === RST_ENABLE) {
    logicout := ZERO_WORD
  }.otherwise {
    logicout := ZERO_WORD // default
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
  when(reset.asBool === RST_ENABLE) {
    shiftres := ZERO_WORD
  }.otherwise {
    shiftres := ZERO_WORD // default
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
  when(reset.asBool === RST_ENABLE) {
    HI := ZERO_WORD
    LO := ZERO_WORD
  }.otherwise {
    HI := hi
    LO := lo
  }

//MFHI、MFLO、MOVN、MOVZ指令
  when(reset.asBool === RST_ENABLE) {
    moveres := ZERO_WORD
  }.otherwise {
    moveres := ZERO_WORD
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
  wdata := ZERO_WORD // default
  switch(alusel) {
    is(EXE_RES_LOGIC) { wdata := logicout }
    is(EXE_RES_SHIFT) { wdata := shiftres }
    is(EXE_RES_MOVE) { wdata := moveres }
  }

//MTHI和MTLO指令
  when(reset.asBool === RST_ENABLE) {
    whilo := WRITE_DISABLE
    hi := ZERO_WORD
    lo := ZERO_WORD
  }.elsewhen(aluop === EXE_MTHI_OP) {
    whilo := WRITE_ENABLE
    hi := reg1
    lo := LO
  }.elsewhen(aluop === EXE_MTLO_OP) {
    whilo := WRITE_ENABLE
    hi := HI
    lo := reg1
  }.otherwise {
    whilo := WRITE_DISABLE
    hi := ZERO_WORD
    lo := ZERO_WORD
  }
}
