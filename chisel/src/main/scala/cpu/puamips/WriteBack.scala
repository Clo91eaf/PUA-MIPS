package cpu.puamips

import chisel3._
import chisel3.util._
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
  val waddr = RegInit(REG_ADDR_BUS_INIT)
  val wen = RegInit(false.B)
  val wdata = RegInit(REG_BUS_INIT)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  val whilo = RegInit(false.B)
  pc := io.fromMemory.pc
  waddr := io.fromMemory.waddr
  wen := io.fromMemory.wen
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
  io.regfile.waddr := waddr
  io.regfile.wen := wen
  io.regfile.wdata := wdata

  // output-hilo
  io.hilo.whilo := whilo 
  io.hilo.hi := hi
  io.hilo.lo := lo

  // output-debug
  io.debug.pc := pc
  io.debug.waddr := waddr
  io.debug.wen := Fill(4, wen)
  io.debug.wdata := wdata

  // debug
  printf(p"write back :pc 0x${Hexadecimal(pc)}\n")
}
