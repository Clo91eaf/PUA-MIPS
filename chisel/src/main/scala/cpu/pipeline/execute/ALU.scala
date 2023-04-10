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
  val op  = Wire(ALU_OP_BUS)
  val in1 = Wire(BUS)
  val in2 = Wire(BUS)
  op  := io.fromExecute.op
  in1 := io.fromExecute.in1
  in2 := Mux(
    ((op === EXE_SUB_OP) || (op === EXE_SUBU_OP) ||
      (op === EXE_SLT_OP) || (op === EXE_TLT_OP) ||
      (op === EXE_TLTI_OP) || (op === EXE_TGE_OP) ||
      (op === EXE_TGEI_OP)),
    ((~io.fromExecute.in2) + 1.U),
    io.fromExecute.in2,
  )
  val sum      = in1 +& in2 // 无符号加法
  val overflow = sum(31) === in1(31) && sum(31) =/= sum(30)
  // output
  val out  = Wire(BUS)
  val trap = Wire(Bool())
  io.execute.out := out
  io.execute.ov := (op === EXE_ADD_OP || op === EXE_ADDI_OP || op === EXE_SUB_OP) && overflow
  io.execute.trap := trap

  val in1_lt_in2 = Mux(
    ((op === EXE_SLT_OP) || (op === EXE_TLT_OP) ||
      (op === EXE_TLTI_OP) || (op === EXE_TGE_OP) ||
      (op === EXE_TGEI_OP)),
    ((in1(31) && !io.fromExecute.in2(31)) ||
      (!in1(31) && !io.fromExecute.in2(31) && sum(31)) ||
      (in1(31) && io.fromExecute.in2(31) && sum(31))),
    (in1 < io.fromExecute.in2),
  )
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
      EXE_SLT_OP   -> in1_lt_in2,
      EXE_SLTU_OP  -> in1_lt_in2,
      EXE_ADD_OP   -> sum,
      EXE_ADDU_OP  -> sum,
      EXE_ADDI_OP  -> sum,
      EXE_ADDIU_OP -> sum,
      EXE_SUB_OP   -> sum,
      EXE_SUBU_OP  -> sum,
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
      EXE_TEQI_OP  -> (in1 === in2),
      EXE_TNE_OP   -> (in1 =/= in2),
      EXE_TNEI_OP  -> (in1 =/= in2),
      EXE_TGE_OP   -> ~in1_lt_in2,
      EXE_TGEI_OP  -> ~in1_lt_in2,
      EXE_TGEU_OP  -> ~in1_lt_in2,
      EXE_TGEIU_OP -> ~in1_lt_in2,
      EXE_TLT_OP   -> in1_lt_in2,
      EXE_TLTI_OP  -> in1_lt_in2,
      EXE_TLTU_OP  -> in1_lt_in2,
      EXE_TLTIU_OP -> in1_lt_in2,
      EXE_TLT_OP   -> in1_lt_in2,
    ),
  )
}
