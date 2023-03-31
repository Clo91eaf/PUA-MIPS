package cpu.puamips

import Const._
import chisel3._

class MemoryStage extends Module {
  val io = IO(new Bundle {
    val fromControl = Flipped(new Control_MemoryStage())
    val fromExecute = Flipped(new Execute_MemoryStage())

    val execute = new MemoryStage_Execute()
    val memory = new MemoryStage_Memory()
  })
  // input
  val stall = Wire(STALL_BUS)
  stall := io.fromControl.stall

  // output
  val pc = RegInit(REG_BUS_INIT)
  io.memory.pc := pc
  val wd = RegInit(REG_ADDR_BUS_INIT)
  io.memory.wd := wd
  val wreg = RegInit(WRITE_DISABLE)
  io.memory.wreg := wreg
  val wdata = RegInit(REG_BUS_INIT)
  io.memory.wdata := wdata
  val hi = RegInit(REG_BUS_INIT)
  io.memory.hi := hi
  val lo = RegInit(REG_BUS_INIT)
  io.memory.lo := lo
  val whilo = RegInit(WRITE_DISABLE)
  io.memory.whilo := whilo
  val aluop = RegInit(ALU_OP_BUS_INIT)
  io.memory.aluop := aluop
  val addr = RegInit(REG_BUS_INIT)
  io.memory.addr := addr
  val reg2 = RegInit(REG_BUS_INIT)
  io.memory.reg2 := reg2
  val hilo = RegInit(DOUBLE_REG_BUS_INIT)
  io.execute.hilo := hilo
  val cnt = RegInit(CNT_BUS_INIT)
  io.execute.cnt := cnt

  when(stall(3) === STOP && stall(4) === NOT_STOP) {
    wd := NOP_REG_ADDR
    wreg := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    hilo := io.fromExecute.hilo
    cnt := io.fromExecute.cnt
    aluop := EXE_NOP_OP
    addr := ZERO_WORD
    reg2 := ZERO_WORD
    pc := pc
  }.elsewhen(stall(3) === NOT_STOP) {
    wd := io.fromExecute.wd
    wreg := io.fromExecute.wreg
    wdata := io.fromExecute.wdata
    hi := io.fromExecute.hi
    lo := io.fromExecute.lo
    whilo := io.fromExecute.whilo
    hilo := ZERO_WORD
    cnt := 0.U
    aluop := io.fromExecute.aluop
    addr := io.fromExecute.addr
    reg2 := io.fromExecute.reg2
    pc := io.fromExecute.pc
  }.otherwise {
    hilo := io.fromExecute.hilo
    cnt := io.fromExecute.cnt
  }

  // debug
  printf(p"MemoryStage :pc 0x${Hexadecimal(pc)}\n")

}
