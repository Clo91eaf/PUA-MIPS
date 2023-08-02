package cpu.pipeline.fetch

import chisel3._
import chisel3.util._

class Bpu(PHT_DEPTH: Int = 6, BHT_DEPTH: Int = 4) extends Module {
  val io = IO(new Bundle {
    val instrD         = Input(UInt(32.W))
    val enaD           = Input(Bool())
    val pcD            = Input(UInt(32.W))
    val pc_plus4D      = Input(UInt(32.W))
    val branchD        = Output(Bool())
    val pred_takeD     = Output(Bool())
    val branch_targetD = Output(UInt(32.W))

    val pcE          = Input(UInt(32.W))
    val branchE      = Input(Bool())
    val actual_takeE = Input(Bool())

  })

  io.branchD := (io.instrD(31, 26) === 1.U && io.instrD(19, 17) === 0.U) || io.instrD(31, 28) === 1.U
  io.branch_targetD := io.pc_plus4D + Cat(
    Fill(14, io.instrD(15)),
    io.instrD(15, 0),
    0.U(2.W),
  ) // branch为有符号扩展

  val strongly_not_taken :: weakly_not_taken :: weakly_taken :: strongly_taken :: Nil = Enum(4)

  val BHT       = RegInit(VecInit(Seq.fill(1 << BHT_DEPTH)(0.U(6.W))))
  val PHT       = RegInit(VecInit(Seq.fill(1 << PHT_DEPTH)(strongly_not_taken)))
  val BHT_index = io.pcD(1 + BHT_DEPTH, 2)
  val BHR_value = BHT(BHT_index)
  val PHT_index = BHR_value

  io.pred_takeD := io.enaD && io.branchD && (PHT(PHT_index) === weakly_taken || PHT(PHT_index) === strongly_taken)

  val update_BHT_index = io.pcE(1 + BHT_DEPTH, 2)
  val update_BHR_value = BHT(update_BHT_index)
  val update_PHT_index = update_BHR_value

  when(io.branchE) {
    BHT(update_BHT_index) := Cat(BHT(update_BHT_index)(5, 1), io.actual_takeE)
    switch(PHT(update_PHT_index)) {
      is(strongly_not_taken) {
        PHT(update_PHT_index) := Mux(io.actual_takeE, weakly_not_taken, strongly_not_taken)
      }
      is(weakly_not_taken) {
        PHT(update_PHT_index) := Mux(io.actual_takeE, weakly_taken, strongly_not_taken)
      }
      is(weakly_taken) {
        PHT(update_PHT_index) := Mux(io.actual_takeE, strongly_taken, weakly_not_taken)
      }
      is(strongly_taken) {
        PHT(update_PHT_index) := Mux(io.actual_takeE, strongly_taken, weakly_taken)
      }
    }
  }
}
