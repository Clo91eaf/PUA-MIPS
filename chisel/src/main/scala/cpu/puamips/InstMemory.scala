package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import chisel3.util.experimental.loadMemoryFromFile

class InstMemory extends Module {
  val io = IO(new Bundle {
    val ce = Input(Bool())
    val addr = Input(InstAddrBus)
    val inst = Output(InstBus)
  })
  val instr = Reg(InstBus)
  io.inst := instr

  val inst_mem = Mem(InstMemNum, InstBus)

  loadMemoryFromFile(inst_mem, "inst_rom.data")

  when(io.ce === ChipDisable) {
    instr := ZeroWord
  }.otherwise {
    instr := inst_mem(io.addr(InstMemNumLog2 + 1, 2))
  }
}
