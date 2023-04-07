package cpu.pipeline

import cpu.defines.Const._
import chisel3._
import chisel3.util._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val fromControl = Flipped(new Control_WriteBackStage())
    val fromMemory  = Flipped(new Memory_WriteBackStage())

    val llbitReg = new WriteBackStage_LLbitReg()
    val memory   = new WriteBackStage_Memory()
    val regFile  = new WriteBackStage_RegFile()
    val execute  = new WriteBackStage_Execute()
    val hilo     = new WriteBackStage_HILO()
    val cp0      = new WriteBackStage_CP0()
    val debug    = new DEBUG()
  })
  // input
  val stall = Wire(STALL_BUS)

  // input-control
  stall := io.fromControl.stall

  // output
  val pc          = RegInit(BUS_INIT)
  val waddr       = RegInit(ADDR_BUS_INIT)
  val wen         = RegInit(WRITE_DISABLE)
  val wdata       = RegInit(BUS_INIT)
  val hi          = RegInit(BUS_INIT)
  val lo          = RegInit(BUS_INIT)
  val whilo       = RegInit(WRITE_DISABLE)
  val LLbit_wen   = RegInit(false.B)
  val LLbit_value = RegInit(false.B)
  val cp0_wen     = RegInit(WRITE_DISABLE)
  val cp0_waddr   = RegInit(CP0_ADDR_BUS_INIT)
  val cp0_data    = RegInit(BUS_INIT)

  // output-reg file
  io.regFile.waddr := waddr
  io.regFile.wen   := wen
  io.regFile.wdata := wdata

  // output-hilo
  io.hilo.hi    := hi
  io.hilo.lo    := lo
  io.hilo.whilo := whilo

  // output-execute
  io.execute.hi        := hi
  io.execute.lo        := lo
  io.execute.whilo     := whilo
  io.execute.cp0_wen   := cp0_wen
  io.execute.cp0_waddr := cp0_waddr
  io.execute.cp0_data  := cp0_data

  // output-memory
  io.memory.LLbit_wen   := LLbit_wen
  io.memory.LLbit_value := LLbit_value
  io.memory.cp0_wen     := cp0_wen
  io.memory.cp0_waddr   := cp0_waddr
  io.memory.cp0_data    := cp0_data

  // output-llbit reg
  io.llbitReg.LLbit_wen   := LLbit_wen
  io.llbitReg.LLbit_value := LLbit_value

  // output-cp0
  io.cp0.cp0_wen   := cp0_wen
  io.cp0.cp0_waddr := cp0_waddr
  io.cp0.cp0_data  := cp0_data

  // output-debug
  io.debug.pc    := pc
  io.debug.waddr := waddr
  io.debug.wen   := Fill(4, wen)
  io.debug.wdata := wdata

  // io-finish

  when(io.fromControl.flush) {
    waddr       := NOP_REG_ADDR
    wen         := WRITE_DISABLE
    wdata       := ZERO_WORD
    hi          := ZERO_WORD
    lo          := ZERO_WORD
    whilo       := WRITE_DISABLE
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := WRITE_DISABLE
    cp0_waddr   := "b00000".U
    cp0_data    := ZERO_WORD
  }.elsewhen(stall(4) === STOP && stall(5) === NOT_STOP) {
    waddr       := NOP_REG_ADDR
    wen         := WRITE_DISABLE
    wdata       := ZERO_WORD
    hi          := ZERO_WORD
    lo          := ZERO_WORD
    whilo       := WRITE_DISABLE
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := WRITE_DISABLE
    cp0_waddr   := 0.U
    cp0_data    := ZERO_WORD
    pc          := pc
  }.elsewhen(stall(4) === NOT_STOP) {
    // input-memory
    waddr       := io.fromMemory.waddr
    wen         := io.fromMemory.wen
    wdata       := io.fromMemory.wdata
    hi          := io.fromMemory.hi
    lo          := io.fromMemory.lo
    whilo       := io.fromMemory.whilo
    LLbit_wen   := io.fromMemory.LLbit_wen
    LLbit_value := io.fromMemory.LLbit_value
    cp0_wen     := io.fromMemory.cp0_wen
    cp0_waddr   := io.fromMemory.cp0_waddr
    cp0_data    := io.fromMemory.cp0_data
    pc          := io.fromMemory.pc
  }

  // debug
  // printf(p"writeBackStage :pc 0x${Hexadecimal(pc)}\n")
}
