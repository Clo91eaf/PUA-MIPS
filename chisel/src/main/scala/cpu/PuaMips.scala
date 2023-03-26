import chisel3._
import cpu.puamips._
import cpu.puamips.Const._

class PuaMips extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
    val inst_sram = new INST_SRAM()
    val data_sram = new DATA_SRAM()
    val debug = new DEBUG()
  })
  val fetch = Module(new Fetch())
  val decoder = Module(new Decoder())
  val regfile = Module(new Regfile())
  val execute = Module(new Execute())
  val memory = Module(new Memory())
  val writeBack = Module(new WriteBack())
  val hilo = Module(new HILO())
  // @formatter:off
  // func_test interfacter
  io.inst_sram.en := fetch.io.instMemory.ce 
  io.inst_sram.wen := WEN_BUS_INIT
  io.inst_sram.addr := fetch.io.instMemory.pc 
  io.inst_sram.wdata := REG_BUS_INIT
  fetch.io.fromInstMemory.inst := io.inst_sram.rdata 

  io.data_sram.en := memory.io.dataMemory.ce 
  io.data_sram.wen := memory.io.dataMemory.wen 
  io.data_sram.addr := memory.io.dataMemory.addr
  io.data_sram.wdata := memory.io.dataMemory.data
  memory.io.fromDataMemory.data := io.data_sram.rdata

  io.debug <> writeBack.io.debug

  // fetch
  fetch.io.decoder      <> decoder.io.fromFetch
  // fetch.io.instMemory   <> instMemory.io.fromFetch

  // inst memory
  // instMemory.io.fetch   <> fetch.io.fromInstMemory

  // data memory
  // dataMemory.io.memory  <> memory.io.fromDataMemory

  // decoder
  decoder.io.execute    <> execute.io.fromDecoder
  decoder.io.regfile    <> regfile.io.fromDecoder
  decoder.io.fetch      <> fetch.io.fromDecoder

  // execute
  execute.io.decoder    <> decoder.io.fromExecute
  execute.io.memory     <> memory.io.fromExecute

  // memory
  memory.io.decoder     <> decoder.io.fromMemory
  memory.io.execute     <> execute.io.fromMemory
  memory.io.writeBack   <> writeBack.io.fromMemory
  // memory.io.dataMemory  <> dataMemory.io.fromMemory

  // write back
  writeBack.io.execute  <> execute.io.fromWriteBack
  writeBack.io.regfile  <> regfile.io.fromWriteBack
  writeBack.io.hilo     <> hilo.io.fromWriteBack

  // hilo
  hilo.io.writeBack     <> writeBack.io.fromHILO

  // reg file
  regfile.io.decoder    <> decoder.io.fromRegfile

  // @formatter:on
}
