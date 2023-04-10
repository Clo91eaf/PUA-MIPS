package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import os.copy

class ALU extends Module {
  val io = IO(new Bundle {
    val fromExecute = Flipped(new Execute_ALU())
    val execute     = new ALU_Execute()
  })
  // input
  val op  = io.fromExecute.op
  val in1 = io.fromExecute.in1
  val in2 = io.fromExecute.in2

  val sum  = in1 + in2 
  val diff = in1 - in2
  val overflow = MuxLookup(
    op,
    false.B,
    Seq(
      EXE_ADD_OP  -> ((in1(31) === in2(31)) & (in1(31) ^ sum(31))),
      EXE_SUB_OP  -> ((in1(31) ^ in2(31)) & (in1(31) ^ diff(31))),
    ),
  )
  // output
  val out  = Wire(BUS)
  val trap = Wire(Bool())
  io.execute.out := out
  io.execute.ov := overflow
  io.execute.trap := trap

  val lt = in1.asSInt() < in2.asSInt()
  val ltu = in1 < in2

  out := MuxLookup(
    op,
    ZERO_WORD,
    Seq(
      EXE_OR_OP    -> (in1 | in2),
      EXE_AND_OP   -> (in1 & in2),
      EXE_NOR_OP   -> (~(in1 | in2)),
      EXE_XOR_OP   -> (in1 ^ in2),
      EXE_SLL_OP   -> (in2 << in1(4, 0)),
      EXE_SRL_OP   -> (in2 >> in1(4, 0)),
      EXE_SRA_OP   -> ((in2.asSInt >> in1(4, 0)).asUInt),
      EXE_SLT_OP   -> lt,
      EXE_SLTU_OP  -> ltu,
      EXE_ADD_OP   -> sum,
      EXE_ADDU_OP  -> sum,
      EXE_SUB_OP   -> diff,
      EXE_SUBU_OP  -> diff,
      EXE_CLZ_OP -> (31 to 0 by -1).foldLeft(32.U) { (res, i) =>
        Mux(in1(i), res, i.U)
      },
      EXE_CLO_OP -> (31 to 0 by -1).foldLeft(32.U) { (res, i) =>
        Mux(~in1(i), res, i.U)
      },
    ),
  )
  trap := MuxLookup(
    op,
    TRAP_NOT_ASSERT,
    Seq(
      EXE_TEQ_OP   -> (in1 === in2),
      EXE_TNE_OP   -> (in1 =/= in2),
      EXE_TGE_OP   -> ~lt,
      EXE_TGEU_OP  -> ~ltu,
      EXE_TLT_OP   -> lt,
      EXE_TLTU_OP  -> ltu,
    ),
  )
}
