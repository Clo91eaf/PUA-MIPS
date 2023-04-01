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
  val wd = RegInit(REG_ADDR_BUS_INIT)
  io.regFile.wd := wd
  val wreg = RegInit(WRITE_DISABLE)
  io.regFile.wreg := wreg
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
  val LLbit_wen= RegInit(false.B)
  io.llbitReg.LLbit_wen:= LLbit_wen
  io.memory.LLbit_wen:= LLbit_wen
  val LLbit_value = RegInit(false.B)
  io.llbitReg.LLbit_value := LLbit_value
  io.memory.LLbit_value := LLbit_value
  val cp0_we = RegInit(WRITE_DISABLE)
  io.cp0.cp0_we := cp0_we
  io.execute.cp0_we := cp0_we
  val cp0_write_addr = RegInit(CP0_ADDR_BUS_INIT)
  io.cp0.cp0_write_addr := cp0_write_addr
  io.execute.cp0_write_addr := cp0_write_addr
  val cp0_data = RegInit(REG_BUS_INIT)
  io.cp0.cp0_data := cp0_data
  io.execute.cp0_data := cp0_data

  // output-debug
  io.debug.pc := pc
  io.debug.waddr := wd
  io.debug.wen:= Fill(4, wreg)
  io.debug.wdata := wdata

  when(stall(4) === STOP && stall(5) === NOT_STOP) {
    wd := NOP_REG_ADDR
    wreg := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    LLbit_wen:= false.B
    LLbit_value := false.B
    cp0_we := WRITE_DISABLE
    cp0_write_addr := 0.U
    cp0_data := ZERO_WORD
    pc := pc
  }.elsewhen(stall(4) === NOT_STOP) {
    wd := io.fromMemory.wd
    wreg := io.fromMemory.wreg
    wdata := io.fromMemory.wdata
    hi := io.fromMemory.hi
    lo := io.fromMemory.lo
    whilo := io.fromMemory.whilo
    LLbit_wen:= io.fromMemory.LLbit_wen
    LLbit_value := io.fromMemory.LLbit_value
    cp0_we := io.fromMemory.cp0_we
    cp0_write_addr := io.fromMemory.cp0_write_addr
    cp0_data := io.fromMemory.cp0_data
    pc := io.fromMemory.pc
  }

  // debug
  // printf(p"writeBackStage :pc 0x${Hexadecimal(pc)}\n")
}
