package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class PreFetchStage extends Module {
  val io = IO(new Bundle {
    val fromFetchStage     = Flipped(new FetchStage_PreFetchStage())
    val fromDecoder        = Flipped(new Decoder_PreFetchStage())
    val fromInstMemory     = Flipped(new InstMemory_PreFetchStage())
    val fromWriteBackStage = Flipped(new WriteBackStage_PreFetchStage())

    val fetchStage = new PreFetchStage_FetchStage()
    val instMemory = new PreFetchStage_InstMemory()
  })
  val valid       = Wire(Bool())
  val ready_go    = Wire(Bool())
  val to_fs_valid = Wire(Bool())

  val br_leaving_ds = Wire(Bool())
  val br_stall      = Wire(Bool())
  val br_taken_w    = Wire(Bool())
  val br_target_w   = Wire(BUS)
  val bd_done_w     = Wire(Bool())
  val br_taken_r    = RegInit(false.B)
  val br_target_r   = RegInit(BUS_INIT)
  val bd_done_r     = RegInit(false.B)
  val br_taken      = Wire(Bool())
  val br_target     = Wire(BUS)
  val bd_done       = Wire(Bool())

  val seq_pc = RegInit(PC_INIT)
  val pc     = Mux(br_taken && bd_done, br_target, seq_pc)

  val inst_sram_req     = Wire(Bool())
  val inst_sram_addr    = Wire(BUS)
  val inst_waiting      = Wire(Bool())
  val inst_buff         = RegInit(BUS_INIT)
  val inst_buff_valid   = RegInit(false.B)
  val inst_ok           = Wire(Bool())
  val inst              = Wire(BUS)
  val inst_sram_data_ok = Wire(Bool())
  val addr_ok_r         = RegInit(false.B)
  val addr_ok           = Wire(Bool())

  // output
  io.fetchStage.valid := valid && ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  io.fetchStage.inst_ok := inst_ok
  io.fetchStage.inst    := inst
  io.fetchStage.pc      := pc
  io.instMemory.req     := inst_sram_req
  io.instMemory.addr    := inst_sram_addr
  io.instMemory.waiting := inst_waiting

  // handshake
  valid    := !reset.asBool
  ready_go := addr_ok
  to_fs_valid :=
    valid && ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex

  // branch
  br_leaving_ds := io.fromDecoder.br_leaving_ds
  br_stall      := io.fromDecoder.branch_stall

  br_taken_w  := io.fromDecoder.branch_flag
  br_target_w := io.fromDecoder.branch_target_address
  bd_done_w   := io.fromFetchStage.valid

  when(br_taken && valid && ready_go || io.fromWriteBackStage.ex || io.fromWriteBackStage.eret) {
    br_taken_r  := false.B
    br_target_r := BUS_INIT
    bd_done_r   := false.B
  }.elsewhen(br_leaving_ds) {
    br_taken_r  := br_taken_w
    br_target_r := br_taken_w
    bd_done_r   := io.fromFetchStage.valid || to_fs_valid && io.fromFetchStage.allowin
  }.elsewhen(br_taken && to_fs_valid && io.fromFetchStage.allowin && !bd_done) {
    bd_done_r := true.B
  }

  br_taken  := br_taken_r || br_taken_w
  br_target := Mux(br_taken_r, br_target_r, br_target_w)
  bd_done   := bd_done_r || bd_done_w

  // pc
  when(io.fromWriteBackStage.ex) {
    seq_pc := "hbfc00380".U
  }.elsewhen(io.fromWriteBackStage.eret) {
    seq_pc := io.fromWriteBackStage.cp0_epc
  }.elsewhen(ready_go && io.fromFetchStage.allowin) {
    seq_pc := pc + 4.U
  }

  // inst sram
  inst_sram_req := valid &&
    !addr_ok_r &&
    !(bd_done && br_stall) &&
    !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  inst_sram_addr := Cat(pc(31, 2), 0.U(2.W))
  inst_waiting   := addr_ok && !inst_ok

  inst_sram_data_ok := io.fromInstMemory.data_ok && io.fromFetchStage.inst_unable

  when(inst_sram_req && io.fromInstMemory.addr_ok && !io.fromFetchStage.allowin) {
    addr_ok_r := true.B
  }.elsewhen(io.fromFetchStage.allowin || io.fromWriteBackStage.eret || io.fromWriteBackStage.ex) {
    addr_ok_r := false.B
  }

  addr_ok := (inst_sram_req && io.fromInstMemory.addr_ok) || addr_ok_r

  when(io.fromFetchStage.allowin || io.fromWriteBackStage.eret || io.fromWriteBackStage.ex) {
    inst_buff_valid := false.B
    inst_buff       := 0.U
  }.elsewhen(addr_ok && inst_sram_data_ok && !io.fromFetchStage.allowin) {
    inst_buff_valid := true.B
    inst_buff       := io.fromInstMemory.rdata
  }

  inst_ok := inst_buff_valid || (addr_ok && inst_sram_data_ok)
  inst    := Mux(inst_buff_valid, inst_buff, io.fromInstMemory.rdata)
}
