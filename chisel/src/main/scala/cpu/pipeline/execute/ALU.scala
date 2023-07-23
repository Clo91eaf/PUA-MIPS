package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class MultDivSignal extends Bundle {
  val ready  = Input(Bool())
  val result = Input(UInt(HILO_WID.W))

  val start  = Output(Bool())
  val signed = Output(Bool())
}
class Alu extends Module {
  val io = IO(new Bundle {
    val inst_info = Input(new InstInfo())
    val src_info  = Input(new SrcInfo())
    val cp0_rdata = Input(UInt(DATA_WID.W))
    val hilo = new Bundle {
      val rdata = Input(UInt(HILO_WID.W))
      val wdata = Output(UInt(HILO_WID.W))
    }
    val mul      = new MultDivSignal()
    val div      = new MultDivSignal()
    val result   = Output(UInt(DATA_WID.W))
    val overflow = Output(Bool())
    val trap     = Output(Bool())
  })
  val op   = io.inst_info.op
  val src1 = io.src_info.src1_data
  val src2 = io.src_info.src2_data

  val sum  = src1 + src2
  val diff = src1 - src2
  val slt  = src1.asSInt() < src2.asSInt()
  val sltu = src1 < src2
  val clo  = WireInit(32.U)
  val clz  = WireInit(32.U)
  for (i <- 0 until 32) {
    when(!src1(i)) {
      clo := (31 - i).U
    }.otherwise {
      clz := (31 - i).U
    }
  }

  val hilo = io.hilo.rdata

  io.hilo.wdata := MuxLookup(
    op,
    0.U,
    Seq(
      EXE_MTHI  -> Cat(src1, hilo(31, 0)),
      EXE_MTLO  -> Cat(hilo(63, 32), src1),
      EXE_MULT  -> Mux(io.mul.ready, io.mul.result, 0.U),
      EXE_MULTU -> Mux(io.mul.ready, io.mul.result, 0.U),
      EXE_MADD  -> Mux(io.mul.ready, hilo + io.mul.result, 0.U),
      EXE_MADDU -> Mux(io.mul.ready, hilo + io.mul.result, 0.U),
      EXE_MSUB  -> Mux(io.mul.ready, hilo - io.mul.result, 0.U),
      EXE_MSUBU -> Mux(io.mul.ready, hilo - io.mul.result, 0.U),
      EXE_DIV   -> Mux(io.div.ready, io.div.result, 0.U),
      EXE_DIVU  -> Mux(io.div.ready, io.div.result, 0.U),
    ),
  )

  io.mul.signed := Mux(VecInit(EXE_MULT, EXE_MADD, EXE_MSUB).contains(op), true.B, false.B)
  io.mul.start := Mux(
    VecInit(EXE_MULT, EXE_MULTU, EXE_MADD, EXE_MSUB, EXE_MADDU, EXE_MSUBU).contains(op),
    !io.mul.ready,
    false.B,
  )
  io.div.signed := Mux(VecInit(EXE_DIV).contains(op), true.B, false.B)
  io.div.start  := Mux(VecInit(EXE_DIV, EXE_DIVU).contains(op), !io.div.ready, false.B)

  io.result := MuxLookup(
    op,
    0.U,
    Seq(
      // 算数指令
      EXE_ADD  -> sum,
      EXE_ADDU -> sum,
      EXE_SUB  -> diff,
      EXE_SUBU -> diff,
      EXE_SLT  -> slt,
      EXE_SLTU -> sltu,
      // 逻辑指令
      EXE_AND -> (src1 & src2),
      EXE_OR  -> (src1 | src2),
      EXE_NOR -> (~(src1 | src2)),
      EXE_XOR -> (src1 ^ src2),
      // 移位指令
      EXE_SLL -> (src2 << src1(4, 0)),
      EXE_SRL -> (src2 >> src1(4, 0)),
      EXE_SRA -> ((src2.asSInt >> src1(4, 0)).asUInt),
      // 数据移动指令
      EXE_MFHI -> io.hilo.rdata(63, 32),
      EXE_MFLO -> io.hilo.rdata(31, 0),
      EXE_MFC0 -> io.cp0_rdata,
      EXE_MOVN -> src1,
      EXE_MOVN -> src1,
      // 前导记数指令
      EXE_CLZ -> clz,
      EXE_CLO -> clo,
      // 特殊指令
      EXE_SC -> 1.U,
      // 乘除法
      EXE_MULT  -> Mux(io.mul.ready, io.mul.result(31, 0), 0.U),
      EXE_MULTU -> Mux(io.mul.ready, io.mul.result(31, 0), 0.U),
    ),
  )

  io.overflow := MuxLookup(
    op,
    false.B,
    Seq(
      EXE_ADD -> ((src1(31) === src2(31)) & (src1(31) =/= sum(31))),
      EXE_SUB -> ((src1(31) =/= src2(31)) & (src1(31) =/= diff(31))),
    ),
  )

  io.trap := MuxLookup(
    op,
    false.B,
    Seq(
      EXE_TEQ  -> (src1 === src2),
      EXE_TNE  -> (src1 =/= src2),
      EXE_TGE  -> ~slt,
      EXE_TGEU -> ~sltu,
      EXE_TLT  -> slt,
      EXE_TLTU -> sltu,
    ),
  )
}
