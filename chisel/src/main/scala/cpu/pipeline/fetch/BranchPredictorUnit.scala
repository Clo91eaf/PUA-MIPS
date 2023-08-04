package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig

// TODO add RAS
class BranchPredictorUnit(PHT_DEPTH: Int = 7, BHT_DEPTH: Int = 5)(implicit
    config: CpuConfig,
) extends Module {
  val io = IO(new Bundle {
    val decoder = new Bundle {
      val inst     = Input(UInt(INST_WID.W))
      val op       = Input(UInt(OP_WID.W))
      val ena      = Input(Bool())
      val pc       = Input(UInt(DATA_ADDR_WID.W))
      val pc_plus4 = Input(UInt(DATA_ADDR_WID.W))

      val branch        = Output(Bool())
      val pred_take     = Output(Bool())
      val branch_target = Output(UInt(DATA_ADDR_WID.W))
    }

    val execute = new Bundle {
      val pc          = Input(UInt(DATA_ADDR_WID.W))
      val branch      = Input(Bool())
      val actual_take = Input(Bool())
    }
  })

  io.decoder.branch := VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL)
    .contains(io.decoder.op)
  io.decoder.branch_target := io.decoder.pc_plus4 + Cat(Fill(14, io.decoder.inst(15)), io.decoder.inst(15, 0), 0.U(2.W))

  val strongly_not_taken :: weakly_not_taken :: weakly_taken :: strongly_taken :: Nil = Enum(4)

  val BHT       = RegInit(VecInit(Seq.fill(1 << BHT_DEPTH)(0.U(PHT_DEPTH.W))))
  val PHT       = RegInit(VecInit(Seq.fill(1 << PHT_DEPTH)(strongly_taken)))
  val BHT_index = io.decoder.pc(1 + BHT_DEPTH, 2)
  val PHT_index = BHT(BHT_index)

  io.decoder.pred_take := io.decoder.ena && io.decoder.branch && (PHT(PHT_index) === weakly_taken || PHT(
    PHT_index,
  ) === strongly_taken)
  val update_BHT_index = io.execute.pc(1 + BHT_DEPTH, 2)
  val update_PHT_index = BHT(update_BHT_index)

  when(io.execute.branch) {
    BHT(update_BHT_index) := Cat(BHT(update_BHT_index)(5, 1), io.execute.actual_take)
    switch(PHT(update_PHT_index)) {
      is(strongly_not_taken) {
        PHT(update_PHT_index) := Mux(io.execute.actual_take, weakly_not_taken, strongly_not_taken)
      }
      is(weakly_not_taken) { PHT(update_PHT_index) := Mux(io.execute.actual_take, weakly_taken, strongly_not_taken) }
      is(weakly_taken) { PHT(update_PHT_index) := Mux(io.execute.actual_take, strongly_taken, weakly_not_taken) }
      is(strongly_taken) { PHT(update_PHT_index) := Mux(io.execute.actual_take, strongly_taken, weakly_taken) }
    }
  }
}
