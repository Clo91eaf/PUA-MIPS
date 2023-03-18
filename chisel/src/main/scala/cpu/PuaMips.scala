package cpu

import chisel3._
import cpu.puamips._
import cpu.puamips.Const._

class PuaMips extends Module {
  val io = IO(new Bundle {
    val rom_data_i = Input(RegBus)
    val rom_addr_o = Output(RegBus)
    val rom_ce_o = Output(Bool())
  })
  val fetch = Module(new Fetch())
  val instMemory = Module(new InstMemory())
  val decoder = Module(new Decoder())
  val regfile = Module(new Regfile())
  val execute = Module(new Execute())
  val memory = Module(new Memory())
  val writeBack = Module(new WriteBack())
  val hilo = Module(new HILO())

  // fetch
  fetch.io.decoder <> decoder.io.fromFetch
  fetch.io.instMemory <> instMemory.io.fetch
  
  // inst memory
  instMemory.io.decoder <> decoder.io.fromInstMemory

  // decoder
  decoder.io.execute <> execute.io.fromDecoder
  decoder.io.regfile <> regfile.io.fromDecoder

  // execute
  execute.io.decoder <> decoder.io.fromExecute
  execute.io.memory <> memory.io.fromExecute

  // memory
  memory.io.decoder <> decoder.io.fromMemory
  memory.io.execute <> execute.io.fromMemory
  memory.io.writeBack <> writeBack.io.fromMemory

  // write back
  writeBack.io.execute <> execute.io.fromWriteBack
  writeBack.io.regfile <> regfile.io.fromWriteBack
  writeBack.io.hilo <> hilo.io.fromWriteBack

  // hilo
  hilo.io.writeBack <> writeBack.io.fromHilo
}
