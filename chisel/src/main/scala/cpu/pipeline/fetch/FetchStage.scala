package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val fromPreFetchStage  = Flipped(new PreFetchStage_FetchStage())
    val fromInstMemory     = Flipped(new InstMemory_FetchStage())
    val fromDecoder        = Flipped(new Decoder_FetchStage())
    val fromWriteBackStage = Flipped(new WriteBackStage_FetchStage())
    val preFetchStage      = new FetchStage_PreFetchStage()
    val decoderStage       = new FetchStage_DecoderStage
    val instMemory         = new FetchStage_InstMemory()
  })

  val valid             = RegInit(false.B)
  val ready_go          = Wire(Bool())
  val pfs_to_fs_inst_ok = RegInit(false.B)
  val pfs_to_fs_inst    = RegInit(BUS_INIT)
  val pc                = RegInit(BUS_INIT)
  val inst_buff         = RegInit(BUS_INIT)
  val inst_ok           = Wire(Bool())
  val inst              = Wire(BUS)
  val ex                = Wire(Bool())
  val badvaddr          = Wire(BUS)

  // output
  io.preFetchStage.valid       := valid
  io.preFetchStage.allowin     := !valid || (ready_go && io.fromDecoder.allowin)
  io.preFetchStage.inst_unable := !valid || inst_buff.orR || pfs_to_fs_inst_ok

  io.decoderStage.valid := valid && ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  io.decoderStage.pc       := pc
  io.decoderStage.inst     := inst
  io.decoderStage.ex       := ex
  io.decoderStage.badvaddr := badvaddr

  io.instMemory.waiting := valid && !inst_ok

  /*-------------------------------io finish------------------------------*/
  val addr_error = (pc(1, 0) =/= "b00".U)
  ex       := valid && addr_error
  badvaddr := pc

  when(io.fromWriteBackStage.ex || io.fromWriteBackStage.eret) {
    valid := false.B
  }.elsewhen(io.fromDecoder.allowin && io.fromPreFetchStage.valid) {
    valid := io.fromPreFetchStage.valid
  }

  when(io.fromPreFetchStage.valid && io.fromDecoder.allowin) {
    pfs_to_fs_inst_ok := io.fromPreFetchStage.inst_ok
    pfs_to_fs_inst    := io.fromPreFetchStage.inst
    pc                := io.fromPreFetchStage.pc
  }

  when(!inst_buff.orR && valid && io.fromInstMemory.data_ok && !io.fromDecoder.allowin) {
    inst_buff := io.fromInstMemory.rdata
  }.elsewhen(io.fromDecoder.allowin || io.fromWriteBackStage.eret || io.fromWriteBackStage.ex) {
    inst_buff := 0.U
  }

  inst_ok := pfs_to_fs_inst_ok || inst_buff.orR || (valid && io.fromInstMemory.data_ok)
  inst := MuxCase(
    io.fromInstMemory.rdata,
    Seq(
      pfs_to_fs_inst_ok -> pfs_to_fs_inst,
      inst_buff.orR     -> inst_buff,
    ),
  )
  ready_go := inst_ok
}
