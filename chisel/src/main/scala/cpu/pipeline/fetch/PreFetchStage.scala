package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines._

class PreFetchStage extends Module {
  val io = IO(new Bundle {
    val fromFetchStage     = Flipped(new FetchStage_PreFetchStage())
    val fromDecoder        = Flipped(new Decoder_PreFetchStage())
    val fromInstMemory     = Flipped(new InstMemory_PreFetchStage())
    val fromWriteBackStage = Flipped(new WriteBackStage_PreFetchStage())

    val fetchStage = new PreFetchStage_FetchStage()
    val instMemory = new PreFetchStage_InstMemory()
  })
  // output
  io.fetchStage.valid := valid && ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  io.fetchStage.inst_ok := inst_ok
  io.fetchStage.inst    := inst
  io.fetchStage.pc      := pc
  io.instMemory.req     := inst_sram_req
  io.instMemory.addr    := inst_sram_addr
  io.instMemory.waiting := inst_waiting

  // handshake
  val valid    = !reset.asBool
  val ready_go = addr_ok
  val to_fs_valid =
    valid && ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex

  // branch
  val br_leaving_ds = io.fromDecoder.br_leaving_ds
  val br_stall      = io.fromDecoder.branch_stall

  val br_taken_w  = io.fromDecoder.branch_flag
  val br_target_w = io.fromDecoder.branch_target_address
  val bd_done_w   = io.fromFetchStage.valid

  val br_taken_r  = RegInit(false.B)
  val br_target_r = RegInit(0.U(32.W))
  val bd_done_r   = RegInit(false.B)

  val br_taken  = br_taken_r || br_taken_w
  val br_target = Mux(br_taken_r, br_target_r, br_target_w)
  val bd_done   = bd_done_r || bd_done_w

  when(br_leaving_ds) {
    br_taken_r  := br_taken_w
    br_target_r := br_target_w
    bd_done_r   := io.fromFetchStage.valid || (valid && io.fromFetchStage.allowin && !bd_done)
  }.elsewhen(
    !br_leaving_ds && (io.fromWriteBackStage.eret || io.fromWriteBackStage.ex || io.fromFetchStage.allowin),
  ) {
    br_taken_r  := false.B
    br_target_r := 0.U
    bd_done_r   := false.B
  }

  // pc
  val seq_pc = RegInit("h_bfc00000".U(32.W))
  when(io.fromWriteBackStage.ex) {
    seq_pc := "h80000180".U
  }.elsewhen(io.fromWriteBackStage.eret) {
    seq_pc := io.fromWriteBackStage.cp0_epc
  }.elsewhen(ready_go && io.fromFetchStage.allowin) {
    seq_pc := pc + 4.U
  }
  val pc = Mux(br_taken && bd_done, br_target, seq_pc)

  // inst sram
  val inst_sram_req = valid &&
    !io.fromInstMemory.addr_ok &&
    !(bd_done && br_stall) &&
    !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  val inst_sram_addr = Cat(pc(31, 2), 0.U(2.W))
  val inst_waiting   = ready_go && !inst_ok

  val inst_buff = RegInit(0.U(32.W))

  when(io.fromFetchStage.allowin || io.fromWriteBackStage.eret || io.fromWriteBackStage.ex) {
    inst_buff := 0.U
  }.elsewhen(ready_go && io.fromInstMemory.data_ok && !io.fromFetchStage.allowin) {
    inst_buff := io.fromInstMemory.rdata
  }

  val inst_ok = inst_buff.orR || (ready_go && io.fromInstMemory.data_ok)
  val inst    = Mux(inst_buff.orR, inst_buff, io.fromInstMemory.rdata)

  val inst_sram_data_ok = io.fromInstMemory.data_ok && io.fromFetchStage.inst_unable

  val addr_ok_r = RegInit(false.B)
  when(inst_sram_req && io.fromInstMemory.addr_ok && !io.fromFetchStage.allowin) {
    addr_ok_r := true.B
  }.elsewhen(io.fromFetchStage.allowin || io.fromWriteBackStage.eret || io.fromWriteBackStage.ex) {
    addr_ok_r := false.B
  }

  val addr_ok = (inst_sram_req && io.fromInstMemory.addr_ok) || addr_ok_r
}
