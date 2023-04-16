package cpu

import chisel3._
import chisel3.util._
import chisel3.internal.DontCareBinding

import defines._
import defines.Const._

import memory._
import pipeline._
import pipeline.fetch._
import pipeline.decoder._
import pipeline.execute._
import pipeline.memory._
import pipeline.writeback._

class PuaMips extends Module {
  val io = IO(new Bundle {
    val ext_int   = Input(UInt(6.W))
    val inst_sram = new INST_SRAM()
    val data_sram = new DATA_SRAM()
    val debug     = new DEBUG()
  })
  val fetchStage     = Module(new FetchStage())
  val decoderStage   = Module(new DecoderStage())
  val decoder        = Module(new Decoder())
  val executeStage   = Module(new ExecuteStage())
  val execute        = Module(new Execute())
  val alu            = Module(new ALU())
  val mul            = Module(new Mul())
  val div            = Module(new Div())
  val mov            = Module(new Mov())
  val dataMemory     = Module(new DataMemory())
  val memoryStage    = Module(new MemoryStage())
  val memory         = Module(new Memory())
  val writeBackStage = Module(new WriteBackStage())
  val regfile        = Module(new Regfile())
  val llbitReg       = Module(new LLbitReg())
  val hilo           = Module(new HILO())
  val cp0            = Module(new CP0Reg())

  // func_test interfacter
  
  io.inst_sram.en                    := fetchStage.io.instMemory.en
  io.inst_sram.wen                   := fetchStage.io.instMemory.wen
  io.inst_sram.addr                  := fetchStage.io.instMemory.addr
  io.inst_sram.wdata                 := fetchStage.io.instMemory.wdata
  fetchStage.io.fromInstMemory.rdata := io.inst_sram.rdata

  io.data_sram.en                  := dataMemory.io.dataSram.en
  io.data_sram.wen                 := dataMemory.io.dataSram.wen
  io.data_sram.addr                := dataMemory.io.dataSram.addr
  io.data_sram.wdata               := dataMemory.io.dataSram.wdata
  dataMemory.io.fromDataSram.rdata := io.data_sram.rdata

  io.debug <> writeBackStage.io.debug

  // fetchStage
  fetchStage.io.decoderStage <> decoderStage.io.fromFetchStage

  // decoderStage
  decoderStage.io.decoder <> decoder.io.fromDecoderStage

  // decoder
  decoder.io.executeStage <> executeStage.io.fromDecoder
  decoder.io.regfile <> regfile.io.fromDecoder
  decoder.io.fetchStage <> fetchStage.io.fromDecoder
  decoder.io.decoderStage <> decoderStage.io.fromDecoder

  // executeStage
  executeStage.io.decoder <> decoder.io.fromExecuteStage
  executeStage.io.execute <> execute.io.fromExecuteStage

  // execute
  execute.io.alu <> alu.io.fromExecute
  execute.io.mul <> mul.io.fromExecute
  execute.io.div <> div.io.fromExecute
  execute.io.mov <> mov.io.fromExecute
  alu.io.execute <> execute.io.fromAlu
  mul.io.execute <> execute.io.fromMul
  div.io.execute <> execute.io.fromDiv
  mov.io.execute <> execute.io.fromMov

  execute.io.dataMemory <> dataMemory.io.fromExecute
  execute.io.decoder <> decoder.io.fromExecute
  execute.io.memoryStage <> memoryStage.io.fromExecute
  execute.io.executeStage <> executeStage.io.fromExecute

  // memoryStage
  memoryStage.io.execute <> execute.io.fromMemoryStage
  memoryStage.io.memory <> memory.io.fromMemoryStage

  // datasram
  dataMemory.io.memoryStage <> memoryStage.io.fromDataMemory
  dataMemory.io.memory <> memory.io.fromDataMemory

  // memory
  memory.io.decoder <> decoder.io.fromMemory
  memory.io.execute <> execute.io.fromMemory
  memory.io.mov <> mov.io.fromMemory
  memory.io.writeBackStage <> writeBackStage.io.fromMemory
  memory.io.memoryStage <> memoryStage.io.fromMemory

  // writeBackStage
  writeBackStage.io.decoder <> decoder.io.fromWriteBackStage
  writeBackStage.io.execute <> execute.io.fromWriteBackStage
  writeBackStage.io.mov <> mov.io.fromWriteBackStage
  writeBackStage.io.regFile <> regfile.io.fromWriteBackStage
  writeBackStage.io.hilo <> hilo.io.fromWriteBackStage
  writeBackStage.io.llbitReg <> llbitReg.io.fromWriteBackStage
  writeBackStage.io.memory <> memory.io.fromWriteBackStage
  writeBackStage.io.cp0 <> cp0.io.fromWriteBackStage
  writeBackStage.io.fetchStage <> fetchStage.io.fromWriteBackStage
  writeBackStage.io.ext_int := io.ext_int

  // cp0
  cp0.io.writeBackStage <> writeBackStage.io.fromCP0

  // hilo
  hilo.io.execute <> execute.io.fromHILO

  // regfile
  regfile.io.decoder <> decoder.io.fromRegfile

  // llbitReg
  llbitReg.io.flush := DontCare
  llbitReg.io.memory <> memory.io.fromLLbitReg

}
