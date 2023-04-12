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

    val memoryStage    = new Memory_MemoryStage()
    val decoder        = new Memory_Decoder()
    val execute        = new Memory_Execute()
    val writeBackStage = new Memory_WriteBackStage()
    val cp0            = new Memory_CP0()
  })
  // input
  val aluop      = io.fromMemoryStage.aluop
  val pc         = io.fromMemoryStage.pc
  val reg2_i     = io.fromMemoryStage.reg2
  val mem_data_i = io.fromDataMemory.rdata
  val ms_valid   = io.fromMemoryStage.valid

  // output
  val reg_waddr         = Wire(ADDR_BUS)
  val reg_wen           = Wire(Bool())
  val reg_wdata         = Wire(BUS)
  val hi                = Wire(BUS)
  val lo                = Wire(BUS)
  val whilo             = Wire(Bool())
  val LLbit_wen         = Wire(Bool())
  val LLbit_value       = Wire(Bool())
  val cp0_wen           = Wire(Bool())
  val cp0_waddr         = Wire(CP0_ADDR_BUS)
  val cp0_wdata         = Wire(BUS)
  val except_type       = Wire(UInt(32.W))
  val epc               = Wire(BUS)
  val is_in_delayslot   = Wire(Bool())
  val current_inst_addr = Wire(BUS)
  val LLbit             = Wire(Bool())
  val zero32            = Wire(BUS)
  val cp0_status        = Wire(BUS)
  val cp0_cause         = Wire(BUS)
  val cp0_epc           = Wire(BUS)
  val allowin           = Wire(Bool())
  val valid             = Wire(Bool())
  val inst_is_mfc0      = Wire(Bool())
  val ms_fwd_valid      = Wire(Bool())

  // output-decoder
  io.decoder.reg_waddr    := reg_waddr
  io.decoder.reg_wen      := reg_wen
  io.decoder.reg_wdata    := reg_wdata
  io.decoder.inst_is_mfc0 := inst_is_mfc0
  io.decoder.ms_fwd_valid := ms_fwd_valid

  // output-execute
  io.execute.hi      := hi
  io.execute.lo      := lo
  io.execute.whilo   := whilo
  io.execute.allowin := allowin

  // output-memory stage
  io.memoryStage.allowin := allowin

  // output-write back stage
  io.writeBackStage.reg_wen      := reg_wen
  io.writeBackStage.reg_waddr    := reg_waddr
  io.writeBackStage.pc           := pc
  io.writeBackStage.reg_wdata    := reg_wdata
  io.writeBackStage.hi           := hi
  io.writeBackStage.lo           := lo
  io.writeBackStage.whilo        := whilo
  io.writeBackStage.LLbit_wen    := LLbit_wen
  io.writeBackStage.LLbit_value  := LLbit_value
  io.writeBackStage.cp0_wen      := cp0_wen
  io.writeBackStage.cp0_waddr    := cp0_waddr
  io.writeBackStage.cp0_wdata    := cp0_wdata
  io.writeBackStage.valid        := valid
  io.writeBackStage.inst_is_mfc0 := inst_is_mfc0

  // output-execute
  io.execute.cp0_wen   := cp0_wen
  io.execute.cp0_waddr := cp0_waddr
  io.execute.cp0_wdata := cp0_wdata

  // output-cp0
  io.cp0.except_type       := except_type
  io.cp0.is_in_delayslot   := is_in_delayslot
  io.cp0.current_inst_addr := current_inst_addr

  // input-memory stage
  is_in_delayslot   := io.fromMemoryStage.is_in_delayslot
  current_inst_addr := io.fromMemoryStage.current_inst_addr
  epc               := cp0_epc

  // io-finish
  inst_is_mfc0 := io.fromMemoryStage.valid && (aluop === EXE_MFC0_OP)

  ms_fwd_valid := ms_valid

  val ready_go = true.B
  allowin := !ms_valid || ready_go && io.fromWriteBackStage.allowin
  val ws_not_eret_ex = !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex
  valid := ms_valid && ready_go && ws_not_eret_ex

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
    reg_waddr   := NOP_REG_ADDR
    reg_wen     := WRITE_DISABLE
    reg_wdata   := ZERO_WORD
    hi          := ZERO_WORD
    lo          := ZERO_WORD
    whilo       := WRITE_DISABLE
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := WRITE_DISABLE
    cp0_waddr   := "b00000".U
    cp0_wdata   := ZERO_WORD
  }.otherwise {
    // input-memory stage
    reg_waddr   := io.fromMemoryStage.reg_waddr
    reg_wen     := io.fromMemoryStage.reg_wen
    reg_wdata   := io.fromMemoryStage.reg_wdata
    hi          := io.fromMemoryStage.hi
    lo          := io.fromMemoryStage.lo
    whilo       := io.fromMemoryStage.whilo
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := io.fromMemoryStage.cp0_wen
    cp0_waddr   := io.fromMemoryStage.cp0_waddr
    cp0_wdata   := io.fromMemoryStage.cp0_wdata

    val addrLowBit2 = io.fromMemoryStage.mem_addr(1, 0)

    reg_wdata := MuxLookup(
      aluop,
      io.fromMemoryStage.reg_wdata,
      Seq(
        EXE_LB_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.signedExtend(mem_data_i(7, 0)),
            "b01".U -> Util.signedExtend(mem_data_i(15, 8)),
            "b10".U -> Util.signedExtend(mem_data_i(23, 16)),
            "b11".U -> Util.signedExtend(mem_data_i(31, 24)),
          ),
        ),
        EXE_LBU_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.zeroExtend(mem_data_i(7, 0)),
            "b01".U -> Util.zeroExtend(mem_data_i(15, 8)),
            "b10".U -> Util.zeroExtend(mem_data_i(23, 16)),
            "b11".U -> Util.zeroExtend(mem_data_i(31, 24)),
          ),
        ),
        EXE_LH_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.signedExtend(mem_data_i(15, 0)),
            "b10".U -> Util.signedExtend(mem_data_i(31, 16)),
          ),
        ),
        EXE_LHU_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.zeroExtend(mem_data_i(15, 0)),
            "b10".U -> Util.zeroExtend(mem_data_i(31, 16)),
          ),
        ),
        EXE_LW_OP -> mem_data_i,
        EXE_LWL_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> mem_data_i(31, 0),
            "b01".U -> Cat(mem_data_i(23, 0), reg2_i(7, 0)),
            "b10".U -> Cat(mem_data_i(15, 0), reg2_i(15, 0)),
            "b11".U -> Cat(mem_data_i(7, 0), reg2_i(23, 0)),
          ),
        ),
        EXE_LWR_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Cat(reg2_i(31, 8), mem_data_i(31, 24)),
            "b01".U -> Cat(reg2_i(31, 16), mem_data_i(31, 16)),
            "b10".U -> Cat(reg2_i(31, 24), mem_data_i(31, 8)),
            "b11".U -> mem_data_i,
          ),
        ),
        EXE_LL_OP -> mem_data_i,
        EXE_SC_OP -> Mux(LLbit, 1.U, ZERO_WORD),
      ),
    ) // reg_wdata

    LLbit_wen := MuxLookup(
      aluop,
      false.B,
      Seq(
        EXE_LL_OP -> true.B,
        EXE_SC_OP -> LLbit,
      ),
    ) // LLbit_wen

    LLbit_value := MuxLookup(
      aluop,
      false.B,
      Seq(
        EXE_LL_OP -> true.B,
        EXE_SC_OP -> (!LLbit),
      ),
    ) // LLbit_value
  }

  when(reset.asBool === RST_ENABLE) {
    cp0_status := ZERO_WORD
  }.elsewhen(
    (io.fromWriteBackStage.cp0_wen === WRITE_ENABLE) &&
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_STATUS),
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
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_EPC),
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
      (io.fromWriteBackStage.cp0_waddr === CP0_REG_CAUSE),
  ) {
    cp0_cause := Cat(
      io.fromCP0.cause(31, 24),
      io.fromWriteBackStage.cp0_wdata(23),
      io.fromWriteBackStage.cp0_wdata(22),
      io.fromCP0.cause(21, 10),
      io.fromWriteBackStage.cp0_wdata(9, 8),
      io.fromCP0.cause(7, 0),
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
          (cp0_status(0)),
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
