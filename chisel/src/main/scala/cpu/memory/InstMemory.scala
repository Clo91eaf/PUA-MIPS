package cpu.pipeline

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class InstMemory extends Module {
  val io = IO(new Bundle {
    val fromPreFetchStage  = Flipped(new PreFetchStage_InstMemory())
    val fromFetchStage     = Flipped(new FetchStage_InstMemory())
    val fromWriteBackStage = Flipped(new WriteBackStage_InstMemory())
    val preFetchStage      = new InstMemory_PreFetchStage()
    val fetchStage         = new InstMemory_FetchStage()
    val instSram           = new InstMemory_InstSram()
  })
  val inst_sram_discard = RegInit(0.U(2.W))
  val inst_sram_data_ok_discard = io.instSram.data_ok && ~(inst_sram_discard.orR)

  io.preFetchStage.addr_ok := io.instSram.addr_ok
  io.preFetchStage.rdata   := io.instSram.rdata
  io.preFetchStage.data_ok := inst_sram_data_ok_discard

  io.fetchStage.rdata := io.instSram.rdata
  io.fetchStage.data_ok := inst_sram_data_ok_discard

  io.instSram.req   := io.fromPreFetchStage.req
  io.instSram.wr    := false.B
  io.instSram.size  := 2.U
  io.instSram.addr  := io.fromPreFetchStage.addr
  io.instSram.wstrb := 0.U(4.W)
  io.instSram.wdata := BUS_INIT

  when(io.fromWriteBackStage.ex || io.fromWriteBackStage.eret) {
    inst_sram_discard := Cat(io.fromPreFetchStage.waiting, io.fromFetchStage.waiting)
  }.elsewhen(io.instSram.data_ok) {
    when (inst_sram_discard === 3.U) {
      inst_sram_discard := 1.U
    }.elsewhen(inst_sram_discard === 1.U) {
      inst_sram_discard := 0.U
    }.elsewhen(inst_sram_discard === 2.U) {
      inst_sram_discard := 0.U
    }
  }
}
