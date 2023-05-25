package cpu.pipeline.execute

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class ExecuteStage extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_ExecuteStage())
    val fromExecute = Flipped(new Execute_ExecuteStage())
    val fromCtrl    = Flipped(new Ctrl_Stage())

    val decoder = new ExecuteStage_Decoder()
    val execute = new ExecuteStage_Execute()
  })

  // output
  val aluop              = RegInit(ALU_OP_BUS_INIT)
  val alusel             = RegInit(ALU_SEL_BUS_INIT)
  val inst               = RegInit(BUS_INIT)
  val ex_is_in_delayslot = RegInit(NOT_IN_DELAY_SLOT)
  val is_in_delayslot    = RegInit(NOT_IN_DELAY_SLOT)
  val link_addr          = RegInit(BUS_INIT)
  val reg1               = RegInit(BUS_INIT)
  val reg2               = RegInit(BUS_INIT)
  val reg_waddr          = RegInit(ADDR_BUS_INIT)
  val reg_wen            = RegInit(REG_WRITE_DISABLE)
  val pc                 = RegInit(INST_ADDR_BUS_INIT)
  val es_valid           = RegInit(false.B)
  val ex                 = RegInit(false.B)
  val bd                 = RegInit(false.B)
  val badvaddr           = RegInit(BUS_INIT)
  val cp0_addr           = RegInit(0.U(8.W))
  val excode             = RegInit(0.U(5.W))
  val overflow_inst      = RegInit(false.B)
  val fs_to_ds_ex        = RegInit(false.B)
  val tlb_refill         = RegInit(false.B)
  val after_tlb          = RegInit(false.B)
  val mem_re             = RegInit(false.B)
  val mem_we             = RegInit(false.B)

  // output-execute
  // wire
  io.execute.do_flush := io.fromCtrl.do_flush
  io.execute.after_ex := io.fromCtrl.after_ex
  // reg
  io.execute.valid           := es_valid
  io.execute.aluop           := aluop
  io.execute.alusel          := alusel
  io.execute.inst            := inst
  io.execute.is_in_delayslot := ex_is_in_delayslot
  io.execute.link_addr       := link_addr
  io.execute.reg1            := reg1
  io.execute.reg2            := reg2
  io.execute.reg_waddr       := reg_waddr
  io.execute.reg_wen         := reg_wen
  io.execute.pc              := pc
  io.execute.bd              := bd
  io.execute.badvaddr        := badvaddr
  io.execute.cp0_addr        := cp0_addr
  io.execute.excode          := excode
  io.execute.overflow_inst   := overflow_inst
  io.execute.fs_to_ds_ex     := fs_to_ds_ex
  io.execute.ds_to_es_ex     := ex
  io.execute.tlb_refill      := tlb_refill
  io.execute.after_tlb       := after_tlb
  io.execute.mem_re          := mem_re
  io.execute.mem_we          := mem_we

  // output-decoder
  io.decoder.is_in_delayslot := is_in_delayslot

  when(io.fromCtrl.do_flush) {
    es_valid := false.B
  }.elsewhen(io.fromExecute.allowin) {
    es_valid := io.fromDecoder.valid
  }

  when(io.fromDecoder.valid && io.fromExecute.allowin) {
    aluop              := io.fromDecoder.aluop
    alusel             := io.fromDecoder.alusel
    reg1               := io.fromDecoder.reg1
    reg2               := io.fromDecoder.reg2
    reg_waddr          := io.fromDecoder.reg_waddr
    reg_wen            := io.fromDecoder.reg_wen
    link_addr          := io.fromDecoder.link_addr
    ex_is_in_delayslot := io.fromDecoder.is_in_delayslot
    is_in_delayslot    := io.fromDecoder.next_inst_in_delayslot
    inst               := io.fromDecoder.inst
    pc                 := io.fromDecoder.pc
    ex                 := io.fromDecoder.ex
    bd                 := io.fromDecoder.bd
    badvaddr           := io.fromDecoder.badvaddr
    cp0_addr           := io.fromDecoder.cp0_addr
    excode             := io.fromDecoder.excode
    overflow_inst      := io.fromDecoder.overflow_inst
    fs_to_ds_ex        := io.fromDecoder.fs_to_ds_ex
    tlb_refill         := io.fromDecoder.tlb_refill
    after_tlb          := io.fromDecoder.after_tlb
    mem_re             := io.fromDecoder.mem_re
    mem_we             := io.fromDecoder.mem_we
  }
}
