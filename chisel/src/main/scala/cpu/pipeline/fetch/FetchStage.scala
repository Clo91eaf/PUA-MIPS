package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val fromPreFetchStage = Flipped(new PreFetchStage_FetchStage())
    val fromInstMemory    = Flipped(new InstMemory_FetchStage())
    val fromDecoder       = Flipped(new Decoder_FetchStage())
    val fromCtrl          = Flipped(new Ctrl_FetchStage())

    val ctrl          = new FetchStage_Ctrl()
    val preFetchStage = new FetchStage_PreFetchStage()
    val decoderStage  = new FetchStage_DecoderStage()
    val instMemory    = new FetchStage_InstMemory()
  })

  val fs_valid           = RegInit(false.B)
  val allowin            = Wire(Bool())
  val ready_go           = Wire(Bool())
  val fs_to_ds_valid     = Wire(Bool())
  val pfs_to_fs_inst_ok  = RegInit(false.B)
  val pfs_to_fs_inst     = RegInit(BUS_INIT)
  val pc                 = RegInit(BUS_INIT)
  val inst_buff          = RegInit(BUS_INIT)
  val inst_buff_valid    = RegInit(false.B)
  val fs_tlb_refill      = RegInit(false.B)
  val pfs_to_fs_ex       = RegInit(false.B)
  val pfs_to_fs_badvaddr = RegInit(0.U(32.W))
  val pfs_to_fs_excode   = RegInit(0.U(5.W))
  val inst_ok            = Wire(Bool())
  val inst               = Wire(BUS)

  val do_flush = io.fromCtrl.do_flush
  val after_ex = io.fromCtrl.after_ex

  // output
  val ex       = Wire(Bool())
  val badvaddr = Wire(UInt(32.W))
  val excode   = Wire(UInt(5.W))

  io.preFetchStage.valid       := fs_valid
  io.preFetchStage.allowin     := allowin
  io.preFetchStage.inst_unable := !fs_valid || inst_buff_valid || pfs_to_fs_inst_ok

  io.decoderStage.valid      := fs_to_ds_valid
  io.decoderStage.pc         := pc
  io.decoderStage.inst       := inst
  io.decoderStage.ex         := ex
  io.decoderStage.badvaddr   := badvaddr
  io.decoderStage.excode     := excode
  io.decoderStage.tlb_refill := fs_tlb_refill

  io.instMemory.waiting := fs_valid && !inst_ok

  io.ctrl.ex := ex

  /*-------------------------------io finish------------------------------*/
  ready_go       := inst_ok || ex
  allowin        := !fs_valid || ready_go && io.fromDecoder.allowin
  fs_to_ds_valid := fs_valid && ready_go && !do_flush

  when(do_flush) {
    fs_valid := false.B
  }.elsewhen(allowin) {
    fs_valid := io.fromPreFetchStage.valid
  }

  when(io.fromPreFetchStage.valid && allowin) {
    pfs_to_fs_inst_ok  := io.fromPreFetchStage.inst_ok
    pfs_to_fs_inst     := io.fromPreFetchStage.inst
    pc                 := io.fromPreFetchStage.pc
    fs_tlb_refill      := io.fromPreFetchStage.tlb_refill
    pfs_to_fs_ex       := io.fromPreFetchStage.ex
    pfs_to_fs_badvaddr := io.fromPreFetchStage.badvaddr
    pfs_to_fs_excode   := io.fromPreFetchStage.excode
  }

  when(do_flush) {
    inst_buff_valid := false.B
    inst_buff       := 0.U
  }.elsewhen(!inst_buff_valid && fs_valid && io.fromInstMemory.data_ok && !io.fromDecoder.allowin) {
    inst_buff_valid := true.B
    inst_buff       := io.fromInstMemory.rdata
  }.elsewhen(io.fromDecoder.allowin) {
    inst_buff_valid := false.B
    inst_buff       := 0.U
  }

  inst_ok := pfs_to_fs_inst_ok || inst_buff_valid || (fs_valid && io.fromInstMemory.data_ok)
  val inst_temp = MuxCase(
    io.fromInstMemory.rdata,
    Seq(
      pfs_to_fs_inst_ok -> pfs_to_fs_inst,
      inst_buff_valid   -> inst_buff,
    ),
  )
  inst := Mux(!ex, inst_temp, 0.U)

  ex       := fs_valid && pfs_to_fs_ex
  badvaddr := pfs_to_fs_badvaddr
  excode   := pfs_to_fs_excode
}
