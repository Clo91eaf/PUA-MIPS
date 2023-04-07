package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Memory extends Module {
  val io = IO(new Bundle {
    val fromLLbitReg       = Flipped(new LLbitReg_Memory())
    val fromMemoryStage    = Flipped(new MemoryStage_Memory())
    val fromDataMemory     = Flipped(new DataMemory_Memory())
    val fromWriteBackStage = Flipped(new WriteBackStage_Memory())
    val fromCP0            = Flipped(new CP0_Memory())

    val decoder        = new Memory_Decoder()
    val execute        = new Memory_Execute()
    val writeBackStage = new Memory_WriteBackStage()
    val dataMemory     = new Memory_DataMemory()
    val cp0            = new Memory_CP0()
    val control        = new Memory_Control()

  })
  // input
  val aluop      = Wire(ALU_OP_BUS)
  val pc         = Wire(BUS)
  val reg2_i     = Wire(BUS)
  val mem_data_i = Wire(BUS)

  // input-execute
  aluop := io.fromMemoryStage.aluop

  // input-memory stage
  pc     := io.fromMemoryStage.pc
  reg2_i := io.fromMemoryStage.reg2

  // input-data memory
  mem_data_i := io.fromDataMemory.mem_rdata

  // output
  val reg_waddr             = Wire(ADDR_BUS)
  val reg_wen               = Wire(Bool())
  val reg_wdata             = Wire(BUS)
  val hi                = Wire(BUS)
  val lo                = Wire(BUS)
  val whilo             = Wire(Bool())
  val LLbit_wen         = Wire(Bool())
  val LLbit_value       = Wire(Bool())
  val mem_addr          = Wire(BUS)
  val mem_wsel           = Wire(DATA_MEMORY_SEL_BUS)
  val mem_wdata          = Wire(BUS)
  val mem_ce            = Wire(Bool())
  val cp0_wen           = Wire(Bool())
  val cp0_waddr         = Wire(CP0_ADDR_BUS)
  val cp0_wdata          = Wire(BUS)
  val except_type       = Wire(UInt(32.W))
  val mem_wen            = Wire(Bool())
  val epc               = Wire(BUS)
  val is_in_delayslot   = Wire(Bool())
  val current_inst_addr = Wire(BUS)
  val LLbit             = Wire(Bool())
  val zero32            = Wire(BUS)
  val cp0_status        = Wire(BUS)
  val cp0_cause         = Wire(BUS)
  val cp0_epc           = Wire(BUS)

  // output-decoder
  io.decoder.reg_waddr := reg_waddr
  io.decoder.reg_wen   := reg_wen
  io.decoder.reg_wdata := reg_wdata

  // output-execute
  io.execute.hi    := hi
  io.execute.lo    := lo
  io.execute.whilo := whilo

  // output-write back stage
  io.writeBackStage.reg_wen         := reg_wen
  io.writeBackStage.reg_waddr       := reg_waddr
  io.writeBackStage.pc          := pc
  io.writeBackStage.reg_wdata       := reg_wdata
  io.writeBackStage.hi          := hi
  io.writeBackStage.lo          := lo
  io.writeBackStage.whilo       := whilo
  io.writeBackStage.LLbit_wen   := LLbit_wen
  io.writeBackStage.LLbit_value := LLbit_value
  io.writeBackStage.cp0_wen     := cp0_wen
  io.writeBackStage.cp0_waddr   := cp0_waddr
  io.writeBackStage.cp0_wdata   := cp0_wdata

  // output-data memory
  io.dataMemory.mem_addr := mem_addr
  io.dataMemory.mem_wsel  := mem_wsel
  io.dataMemory.mem_wdata := mem_wdata
  io.dataMemory.mem_ce   := mem_ce
  io.dataMemory.mem_wen  := mem_wen & ~except_type.orR()

  // output-execute
  io.execute.cp0_wen   := cp0_wen
  io.execute.cp0_waddr := cp0_waddr
  io.execute.cp0_wdata := cp0_wdata

  // output-control
  io.control.except_type := except_type
  io.control.cp0_epc     := epc

  // output-cp0
  io.cp0.except_type       := except_type
  io.cp0.is_in_delayslot   := is_in_delayslot
  io.cp0.current_inst_addr := current_inst_addr

  // input-memory stage
  is_in_delayslot   := io.fromMemoryStage.is_in_delayslot
  current_inst_addr := io.fromMemoryStage.current_inst_addr
  epc               := cp0_epc

  // io-finish
  zero32 := 0.U(32.W)

  // 获取最新的LLbit的值
  when(reset.asBool === RST_ENABLE) {
    LLbit := false.B
  }.otherwise {
    when(io.fromWriteBackStage.LLbit_wen) {

      // input-write back stage
      LLbit := io.fromWriteBackStage.LLbit_value
    }.otherwise {

      // input-l lbit reg
      LLbit := io.fromLLbitReg.LLbit
    }
  }

  when(reset.asBool === RST_ENABLE) {
    reg_waddr       := NOP_REG_ADDR
    reg_wen         := WRITE_DISABLE
    reg_wdata       := ZERO_WORD
    hi          := ZERO_WORD
    lo          := ZERO_WORD
    whilo       := WRITE_DISABLE
    mem_addr    := ZERO_WORD
    mem_wen     := WRITE_DISABLE
    mem_wsel     := "b0000".U
    mem_wdata    := ZERO_WORD
    mem_ce      := CHIP_DISABLE
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := WRITE_DISABLE
    cp0_waddr   := "b00000".U
    cp0_wdata   := ZERO_WORD
  }.otherwise {
    // input-memory stage
    reg_waddr       := io.fromMemoryStage.reg_waddr
    reg_wen         := io.fromMemoryStage.reg_wen
    reg_wdata       := io.fromMemoryStage.reg_wdata
    hi          := io.fromMemoryStage.hi
    lo          := io.fromMemoryStage.lo
    whilo       := io.fromMemoryStage.whilo
    mem_wen     := WRITE_DISABLE
    mem_addr    := ZERO_WORD
    mem_wsel     := "b1111".U
    mem_ce      := CHIP_DISABLE
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := io.fromMemoryStage.cp0_wen
    cp0_waddr   := io.fromMemoryStage.cp0_waddr
    cp0_wdata   := io.fromMemoryStage.cp0_wdata

    mem_wen := MuxLookup(
      aluop,
      WRITE_DISABLE,
      Seq(
        EXE_SB_OP  -> WRITE_ENABLE,
        EXE_SH_OP  -> WRITE_ENABLE,
        EXE_SW_OP  -> WRITE_ENABLE,
        EXE_SWL_OP -> WRITE_ENABLE,
        EXE_SWR_OP -> WRITE_ENABLE,
        EXE_SC_OP  -> WRITE_ENABLE
      )
    )

    mem_ce := MuxLookup(
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
        EXE_SC_OP  -> Mux(LLbit, CHIP_ENABLE, CHIP_DISABLE)
      )
    ) // mem_ce

    mem_addr := MuxLookup(
      aluop,
      ZERO_WORD,
      Seq(
        EXE_LB_OP  -> io.fromMemoryStage.mem_addr,
        EXE_LBU_OP -> io.fromMemoryStage.mem_addr,
        EXE_LH_OP  -> io.fromMemoryStage.mem_addr,
        EXE_LHU_OP -> io.fromMemoryStage.mem_addr,
        EXE_LW_OP  -> io.fromMemoryStage.mem_addr,
        EXE_LWL_OP -> Cat(io.fromMemoryStage.mem_addr(31, 2), 0.U(2.W)),
        EXE_LWR_OP -> Cat(io.fromMemoryStage.mem_addr(31, 2), 0.U(2.W)),
        EXE_LL_OP  -> io.fromMemoryStage.mem_addr,
        EXE_SB_OP  -> io.fromMemoryStage.mem_addr,
        EXE_SH_OP  -> io.fromMemoryStage.mem_addr,
        EXE_SW_OP  -> io.fromMemoryStage.mem_addr,
        EXE_SWL_OP -> Cat(io.fromMemoryStage.mem_addr(31, 2), 0.U(2.W)),
        EXE_SWR_OP -> Cat(io.fromMemoryStage.mem_addr(31, 2), 0.U(2.W)),
        EXE_SC_OP  -> Mux(LLbit, io.fromMemoryStage.mem_addr, ZERO_WORD)
      )
    ) // mem_addr

    val addrLowBit2 = io.fromMemoryStage.mem_addr(1, 0)

    mem_wsel := MuxLookup(
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
        EXE_LW_OP  -> "b1111".U,
        EXE_LWL_OP -> "b1111".U,
        EXE_LWR_OP -> "b1111".U,
        EXE_LL_OP  -> "b1111".U,
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
    ) // mem_wsel

    reg_wdata := MuxLookup(
      aluop,
      io.fromMemoryStage.reg_wdata,
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
    ) // reg_wdata

    mem_wdata := MuxLookup(
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
    ) // mem_wdata

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

    // input-write back stage
    cp0_status := io.fromWriteBackStage.cp0_wdata
  }.otherwise {

    // input-c p0
    cp0_status := io.fromCP0.status
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_epc := ZERO_WORD
  }.elsewhen(
    (io.fromWriteBackStage.cp0_wen === WRITE_ENABLE) &&
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_EPC)
  ) {

    // input-write back stage
    cp0_epc := io.fromWriteBackStage.cp0_wdata
  }.otherwise {

    // input-c p0
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
      io.fromWriteBackStage.cp0_wdata(23),
      io.fromWriteBackStage.cp0_wdata(22),
      io.fromCP0.cause(21, 10),
      io.fromWriteBackStage.cp0_wdata(9, 8),
      io.fromCP0.cause(7, 0)
    )

  }.otherwise {
    cp0_cause := io.fromCP0.cause
  }

  when(reset.asBool === RST_ENABLE) {
    except_type := ZERO_WORD
  }.otherwise {
    except_type := ZERO_WORD

    when(io.fromMemoryStage.current_inst_addr =/= ZERO_WORD) {
      when(
        ((cp0_cause(15, 8) & (cp0_status(15, 8))) =/= 0.U) &&
          (cp0_status(1) === 0.U) &&
          (cp0_status(0))
      ) {
        except_type := "h00000001".U // interrupt
      }.elsewhen(io.fromMemoryStage.except_type(8)) {
        except_type := "h00000008".U // syscall
      }.elsewhen(io.fromMemoryStage.except_type(9)) {
        except_type := "h0000000a".U // inst_invalid
      }.elsewhen(io.fromMemoryStage.except_type(10)) {
        except_type := "h0000000d".U // trap
      }.elsewhen(io.fromMemoryStage.except_type(11)) { // ov
        except_type := "h0000000c".U
      }.elsewhen(io.fromMemoryStage.except_type(12)) { // 返回指令
        except_type := "h0000000e".U
      }
    }

  }

  // debug
  // printf(p"memory :pc 0x${Hexadecimal(pc)}\n")
}
