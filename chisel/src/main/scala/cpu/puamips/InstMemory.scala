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
    val addr = Input(INST_ADDR_BUS)
    val inst = Output(INST_BUS)
  })
  // input-fetch
  val ce = RegInit(false.B)
  val pc = RegInit(REG_BUS_INIT)
  ce := io.fetch.ce
  pc := io.fetch.pc

  // output-decoder
  val inst = RegInit(REG_BUS_INIT)
  io.decoder.inst := inst

  val inst_mem = Mem(INST_MEM_NUM, INST_BUS)
  loadMemoryFromFile(inst_mem, "inst_rom.data")

  when(io.ce === CHIP_DISABLE) {
    inst := ZERO_WORD
  }.otherwise {
    inst := inst_mem(io.addr(INST_MEM_NUM_LOG2 + 1, 2))
  }
}
