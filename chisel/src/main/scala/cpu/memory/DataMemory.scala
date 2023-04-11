package cpu.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class DataMemory extends Module {
  val io = IO(new Bundle {
    val fromExecute  = Flipped(new Execute_DataMemory())
    val fromDataSram = Flipped(new DataSram_DataMemory())
    val memoryStage  = new DataMemory_DataStage()
    val memory       = new DataMemory_Memory()
    val dataSram     = new DataMemory_DataSram()
  })
  val aluop = io.fromExecute.op
  val addr  = io.fromExecute.addr
  val data  = io.fromExecute.data
  val rdata = io.fromDataSram.mem_rdata

  val mem_wen = MuxLookup(
    aluop,
    WRITE_DISABLE,
    Seq(
      EXE_SB_OP  -> WRITE_ENABLE,
      EXE_SH_OP  -> WRITE_ENABLE,
      EXE_SW_OP  -> WRITE_ENABLE,
      EXE_SWL_OP -> WRITE_ENABLE,
      EXE_SWR_OP -> WRITE_ENABLE,
      EXE_SC_OP  -> WRITE_ENABLE,
    ),
  )
  val mem_ce = MuxLookup(
    aluop,
    CHIP_DISABLE,
    Seq(
      EXE_LB_OP  -> CHIP_ENABLE,
      EXE_LBU_OP -> CHIP_ENABLE,
      EXE_LH_OP  -> CHIP_ENABLE,
      EXE_LHU_OP -> CHIP_ENABLE,
      EXE_LW_OP  -> CHIP_ENABLE,
      EXE_LWL_OP -> CHIP_ENABLE,
      EXE_LWL_OP -> CHIP_ENABLE,
      EXE_LL_OP  -> CHIP_ENABLE,
      EXE_SB_OP  -> CHIP_ENABLE,
      EXE_SH_OP  -> CHIP_ENABLE,
      EXE_SW_OP  -> CHIP_ENABLE,
      EXE_SWL_OP -> CHIP_ENABLE,
      EXE_SWR_OP -> CHIP_ENABLE,
      // EXE_SC_OP  -> Mux(LLbit, CHIP_ENABLE, CHIP_DISABLE)
    ),
  ) // mem_ce
  val mem_addr = MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_LB_OP  -> addr,
      EXE_LBU_OP -> addr,
      EXE_LH_OP  -> addr,
      EXE_LHU_OP -> addr,
      EXE_LW_OP  -> addr,
      EXE_LWL_OP -> Cat(addr(31, 2), 0.U(2.W)),
      EXE_LWR_OP -> Cat(addr(31, 2), 0.U(2.W)),
      EXE_LL_OP  -> addr,
      EXE_SB_OP  -> addr,
      EXE_SH_OP  -> addr,
      EXE_SW_OP  -> addr,
      EXE_SWL_OP -> Cat(addr(31, 2), 0.U(2.W)),
      EXE_SWR_OP -> Cat(addr(31, 2), 0.U(2.W)),
      // EXE_SC_OP  -> Mux(LLbit, addr, ZERO_WORD)
    ),
  ) // mem_addr

  val addrLowBit2 = addr(1, 0)
  val mem_wsel = MuxLookup(
    aluop,
    "b1111".U,
    Seq(
      EXE_SB_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U,
        ),
      ),
      EXE_SH_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U,
        ),
      ),
      EXE_SW_OP -> "b1111".U,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1111".U,
          "b01".U -> "b0111".U,
          "b10".U -> "b0011".U,
          "b11".U -> "b0001".U,
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b1100".U,
          "b10".U -> "b1110".U,
          "b11".U -> "b1111".U,
        ),
      ),
      EXE_SC_OP -> "b1111".U,
    ),
  ) // mem_wsel

  val zero32 = Wire(BUS)
  zero32 := 0.U(32.W)
  val mem_wdata = MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_SB_OP -> Fill(4, data(7, 0)),
      EXE_SH_OP -> Fill(2, data(15, 0)),
      EXE_SW_OP -> data,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> data,
          "b01".U -> Util.zeroExtend(data(31, 8)),
          "b10".U -> Util.zeroExtend(data(31, 16)),
          "b11".U -> Util.zeroExtend(data(31, 24)),
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Cat(data(7, 0), zero32(23, 0)),
          "b01".U -> Cat(data(15, 0), zero32(15, 0)),
          "b10".U -> Cat(data(23, 0), zero32(7, 0)),
          "b11".U -> data,
        ),
      ),
      // EXE_SC_OP -> Mux(LLbit, data, ZERO_WORD)
    ),
  ) // mem_wdata

  val read_mask = MuxLookup(
    aluop,
    "b1111".U,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "hf000".U,
          "b01".U -> "h0f00".U,
          "b10".U -> "h00f0".U,
          "b11".U -> "h000f".U,
        ),
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "hf000".U,
          "b01".U -> "h0f00".U,
          "b10".U -> "h00f0".U,
          "b11".U -> "h000f".U,
        ),
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "hff00".U,
          "b10".U -> "h00ff".U,
        ),
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "hff00".U,
          "b10".U -> "h00ff".U,
        ),
      ),
    ),
  )

  io.dataSram.mem_addr    := Cat(mem_addr(31, 2), 0.U(2.W))
  io.dataSram.mem_wsel    := mem_wsel
  io.dataSram.mem_wdata   := mem_wdata
  io.dataSram.mem_ce      := mem_ce
  io.dataSram.mem_wen     := mem_wen & io.fromExecute.valid
  io.memory.mem_rdata     := rdata & read_mask 
  io.memoryStage.mem_addr := mem_addr
}
