package cpu.pipeline.writeback

import chisel3._
import cpu.defines.Const._

import cpu.defines._
class LLbitReg extends Module {
  val io = IO(new Bundle {
    val fromWriteBackStage = Flipped(new WriteBackStage_LLbitReg())

    val memory = new LLbitReg_Memory()
    val flush  = Input(Bool())
  })
  // output
  val LLbit = RegInit(false.B)
  io.memory.LLbit := LLbit

  when(io.flush) {
    LLbit := false.B
  }.elsewhen(io.fromWriteBackStage.LLbit_wen === WRITE_ENABLE) {
    LLbit := io.fromWriteBackStage.LLbit_value
  }

  // debug
  // printf(p"LLbitReg :LLbit 0x${Hexadecimal(LLbit)}\n")

}
