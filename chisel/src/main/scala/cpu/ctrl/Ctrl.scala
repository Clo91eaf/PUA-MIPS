package cpu.ctrl

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Ctrl extends Module {
  val io = IO(new Bundle {
    val fromInstMemory     = Flipped(new Sram_Ctrl())
    val fromDataMemory     = Flipped(new Sram_Ctrl())
    val fromFetchStage     = Flipped(new Pipeline_Ctrl())
    val fromDecoder        = Flipped(new Pipeline_Ctrl())
    val fromExecute        = Flipped(new Pipeline_Ctrl())
    val fromMemory         = Flipped(new Pipeline_Ctrl())
    val fromWriteBackStage = Flipped(new WriteBackStage_Ctrl())

    val preFetchStage = new Ctrl_PreFetchStage()
    val fetchStage    = new Ctrl_Stage()
    val decoderStage  = new Ctrl_Stage()
    val executeStage  = new Ctrl_Stage()
    val memoryStage   = new Ctrl_Stage()
    val InstSram    = new Ctrl_Sram()
    val DataSram    = new Ctrl_Sram()
  })

  val inst_sram_discard = io.fromInstMemory.sram_discard
  val data_sram_discard = io.fromDataMemory.sram_discard

  val ws_do_flush = io.fromWriteBackStage.do_flush
  val ws_flush_pc = io.fromWriteBackStage.flush_pc

  val fs_ex = io.fromFetchStage.ex
  val ds_ex = io.fromDecoder.ex
  val es_ex = io.fromExecute.ex
  val ms_ex = io.fromMemory.ex
  val ws_ex = io.fromWriteBackStage.ex

  io.preFetchStage.block    := inst_sram_discard.orR || data_sram_discard.orR
  io.preFetchStage.flush_pc := ws_flush_pc

  io.DataSram.do_flush    := ws_do_flush
  io.InstSram.do_flush    := ws_do_flush
  io.preFetchStage.do_flush := ws_do_flush
  io.fetchStage.do_flush    := ws_do_flush
  io.decoderStage.do_flush  := ws_do_flush
  io.executeStage.do_flush  := ws_do_flush
  io.memoryStage.do_flush   := ws_do_flush

  io.preFetchStage.after_ex := fs_ex || ds_ex || es_ex || ms_ex || ws_ex
  io.fetchStage.after_ex    := ds_ex || es_ex || ms_ex || ws_ex
  io.decoderStage.after_ex  := es_ex || ms_ex || ws_ex
  io.executeStage.after_ex  := ms_ex || ws_ex
  io.memoryStage.after_ex   := ws_ex
}
