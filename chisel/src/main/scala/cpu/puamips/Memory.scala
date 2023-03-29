package cpu.puamips

import chisel3._
import cpu.puamips.Const._
import chisel3.util._

class Memory extends Module {
  val io = IO(new Bundle {
    val fromLLbitReg = Flipped(new LLbitReg_Memory())
    val fromMemoryStage = Flipped(new MemoryStage_Memory())
    val fromDataMemory = Flipped(new DataMemory_Memory())
    val fromWriteBackStage = Flipped(new WriteBackStage_Memory())

    val decoder = new Memory_Decoder()
    val execute = new Memory_Execute()
    val writeBackStage = new Memory_WriteBackStage()
    val dataMemory = new Memory_DataMemory()
  })
  // input-execute
  val aluop = Wire(ALU_OP_BUS)
  aluop := io.fromMemoryStage.aluop
  val mem_data_i = Wire(REG_BUS)
  mem_data_i := io.fromDataMemory.data
  val reg2_i = Wire(REG_BUS)
  reg2_i := io.fromMemoryStage.reg2

  // output
  val pc = RegInit(REG_BUS_INIT)
  pc := io.fromMemoryStage.pc
  io.writeBackStage.pc := pc
  val wd = RegInit(REG_ADDR_BUS_INIT)
  io.decoder.wd := wd
  io.writeBackStage.wd := wd
  val wreg = RegInit(WRITE_DISABLE)
  io.decoder.wreg := wreg
  io.writeBackStage.wreg := wreg
  val wdata = RegInit(REG_BUS_INIT)
  io.decoder.wdata := wdata
  io.writeBackStage.wdata := wdata
  val hi = RegInit(REG_BUS_INIT)
  io.execute.hi := hi
  io.writeBackStage.hi := hi
  val lo = RegInit(REG_BUS_INIT)
  io.execute.lo := lo
  io.writeBackStage.lo := lo
  val whilo = RegInit(WRITE_DISABLE)
  io.execute.whilo := whilo
  io.writeBackStage.whilo := whilo
  val LLbit_we = RegInit(false.B)
  io.writeBackStage.LLbit_we := LLbit_we
  val LLbit_value = RegInit(false.B)
  io.writeBackStage.LLbit_value := LLbit_value
  val mem_addr = RegInit(REG_BUS_INIT)
  io.dataMemory.addr := mem_addr
  val mem_we = RegInit(WRITE_DISABLE)
  io.dataMemory.we := mem_we
  val mem_sel = RegInit(DATA_MEMORY_SEL_BUS_INIT)
  io.dataMemory.sel := mem_sel
  val mem_data = RegInit(REG_BUS_INIT)
  io.dataMemory.data := mem_data
  val mem_ce = RegInit(CHIP_DISABLE)
  io.dataMemory.ce := mem_ce

  val LLbit = RegInit(false.B)
  val zero32 = Wire(REG_BUS)
  zero32 := 0.U(32.W)

  // 获取最新的LLbit的值
  when(io.fromWriteBackStage.LLbit_we) {
    LLbit := io.fromWriteBackStage.LLbit_value
  }.otherwise {
    LLbit := io.fromLLbitReg.LLbit
  }

  wd := io.fromMemoryStage.wd
  wreg := io.fromMemoryStage.wreg
  wdata := io.fromMemoryStage.wdata
  hi := io.fromMemoryStage.hi
  lo := io.fromMemoryStage.lo
  whilo := io.fromMemoryStage.whilo
  mem_we := WRITE_DISABLE

  mem_addr := ZERO_WORD
  mem_sel := "b1111".U
  mem_ce := CHIP_DISABLE
  LLbit_we := false.B
  LLbit_value := 0.U

  mem_ce := MuxLookup(
    aluop,
    CHIP_DISABLE,
    Seq(
      EXE_LB_OP -> CHIP_ENABLE,
      EXE_LBU_OP -> CHIP_ENABLE,
      EXE_LH_OP -> CHIP_ENABLE,
      EXE_LHU_OP -> CHIP_ENABLE,
      EXE_LW_OP -> CHIP_ENABLE,
      EXE_LWL_OP -> CHIP_ENABLE,
      EXE_LWL_OP -> CHIP_ENABLE,
      EXE_LL_OP -> CHIP_ENABLE,
      EXE_SB_OP -> CHIP_ENABLE,
      EXE_SH_OP -> CHIP_ENABLE,
      EXE_SW_OP -> CHIP_ENABLE,
      EXE_SWL_OP -> CHIP_ENABLE,
      EXE_SWR_OP -> CHIP_ENABLE,
      EXE_SC_OP -> Mux(LLbit, CHIP_ENABLE, CHIP_DISABLE)
    )
  ) // mem_ce

  mem_addr := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_LB_OP -> io.fromMemoryStage.addr,
      EXE_LBU_OP -> io.fromMemoryStage.addr,
      EXE_LH_OP -> io.fromMemoryStage.addr,
      EXE_LHU_OP -> io.fromMemoryStage.addr,
      EXE_LW_OP -> io.fromMemoryStage.addr,
      EXE_LWL_OP -> Cat(io.fromMemoryStage.addr(31, 2), 0.U(2.W)),
      EXE_LWR_OP -> Cat(io.fromMemoryStage.addr(31, 2), 0.U(2.W)),
      EXE_LL_OP -> io.fromMemoryStage.addr,
      EXE_SB_OP -> io.fromMemoryStage.addr,
      EXE_SH_OP -> io.fromMemoryStage.addr,
      EXE_SW_OP -> io.fromMemoryStage.addr,
      EXE_SWL_OP -> Cat(io.fromMemoryStage.addr(31, 2), 0.U(2.W)),
      EXE_SWR_OP -> Cat(io.fromMemoryStage.addr(31, 2), 0.U(2.W)),
      EXE_SC_OP -> Mux(LLbit, io.fromMemoryStage.addr, ZERO_WORD)
    )
  ) // mem_addr

  val addrLowBit2 = io.fromMemoryStage.addr(1, 0)

  mem_sel := MuxLookup(
    aluop,
    "b1111".U,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U
        )
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U
        )
      ),
      EXE_LW_OP -> "b1111".U,
      EXE_LWL_OP -> "b1111".U,
      EXE_LWR_OP -> "b1111".U,
      EXE_LL_OP -> "b1111".U,
      EXE_SB_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_SH_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U
        )
      ),
      EXE_SW_OP -> "b1111".U,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1111".U,
          "b01".U -> "b0111".U,
          "b10".U -> "b0011".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b1100".U,
          "b10".U -> "b1110".U,
          "b11".U -> "b1111".U
        )
      ),
      EXE_SC_OP -> "b1111".U
    )
  ) // mem_sel

  wdata := MuxLookup(
    aluop,
    io.fromMemoryStage.wdata,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.signedExtend(mem_data_i(31, 24)),
          "b01".U -> Util.signedExtend(mem_data_i(23, 16)),
          "b10".U -> Util.signedExtend(mem_data_i(15, 8)),
          "b11".U -> Util.signedExtend(mem_data_i(7, 0))
        )
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.zeroExtend(mem_data_i(31, 24)),
          "b01".U -> Util.zeroExtend(mem_data_i(23, 16)),
          "b10".U -> Util.zeroExtend(mem_data_i(15, 8)),
          "b11".U -> Util.zeroExtend(mem_data_i(7, 0))
        )
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.signedExtend(mem_data_i(31, 16)),
          "b10".U -> Util.signedExtend(mem_data_i(15, 0))
        )
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.zeroExtend(mem_data_i(31, 16)),
          "b10".U -> Util.zeroExtend(mem_data_i(15, 0))
        )
      ),
      EXE_LW_OP -> mem_data_i,
      EXE_LWL_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> mem_data_i(31, 0),
          "b01".U -> Cat(mem_data_i(23, 0), reg2_i(7, 0)),
          "b10".U -> Cat(mem_data_i(15, 0), reg2_i(15, 0)),
          "b11".U -> Cat(mem_data_i(7, 0), reg2_i(23, 0))
        )
      ),
      EXE_LWR_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Cat(reg2_i(31, 8), mem_data_i(31, 24)),
          "b01".U -> Cat(reg2_i(31, 16), mem_data_i(31, 16)),
          "b10".U -> Cat(reg2_i(31, 24), mem_data_i(31, 8)),
          "b11".U -> mem_data_i
        )
      ),
      EXE_LL_OP -> mem_data_i,
      EXE_SC_OP -> Mux(!LLbit, ZERO_WORD, io.fromMemoryStage.wdata)
    )
  ) // wdata

  mem_data := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_SB_OP -> Fill(4, reg2_i(7, 0)),
      EXE_SH_OP -> Fill(2, reg2_i(15, 0)),
      EXE_SW_OP -> reg2_i,
      EXE_SWL_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> reg2_i,
          "b01".U -> Util.zeroExtend(reg2_i(31, 8)),
          "b10".U -> Util.zeroExtend(reg2_i(31, 16)),
          "b11".U -> Util.zeroExtend(reg2_i(31, 24))
        )
      ),
      EXE_SWR_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Cat(reg2_i(7, 0), zero32(23, 0)),
          "b01".U -> Cat(reg2_i(15, 0), zero32(15, 0)),
          "b10".U -> Cat(reg2_i(23, 0), zero32(7, 0)),
          "b11".U -> reg2_i
        )
      ),
      EXE_SC_OP -> Mux(LLbit, reg2_i, ZERO_WORD)
    )
  ) // mem_data

  LLbit_we := MuxLookup(
    aluop,
    false.B,
    Seq(
      EXE_LL_OP -> true.B,
      EXE_SC_OP -> LLbit
    )
  ) // LLbit_we

  LLbit_value := MuxLookup(
    aluop,
    false.B,
    Seq(
      EXE_LL_OP -> true.B,
      EXE_SC_OP -> (!LLbit)
    )
  ) // LLbit_value

  printf(p"memory :pc 0x${Hexadecimal(pc)}\n")
}
