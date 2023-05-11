package cpu.pipeline.decoder

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class DecoderStage extends Module {
  val io = IO(new Bundle {
    val fromFetchStage = Flipped(new FetchStage_DecoderStage())
    val fromDecoder    = Flipped(new Decoder_DecoderStage())
    val fromCtrl       = Flipped(new Ctrl_DecoderStage())
    val decoder        = new DecoderStage_Decoder()
  })

  val pc         = RegInit(BUS_INIT)
  val inst       = RegInit(BUS_INIT)
  val ex         = RegInit(false.B)
  val badvaddr   = RegInit(BUS_INIT)
  val valid      = RegInit(false.B)
  val tlb_refill = RegInit(false.B)
  val excode     = RegInit(0.U(5.W))

  // output-decoder
  // wire
  io.decoder.valid    := valid
  io.decoder.after_ex := io.fromCtrl.after_ex
  io.decoder.do_flush := io.fromCtrl.do_flush
  // reg
  io.decoder.pc         := pc
  io.decoder.inst       := inst
  io.decoder.ex         := ex
  io.decoder.badvaddr   := badvaddr
  io.decoder.excode     := excode
  io.decoder.tlb_refill := tlb_refill

  when(io.fromCtrl.do_flush) {
    valid := false.B
  }.elsewhen(io.fromDecoder.allowin) {
    valid := io.fromFetchStage.valid
  }

  when(io.fromFetchStage.valid && io.fromDecoder.allowin) {
    pc         := io.fromFetchStage.pc
    inst       := io.fromFetchStage.inst
    ex         := io.fromFetchStage.ex
    badvaddr   := io.fromFetchStage.badvaddr
    excode     := io.fromFetchStage.excode
    tlb_refill := io.fromFetchStage.tlb_refill
  }
}
