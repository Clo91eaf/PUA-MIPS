package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class BranchCtrl extends Module {
  val io = IO(new Bundle {
    val in = new Bundle {
      val inst_info   = Input(new InstInfo())
      val src_info    = Input(new SrcInfo())
      val pred_branch = Input(Bool())
    }
    val out = new Bundle {
      val branch_flag = Output(Bool())
      val pred_fail   = Output(Bool())
    }
  })
  val src1 = io.in.src_info.src1_data
  val src2 = io.in.src_info.src2_data
  io.out.pred_fail := io.in.pred_branch =/= io.out.branch_flag
  io.out.branch_flag := MuxLookup(
    io.in.inst_info.op,
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
}
