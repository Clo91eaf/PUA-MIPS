package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import chisel3.util.experimental.loadMemoryFromFile

class InstMemory extends Module {
  val io = IO(new Bundle {
    val fromTop = new Top_InstMemory()
    val top = new InstMemory_Top()
  })
  // input-top
  val ce = RegInit(false.B)
  val pc = RegInit(REG_BUS_INIT)
  ce := io.fromTop.ce
  pc := io.fromTop.pc

  // output-top
  val inst = RegInit(REG_BUS_INIT)
  io.top.inst := inst

  val inst_mem = Mem(INST_MEM_NUM, INST_BUS)
  loadMemoryFromFile(inst_mem, "inst_rom.data")
  val addr = pc

  when(ce === CHIP_DISABLE) {
    inst := ZERO_WORD
  }.otherwise {
    inst := inst_mem(addr(INST_MEM_NUM_LOG2 + 1, 2))
  }
}
