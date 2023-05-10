package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class PreFetchStage extends Module {
  val io = IO(new Bundle {
    val fromFetchStage = Flipped(new FetchStage_PreFetchStage())
    val fromDecoder    = Flipped(new Decoder_PreFetchStage())
    val fromInstMemory = Flipped(new InstMemory_PreFetchStage())
    val fromInstMMU    = Flipped(new MMU_Common())
    val fromCtrl       = Flipped(new Ctrl_PreFetchStage())

    val fetchStage = new PreFetchStage_FetchStage()
    val instMemory = new PreFetchStage_InstMemory()
  })
  val tlb_refill   = io.fromInstMMU.tlb_refill
  val tlb_invalid  = io.fromInstMMU.tlb_invalid
  val tlb_modified = io.fromInstMMU.tlb_modified
  val pfs_block    = io.fromCtrl.block
  val fs_allowin   = io.fromFetchStage.allowin

  // exception handle
  val after_ex = io.fromCtrl.after_ex
  val do_flush = io.fromCtrl.do_flush
  val flush_pc = io.fromCtrl.flush_pc

  val pfs_valid       = RegInit(false.B)
  val to_pfs_valid    = Wire(Bool())
  val pfs_allowin     = Wire(Bool())
  val pfs_ready_go    = Wire(Bool())
  val pfs_to_fs_valid = Wire(Bool())

  val pfs_ex       = Wire(Bool())
  val pfs_badvaddr = Wire(BUS)
  val pfs_excode   = Wire(UInt(5.W))

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

  val target_leaving_pfs = br_taken && pfs_to_fs_valid && fs_allowin && bd_done
  val bd_leaving_pfs     = br_taken && pfs_to_fs_valid && fs_allowin && !bd_done

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
  io.fetchStage.valid      := pfs_to_fs_valid
  io.fetchStage.inst_ok    := inst_ok
  io.fetchStage.inst       := inst
  io.fetchStage.pc         := pc
  io.fetchStage.tlb_refill := tlb_refill
  io.fetchStage.excode     := pfs_excode
  io.fetchStage.ex         := pfs_ex
  io.fetchStage.badvaddr   := pfs_badvaddr
  io.instMemory.req        := inst_sram_req
  io.instMemory.addr       := inst_sram_addr
  io.instMemory.waiting    := inst_waiting

  // handshake
  to_pfs_valid    := !reset.asBool && !pfs_ex && !after_ex
  pfs_allowin     := !pfs_valid || pfs_ready_go && fs_allowin
  pfs_ready_go    := addr_ok || (pfs_ex && !pfs_block)
  pfs_to_fs_valid := pfs_valid && pfs_ready_go && !do_flush

  when(do_flush) {
    pfs_valid := true.B
  }.elsewhen(pfs_allowin) {
    pfs_valid := to_pfs_valid
  }

  // branch
  br_leaving_ds := io.fromDecoder.br_leaving_ds
  br_stall      := io.fromDecoder.branch_stall

  br_taken_w  := io.fromDecoder.branch_flag
  br_target_w := io.fromDecoder.branch_target_address
  bd_done_w   := io.fromFetchStage.valid

  when(target_leaving_pfs || do_flush) {
    br_taken_r  := false.B
    br_target_r := BUS_INIT
  }.elsewhen(br_leaving_ds) {
    br_taken_r  := br_taken_w
    br_target_r := br_target_w
  }

  when(target_leaving_pfs || do_flush) {
    bd_done_r := false.B
  }.elsewhen(br_leaving_ds) {
    bd_done_r := io.fromFetchStage.valid || pfs_to_fs_valid && fs_allowin
  }.elsewhen(bd_leaving_pfs) {
    bd_done_r := true.B
  }

  br_taken  := br_taken_r || br_taken_w
  br_target := Mux(br_taken_r, br_target_r, br_target_w)
  bd_done   := bd_done_r || bd_done_w

  // pc
  when(do_flush) {
    seq_pc := flush_pc
  }.elsewhen(pfs_ready_go && fs_allowin) {
    seq_pc := pc + 4.U
  }

  // inst sram
  inst_sram_req := pfs_valid &&
    !addr_ok_r &&
    !(bd_done && br_stall) &&
    !do_flush
  inst_sram_addr := Cat(pc(31, 2), 0.U(2.W))
  inst_waiting   := addr_ok && !inst_ok

  inst_sram_data_ok := io.fromInstMemory.data_ok && io.fromFetchStage.inst_unable

  when(do_flush) {
    addr_ok_r := false.B
  }.elsewhen(inst_sram_req && io.fromInstMemory.addr_ok && !fs_allowin) {
    addr_ok_r := true.B
  }.elsewhen(fs_allowin) {
    addr_ok_r := false.B
  }

  addr_ok := (inst_sram_req && io.fromInstMemory.addr_ok) || addr_ok_r

  when(fs_allowin || do_flush) {
    inst_buff_valid := false.B
    inst_buff       := 0.U
  }.elsewhen(addr_ok && inst_sram_data_ok && !fs_allowin) {
    inst_buff_valid := true.B
    inst_buff       := io.fromInstMemory.rdata
  }

  inst_ok := inst_buff_valid || (addr_ok && inst_sram_data_ok)
  inst    := Mux(inst_buff_valid, inst_buff, io.fromInstMemory.rdata)

  val addr_error = (pc(1, 0) =/= 0.U)
  pfs_ex := pfs_valid && (tlb_refill || tlb_invalid || addr_error)
  pfs_excode := MuxCase(
    EX_NO,
    Seq(
      addr_error                  -> EX_ADEL,
      (tlb_refill || tlb_invalid) -> EX_TLBL,
    ),
  )
  pfs_badvaddr := inst_sram_addr
}
