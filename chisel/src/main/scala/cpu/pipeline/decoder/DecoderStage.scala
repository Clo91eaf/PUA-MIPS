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
  val badvaddr = RegInit(BUS_INIT)
  val valid    = RegInit(false.B)

  // output-decoder
  io.decoder.pc       := pc
  io.decoder.inst     := inst
  io.decoder.ex       := ex
  io.decoder.badvaddr := badvaddr
  io.decoder.valid    := valid

  when(io.fromDecoder.allowin) {
    valid := io.fromFetchStage.valid
  }

  when(io.fromFetchStage.valid && io.fromDecoder.allowin) {
    pc       := io.fromFetchStage.pc
    inst     := io.fromFetchStage.inst
    ex       := io.fromFetchStage.ex
    badvaddr := io.fromFetchStage.badvaddr
  }

  // debug
  // printf(p"decoderStage :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
