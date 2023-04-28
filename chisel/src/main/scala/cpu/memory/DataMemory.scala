package cpu.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class DataMemory extends Module {
  val io = IO(new Bundle {
    val fromExecute        = Flipped(new Execute_DataMemory())
    val fromMemory         = Flipped(new Memory_DataMemory())
    val fromWriteBackStage = Flipped(new WriteBackStage_DataMemory())
    val execute            = new DataMemory_Execute()
    val memory             = new DataMemory_Memory()
    val dataSram           = new DataMemory_DataSram()
  })
  val req        = io.fromExecute.req
  val aluop      = io.fromExecute.op
  val addr       = io.fromExecute.addr
  val data       = io.fromExecute.data
  val es_waiting = io.fromExecute.waiting
  val addr_ok    = io.dataSram.addr_ok
  val data_ok    = io.dataSram.data_ok
  val rdata      = io.dataSram.rdata
  val ms_waiting = io.fromMemory.waiting

  val wen = WireInit(WRITE_DISABLE)
  val en = WireInit(CHIP_DISABLE)
  val mem_addr = WireInit(ZERO_WORD)

  switch(aluop) {
    is(EXE_SB_OP, EXE_SH_OP, EXE_SW_OP, EXE_SWL_OP, EXE_SWR_OP, EXE_SC_OP) {
      wen := WRITE_ENABLE
    }
  }

  switch(aluop) {
    is(
      EXE_LB_OP, EXE_LBU_OP, EXE_LH_OP, EXE_LHU_OP, EXE_LW_OP, EXE_LL_OP, EXE_SB_OP, EXE_SH_OP, EXE_SW_OP, EXE_LWL_OP, EXE_LWR_OP, EXE_SWL_OP, EXE_SWR_OP,
    ) {
      en := CHIP_ENABLE
    }
  }

  switch(aluop) {
    is(
      EXE_LB_OP, EXE_LBU_OP, EXE_LH_OP, EXE_LHU_OP, EXE_LW_OP, EXE_LL_OP, EXE_SB_OP, EXE_SH_OP, EXE_SW_OP,
    ) {
      mem_addr := addr
    }
    is(EXE_LWL_OP, EXE_LWR_OP, EXE_SWL_OP, EXE_SWR_OP) {
      mem_addr := Cat(addr(31, 2), 0.U(2.W))
    }
  }

  val addrLowBit2 = addr(1, 0)
  val wsel = MuxLookup(
    aluop,
    "b1111".U, // default SW,SC
    Seq(
      EXE_SB_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b0001".U,
          "b01".U -> "b0010".U,
          "b10".U -> "b0100".U,
          "b11".U -> "b1000".U,
        ),
      ),
      EXE_SH_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b0011".U,
          "b10".U -> "b1100".U,
        ),
      ),
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b11".U -> "b1111".U,
          "b10".U -> "b0111".U,
          "b01".U -> "b0011".U,
          "b00".U -> "b0001".U,
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b11".U -> "b1000".U,
          "b10".U -> "b1100".U,
          "b01".U -> "b1110".U,
          "b00".U -> "b1111".U,
        ),
      ),
    ),
  ) // wsel

  val zero32 = Wire(BUS)
  zero32 := 0.U(32.W)
  val wdata = MuxLookup(
    aluop,
    data,
    Seq(
      EXE_SB_OP -> Fill(4, data(7, 0)),
      EXE_SH_OP -> Fill(2, data(15, 0)),
      EXE_SW_OP -> data,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b11".U -> data,
          "b10".U -> Util.zeroExtend(data(31, 8)),
          "b01".U -> Util.zeroExtend(data(31, 16)),
          "b00".U -> Util.zeroExtend(data(31, 24)),
        ),
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b11".U -> Cat(data(7, 0), zero32(23, 0)),
          "b10".U -> Cat(data(15, 0), zero32(15, 0)),
          "b01".U -> Cat(data(23, 0), zero32(7, 0)),
          "b00".U -> data,
        ),
      ),
      // EXE_SC_OP -> Mux(LLbit, data, ZERO_WORD)
    ),
  ) // wdata

  val read_mask = MuxLookup(
    aluop,
    "hffffffff".U,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h000000ff".U,
          "b01".U -> "h0000ff00".U,
          "b10".U -> "h00ff0000".U,
          "b11".U -> "hff000000".U,
        ),
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h000000ff".U,
          "b01".U -> "h0000ff00".U,
          "b10".U -> "h00ff0000".U,
          "b11".U -> "hff000000".U,
        ),
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h0000ffff".U,
          "b10".U -> "hffff0000".U,
        ),
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h0000ffff".U,
          "b10".U -> "hffff0000".U,
        ),
      ),
    ),
  )
  val read_mask_next = RegNext(read_mask)

  val size = aluop match {
    case EXE_LW_OP | EXE_SW_OP              => 2.U
    case EXE_LH_OP | EXE_LHU_OP | EXE_SH_OP => 1.U
    case EXE_LB_OP | EXE_LBU_OP | EXE_SB_OP => 0.U
    case EXE_LWL_OP | EXE_SWL_OP =>
      Mux(addrLowBit2 === 3.U, 2.U, addrLowBit2)
    case EXE_LWR_OP | EXE_SWR_OP =>
      Mux(addrLowBit2 === 0.U, 2.U, 3.U - addrLowBit2)
    case _ => 0.U
  }

  val sramAddrLowBit2 = WireInit(addrLowBit2)
  switch(aluop) {
    is(EXE_LWL_OP, EXE_SWL_OP) {
      sramAddrLowBit2 := 0.U
    }
  }

  val data_sram_discard         = RegInit(0.U(2.W))
  val data_sram_data_ok_discard = addr_ok && ~(data_sram_discard.orR)
  // data sram
  io.dataSram.req    := en && req
  io.dataSram.wr     := wen && req && wsel.orR
  io.dataSram.size   := size
  io.dataSram.addr   := Cat(addr(31, 2), sramAddrLowBit2)
  io.dataSram.wstrb  := Fill(4, wen && req) & wsel
  io.dataSram.wdata  := wdata
  io.memory.rdata    := rdata & read_mask_next
  io.memory.data_ok  := data_sram_discard
  io.execute.rdata   := rdata & read_mask_next
  io.execute.addr_ok := addr_ok
  io.execute.data_ok := data_sram_discard

  when(io.fromWriteBackStage.ex || io.fromWriteBackStage.eret) {
    data_sram_discard := Cat(es_waiting, ms_waiting)
  }.elsewhen(data_ok) {
    when(data_sram_discard === 3.U) {
      data_sram_discard := 1.U
    }.elsewhen(data_sram_discard === 1.U) {
      data_sram_discard := 0.U
    }.elsewhen(data_sram_discard === 2.U) {
      data_sram_discard := 0.U
    }
  }
}
