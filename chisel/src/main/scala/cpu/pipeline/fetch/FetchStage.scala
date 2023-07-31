package cpu.pipeline.fetch

import chisel3._
import chisel3.util._

class FetchStage extends Module {
  val io = IO(new Bundle {
    val memory = new Bundle {
      val flush    = Input(Bool())
      val flush_pc = Input(UInt(32.W))
    }
    val decoder = new Bundle {
      val branch = Input(Bool())
      val target = Input(UInt(32.W))
    }
    val execute = new Bundle {
      val branch = Input(Bool())
      val target = Input(UInt(32.W))
    }
    val instBuffer = new Bundle {
      val full = Input(Bool())
    }
    val iCache = new Bundle {
      val inst_valid = Input(Vec(4, Bool()))
      val pc         = Output(UInt(32.W))
      val pc_next    = Output(UInt(32.W))
    }

  })
  val pc = RegNext(io.iCache.pc_next, "h_bfc00000".U(32.W))
  io.iCache.pc := pc

  // when inst_valid(1) is true, inst_valid(0) must be true
  io.iCache.pc_next := MuxCase(
    pc,
    Seq(
      io.memory.flush         -> io.memory.flush_pc,
      io.execute.branch       -> io.execute.target,
      io.decoder.branch       -> io.decoder.target,
      io.instBuffer.full      -> pc,
      io.iCache.inst_valid(3) -> (pc + 16.U),
      io.iCache.inst_valid(2) -> (pc + 12.U),
      io.iCache.inst_valid(1) -> (pc + 8.U),
      io.iCache.inst_valid(0) -> (pc + 4.U),
    ),
  )
}
