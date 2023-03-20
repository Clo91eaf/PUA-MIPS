package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class WriteBack extends Module {
  val io = IO(new Bundle {
    val fromMemory = Flipped(new Memory_WriteBack())
    val fromHILO = Flipped(new HILO_WriteBack())
    val hilo = new WriteBack_HILO()
    val regfile = new WriteBack_RegFile()
    val execute = new WriteBack_Execute()
    val debug = new DEBUG()
  })
  // input-memory
  val pc = RegInit(REG_BUS_INIT)
  val wd = RegInit(REG_ADDR_BUS_INIT)
  val wreg = RegInit(false.B)
  val wdata = RegInit(REG_BUS_INIT)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  val whilo = RegInit(false.B)
  pc := io.fromMemory.pc
  wd := io.fromMemory.wd
  wreg := io.fromMemory.wreg
  wdata := io.fromMemory.wdata
  hi := io.fromMemory.hi
  lo := io.fromMemory.lo
  whilo := io.fromMemory.whilo

  // input-hilo
  hi := io.fromHILO.hi
  lo := io.fromHILO.lo

  // output-execute
  io.execute.whilo := whilo
  io.execute.hi := hi
  io.execute.lo := lo

  // output-regfile
  io.regfile.wd := wd
  io.regfile.wreg := wreg
  io.regfile.wdata := wdata

  // output-hilo
  val we = RegInit(false.B)
  io.hilo.we := we 
  io.hilo.hi := hi
  io.hilo.lo := lo

  // output-debug
  io.debug.pc := pc
  io.debug.wd := wd
  io.debug.wreg := wreg
  io.debug.wdata := wdata

  // debug
  printf(p"write back :pc 0x${Hexadecimal(pc)}\n")
}
