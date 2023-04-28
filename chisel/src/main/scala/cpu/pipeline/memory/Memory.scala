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

    val decoder        = new Memory_Decoder()
    val mov            = new Memory_Mov()
    val memoryStage    = new Memory_MemoryStage()
    val dataMemory     = new Memory_DataMemory()
    val execute        = new Memory_Execute()
    val writeBackStage = new Memory_WriteBackStage()
  })
  // input
  val aluop      = io.fromMemoryStage.aluop
  val pc         = io.fromMemoryStage.pc
  val reg2_i     = io.fromMemoryStage.reg2
  val mem_data_i = io.fromDataMemory.rdata
  val ms_valid   = io.fromMemoryStage.valid

  // output
  val reg_waddr       = Wire(ADDR_BUS)
  val reg_wen         = Wire(REG_WRITE_BUS)
  val reg_wdata       = Wire(BUS)
  val hi              = Wire(BUS)
  val lo              = Wire(BUS)
  val whilo           = Wire(Bool())
  val LLbit_wen       = Wire(Bool())
  val LLbit_value     = Wire(Bool())
  val cp0_wen         = Wire(Bool())
  val cp0_waddr       = Wire(CP0_ADDR_BUS)
  val cp0_wdata       = Wire(BUS)
  val is_in_delayslot = Wire(Bool())
  val LLbit           = Wire(Bool())
  val zero32          = Wire(BUS)
  val allowin         = Wire(Bool())
  val valid           = Wire(Bool())
  val inst_is_mfc0    = Wire(Bool())
  val inst_is_mtc0    = Wire(Bool())
  val inst_is_eret    = Wire(Bool())
  val inst_is_syscall = Wire(Bool())
  val ms_fwd_valid    = Wire(Bool())

  // output-decoder
  io.decoder.reg_waddr    := reg_waddr
  io.decoder.reg_wen      := reg_wen
  io.decoder.reg_wdata    := reg_wdata
  io.decoder.inst_is_mfc0 := inst_is_mfc0
  io.decoder.ms_fwd_valid := ms_fwd_valid

  // output-execute
  val ms_data_buff       = RegInit(BUS_INIT)
  val ms_data_buff_valid = RegInit(false.B)
  io.execute.hi          := hi
  io.execute.lo          := lo
  io.execute.whilo       := whilo && valid
  io.execute.allowin     := allowin
  io.execute.ex          := ms_valid && io.fromMemoryStage.ex
  io.execute.eret        := ms_valid && inst_is_eret
  io.execute.inst_unable := !ms_valid || ms_data_buff_valid || io.fromMemoryStage.data_ok

  when(!ms_data_buff_valid && ms_valid && io.fromDataMemory.data_ok) {
    ms_data_buff_valid := true.B
    ms_data_buff       := io.fromDataMemory.rdata
  }.elsewhen(
    io.fromWriteBackStage.allowin && io.fromWriteBackStage.eret && io.fromWriteBackStage.ex,
  ) {
    ms_data_buff_valid := false.B
    ms_data_buff       := BUS_INIT
  }

  val data_ok =
    io.fromMemoryStage.data_ok || (io.fromMemoryStage.wait_mem && io.fromDataMemory.data_ok)
  val data = MuxCase(
    io.fromDataMemory.rdata,
    Seq(
      io.fromMemoryStage.data_ok -> io.fromMemoryStage.data,
      ms_data_buff_valid         -> ms_data_buff,
    ),
  )
  io.dataMemory.waiting := ms_valid && io.fromMemoryStage.wait_mem && !data_ok

  // output-memory stage
  io.memoryStage.allowin := allowin

  // output-write back stage
  io.writeBackStage.reg_wen         := reg_wen
  io.writeBackStage.reg_waddr       := reg_waddr
  io.writeBackStage.pc              := pc
  io.writeBackStage.reg_wdata       := reg_wdata
  io.writeBackStage.hi              := hi
  io.writeBackStage.lo              := lo
  io.writeBackStage.whilo           := whilo
  io.writeBackStage.LLbit_wen       := LLbit_wen
  io.writeBackStage.LLbit_value     := LLbit_value
  io.writeBackStage.valid           := valid
  io.writeBackStage.inst_is_mfc0    := inst_is_mfc0
  io.writeBackStage.inst_is_mtc0    := inst_is_mtc0
  io.writeBackStage.inst_is_eret    := inst_is_eret
  io.writeBackStage.inst_is_syscall := inst_is_syscall
  io.writeBackStage.cp0_addr        := io.fromMemoryStage.cp0_addr
  io.writeBackStage.excode          := io.fromMemoryStage.excode
  io.writeBackStage.badvaddr        := io.fromMemoryStage.badvaddr
  io.writeBackStage.ex              := io.fromMemoryStage.ex
  io.writeBackStage.bd              := io.fromMemoryStage.bd

  // output-execute
  io.mov.cp0_wen   := cp0_wen
  io.mov.cp0_waddr := cp0_waddr
  io.mov.cp0_wdata := cp0_wdata

  // input-memory stage
  is_in_delayslot := io.fromMemoryStage.is_in_delayslot

  /*-------------------------------io finish-------------------------------*/
  inst_is_mfc0    := io.fromMemoryStage.valid && (aluop === EXE_MFC0_OP)
  inst_is_mtc0    := io.fromMemoryStage.valid && (aluop === EXE_MTC0_OP)
  inst_is_eret    := io.fromMemoryStage.valid && (aluop === EXE_ERET_OP)
  inst_is_syscall := io.fromMemoryStage.valid && (aluop === EXE_SYSCALL_OP)

  ms_fwd_valid := valid // p195 ms_to_ws_valid

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
    reg_wen     := REG_WRITE_DISABLE
    reg_wdata   := ZERO_WORD
    hi          := ZERO_WORD
    lo          := ZERO_WORD
    whilo       := WRITE_DISABLE
    LLbit_wen   := false.B
    LLbit_value := false.B
    cp0_wen     := WRITE_DISABLE
    cp0_waddr   := 0.U
    cp0_wdata   := ZERO_WORD
  }.otherwise {
    // input-memory stage
    reg_waddr   := io.fromMemoryStage.reg_waddr
    hi          := io.fromMemoryStage.hi
    lo          := io.fromMemoryStage.lo
    whilo       := io.fromMemoryStage.whilo
    LLbit_wen   := false.B
    LLbit_value := false.B

    cp0_waddr := io.fromMemoryStage.cp0_addr
    cp0_wen   := (aluop === EXE_MTC0_OP) && ms_valid
    cp0_wdata := reg_wdata

    val addrLowBit2 = io.fromMemoryStage.mem_addr(1, 0)

    reg_wen := MuxLookup(
      aluop,
      io.fromMemoryStage.reg_wen,
      Seq(
        EXE_LWL_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> "b1000".U,
            "b01".U -> "b1100".U,
            "b10".U -> "b1110".U,
            "b11".U -> "b1111".U,
          ),
        ),
        EXE_LWR_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> "b1111".U,
            "b01".U -> "b0111".U,
            "b10".U -> "b0011".U,
            "b11".U -> "b0001".U,
          ),
        ),
      ),
    )

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
            "b00".U -> Cat(mem_data_i(7, 0), reg2_i(23, 0)),
            "b01".U -> Cat(mem_data_i(15, 0), reg2_i(15, 0)),
            "b10".U -> Cat(mem_data_i(23, 0), reg2_i(7, 0)),
            "b11".U -> mem_data_i,
          ),
        ),
        EXE_LWR_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> mem_data_i,
            "b01".U -> Cat(reg2_i(31, 24), mem_data_i(31, 8)),
            "b10".U -> Cat(reg2_i(31, 16), mem_data_i(31, 16)),
            "b11".U -> Cat(reg2_i(31, 8), mem_data_i(31, 24)),
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

  // debug
  // printf(p"memory :pc 0x${Hexadecimal(pc)}\n")
}
