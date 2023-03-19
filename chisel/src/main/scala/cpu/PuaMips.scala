package cpu

import chisel3._
import cpu.puamips._
import cpu.puamips.Const._

class PuaMips extends Module {
  val io = IO(new Bundle {
    val pc = Output(REG_BUS)
    val ce = Output(Bool())
    val inst = Input(REG_BUS)
    // val success = Output(Bool())
    // val fromFetch = Flipped(new Fetch_Top())
    // val fromInstMemory = Flipped(new InstMemory_Top())
    // val decoder = new Top_Decoder()
    // val instMemory = new Top_InstMemory()
    // val rom_data_i = Input(REG_BUS)
    // val rom_addr_o = Output(REG_BUS)
    // val rom_ce_o = Output(Bool())
  })
  val fetch = Module(new Fetch())
  val decoder = Module(new Decoder())
  val regfile = Module(new Regfile())
  val execute = Module(new Execute())
  val memory = Module(new Memory())
  val writeBack = Module(new WriteBack())
  val hilo = Module(new HILO())

  // top
  io.pc <> fetch.io.top.pc
  io.ce <> fetch.io.top.ce
  io.inst <> decoder.io.fromTop.inst
  // io.success <> writeBack.io.success
  // fetch.io.top          <> io.fromFetch
  // io.fromFetch          <> io.instMemory
  // io.fromInstMemory     <> io.decoder
  // io.decoder            <> decoder.io.fromTop

  // fetch
  fetch.io.decoder      <> decoder.io.fromFetch

  // decoder
  decoder.io.execute    <> execute.io.fromDecoder
  decoder.io.regfile    <> regfile.io.fromDecoder

  // execute
  execute.io.decoder    <> decoder.io.fromExecute
  execute.io.memory     <> memory.io.fromExecute

  // memory
  memory.io.decoder     <> decoder.io.fromMemory
  memory.io.execute     <> execute.io.fromMemory
  memory.io.writeBack   <> writeBack.io.fromMemory

  // write back
  writeBack.io.execute  <> execute.io.fromWriteBack
  writeBack.io.regfile  <> regfile.io.fromWriteBack
  writeBack.io.hilo     <> hilo.io.fromWriteBack

  // hilo
  hilo.io.writeBack     <> writeBack.io.fromHilo

  // reg file
  regfile.io.decoder    <> decoder.io.fromRegfile
}
