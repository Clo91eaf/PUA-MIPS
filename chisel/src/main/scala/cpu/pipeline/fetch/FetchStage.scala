package cpu.pipeline.fetchStage

import chisel3._
import cpu.defines._
import cpu.defines.Const._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val fromControl  = Flipped(new Control_FetchStage())
    val fromDecoder  = Flipped(new Decoder_FetchStage())
    val decoderStage = new FetchStage_DecoderStage()
    val instMemory   = new FetchStage_InstMemory()
  })
  // input
  val stall                 = Wire(STALL_BUS)
  val branch_flag           = Wire(Bool())
  val branch_target_address = Wire(BUS)

  // input-control
  stall := io.fromControl.stall

  // input-decoder
  branch_flag           := io.fromDecoder.branch_flag
  branch_target_address := io.fromDecoder.branch_target_address

  // output
  val pc      = RegInit(PC_INIT)
  val inst_en = Wire(Bool()) // inst enable: inst sram使能信号
  val pre_pc  = RegInit(PC_INIT)

  // output-decoderStage
  io.decoderStage.pc := pc

  // output-instMemory
  io.instMemory.pc      := pc
  io.instMemory.inst_en := inst_en

  // io-finish

  when(inst_en === false.B) {
    pc := PC_INIT
  }.elsewhen(io.fromControl.flush) {
    pc := io.fromControl.new_pc
  }.elsewhen(stall(0) === NOT_STOP) {
    when(branch_flag === BRANCH) {
      pc := branch_target_address
    }.otherwise {
      pc := pc + 4.U
    }
  }

  inst_en := ~reset.asBool() // 复位结束,使能指令存储器

  // printf(p"fetch :pc 0x${Hexadecimal(pc)}\n")
}
