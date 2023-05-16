package cpu

import chisel3._
import chisel3.util._
import chisel3.internal.DontCareBinding

import defines._
import defines.Const._
import axi._
import pipeline.fetch._
import pipeline.decoder._
import pipeline.execute._
import pipeline.memory._
import pipeline.writeback._
import mmu._
import ctrl._
import os.write

class PuaMips extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
    val axi     = new AXI()
    val debug   = new DEBUG()
  })
  val preFetchStage  = Module(new PreFetchStage())
  val fetchStage     = Module(new FetchStage())
  val decoderStage   = Module(new DecoderStage())
  val decoder        = Module(new Decoder())
  val executeStage   = Module(new ExecuteStage())
  val execute        = Module(new Execute())
  val alu            = Module(new ALU())
  val mul            = Module(new Mul())
  val div            = Module(new Div())
  val mov            = Module(new Mov())
  val instMemory     = Module(new InstMemory())
  val dataMemory     = Module(new DataMemory())
  val sramAXITrans   = Module(new SramAXITrans())
  val memoryStage    = Module(new MemoryStage())
  val memory         = Module(new Memory())
  val writeBackStage = Module(new WriteBackStage())
  val regfile        = Module(new Regfile())
  val hilo           = Module(new HILO())
  val cp0            = Module(new CP0Reg())
  val instMMU        = Module(new InstMMU())
  val dataMMU        = Module(new DataMMU())
  val ctrl           = Module(new Ctrl())
  val tlb            = Module(new TLB())

  // axi interface
  io.axi <> sramAXITrans.io.axi
  sramAXITrans.io.dataMemory <> dataMemory.io.sramAXITrans
  sramAXITrans.io.instMemory <> instMemory.io.sramAXITrans

  // debug
  io.debug <> writeBackStage.io.debug

  // inst memory
  instMemory.io.preFetchStage <> preFetchStage.io.fromInstMemory
  instMemory.io.fetchStage <> fetchStage.io.fromInstMemory
  instMemory.io.ctrl <> ctrl.io.fromInstMemory

  // data memory
  dataMemory.io.execute <> execute.io.fromDataMemory
  dataMemory.io.memory <> memory.io.fromDataMemory

  // preFetchStage
  preFetchStage.io.fetchStage <> fetchStage.io.fromPreFetchStage
  preFetchStage.io.instMemory <> instMemory.io.fromPreFetchStage
  preFetchStage.io.instMMU <> instMMU.io.fromPreFetchStage

  // fetchStage
  fetchStage.io.preFetchStage <> preFetchStage.io.fromFetchStage
  fetchStage.io.decoderStage <> decoderStage.io.fromFetchStage
  fetchStage.io.instMemory <> instMemory.io.fromFetchStage
  fetchStage.io.ctrl <> ctrl.io.fromFetchStage

  // decoderStage
  decoderStage.io.decoder <> decoder.io.fromDecoderStage

  // decoder
  decoder.io.preFetchStage <> preFetchStage.io.fromDecoder
  decoder.io.fetchStage <> fetchStage.io.fromDecoder
  decoder.io.executeStage <> executeStage.io.fromDecoder
  decoder.io.regfile <> regfile.io.fromDecoder
  decoder.io.decoderStage <> decoderStage.io.fromDecoder
  decoder.io.ctrl <> ctrl.io.fromDecoder

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

  execute.io.decoder <> decoder.io.fromExecute
  execute.io.memoryStage <> memoryStage.io.fromExecute
  execute.io.dataMemory <> dataMemory.io.fromExecute
  execute.io.executeStage <> executeStage.io.fromExecute
  execute.io.ctrl <> ctrl.io.fromExecute
  execute.io.dataMMU <> dataMMU.io.fromExecute

  // memoryStage
  memoryStage.io.execute <> execute.io.fromMemoryStage
  memoryStage.io.memory <> memory.io.fromMemoryStage

  // data memory
  dataMemory.io.memory <> memory.io.fromDataMemory
  dataMemory.io.execute <> execute.io.fromDataMemory
  dataMemory.io.ctrl <> ctrl.io.fromDataMemory

  // memory
  memory.io.decoder <> decoder.io.fromMemory
  memory.io.mov <> mov.io.fromMemory
  memory.io.memoryStage <> memoryStage.io.fromMemory
  memory.io.dataMemory <> dataMemory.io.fromMemory
  memory.io.execute <> execute.io.fromMemory
  memory.io.writeBackStage <> writeBackStage.io.fromMemory
  memory.io.ctrl <> ctrl.io.fromMemory

  // writeBackStage
  writeBackStage.io.decoder <> decoder.io.fromWriteBackStage
  writeBackStage.io.execute <> execute.io.fromWriteBackStage
  writeBackStage.io.memory <> memory.io.fromWriteBackStage
  writeBackStage.io.regFile <> regfile.io.fromWriteBackStage
  writeBackStage.io.mov <> mov.io.fromWriteBackStage
  writeBackStage.io.hilo <> hilo.io.fromWriteBackStage
  writeBackStage.io.cp0 <> cp0.io.fromWriteBackStage
  writeBackStage.io.ext_int := io.ext_int
  writeBackStage.io.tlb <> tlb.io.fromWriteBackStage
  writeBackStage.io.ctrl <> ctrl.io.fromWriteBackStage
  writeBackStage.io.instMMU <> instMMU.io.fromWriteBackStage
  writeBackStage.io.dataMMU <> dataMMU.io.fromWriteBackStage

  // cp0
  cp0.io.writeBackStage <> writeBackStage.io.fromCP0

  // hilo
  hilo.io.execute <> execute.io.fromHILO

  // regfile
  regfile.io.decoder <> decoder.io.fromRegfile

  // ctrl
  ctrl.io.preFetchStage <> preFetchStage.io.fromCtrl
  ctrl.io.fetchStage <> fetchStage.io.fromCtrl
  ctrl.io.decoderStage <> decoderStage.io.fromCtrl
  ctrl.io.executeStage <> executeStage.io.fromCtrl
  ctrl.io.memoryStage <> memoryStage.io.fromCtrl
  ctrl.io.instMemory <> instMemory.io.fromCtrl
  ctrl.io.dataMemory <> dataMemory.io.fromCtrl

  // mmu
  dataMMU.io.execute <> execute.io.fromDataMMU
  dataMMU.io.tlb <> tlb.io.fromDataMMU
  dataMMU.io.dataMemory <> dataMemory.io.fromDataMMU

  instMMU.io.preFetchStage <> preFetchStage.io.fromInstMMU
  instMMU.io.tlb <> tlb.io.fromInstMMU
  instMMU.io.instMemory <> instMemory.io.fromInstMMU

  // tlb
  tlb.io.writeBackStage <> writeBackStage.io.fromTLB
  tlb.io.instMMU <> instMMU.io.fromTLB
  tlb.io.dataMMU <> dataMMU.io.fromTLB
  tlb.io.execute <> execute.io.fromTLB

}
