package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class MemoryStage extends Module {
  val io = IO(new Bundle {
    val fromExecute = Flipped(new Execute_MemoryStage())
    val fromMemory  = Flipped(new Memory_MemoryStage())
    val fromCtrl    = Flipped(new Ctrl_MemoryStage())

    val execute = new MemoryStage_Execute()
    val memory  = new MemoryStage_Memory()
  })

  // output
  // wire
  io.memory.after_ex := io.fromCtrl.after_ex
  io.memory.do_flush := io.fromCtrl.do_flush
  // reg
  val pc              = RegInit(BUS_INIT)
  val reg_waddr       = RegInit(ADDR_BUS_INIT)
  val reg_wen         = RegInit(REG_WRITE_DISABLE)
  val reg_wdata       = RegInit(BUS_INIT)
  val hi              = RegInit(BUS_INIT)
  val lo              = RegInit(BUS_INIT)
  val whilo           = RegInit(WRITE_DISABLE)
  val aluop           = RegInit(ALU_OP_BUS_INIT)
  val mem_addr        = RegInit(BUS_INIT)
  val reg2            = RegInit(BUS_INIT)
  val hilo            = RegInit(DOUBLE_BUS_INIT)
  val is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  val valid           = RegInit(false.B)
  val ex              = RegInit(false.B)
  val bd              = RegInit(false.B)
  val badvaddr        = RegInit(BUS_INIT)
  val cp0_addr        = RegInit(0.U(8.W))
  val excode          = RegInit(0.U(5.W))
  val data_ok         = RegInit(false.B)
  val data            = RegInit(BUS_INIT)
  val wait_mem        = RegInit(false.B)
  val res_from_mem    = RegInit(false.B)
  val tlb_refill      = RegInit(false.B)
  val after_tlb       = RegInit(false.B)
  val s1_found        = RegInit(false.B)
  val s1_index        = RegInit(0.U(log2Ceil(TLB_NUM).W))

  // output-memory
  io.memory.pc              := pc
  io.memory.reg_waddr       := reg_waddr
  io.memory.reg_wen         := reg_wen
  io.memory.reg_wdata       := reg_wdata
  io.memory.hi              := hi
  io.memory.lo              := lo
  io.memory.whilo           := whilo
  io.memory.aluop           := aluop
  io.memory.mem_addr        := mem_addr
  io.memory.reg2            := reg2
  io.memory.ex              := ex
  io.memory.bd              := bd
  io.memory.badvaddr        := badvaddr
  io.memory.cp0_addr        := cp0_addr
  io.memory.excode          := excode
  io.memory.data_ok         := data_ok
  io.memory.data            := data
  io.memory.wait_mem        := wait_mem
  io.memory.res_from_mem    := res_from_mem
  io.memory.is_in_delayslot := is_in_delayslot
  io.memory.valid           := valid
  io.memory.after_tlb       := after_tlb
  io.memory.s1_found        := s1_found
  io.memory.s1_index        := s1_index
  io.memory.tlb_refill      := tlb_refill

  // output-execute
  io.execute.hilo := hilo

  /*--------------------io finish--------------------*/
  when(io.fromCtrl.do_flush) {
    valid := false.B
  }.elsewhen(io.fromMemory.allowin) {
    valid := io.fromExecute.valid
  }

  when(io.fromExecute.valid && io.fromMemory.allowin) {
    reg_waddr       := io.fromExecute.reg_waddr
    reg_wen         := io.fromExecute.reg_wen
    reg_wdata       := io.fromExecute.reg_wdata
    hi              := io.fromExecute.hi
    lo              := io.fromExecute.lo
    whilo           := io.fromExecute.whilo
    hilo            := ZERO_WORD
    aluop           := io.fromExecute.aluop
    reg2            := io.fromExecute.reg2
    is_in_delayslot := io.fromExecute.is_in_delayslot
    pc              := io.fromExecute.pc
    mem_addr        := io.fromExecute.mem_addr
    ex              := io.fromExecute.ex
    bd              := io.fromExecute.bd
    badvaddr        := io.fromExecute.badvaddr
    cp0_addr        := io.fromExecute.cp0_addr
    excode          := io.fromExecute.excode
    data_ok         := io.fromExecute.data_ok
    data            := io.fromExecute.data
    wait_mem        := io.fromExecute.wait_mem
    res_from_mem    := io.fromExecute.res_from_mem
    tlb_refill      := io.fromExecute.tlb_refill
    after_tlb       := io.fromExecute.after_tlb
    s1_found        := io.fromExecute.s1_found
    s1_index        := io.fromExecute.s1_index
  }

  // debug
  // printf(p"MemoryStage :pc 0x${Hexadecimal(pc)}\n")

}
