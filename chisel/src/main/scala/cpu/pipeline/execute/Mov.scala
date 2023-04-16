package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Mov extends Module {
  val io = IO(new Bundle {
    val fromMemory         = Flipped(new Memory_Mov())
    val fromWriteBackStage = Flipped(new WriteBackStage_Mov())
    val fromExecute        = Flipped(new Execute_Mov())
    val execute            = new Mov_Execute()
  })
  val op   = io.fromExecute.op
  val in   = io.fromExecute.in
  val inst = io.fromExecute.inst
  val hi   = io.fromExecute.hi
  val lo   = io.fromExecute.lo

  val mem_cp0 =
    io.fromMemory.cp0_wen === WRITE_ENABLE &&
      io.fromMemory.cp0_waddr === inst(15, 11)
  val wbs_cp0 =
    io.fromWriteBackStage.cp0_wen === WRITE_ENABLE &&
      io.fromWriteBackStage.cp0_waddr === inst(15, 11)

  io.execute.out := MuxLookup(
    op,
    ZERO_WORD,
    Seq(
      EXE_MFHI_OP -> hi,
      EXE_MFLO_OP -> lo,
      EXE_MOVZ_OP -> in,
      EXE_MOVN_OP -> in,
      EXE_MFC0_OP -> MuxCase(
        io.fromWriteBackStage.cp0_rdata,
        Seq(
          (mem_cp0) -> io.fromMemory.cp0_wdata,
          (wbs_cp0) -> io.fromWriteBackStage.cp0_wdata,
        ),
      ),
    ),
  )
}
