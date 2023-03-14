package cpu.ori

import cpu.ori.Constants._

import chisel3._
import chisel3.util._
import chisel3.util.experimental.loadMemoryFromFile

class Inst_rom extends Module {
  val io = IO(new Bundle {
    val ce = Input(Bool())
    val addr = Input(InstAddrBus)
    val inst = Output(InstBus)
  })
  val instr = Reg(InstBus)
  io.inst := instr

  val inst_mem = Mem(InstMemNum, InstBus)

  loadMemoryFromFile(inst_mem, "inst_rom.data")
  
  io.inst := Mux(io.ce === false.B, 0.U, inst_mem(io.addr >> 2))
}
