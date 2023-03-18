package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class WriteBack extends Module {
  val io = IO(new Bundle {
    val memory = Flipped(new Memory_WriteBack())
    val regfile = new WriteBack_RegFile() 
    val execute = new WriteBack_Execute()
})
  // input-memory 
  val wd    = RegInit(RegAddrBusInit)
  val wreg  = RegInit(false.B)
  val wdata = RegInit(RegBusInit)
  val hi    = RegInit(RegBusInit)
  val lo    = RegInit(RegBusInit)
  val whilo = RegInit(false.B)
  wd    := io.memory.wd    
  wreg  := io.memory.wreg  
  wdata := io.memory.wdata 
  hi    := io.memory.hi    
  lo    := io.memory.lo    
  whilo := io.memory.whilo 

  // output-execute
  io.execute.whilo := whilo
  io.execute.hi := hi
  io.execute.lo := lo

  // output-regfile
  io.regfile.wd    := wd    
  io.regfile.wreg  := wreg  
  io.regfile.wdata := wdata 
}
