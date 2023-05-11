package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class WriteBackStage extends Module {
  val io = IO(new Bundle {
    val fromMemory = Flipped(new Memory_WriteBackStage())
    val fromCP0    = Flipped(new CP0_WriteBackStage())
    val ext_int    = Input(UInt(6.W))

    val preFetchStage = new WriteBackStage_PreFetchStage()
    val fetchStage    = new WriteBackStage_FetchStage()
    val instMemory    = new WriteBackStage_InstMemory()
    val decoderStage  = new WriteBackStage_DecoderStage()
    val decoder       = new WriteBackStage_Decoder()
    val regFile       = new WriteBackStage_RegFile()
    val execute       = new WriteBackStage_Execute()
    val memory        = new WriteBackStage_Memory()
    val dataMemory    = new WriteBackStage_DataMemory()
    val mov           = new WriteBackStage_Mov()
    val hilo          = new WriteBackStage_HILO()
    val cp0           = new WriteBackStage_CP0()
    val debug         = new DEBUG()
  })
  // input
  val ws_pc              = RegInit(BUS_INIT)
  val ws_reg_waddr       = RegInit(ADDR_BUS_INIT)
  val ws_reg_wen         = RegInit(REG_WRITE_DISABLE)
  val ws_reg_wdata       = RegInit(BUS_INIT)
  val ws_hi              = RegInit(BUS_INIT)
  val ws_lo              = RegInit(BUS_INIT)
  val ws_whilo           = RegInit(WRITE_DISABLE)
  val ws_valid           = RegInit(false.B)
  val ws_inst_is_mtc0    = RegInit(false.B)
  val ws_inst_is_mfc0    = RegInit(false.B)
  val ws_inst_is_eret    = RegInit(false.B)
  val ws_inst_is_syscall = RegInit(false.B)
  val ws_bd              = RegInit(false.B)
  val ws_badvaddr        = RegInit(BUS_INIT)
  val ws_cp0_addr        = RegInit(0.U(8.W))
  val ws_excode          = RegInit(0.U(5.W))
  val ws_ex              = RegInit(false.B)
  val ext_int            = io.ext_int
  // input-cp0
  val cp0_rdata = io.fromCP0.cp0_rdata

  // output
  val allowin      = Wire(Bool())
  val eret         = WireInit(false.B)
  val ex           = WireInit(false.B)
  val inst_is_mfc0 = Wire(Bool())
  val cp0_we       = Wire(Bool())
  val cp0_wdata    = Wire(UInt(32.W))
  val cp0_epc      = Wire(UInt(32.W))
  val cp0_status   = Wire(UInt(32.W))
  val cp0_cause    = Wire(UInt(32.W))

  // output-inst memory
  io.instMemory.ex   := ex
  io.instMemory.eret := eret

  // output-data memory
  io.dataMemory.ex   := ex
  io.dataMemory.eret := eret

  // output-reg file
  io.regFile.reg_waddr := ws_reg_waddr
  io.regFile.reg_wen   := ws_reg_wen & Fill(4, ws_valid & ~ws_ex)
  io.regFile.reg_wdata := Mux(ws_inst_is_mfc0, cp0_rdata, ws_reg_wdata)

  // output-hilo
  io.hilo.hi    := ws_hi
  io.hilo.lo    := ws_lo
  io.hilo.whilo := ws_whilo & ws_valid

  // output-preFetchStage
  io.preFetchStage.eret    := eret
  io.preFetchStage.ex      := ex
  io.preFetchStage.cp0_epc := cp0_epc

  // output-fetchStage
  io.fetchStage.eret    := eret
  io.fetchStage.ex      := ex
  io.fetchStage.cp0_epc := cp0_epc

  // output-decoder stage
  io.decoderStage.eret := eret
  io.decoderStage.ex   := ex

  // output-decoder
  io.decoder.inst_is_mfc0 := inst_is_mfc0
  io.decoder.reg_waddr    := ws_reg_waddr
  io.decoder.cp0_cause    := cp0_cause
  io.decoder.cp0_status   := cp0_status

  // output-execute
  io.execute.hi    := ws_hi
  io.execute.lo    := ws_lo
  io.execute.whilo := ws_whilo && ws_valid

  // output-mov
  io.mov.cp0_wen   := cp0_we
  io.mov.cp0_waddr := ws_cp0_addr
  io.mov.cp0_wdata := cp0_wdata
  io.mov.cp0_rdata := cp0_rdata

  // output-memory
  io.memory.allowin     := allowin
  io.memory.eret        := eret
  io.memory.ex          := ex

  // output-debug
  io.debug.pc    := ws_pc
  io.debug.waddr := io.regFile.reg_waddr
  io.debug.wen   := io.regFile.reg_wen
  io.debug.wdata := io.regFile.reg_wdata

  // output-cp0
  io.cp0.wb_ex       := ws_ex
  io.cp0.wb_bd       := ws_bd
  io.cp0.eret_flush  := ws_inst_is_eret
  io.cp0.wb_excode   := ws_excode
  io.cp0.wb_pc       := ws_pc
  io.cp0.wb_badvaddr := ws_badvaddr
  io.cp0.ext_int_in  := ext_int
  io.cp0.cp0_addr    := ws_cp0_addr
  io.cp0.mtc0_we     := cp0_we
  io.cp0.cp0_wdata   := cp0_wdata

  // io-finish

  val ready_go = true.B
  allowin := !ws_valid || ready_go

  when(allowin) {
    ws_valid := io.fromMemory.valid
  }

  when(io.fromMemory.valid && allowin) {
    // input-memory
    ws_reg_waddr       := io.fromMemory.reg_waddr
    ws_reg_wen         := io.fromMemory.reg_wen
    ws_reg_wdata       := io.fromMemory.reg_wdata
    ws_hi              := io.fromMemory.hi
    ws_lo              := io.fromMemory.lo
    ws_whilo           := io.fromMemory.whilo
    ws_pc              := io.fromMemory.pc
    ws_inst_is_mfc0    := io.fromMemory.inst_is_mfc0
    ws_inst_is_mtc0    := io.fromMemory.inst_is_mtc0
    ws_inst_is_syscall := io.fromMemory.inst_is_syscall
    ws_inst_is_eret    := io.fromMemory.inst_is_eret
    ws_excode          := io.fromMemory.excode
    ws_badvaddr        := io.fromMemory.badvaddr
    ws_cp0_addr        := io.fromMemory.cp0_addr
    ws_ex              := io.fromMemory.ex
    ws_bd              := io.fromMemory.bd
  }

  cp0_epc    := Fill(32, ws_valid) & io.fromCP0.cp0_epc
  cp0_status := Fill(32, ws_valid) & io.fromCP0.cp0_status
  cp0_cause  := Fill(32, ws_valid) & io.fromCP0.cp0_cause

  eret         := ws_inst_is_eret && ws_valid
  ex           := ws_ex && ws_valid
  inst_is_mfc0 := ws_inst_is_mfc0 && ws_valid

  cp0_we    := ws_inst_is_mtc0 && ws_valid && !ws_ex
  cp0_wdata := ws_reg_wdata

  // debug
  // printf(p"writeBackStage :pc 0x${Hexadecimal(pc)}\n")
}
