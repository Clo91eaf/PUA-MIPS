package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val fromMemory = Flipped(new Memory_WriteBackStage())

    val llbitReg   = new WriteBackStage_LLbitReg()
    val memory     = new WriteBackStage_Memory()
    val regFile    = new WriteBackStage_RegFile()
    val execute    = new WriteBackStage_Execute()
    val mov        = new WriteBackStage_Mov()
    val hilo       = new WriteBackStage_HILO()
    val cp0        = new WriteBackStage_CP0()
    val fetchStage = new WriteBackStage_FetchStage()
    val debug      = new DEBUG()
    val decoder    = new WriteBackStage_Decoder()
  })
  // input

  // output
  val pc           = RegInit(BUS_INIT)
  val reg_waddr    = RegInit(ADDR_BUS_INIT)
  val reg_wen      = RegInit(REG_WRITE_DISABLE)
  val reg_wdata    = RegInit(BUS_INIT)
  val hi           = RegInit(BUS_INIT)
  val lo           = RegInit(BUS_INIT)
  val whilo        = RegInit(WRITE_DISABLE)
  val LLbit_wen    = RegInit(false.B)
  val LLbit_value  = RegInit(false.B)
  val cp0_wen      = RegInit(WRITE_DISABLE)
  val cp0_waddr    = RegInit(CP0_ADDR_BUS_INIT)
  val cp0_wdata    = RegInit(BUS_INIT)
  val inst_is_mfc0 = RegInit(false.B)
  val allowin      = Wire(Bool())
  val ws_valid     = RegInit(false.B)
  val eret         = false.B
  val ex           = false.B

  // output-reg file
  io.regFile.reg_waddr := reg_waddr
  io.regFile.reg_wen   := reg_wen & Fill(4, ws_valid)
  io.regFile.reg_wdata := reg_wdata

  // output-hilo
  io.hilo.hi    := hi
  io.hilo.lo    := lo
  io.hilo.whilo := whilo

  // output-fetchStage
  io.fetchStage.eret := eret
  io.fetchStage.ex   := ex

  // output-decoder
  io.decoder.eret         := eret
  io.decoder.ex           := ex
  io.decoder.inst_is_mfc0 := inst_is_mfc0
  io.decoder.reg_waddr    := reg_waddr

  // output-execute
  io.execute.hi    := hi
  io.execute.lo    := lo
  io.execute.whilo := whilo
  io.execute.eret  := eret
  io.execute.ex    := ex

  // output-mov
  io.mov.cp0_wen   := cp0_wen
  io.mov.cp0_waddr := cp0_waddr
  io.mov.cp0_wdata := cp0_wdata

  // output-memory
  io.memory.LLbit_wen   := LLbit_wen
  io.memory.LLbit_value := LLbit_value
  io.memory.cp0_wen     := cp0_wen
  io.memory.cp0_waddr   := cp0_waddr
  io.memory.cp0_wdata   := cp0_wdata
  io.memory.allowin     := allowin
  io.memory.eret        := eret
  io.memory.ex          := ex

  // output-llbit reg
  io.llbitReg.LLbit_wen   := LLbit_wen
  io.llbitReg.LLbit_value := LLbit_value

  // output-cp0
  io.cp0.cp0_wen   := cp0_wen
  io.cp0.cp0_waddr := cp0_waddr
  io.cp0.cp0_wdata := cp0_wdata

  // output-debug
  io.debug.pc    := pc
  io.debug.waddr := io.regFile.reg_waddr
  io.debug.wen   := Fill(4, io.regFile.reg_wen.orR)
  io.debug.wdata := io.regFile.reg_wdata

  // io-finish

  val ready_go = true.B
  allowin := !ws_valid || ready_go

  when(allowin) {
    ws_valid := io.fromMemory.valid
  }

  when(io.fromMemory.valid && allowin) {
    // input-memory
    reg_waddr    := io.fromMemory.reg_waddr
    reg_wen      := io.fromMemory.reg_wen
    reg_wdata    := io.fromMemory.reg_wdata
    hi           := io.fromMemory.hi
    lo           := io.fromMemory.lo
    whilo        := io.fromMemory.whilo
    LLbit_wen    := io.fromMemory.LLbit_wen
    LLbit_value  := io.fromMemory.LLbit_value
    cp0_wen      := io.fromMemory.cp0_wen
    cp0_waddr    := io.fromMemory.cp0_waddr
    cp0_wdata    := io.fromMemory.cp0_wdata
    pc           := io.fromMemory.pc
    inst_is_mfc0 := io.fromMemory.inst_is_mfc0
  }

  // debug
  // printf(p"writeBackStage :pc 0x${Hexadecimal(pc)}\n")
}
