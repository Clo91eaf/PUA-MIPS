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

    val branch        = Output(Bool())
    val pred_take     = Output(Bool())
    val branch_target = Output(UInt(DATA_ADDR_WID.W))
  }

  val execute = new Bundle {
    val pc          = Input(UInt(DATA_ADDR_WID.W))
    val branch      = Input(Bool())
    val actual_take = Input(Bool())
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
}

class PesudoBranchPredictor(implicit config: CpuConfig) extends Module {
  val io = IO(new BranchPredictorIO())
  io.decoder.branch := VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL)
    .contains(io.decoder.op)
  io.decoder.branch_target := io.decoder.pc_plus4 + Cat(
    Fill(14, io.decoder.inst(15)),
    io.decoder.inst(15, 0),
    0.U(2.W),
  )

  io.regfile.get.src1.raddr := io.decoder.rs1
  io.regfile.get.src2.raddr := io.decoder.rs2
  val (src1, src2) = (io.regfile.get.src1.rdata, io.regfile.get.src2.rdata)
  // io.decoder.pred_take := true.B
  val pred_take = MuxLookup(
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

  io.decoder.pred_take := io.decoder.ena && io.decoder.branch && pred_take
}

class AdaptiveTwoLevelPredictor(PHT_DEPTH: Int = 6, BHT_DEPTH: Int = 4)(implicit
    config: CpuConfig,
) extends Module {
  val io = IO(new BranchPredictorIO())

  // TODO:下面可以修改成并行
  io.decoder.branch :=
    VecInit(EXE_BEQ, EXE_BNE, EXE_BGTZ, EXE_BLEZ, EXE_BGEZ, EXE_BGEZAL, EXE_BLTZ, EXE_BLTZAL).contains(io.decoder.op)
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
    BHT(update_BHT_index) := Cat(BHT(update_BHT_index)(PHT_DEPTH - 2, 0), io.execute.branch)
    switch(PHT(update_PHT_index)) {
      is(strongly_not_taken) {
        PHT(update_PHT_index) := Mux(io.execute.branch, weakly_not_taken, strongly_not_taken)
      }
      is(weakly_not_taken) { PHT(update_PHT_index) := Mux(io.execute.branch, weakly_taken, strongly_not_taken) }
      is(weakly_taken) { PHT(update_PHT_index) := Mux(io.execute.branch, strongly_taken, weakly_not_taken) }
      is(strongly_taken) { PHT(update_PHT_index) := Mux(io.execute.branch, strongly_taken, weakly_taken) }
    }
  }
}
