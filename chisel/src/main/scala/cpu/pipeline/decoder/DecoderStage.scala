package cpu.pipeline.decoder

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class DecoderStage extends Module {
  val io = IO(new Bundle {
    val fromFetchStage = Flipped(new FetchStage_DecoderStage())
    val fromDecoder    = Flipped(new Decoder_DecoderStage())
    val decoder        = new DecoderStage_Decoder()
  })

  val pc       = RegInit(BUS_INIT)
  val inst     = RegInit(BUS_INIT)
  val ex       = RegInit(false.B)
  val bd       = RegInit(false.B)
  val badvaddr = RegInit(false.B)
  val ds_valid = RegInit(false.B)

  // output-decoder
  io.decoder.pc       := pc
  io.decoder.inst     := inst
  io.decoder.ex       := ex
  io.decoder.bd       := bd
  io.decoder.badvaddr := badvaddr
  io.decoder.valid    := ds_valid

  when(io.fromDecoder.allowin) {
    ds_valid := io.fromFetchStage.valid
  }

  when(io.fromFetchStage.valid && io.fromDecoder.allowin) {
    pc       := io.fromFetchStage.pc
    inst     := io.fromFetchStage.inst
    ex       := io.fromFetchStage.ex
    bd       := io.fromFetchStage.bd
    badvaddr := io.fromFetchStage.badvaddr
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
