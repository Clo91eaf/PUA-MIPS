package cpu

import chisel3._
import chisel3.util._
import chisel3.internal.DontCareBinding

import defines._
import defines.Const._

import pipeline._
import pipeline.fetchStage._
import pipeline.decoder._
import pipeline.execute._
import pipeline.memory._
import pipeline.writeback._

class PuaMips extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
    val inst_sram = new INST_SRAM()
    val data_sram = new DATA_SRAM()
    val debug = new DEBUG()
  })
  val fetchStage = Module(new FetchStage())
  val decoderStage = Module(new DecoderStage())
  val decoder = Module(new Decoder())
  val executeStage = Module(new ExecuteStage())
  val alu = Module(new ALU())
  val execute = Module(new Execute())
  val memoryStage = Module(new MemoryStage())
  val memory = Module(new Memory())
  val writeBackStage = Module(new WriteBackStage())
  val regfile = Module(new Regfile())
  val llbitReg = Module(new LLbitReg())
  val divider = Module(new Divider())
  val hilo = Module(new HILO())
  val control = Module(new Control())
  val cp0 = Module(new CP0Reg())

  // func_test interfacter
  io.inst_sram.en                := fetchStage.io.instMemory.en
  io.inst_sram.wen               := fetchStage.io.instMemory.wen
  io.inst_sram.addr              := fetchStage.io.instMemory.addr
  io.inst_sram.wdata             := fetchStage.io.instMemory.wdata
  fetchStage.io.fromInstMemory.rdata := io.inst_sram.rdata

  io.data_sram.en := execute.io.dataMemory.mem_ce
  io.data_sram.wen := execute.io.dataMemory.mem_wsel & Fill(
    4,
    execute.io.dataMemory.mem_wen,
  )
  io.data_sram.addr := execute.io.dataMemory.mem_addr
  io.data_sram.wdata := execute.io.dataMemory.mem_wdata
  memory.io.fromDataMemory.mem_rdata := io.data_sram.rdata

  io.debug <> writeBackStage.io.debug

  // fetchStage
  fetchStage.io.decoderStage <> decoderStage.io.fromFetchStage

  // decoderStage
  decoderStage.io.decoder <> decoder.io.fromDecoderStage

  // decoder
  alu.io.execute          <> execute.io.fromAlu
  execute.io.alu          <> alu.io.fromExecute
  decoder.io.executeStage <> executeStage.io.fromDecoder
  decoder.io.regfile      <> regfile.io.fromDecoder
  decoder.io.fetchStage   <> fetchStage.io.fromDecoder
  decoder.io.decoderStage <> decoderStage.io.fromDecoder

  // executeStage
  executeStage.io.decoder <> decoder.io.fromExecuteStage
  executeStage.io.execute <> execute.io.fromExecuteStage

  // execute
  execute.io.control     <> control.io.fromExecute
  execute.io.decoder     <> decoder.io.fromExecute
  execute.io.memoryStage <> memoryStage.io.fromExecute
  execute.io.divider     <> divider.io.fromExecute
  execute.io.cp0         <> cp0.io.fromExecute

  // memoryStage
  memoryStage.io.execute <> execute.io.fromMemoryStage
  memoryStage.io.memory  <> memory.io.fromMemoryStage

  // memory
  memory.io.decoder        <> decoder.io.fromMemory
  memory.io.execute        <> execute.io.fromMemory
  memory.io.writeBackStage <> writeBackStage.io.fromMemory
  memory.io.control        <> control.io.fromMemory
  memory.io.cp0            <> cp0.io.fromMemory

  // writeBackStage
  writeBackStage.io.execute    <> execute.io.fromWriteBackStage
  writeBackStage.io.regFile    <> regfile.io.fromWriteBackStage
  writeBackStage.io.hilo       <> hilo.io.fromWriteBackStage
  writeBackStage.io.llbitReg   <> llbitReg.io.fromWriteBackStage
  writeBackStage.io.memory     <> memory.io.fromWriteBackStage
  writeBackStage.io.cp0        <> cp0.io.fromWriteBackStage
  writeBackStage.io.fetchStage <> fetchStage.io.fromWriteBackStage

  // hilo
  hilo.io.execute <> execute.io.fromHILO

  // reg file
  regfile.io.decoder <> decoder.io.fromRegfile

  // divider
  divider.io.annul := DontCare
  divider.io.execute <> execute.io.fromDivider

  // llbitReg
  llbitReg.io.flush := DontCare
  llbitReg.io.memory <> memory.io.fromLLbitReg

  // control
  control.io.executeStage   <> executeStage.io.fromControl
  control.io.memoryStage    <> memoryStage.io.fromControl
  control.io.writeBackStage <> writeBackStage.io.fromControl

  //cp0
  cp0.io.execute   <> execute.io.fromCP0
  cp0.io.fetchStage<>fetchStage.io.fromCP0
  cp0.io.memory    <> memory.io.fromCP0
  cp0.io.int_i     := Cat(io.ext_int(5,1), cp0.io.timer_int_o)
}
