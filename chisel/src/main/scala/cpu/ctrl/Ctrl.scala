package cpu.ctrl

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Ctrl extends Module {
  val io = IO(new Bundle {
    val fromInstMemory     = Flipped(new InstMemory_Ctrl())
    val fromDataMemory     = Flipped(new DataMemory_Ctrl())
    val fromFetchStage     = Flipped(new FetchStage_Ctrl())
    val fromDecoder        = Flipped(new Decoder_Ctrl())
    val fromExecute        = Flipped(new Execute_Ctrl())
    val fromMemory         = Flipped(new Memory_Ctrl())
    val fromWriteBackStage = Flipped(new WriteBackStage_Ctrl())

    val preFetchStage = new Ctrl_PreFetchStage()
    val fetchStage    = new Ctrl_FetchStage()
    val decoderStage  = new Ctrl_DecoderStage()
    val executeStage  = new Ctrl_ExecuteStage()
    val memoryStage   = new Ctrl_MemoryStage()
    val instMemory    = new Ctrl_InstMemory()
    val dataMemory    = new Ctrl_DataMemory()
  })

  val inst_sram_discard = io.fromInstMemory.inst_sram_discard
  val data_sram_discard = io.fromDataMemory.data_sram_discard

  val ws_do_flush = io.fromWriteBackStage.do_flush
  val ws_flush_pc = io.fromWriteBackStage.flush_pc

  val fs_ex = io.fromFetchStage.ex
  val ds_ex = io.fromDecoder.ex
  val es_ex = io.fromExecute.ex
  val ms_ex = io.fromMemory.ex
  val ws_ex = io.fromWriteBackStage.ex

  io.dataMemory.do_flush := ws_do_flush
  io.instMemory.do_flush := ws_do_flush

  io.preFetchStage.block    := inst_sram_discard.orR || data_sram_discard.orR
  io.preFetchStage.after_ex := fs_ex || ds_ex || es_ex || ms_ex || ws_ex
  io.preFetchStage.do_flush := ws_do_flush
  io.preFetchStage.flush_pc := ws_flush_pc

  io.fetchStage.after_ex := ds_ex || es_ex || ms_ex || ws_ex
  io.fetchStage.do_flush := ws_do_flush

  io.decoderStage.do_flush := ws_do_flush
  io.decoderStage.after_ex := es_ex || ms_ex || ws_ex

  io.executeStage.do_flush := ws_do_flush
  io.executeStage.after_ex := ms_ex || ws_ex

  io.memoryStage.do_flush := ws_do_flush
  io.memoryStage.after_ex := ws_ex
}
