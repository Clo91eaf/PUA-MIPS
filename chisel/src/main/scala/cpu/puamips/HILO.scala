package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._

class HILO extends Module {
  val io = IO(new Bundle {
    val fromWriteBackStage = Flipped(new WriteBackStage_HILO())
    val execute = new HILO_Execute()
  })
  // output
  val hi = RegInit(REG_BUS_INIT)
  io.execute.hi := hi
  val lo = RegInit(REG_BUS_INIT)
  io.execute.lo := lo

  when(io.fromWriteBackStage.whilo === WRITE_ENABLE) {
    hi := io.fromWriteBackStage.hi
    lo := io.fromWriteBackStage.lo
  }

  printf(p"hilo :hi 0x${Hexadecimal(hi)}, lo 0x${Hexadecimal(lo)}\n")
}
