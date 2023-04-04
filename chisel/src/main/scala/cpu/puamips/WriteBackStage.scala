package cpu.puamips

import Const._
import chisel3._
import chisel3.util._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val fromControl = Flipped(new Control_WriteBackStage())
    val fromMemory = Flipped(new Memory_WriteBackStage())

    val llbitReg = new WriteBackStage_LLbitReg()
    val memory = new WriteBackStage_Memory()
    val regFile = new WriteBackStage_RegFile()
    val execute = new WriteBackStage_Execute()
    val hilo = new WriteBackStage_HILO()
    val cp0 = new WriteBackStage_CP0()
    val debug = new DEBUG()
  })
  // input
  val stall = Wire(STALL_BUS)
  stall := io.fromControl.stall

  // output
  val pc = RegInit(REG_BUS_INIT)
  val waddr = RegInit(REG_ADDR_BUS_INIT)
  io.regFile.waddr := waddr
  val wen = RegInit(WRITE_DISABLE)
  io.regFile.wen := wen
  val wdata = RegInit(REG_BUS_INIT)
  io.regFile.wdata := wdata
  val hi = RegInit(REG_BUS_INIT)
  io.hilo.hi := hi
  io.execute.hi := hi
  val lo = RegInit(REG_BUS_INIT)
  io.hilo.lo := lo
  io.execute.lo := lo
  val whilo = RegInit(WRITE_DISABLE)
  io.hilo.whilo := whilo
  io.execute.whilo := whilo
  val LLbit_wen = RegInit(false.B)
  io.llbitReg.LLbit_wen := LLbit_wen
  io.memory.LLbit_wen := LLbit_wen
  val LLbit_value = RegInit(false.B)
  io.llbitReg.LLbit_value := LLbit_value
  io.memory.LLbit_value := LLbit_value
  val cp0_wen = RegInit(WRITE_DISABLE)
  io.cp0.cp0_wen := cp0_wen
  io.execute.cp0_wen := cp0_wen
  io.memory.cp0_wen := cp0_wen
  val cp0_waddr = RegInit(CP0_ADDR_BUS_INIT)
  io.cp0.cp0_waddr := cp0_waddr
  io.execute.cp0_waddr := cp0_waddr
  io.memory.cp0_waddr := cp0_waddr
  val cp0_data = RegInit(REG_BUS_INIT)
  io.cp0.cp0_data := cp0_data
  io.execute.cp0_data := cp0_data
  io.memory.cp0_data := cp0_data

  // output-debug
  io.debug.pc := pc
  io.debug.waddr := waddr
  io.debug.wen := Fill(4, wen)
  io.debug.wdata := wdata

  when(io.fromControl.flush) {
    waddr := NOP_REG_ADDR
    wen := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    LLbit_wen := false.B
    LLbit_value := false.B
    cp0_wen := WRITE_DISABLE
    cp0_waddr := "b00000".U
    cp0_data := ZERO_WORD
  }.elsewhen(stall(4) === STOP && stall(5) === NOT_STOP) {
    waddr := NOP_REG_ADDR
    wen := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    LLbit_wen := false.B
    LLbit_value := false.B
    cp0_wen := WRITE_DISABLE
    cp0_waddr := 0.U
    cp0_data := ZERO_WORD
    pc := pc
  }.elsewhen(stall(4) === NOT_STOP) {
    waddr := io.fromMemory.waddr
    wen := io.fromMemory.wen
    wdata := io.fromMemory.wdata
    hi := io.fromMemory.hi
    lo := io.fromMemory.lo
    whilo := io.fromMemory.whilo
    LLbit_wen := io.fromMemory.LLbit_wen
    LLbit_value := io.fromMemory.LLbit_value
    cp0_wen := io.fromMemory.cp0_wen
    cp0_waddr := io.fromMemory.cp0_waddr
    cp0_data := io.fromMemory.cp0_data
    pc := io.fromMemory.pc
  }

  // debug
  // printf(p"writeBackStage :pc 0x${Hexadecimal(pc)}\n")
}
