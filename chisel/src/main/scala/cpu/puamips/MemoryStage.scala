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
  val flush = Wire(Bool())
  flush := io.fromControl.flush

  // output
  val pc = RegInit(BUS_INIT)
  io.memory.pc := pc
  val waddr = RegInit(ADDR_BUS_INIT)
  io.memory.waddr := waddr
  val wen = RegInit(WRITE_DISABLE)
  io.memory.wen := wen
  val wdata = RegInit(BUS_INIT)
  io.memory.wdata := wdata
  val hi = RegInit(BUS_INIT)
  io.memory.hi := hi
  val lo = RegInit(BUS_INIT)
  io.memory.lo := lo
  val whilo = RegInit(WRITE_DISABLE)
  io.memory.whilo := whilo
  val aluop = RegInit(ALU_OP_BUS_INIT)
  io.memory.aluop := aluop
  val addr = RegInit(BUS_INIT)
  io.memory.addr := addr
  val reg2 = RegInit(BUS_INIT)
  io.memory.reg2 := reg2
  val hilo = RegInit(DOUBLE_BUS_INIT)
  io.execute.hilo := hilo
  val cnt = RegInit(CNT_BUS_INIT)
  io.execute.cnt := cnt
  val cp0_wen = RegInit(WRITE_DISABLE)
  io.memory.cp0_wen := cp0_wen
  val cp0_waddr = RegInit(CP0_ADDR_BUS_INIT)
  io.memory.cp0_waddr := cp0_waddr
  val cp0_data = RegInit(BUS_INIT)
  io.memory.cp0_data := cp0_data
  val current_inst_addr = RegInit(BUS_INIT)
  io.memory.current_inst_addr := current_inst_addr
  val is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  io.memory.is_in_delayslot := is_in_delayslot
  val excepttype = RegInit(0.U(32.W))
  io.memory.excepttype := excepttype

  when(flush === true.B) {
    waddr := NOP_REG_ADDR
    wen := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    aluop := EXE_NOP_OP
    addr := ZERO_WORD
    reg2 := ZERO_WORD
    cp0_wen := WRITE_DISABLE
    cp0_waddr := "b00000".U
    cp0_data := ZERO_WORD
    excepttype := ZERO_WORD
    is_in_delayslot := NOT_IN_DELAY_SLOT
    current_inst_addr := ZERO_WORD
    hilo := ZERO_WORD
    cnt := "b00".U
    pc := ZERO_WORD
  }.elsewhen(stall(3) === STOP && stall(4) === NOT_STOP) {
    waddr := NOP_REG_ADDR
    wen := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    hilo := io.fromExecute.hilo
    cnt := io.fromExecute.cnt
    aluop := EXE_NOP_OP
    addr := ZERO_WORD
    reg2 := ZERO_WORD
    cp0_wen := WRITE_DISABLE
    cp0_waddr := 0.U
    cp0_data := ZERO_WORD
    excepttype := ZERO_WORD
    is_in_delayslot := NOT_IN_DELAY_SLOT
    current_inst_addr := ZERO_WORD
    pc := pc
  }.elsewhen(stall(3) === NOT_STOP) {
    waddr := io.fromExecute.waddr
    wen := io.fromExecute.wen
    wdata := io.fromExecute.wdata
    hi := io.fromExecute.hi
    lo := io.fromExecute.lo
    whilo := io.fromExecute.whilo
    hilo := ZERO_WORD
    cnt := 0.U
    aluop := io.fromExecute.aluop
    addr := io.fromExecute.addr
    reg2 := io.fromExecute.reg2
    cp0_wen := io.fromExecute.cp0_wen
    cp0_waddr := io.fromExecute.cp0_waddr
    cp0_data := io.fromExecute.cp0_data
    excepttype := io.fromExecute.excepttype
    is_in_delayslot := io.fromExecute.is_in_delayslot
    current_inst_addr := io.fromExecute.current_inst_addr
    pc := io.fromExecute.pc
  }.otherwise {
    hilo := io.fromExecute.hilo
    cnt := io.fromExecute.cnt
  }

  // debug
  // printf(p"MemoryStage :pc 0x${Hexadecimal(pc)}\n")

}
