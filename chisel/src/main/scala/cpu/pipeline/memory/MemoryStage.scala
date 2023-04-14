package cpu.pipeline.memory

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class MemoryStage extends Module {
  val io = IO(new Bundle {
    val fromDataMemory = Flipped(new DataMemory_DataStage())
    val fromExecute    = Flipped(new Execute_MemoryStage())
    val fromMemory     = Flipped(new Memory_MemoryStage())

    val execute = new MemoryStage_Execute()
    val memory  = new MemoryStage_Memory()
  })

  // output
  val pc                = RegInit(BUS_INIT)
  val reg_waddr         = RegInit(ADDR_BUS_INIT)
  val reg_wen           = RegInit(REG_WRITE_DISABLE)
  val reg_wdata         = RegInit(BUS_INIT)
  val hi                = RegInit(BUS_INIT)
  val lo                = RegInit(BUS_INIT)
  val whilo             = RegInit(WRITE_DISABLE)
  val aluop             = RegInit(ALU_OP_BUS_INIT)
  val mem_addr          = RegInit(BUS_INIT)
  val reg2              = RegInit(BUS_INIT)
  val hilo              = RegInit(DOUBLE_BUS_INIT)
  val cnt               = RegInit(CNT_BUS_INIT)
  val cp0_wen           = RegInit(WRITE_DISABLE)
  val cp0_waddr         = RegInit(CP0_ADDR_BUS_INIT)
  val cp0_wdata         = RegInit(BUS_INIT)
  val current_inst_addr = RegInit(BUS_INIT)
  val is_in_delayslot   = RegInit(NOT_IN_DELAY_SLOT)
  val except_type       = RegInit(0.U(32.W))
  val valid             = RegInit(false.B)

  // output-memory
  io.memory.pc        := pc
  io.memory.reg_waddr := reg_waddr
  io.memory.reg_wen   := reg_wen
  io.memory.reg_wdata := reg_wdata
  io.memory.hi        := hi
  io.memory.lo        := lo
  io.memory.whilo     := whilo
  io.memory.aluop     := aluop
  io.memory.mem_addr  := mem_addr
  io.memory.reg2      := reg2

  // output-execute
  io.execute.hilo := hilo
  io.execute.cnt  := cnt

  // output-memory
  io.memory.cp0_wen           := cp0_wen
  io.memory.cp0_waddr         := cp0_waddr
  io.memory.cp0_wdata         := cp0_wdata
  io.memory.current_inst_addr := current_inst_addr
  io.memory.is_in_delayslot   := is_in_delayslot
  io.memory.except_type       := except_type
  io.memory.valid             := valid

  // io-finish
  when(io.fromMemory.allowin) {
    valid := io.fromExecute.valid
  }

  when(io.fromExecute.valid && io.fromMemory.allowin) {
    reg_waddr         := io.fromExecute.reg_waddr
    reg_wen           := io.fromExecute.reg_wen
    reg_wdata         := io.fromExecute.reg_wdata
    hi                := io.fromExecute.hi
    lo                := io.fromExecute.lo
    whilo             := io.fromExecute.whilo
    hilo              := ZERO_WORD
    cnt               := 0.U
    aluop             := io.fromExecute.aluop
    reg2              := io.fromExecute.reg2
    cp0_wen           := io.fromExecute.cp0_wen
    cp0_waddr         := io.fromExecute.cp0_waddr
    cp0_wdata         := io.fromExecute.cp0_wdata
    except_type       := io.fromExecute.except_type
    is_in_delayslot   := io.fromExecute.is_in_delayslot
    current_inst_addr := io.fromExecute.current_inst_addr
    pc                := io.fromExecute.pc
    mem_addr          := io.fromExecute.mem_addr
  }

  // debug
  // printf(p"MemoryStage :pc 0x${Hexadecimal(pc)}\n")

}
