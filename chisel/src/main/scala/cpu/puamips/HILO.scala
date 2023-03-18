package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._

class HILO extends Module {
  val io = IO(new Bundle {
    val fromWriteBack = Flipped(new WriteBack_HILO())
    val writeBack = new HILO_WriteBack()
  })
  // input-write back
  val we = RegInit(Bool())
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  we := io.fromWriteBack.we
  hi := io.fromWriteBack.hi
  lo := io.fromWriteBack.lo
  // output-write back
  io.writeBack.hi := hi
  io.writeBack.lo := lo
}
