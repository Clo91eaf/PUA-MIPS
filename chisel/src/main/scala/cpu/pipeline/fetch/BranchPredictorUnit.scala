package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig

// TODO add RAS
class BranchPredictorUnit(
    GloblePredictMode: Boolean = true, //  false: 局部预测，true: 全局预测
    PHT_DEPTH: Int = 6,                // 可以记录的历史个数
    BHT_DEPTH: Int = 4,                // 取得PC的宽度
    GHR_DEPTH: Int = 10,               // 可以记录的历史记录个数
)(implicit
    config: CpuConfig,
) extends Module {
  val io = IO(new Bundle {
    val decoder = new Bundle {
      val inst     = Input(UInt(INST_WID.W))
      val op       = Input(UInt(OP_WID.W))
      val ena      = Input(Bool())
      val pc       = Input(UInt(DATA_ADDR_WID.W))
      val pc_plus4 = Input(UInt(DATA_ADDR_WID.W))

      val branch_inst   = Output(Bool())
      val pred_take     = Output(Bool())
      val branch_target = Output(UInt(DATA_ADDR_WID.W))
    }

    val execute = new Bundle {
      val pc          = Input(UInt(DATA_ADDR_WID.W))
      val branch      = Input(Bool())
      val branch_inst = Input(Bool())
    }
  })

  val strongly_not_taken :: weakly_not_taken :: weakly_taken :: strongly_taken :: Nil = Enum(4)

  // TODO:下面可以修改成并行
  io.decoder.branch_inst :=
    VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL).contains(io.decoder.op)
  io.decoder.branch_target := io.decoder.pc_plus4 + Cat(
    Fill(14, io.decoder.inst(15)),
    io.decoder.inst(15, 0),
    0.U(2.W),
  )

  if (!GloblePredictMode) {
    // 局部预测模式

    val bht       = RegInit(VecInit(Seq.fill(1 << BHT_DEPTH)(0.U(PHT_DEPTH.W))))
    val pht       = RegInit(VecInit(Seq.fill(1 << PHT_DEPTH)(strongly_taken)))
    val bht_index = io.decoder.pc(1 + BHT_DEPTH, 2)
    val pht_index = bht(bht_index)

    io.decoder.pred_take :=
      io.decoder.ena && io.decoder.branch_inst && (pht(pht_index) === weakly_taken || pht(pht_index) === strongly_taken)
    val update_BHT_index = io.execute.pc(1 + BHT_DEPTH, 2)
    val update_PHT_index = bht(update_BHT_index)

    when(io.execute.branch_inst) {
      bht(update_BHT_index) := Cat(bht(update_BHT_index)(PHT_DEPTH - 2, 0), io.execute.branch)
      switch(pht(update_PHT_index)) {
        is(strongly_not_taken) {
          pht(update_PHT_index) := Mux(io.execute.branch, weakly_not_taken, strongly_not_taken)
        }
        is(weakly_not_taken) {
          pht(update_PHT_index) := Mux(io.execute.branch, weakly_taken, strongly_not_taken)
        }
        is(weakly_taken) {
          pht(update_PHT_index) := Mux(io.execute.branch, strongly_taken, weakly_not_taken)
        }
        is(strongly_taken) {
          pht(update_PHT_index) := Mux(io.execute.branch, strongly_taken, weakly_taken)
        }
      }
    }
  } else {
    // 全局预测模式
    val ghr = RegInit(0.U(GHR_DEPTH.W))                                  // global history register
    val gcp = Seq.fill(2)(RegInit(0.U(GHR_DEPTH.W)))                     // ghr check point
    val pht = RegInit(VecInit(Seq.fill(1 << GHR_DEPTH)(strongly_taken))) // pattern history table

    ghr    := Cat(ghr(GHR_DEPTH - 2, 1), io.decoder.pred_take)
    gcp(0) := Cat(ghr(GHR_DEPTH - 2, 1), !io.decoder.pred_take)
    gcp(1) := gcp(0)
    io.decoder.pred_take :=
      io.decoder.ena && io.decoder.branch_inst && (pht(ghr) === weakly_taken || pht(ghr) === strongly_taken)

    when(io.execute.branch_inst) {
      when(ghr(1) =/= io.execute.branch) {
        ghr := gcp(1)
      }
      switch(pht(ghr)) {
        is(strongly_not_taken) {
          pht(ghr) := Mux(io.execute.branch, weakly_not_taken, strongly_not_taken)
        }
        is(weakly_not_taken) {
          pht(ghr) := Mux(io.execute.branch, weakly_taken, strongly_not_taken)
        }
        is(weakly_taken) {
          pht(ghr) := Mux(io.execute.branch, strongly_taken, weakly_not_taken)
        }
        is(strongly_taken) {
          pht(ghr) := Mux(io.execute.branch, strongly_taken, weakly_taken)
        }
      }
    }
  }
}
