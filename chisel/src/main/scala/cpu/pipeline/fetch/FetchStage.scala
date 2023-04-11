package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val fromDecoder        = Flipped(new Decoder_FetchStage())
    val fromInstMemory     = Flipped(new InstMemory_FetchStage())
    val fromCP0            = Flipped(new CP0_FetchStage())
    val fromWriteBackStage = Flipped(new WriteBackStage_FetchStage())
    val decoderStage       = new FetchStage_DecoderStage()
    val instMemory         = new FetchStage_InstMemory()
  })
  // input
  val branch_stall          = Wire(STALL_BUS)
  val branch_flag           = Wire(Bool())
  val branch_target_address = Wire(BUS)
  val fs_inst               = Wire(INST_BUS)
  val fs_allowin            = Wire(Bool())

  // input-decoder
  branch_stall          := io.fromDecoder.branch_stall
  branch_flag           := io.fromDecoder.branch_flag
  branch_target_address := io.fromDecoder.branch_target_address
  fs_allowin            := io.fromDecoder.allowin

  // input-inst rom
  fs_inst := io.fromInstMemory.rdata

  // output
  val fs_to_ds_valid = Wire(Bool())
  val fs_pc          = RegInit(PC_INIT - 4.U)
  val fs_ex          = Wire(Bool())
  val fs_bd          = Wire(Bool())
  val fs_badvaddr    = Wire(Bool())
  val fs_valid       = RegInit(false.B)
  val fs_ready_go    = Wire(Bool())
  val to_fs_valid    = Wire(Bool())
  val to_fs_ready_go = Wire(Bool())
  val seq_pc         = Wire(INST_ADDR_BUS)
  val next_pc        = Wire(INST_ADDR_BUS)

  // output-decoderStage
  io.decoderStage.pc       := fs_pc
  io.decoderStage.inst     := fs_inst
  io.decoderStage.ex       := fs_ex
  io.decoderStage.bd       := fs_bd
  io.decoderStage.badvaddr := fs_badvaddr
  io.decoderStage.valid    := fs_to_ds_valid

  // output-instMemory
  io.instMemory.en    := to_fs_valid && fs_allowin
  io.instMemory.wen   := 0.U
  io.instMemory.addr  := Cat(next_pc(31, 2), 0.U(2.W))
  io.instMemory.wdata := 0.U

  // io-finish

  // pre-FetchStage
  to_fs_ready_go := !branch_stall
  to_fs_valid    := !reset.asBool && to_fs_ready_go
  seq_pc         := fs_pc + 4.U

  next_pc := MuxCase(
    seq_pc,
    Seq(
      io.fromWriteBackStage.ex   -> "hbfc00380".U,
      io.fromWriteBackStage.eret -> io.fromCP0.epc,
      branch_flag                -> branch_target_address,
    ),
  )

  // FetchStage
  fs_ready_go := true.B
  fs_allowin  := !fs_valid || fs_ready_go && io.fromDecoder.allowin
  fs_to_ds_valid := fs_valid && fs_ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex

  when(fs_allowin) {
    fs_valid := to_fs_valid
  }

  when(to_fs_valid && fs_allowin) {
    fs_pc := next_pc
  }

  val addr_error = Wire(Bool())
  addr_error  := fs_pc(1, 0) =/= 0.U
  fs_ex       := addr_error && fs_valid
  fs_bd       := io.fromDecoder.is_branch
  fs_badvaddr := fs_pc

  // printf(p"fetch :pc 0x${Hexadecimal(pc)}\n")
}
