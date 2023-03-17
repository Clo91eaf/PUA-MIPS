package cpu.puamips 

import chisel3._

class Memory extends Module {
  val io = IO(new Bundle {
    val execute = Flipped(new Execute_Memory())
    val writeBack = new Memory_WriteBack()
  })
  // input-execute
  val wdata = RegInit(RegBusInit)
  val wd = RegInit(RegAddrBusInit)
  val wreg = RegInit(false.B)
  wdata := io.execute.wdata
  wd   := io.execute.wd 
  wreg  := io.execute.wreg 

  // output-write back
  io.writeBack.wdata := wdata
  io.writeBack.wd  := wd 
  io.writeBack.wreg  := wreg 
}