package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Memory extends Module {
  val io = IO(new Bundle {
    val fromExecute = Flipped(new Execute_Memory())
    val decoder = new Memory_Decoder()
    val execute = new Memory_Execute()
    val writeBack = new Memory_WriteBack()
  })
  // input-execute
  val wdata = RegInit(REG_BUS_INIT)
  val wd = RegInit(REG_ADDR_BUS_INIT)
  val wreg = RegInit(false.B)
  wdata := io.fromExecute.wdata
  wd := io.fromExecute.wd
  wreg := io.fromExecute.wreg

  // output-decoder
  io.decoder.wdata := wdata
  io.decoder.wd := wd
  io.decoder.wreg := wreg

  // output-execute
  val whilo = RegInit(false.B)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  io.execute.whilo := whilo
  io.execute.hi := hi
  io.execute.lo := lo

  // output-write back
  io.writeBack.wdata := wdata
  io.writeBack.wd := wd
  io.writeBack.wreg := wreg
  io.writeBack.whilo := whilo
  io.writeBack.hi := hi
  io.writeBack.lo := lo
}
