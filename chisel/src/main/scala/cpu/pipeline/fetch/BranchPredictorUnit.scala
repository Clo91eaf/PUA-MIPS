package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.decoder.Src12Read

class BranchPredictorIO(implicit config: CpuConfig) extends Bundle {
  val decoder = new Bundle {
    val inst     = Input(UInt(INST_WID.W))
    val op       = Input(UInt(OP_WID.W))
    val ena      = Input(Bool())
    val pc       = Input(UInt(DATA_ADDR_WID.W))
    val pc_plus4 = Input(UInt(DATA_ADDR_WID.W))

    val rs1 = Input(UInt(REG_ADDR_WID.W))
    val rs2 = Input(UInt(REG_ADDR_WID.W))

    val branch_inst   = Output(Bool())
    val pred_branch   = Output(Bool())
    val branch_target = Output(UInt(DATA_ADDR_WID.W))
  }

  val execute = new Bundle {
    val pc          = Input(UInt(DATA_ADDR_WID.W))
    val branch_inst = Input(Bool())
    val branch      = Input(Bool())
  }

  val regfile = if (config.branchPredictor == "pesudo") Some(new Src12Read()) else None
}

class BranchPredictorUnit(implicit config: CpuConfig) extends Module {
  val io = IO(new BranchPredictorIO())

  if (config.branchPredictor == "adaptive") {
    val adaptive_predictor = Module(new AdaptiveTwoLevelPredictor())
    io <> adaptive_predictor.io
  }

  if (config.branchPredictor == "pesudo") {
    val pesudo_predictor = Module(new PesudoBranchPredictor())
    io <> pesudo_predictor.io
  }

  if (config.branchPredictor == "global") {
    val global_predictor = Module(new GlobalBranchPredictor())
    io <> global_predictor.io
  }
}

class PesudoBranchPredictor(implicit config: CpuConfig) extends Module {
  val io = IO(new BranchPredictorIO())
  io.decoder.branch_inst := VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL)
    .contains(io.decoder.op)
  io.decoder.branch_target := io.decoder.pc_plus4 + Cat(
    Fill(14, io.decoder.inst(15)),
    io.decoder.inst(15, 0),
    0.U(2.W),
  )

  io.regfile.get.src1.raddr := io.decoder.rs1
  io.regfile.get.src2.raddr := io.decoder.rs2
  val (src1, src2) = (io.regfile.get.src1.rdata, io.regfile.get.src2.rdata)
  val pred_branch = MuxLookup(
    io.decoder.op,
    false.B,
    Seq(
      EXE_BEQ    -> (src1 === src2),
      EXE_BNE    -> (src1 =/= src2),
      EXE_BGTZ   -> (!src1(31) && (src1 =/= 0.U)),
      EXE_BLEZ   -> (src1(31) || src1 === 0.U),
      EXE_BGEZ   -> (!src1(31)),
      EXE_BGEZAL -> (!src1(31)),
      EXE_BLTZ   -> (src1(31)),
      EXE_BLTZAL -> (src1(31)),
    ),
  )

  io.decoder.pred_branch := io.decoder.ena && io.decoder.branch_inst && pred_branch
}

class GlobalBranchPredictor(
    GHR_DEPTH: Int = 4,   // 可以记录的历史记录个数
    PC_HASH_WID: Int = 4, // 取得PC的宽度
    PHT_DEPTH: Int = 6,   // 可以记录的历史个数
    BHT_DEPTH: Int = 4,   // 取得PC的宽度
)(implicit
    config: CpuConfig,
) extends Module {
  val io = IO(new BranchPredictorIO())

  val strongly_not_taken :: weakly_not_taken :: weakly_taken :: strongly_taken :: Nil = Enum(4)

  io.decoder.branch_inst := VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL).contains(io.decoder.op)
  io.decoder.branch_target := io.decoder.pc_plus4 + Cat(
    Fill(14, io.decoder.inst(15)),
    io.decoder.inst(15, 0),
    0.U(2.W),
  )
  // 局部预测模式

  val bht       = RegInit(VecInit(Seq.fill(1 << BHT_DEPTH)(0.U(PHT_DEPTH.W))))
  val pht       = RegInit(VecInit(Seq.fill(1 << PHT_DEPTH)(strongly_taken)))
  val bht_index = io.decoder.pc(1 + BHT_DEPTH, 2)
  val pht_index = bht(bht_index)

  io.decoder.pred_branch :=
    io.decoder.ena && io.decoder.branch_inst && (pht(pht_index) === weakly_taken || pht(pht_index) === strongly_taken)
  val update_bht_index = io.execute.pc(1 + BHT_DEPTH, 2)
  val update_pht_index = bht(update_bht_index)

  when(io.execute.branch_inst) {
    bht(update_bht_index) := Cat(bht(update_bht_index)(PHT_DEPTH - 2, 0), io.execute.branch)
    switch(pht(update_pht_index)) {
      is(strongly_not_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, weakly_not_taken, strongly_not_taken)
      }
      is(weakly_not_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, weakly_taken, strongly_not_taken)
      }
      is(weakly_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, strongly_taken, weakly_not_taken)
      }
      is(strongly_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, strongly_taken, weakly_taken)
      }
    }
  }

}

// class AdaptiveTwoLevelPredictor(
//     PHT_DEPTH: Int = 6, // 可以记录的历史个数
//     BHT_DEPTH: Int = 4, // 取得PC的宽度
// )(implicit
class AdaptiveTwoLevelPredictor(
)(implicit
    config: CpuConfig,
) extends Module {
  val bpuConfig = new BranchPredictorConfig()
  val PHT_DEPTH = bpuConfig.phtDepth
  val BHT_DEPTH = bpuConfig.bhtDepth
  val io = IO(new BranchPredictorIO())

  val strongly_not_taken :: weakly_not_taken :: weakly_taken :: strongly_taken :: Nil = Enum(4)

  io.decoder.branch_inst := VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL).contains(io.decoder.op)
  io.decoder.branch_target := io.decoder.pc_plus4 + Cat(
    Fill(14, io.decoder.inst(15)),
    io.decoder.inst(15, 0),
    0.U(2.W),
  )

  val bht       = RegInit(VecInit(Seq.fill(1 << BHT_DEPTH)(0.U(PHT_DEPTH.W))))
  val pht       = RegInit(VecInit(Seq.fill(1 << PHT_DEPTH)(strongly_taken)))
  val bht_index = io.decoder.pc(1 + BHT_DEPTH, 2)
  val pht_index = bht(bht_index)

  io.decoder.pred_branch := io.decoder.ena && io.decoder.branch_inst && (pht(pht_index) === weakly_taken || pht(pht_index) === strongly_taken)

  val update_bht_index = io.execute.pc(1 + BHT_DEPTH, 2)
  val update_pht_index = bht(update_bht_index)

  when(io.execute.branch_inst) {
    bht(update_bht_index) := Cat(bht(update_bht_index)(PHT_DEPTH - 2, 0), io.execute.branch)
    switch(pht(update_pht_index)) {
      is(strongly_not_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, weakly_not_taken, strongly_not_taken)
      }
      is(weakly_not_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, weakly_taken, strongly_not_taken)
      }
      is(weakly_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, strongly_taken, weakly_not_taken)
      }
      is(strongly_taken) {
        pht(update_pht_index) := Mux(io.execute.branch, strongly_taken, weakly_taken)
      }
    }
  }

}
