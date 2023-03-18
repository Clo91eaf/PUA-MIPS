package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class WriteBack extends Module {
  val io = IO(new Bundle {
    val fromMemory = Flipped(new Memory_WriteBack())
    val fromHilo = Flipped(new HILO_WriteBack())
    val hilo = new WriteBack_HILO()
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
  wd    := io.fromMemory.wd    
  wreg  := io.fromMemory.wreg  
  wdata := io.fromMemory.wdata 
  hi    := io.fromMemory.hi    
  lo    := io.fromMemory.lo    
  whilo := io.fromMemory.whilo 

  // input-hilo
  val we = RegInit(false.B)
  we := io.hilo.we
  hi := io.hilo.hi
  lo := io.hilo.lo

  // output-execute
  io.execute.whilo := whilo
  io.execute.hi := hi
  io.execute.lo := lo

  // output-regfile
  io.regfile.wd    := wd    
  io.regfile.wreg  := wreg  
  io.regfile.wdata := wdata 

  // output-hilo
  io.hilo.hi := hi
  io.hilo.lo := lo
}
