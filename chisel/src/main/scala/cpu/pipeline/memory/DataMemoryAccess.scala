package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class DataMemoryAccess(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val memoryUnit = new Bundle {
      val in = Input(new Bundle {
        val mem_en    = Bool()
        val inst_info = new InstInfo()
        val mem_wdata = UInt(DATA_WID.W)
        val mem_addr  = UInt(DATA_ADDR_WID.W)
        val mem_sel   = Vec(config.fuNum, Bool())
        val ex        = Vec(config.fuNum, new ExceptionInfo())
      })
      val out = Output(new Bundle {
        val mem_rdata = Output(UInt(DATA_WID.W))
      })
    }

    val dataMemory = new Bundle {
      val in = Input(new Bundle {
        val rdata = UInt(DATA_WID.W)
      })
      val out = Output(new Bundle {
        val en    = Bool()
        val rlen  = UInt(2.W)
        val wen   = UInt(4.W)
        val addr  = UInt(DATA_ADDR_WID.W)
        val wdata = UInt(DATA_WID.W)
      })
    }
  })
  val mem_addr  = io.memoryUnit.in.mem_addr
  val mem_addr2 = mem_addr(1, 0)
  val mem_rdata = io.dataMemory.in.rdata
  val mem_wdata = io.memoryUnit.in.mem_wdata
  val op        = io.memoryUnit.in.inst_info.op
  io.dataMemory.out.en := io.memoryUnit.in.mem_en &&
    (io.memoryUnit.in.mem_sel(0) && !io.memoryUnit.in.ex(0).flush_req ||
      io.memoryUnit.in.mem_sel(1) && io.memoryUnit.in.ex(0).flush_req && !io.memoryUnit.in.ex(1).flush_req)
  io.dataMemory.out.addr := mem_addr

  io.memoryUnit.out.mem_rdata := MuxLookup(
    op,
    0.U,
    Seq(
      EXE_LB -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> Util.signedExtend(mem_rdata(31, 24)),
          "b10".U -> Util.signedExtend(mem_rdata(23, 16)),
          "b01".U -> Util.signedExtend(mem_rdata(15, 8)),
          "b00".U -> Util.signedExtend(mem_rdata(7, 0)),
        ),
      ),
      EXE_LBU -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> Util.zeroExtend(mem_rdata(31, 24)),
          "b10".U -> Util.zeroExtend(mem_rdata(23, 16)),
          "b01".U -> Util.zeroExtend(mem_rdata(15, 8)),
          "b00".U -> Util.zeroExtend(mem_rdata(7, 0)),
        ),
      ),
      EXE_LH -> Mux(
        mem_addr2(1),
        Util.signedExtend(mem_rdata(31, 16)),
        Util.signedExtend(mem_rdata(15, 0)),
      ),
      EXE_LHU -> Mux(
        mem_addr2(1),
        Util.zeroExtend(mem_rdata(31, 16)),
        Util.zeroExtend(mem_rdata(15, 0)),
      ),
      EXE_LW -> mem_rdata,
      EXE_LL -> mem_rdata,
      EXE_LWL -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> mem_rdata,
          "b10".U -> Cat(mem_rdata(23, 0), mem_wdata(7, 0)),
          "b01".U -> Cat(mem_rdata(15, 0), mem_wdata(15, 0)),
          "b00".U -> Cat(mem_rdata(7, 0), mem_wdata(23, 0)),
        ),
      ),
      EXE_LWR -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> Cat(mem_wdata(31, 8), mem_rdata(31, 24)),
          "b10".U -> Cat(mem_wdata(31, 16), mem_rdata(31, 16)),
          "b01".U -> Cat(mem_wdata(31, 24), mem_rdata(31, 8)),
          "b00".U -> mem_rdata,
        ),
      ),
    ),
  )
  io.dataMemory.out.wdata := MuxLookup(
    op,
    mem_wdata, // default SW, SC
    Seq(
      EXE_SB -> Fill(4, mem_wdata(7, 0)),
      EXE_SH -> Fill(2, mem_wdata(15, 0)),
      EXE_SWL -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> mem_wdata,
          "b10".U -> Cat(0.U(8.W), mem_wdata(31, 8)),
          "b01".U -> Cat(0.U(16.W), mem_wdata(31, 16)),
          "b00".U -> Cat(0.U(24.W), mem_wdata(31, 24)),
        ),
      ),
      EXE_SWR -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> Cat(mem_wdata(7, 0), 0.U(24.W)),
          "b10".U -> Cat(mem_wdata(15, 0), 0.U(16.W)),
          "b01".U -> Cat(mem_wdata(23, 0), 0.U(8.W)),
          "b00".U -> mem_wdata,
        ),
      ),
    ),
  )
  io.dataMemory.out.wen := MuxLookup(
    op,
    0.U,
    Seq(
      EXE_SB -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> "b1000".U,
          "b10".U -> "b0100".U,
          "b01".U -> "b0010".U,
          "b00".U -> "b0001".U,
        ),
      ),
      EXE_SH -> Mux(mem_addr2(1), "b1100".U, "b0011".U),
      EXE_SW -> "b1111".U,
      EXE_SC -> "b1111".U,
      EXE_SWL -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> "b1111".U,
          "b10".U -> "b0111".U,
          "b01".U -> "b0011".U,
          "b00".U -> "b0001".U,
        ),
      ),
      EXE_SWR -> MuxLookup(
        mem_addr2,
        0.U,
        Seq(
          "b11".U -> "b1000".U,
          "b10".U -> "b1100".U,
          "b01".U -> "b1110".U,
          "b00".U -> "b1111".U,
        ),
      ),
    ),
  )
  io.dataMemory.out.rlen := MuxLookup(
    op,
    0.U,
    Seq(
      EXE_LW  -> 2.U,
      EXE_LL  -> 2.U,
      EXE_LH  -> 1.U,
      EXE_LHU -> 1.U,
      EXE_LB  -> 0.U,
      EXE_LBU -> 0.U,
      EXE_LWL -> 2.U,
      EXE_LWR -> 2.U,
      EXE_SW  -> 2.U,
      EXE_SWL -> 2.U,
      EXE_SWR -> 2.U,
      EXE_SC  -> 2.U,
      EXE_SH  -> 1.U,
      EXE_SB  -> 0.U,
    ),
  )
}
