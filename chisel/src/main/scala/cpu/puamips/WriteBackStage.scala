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
  val LLbit_we = RegInit(false.B)
  io.llbitReg.LLbit_we := LLbit_we
  io.memory.LLbit_we := LLbit_we
  val LLbit_value = RegInit(false.B)
  io.llbitReg.LLbit_value := LLbit_value
  io.memory.LLbit_value := LLbit_value

  // output-debug
  io.debug.pc := pc
  io.debug.waddr := wd
  io.debug.we := Fill(4, wreg)
  io.debug.wdata := wdata

  when(stall(4) === STOP && stall(5) === NOT_STOP) {
    wd := NOP_REG_ADDR
    wreg := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    LLbit_we := false.B
    LLbit_value := false.B
    pc := pc
  }.elsewhen(stall(4) === NOT_STOP) {
    wd := io.fromMemory.wd
    wreg := io.fromMemory.wreg
    wdata := io.fromMemory.wdata
    hi := io.fromMemory.hi
    lo := io.fromMemory.lo
    whilo := io.fromMemory.whilo
    LLbit_we := io.fromMemory.LLbit_we
    LLbit_value := io.fromMemory.LLbit_value
    pc := io.fromMemory.pc
  }

  // debug
  printf(p"writeBackStage :pc 0x${Hexadecimal(pc)}\n")
}
