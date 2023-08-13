package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig

class FetchUnit(implicit
    val config: CpuConfig,
) extends Module {
  val io = IO(new Bundle {
    val memory = new Bundle {
      val flush    = Input(Bool())
      val flush_pc = Input(UInt(PC_WID.W))
    }
    val decoder = new Bundle {
      val branch = Input(Bool())
      val target = Input(UInt(PC_WID.W))
    }
    val execute = new Bundle {
      val branch = Input(Bool())
      val target = Input(UInt(PC_WID.W))
    }
    val instFifo = new Bundle {
      val full = Input(Bool())
    }
    val iCache = new Bundle {
      val inst_valid = Input(Vec(config.instFetchNum, Bool()))
      val pc         = Output(UInt(PC_WID.W))
      val pc_next    = Output(UInt(PC_WID.W))
    }

  })
  val pc = RegNext(io.iCache.pc_next, "h_bfc00000".U(32.W))
  io.iCache.pc := pc

  // when inst_valid(1) is true, inst_valid(0) must be true

  val pc_next_temp = Wire(UInt(PC_WID.W))

  pc_next_temp := pc
  for (i <- 0 until config.instFetchNum) {
    when(io.iCache.inst_valid(i)) {
      pc_next_temp := pc + ((i + 1) * 4).U
    }
  }

  io.iCache.pc_next := MuxCase(
    pc_next_temp,
    Seq(
      io.memory.flush   -> io.memory.flush_pc,
      io.execute.branch -> io.execute.target,
      io.decoder.branch -> io.decoder.target,
      io.instFifo.full  -> pc,
    ),
  )
}
