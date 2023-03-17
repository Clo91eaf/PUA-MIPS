package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import chisel3.util.experimental.loadMemoryFromFile

class InstMemory extends Module {
  val io = IO(new Bundle {
    val fetch = Flipped(new Fetch_InstMemory())
    val decoder = new InstMemory_Decoder()

    val ce = Input(Bool())
    val addr = Input(InstAddrBus)
    val inst = Output(InstBus)
  })
  // input-fetch
  val ce = RegInit(false.B)
  val pc = RegInit(RegBusInit)
  ce := io.fetch.ce
  pc := io.fetch.pc

  // output-decoder
  val inst = RegInit(RegBusInit)
  io.decoder.inst := inst

  val inst_mem = Mem(InstMemNum, InstBus)
  loadMemoryFromFile(inst_mem, "inst_rom.data")

  when(io.ce === ChipDisable) {
    inst := ZeroWord
  }.otherwise {
    inst := inst_mem(io.addr(InstMemNumLog2 + 1, 2))
  }
}
