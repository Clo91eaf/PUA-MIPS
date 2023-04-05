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
    val fromCP0 = Flipped(new CP0_Memory())

    val decoder = new Memory_Decoder()
    val execute = new Memory_Execute()
    val writeBackStage = new Memory_WriteBackStage()
    val dataMemory = new Memory_DataMemory()
    val cp0 = new Memory_CP0()
    val control = new Memory_Control()

  })
  // input-execute
  val aluop = Wire(ALU_OP_BUS)
  aluop := io.fromMemoryStage.aluop
  val mem_data_i = Wire(BUS)
  mem_data_i := io.fromDataMemory.data
  val reg2_i = Wire(BUS)
  reg2_i := io.fromMemoryStage.reg2

  // output
  val pc = Wire(BUS)
  pc := io.fromMemoryStage.pc
  io.writeBackStage.pc := pc
  val waddr = Wire(ADDR_BUS)
  io.decoder.waddr := waddr
  io.writeBackStage.waddr := waddr
  val wen = Wire(Bool())
  io.decoder.wen := wen
  io.writeBackStage.wen := wen
  val wdata = Wire(BUS)
  io.decoder.wdata := wdata
  io.writeBackStage.wdata := wdata
  val hi = Wire(BUS)
  io.execute.hi := hi
  io.writeBackStage.hi := hi
  val lo = Wire(BUS)
  io.execute.lo := lo
  io.writeBackStage.lo := lo
  val whilo = Wire(Bool())
  io.execute.whilo := whilo
  io.writeBackStage.whilo := whilo
  val LLbit_wen = Wire(Bool())
  io.writeBackStage.LLbit_wen := LLbit_wen
  val LLbit_value = Wire(Bool())
  io.writeBackStage.LLbit_value := LLbit_value
  val mem_addr = Wire(BUS)
  io.dataMemory.addr := mem_addr
  val mem_sel = Wire(DATA_MEMORY_SEL_BUS)
  io.dataMemory.sel := mem_sel
  val mem_data = Wire(BUS)
  io.dataMemory.data := mem_data
  val mem_ce = Wire(Bool())
  io.dataMemory.ce := mem_ce
  val cp0_wen = Wire(Bool())
  io.writeBackStage.cp0_wen := cp0_wen
  io.execute.cp0_wen := cp0_wen
  val cp0_waddr = Wire(CP0_ADDR_BUS)
  io.writeBackStage.cp0_waddr := cp0_waddr
  io.execute.cp0_waddr := cp0_waddr
  val cp0_data = Wire(BUS)
  io.writeBackStage.cp0_data := cp0_data
  io.execute.cp0_data := cp0_data
  val excepttype = Wire(UInt(32.W))
  io.control.excepttype := excepttype
  io.cp0.excepttype := excepttype
  val mem_we = Wire(Bool())
  io.dataMemory.wen := mem_we & ~excepttype.orR() 
  val epc = Wire(BUS)
  io.control.cp0_epc := epc
  val is_in_delayslot = Wire(Bool())
  io.cp0.is_in_delayslot := is_in_delayslot
  val current_inst_addr = Wire(BUS)
  io.cp0.current_inst_addr := current_inst_addr

  val LLbit = Wire(Bool())
  val zero32 = Wire(BUS)
  val cp0_status = Wire(BUS)
  val cp0_cause = Wire(BUS)
  val cp0_epc = Wire(BUS)
  zero32 := 0.U(32.W)

  is_in_delayslot := io.fromMemoryStage.is_in_delayslot
  current_inst_addr := io.fromMemoryStage.current_inst_addr
  epc := cp0_epc

  // 获取最新的LLbit的值
  when(reset.asBool === RST_ENABLE) {
    LLbit := false.B
  }.otherwise {
    when(io.fromWriteBackStage.LLbit_wen) {
      LLbit := io.fromWriteBackStage.LLbit_value
    }.otherwise {
      LLbit := io.fromLLbitReg.LLbit
    }
  }

  when(reset.asBool === RST_ENABLE) {
    waddr := NOP_REG_ADDR
    wen := WRITE_DISABLE
    wdata := ZERO_WORD
    hi := ZERO_WORD
    lo := ZERO_WORD
    whilo := WRITE_DISABLE
    mem_addr := ZERO_WORD
    mem_we := WRITE_DISABLE
    mem_sel := "b0000".U
    mem_data := ZERO_WORD
    mem_ce := CHIP_DISABLE
    LLbit_wen := false.B
    LLbit_value := false.B
    cp0_wen := WRITE_DISABLE
    cp0_waddr := "b00000".U
    cp0_data := ZERO_WORD
  }.otherwise {
    waddr := io.fromMemoryStage.waddr
    wen := io.fromMemoryStage.wen
    wdata := io.fromMemoryStage.wdata
    hi := io.fromMemoryStage.hi
    lo := io.fromMemoryStage.lo
    whilo := io.fromMemoryStage.whilo
    mem_we := WRITE_DISABLE
    mem_addr := ZERO_WORD
    mem_sel := "b1111".U
    mem_ce := CHIP_DISABLE
    LLbit_wen := false.B
    LLbit_value := false.B
    cp0_wen := io.fromMemoryStage.cp0_wen
    cp0_waddr := io.fromMemoryStage.cp0_waddr
    cp0_data := io.fromMemoryStage.cp0_data

    mem_we := MuxLookup(
      aluop,
      WRITE_DISABLE,
      Seq(
        EXE_SB_OP -> WRITE_ENABLE,
        EXE_SH_OP -> WRITE_ENABLE,
        EXE_SW_OP -> WRITE_ENABLE,
        EXE_SWL_OP -> WRITE_ENABLE,
        EXE_SWR_OP -> WRITE_ENABLE,
        EXE_SC_OP -> WRITE_ENABLE
      )
    )

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
        EXE_SC_OP -> Mux(LLbit, 1.U, ZERO_WORD)
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

    LLbit_wen := MuxLookup(
      aluop,
      false.B,
      Seq(
        EXE_LL_OP -> true.B,
        EXE_SC_OP -> LLbit
      )
    ) // LLbit_wen

    LLbit_value := MuxLookup(
      aluop,
      false.B,
      Seq(
        EXE_LL_OP -> true.B,
        EXE_SC_OP -> (!LLbit)
      )
    ) // LLbit_value
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_status := ZERO_WORD
  }.elsewhen(
    (io.fromWriteBackStage.cp0_wen === WRITE_ENABLE) &&
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_STATUS)
  ) {
    cp0_status := io.fromWriteBackStage.cp0_data
  }.otherwise {
    cp0_status := io.fromCP0.status
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_epc := ZERO_WORD
  }.elsewhen(
    (io.fromWriteBackStage.cp0_wen === WRITE_ENABLE) &&
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_EPC)
  ) {
    cp0_epc := io.fromWriteBackStage.cp0_data
  }.otherwise {
    cp0_epc := io.fromCP0.epc
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_cause := ZERO_WORD
  }.elsewhen(
    (io.fromWriteBackStage.cp0_wen === WRITE_ENABLE) &&
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_CAUSE)
  ) {
    cp0_cause := Cat(
      io.fromCP0.cause(31, 24),
      io.fromWriteBackStage.cp0_data(23),
      io.fromWriteBackStage.cp0_data(22),
      io.fromCP0.cause(21, 10),
      io.fromWriteBackStage.cp0_data(9, 8),
      io.fromCP0.cause(7, 0)
    )

  }.otherwise {
    cp0_cause := io.fromCP0.cause
  }

  when(reset.asBool === RST_ENABLE) {
    excepttype := ZERO_WORD
  }.otherwise {
    excepttype := ZERO_WORD

    when(io.fromMemoryStage.current_inst_addr =/= ZERO_WORD) {
      when(
        ((cp0_cause(15, 8) & (cp0_status(15, 8))) =/= 0.U) &&
          (cp0_status(1) === 0.U) &&
          (cp0_status(0))
      ) {
        excepttype := "h00000001".U // interrupt
      }.elsewhen(io.fromMemoryStage.excepttype(8)) {
        excepttype := "h00000008".U // syscall
      }.elsewhen(io.fromMemoryStage.excepttype(9)) {
        excepttype := "h0000000a".U // inst_invalid
      }.elsewhen(io.fromMemoryStage.excepttype(10)) {
        excepttype := "h0000000d".U // trap
      }.elsewhen(io.fromMemoryStage.excepttype(11)) { // ov
        excepttype := "h0000000c".U
      }.elsewhen(io.fromMemoryStage.excepttype(12)) { // 返回指令
        excepttype := "h0000000e".U
      }
    }

  }

  // debug
  // printf(p"memory :pc 0x${Hexadecimal(pc)}\n")
}
