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
  val pc = RegInit(REG_BUS_INIT)
  val wdata = RegInit(REG_BUS_INIT)
  val waddr = RegInit(REG_ADDR_BUS_INIT)
  val wen = RegInit(false.B)
  pc := io.fromExecute.pc
  wdata := io.fromExecute.wdata
  waddr := io.fromExecute.waddr
  wen := io.fromExecute.wen

  // output-decoder
  io.decoder.wdata := wdata
  io.decoder.waddr := waddr
  io.decoder.wen := wen

  // output-execute
  val whilo = RegInit(false.B)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  io.execute.whilo := whilo
  io.execute.hi := hi
  io.execute.lo := lo

  // output-write back
  io.writeBack.pc := pc
  io.writeBack.wdata := wdata
  io.writeBack.waddr := waddr
  io.writeBack.wen := wen
  io.writeBack.whilo := whilo
  io.writeBack.hi := hi
  io.writeBack.lo := lo

  printf(p"memory :pc 0x${Hexadecimal(pc)}\n")
}
