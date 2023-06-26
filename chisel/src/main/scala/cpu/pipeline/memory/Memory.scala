package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import os.read

class Memory extends Module {
  val io = IO(new Bundle {
    val fromMemoryStage    = Flipped(new MemoryStage_Memory())
    val fromDataMemory     = Flipped(new DataMemory_Memory())
    val fromWriteBackStage = Flipped(new WriteBackStage_Memory())

    val decoder        = new Memory_Decoder()
    val mov            = new Memory_Mov()
    val memoryStage    = new Memory_MemoryStage()
    val dataMemory     = new Memory_DataMemory()
    val execute        = new Memory_Execute()
    val writeBackStage = new Memory_WriteBackStage()
    val ctrl           = new Memory_Ctrl()
  })
  // input
  val aluop      = io.fromMemoryStage.aluop
  val pc         = io.fromMemoryStage.pc
  val reg2_i     = io.fromMemoryStage.reg2
  val ms_valid   = io.fromMemoryStage.valid
  val tlb_refill = io.fromMemoryStage.tlb_refill
  val s1_index   = io.fromMemoryStage.s1_index
  val s1_found   = io.fromMemoryStage.s1_found
  val after_tlb  = io.fromMemoryStage.after_tlb

  // output
  val reg_waddr       = Wire(ADDR_BUS)
  val reg_wen         = Wire(REG_WRITE_BUS)
  val reg_wdata       = Wire(BUS)
  val hi              = Wire(BUS)
  val lo              = Wire(BUS)
  val whilo           = Wire(Bool())
  val cp0_wen         = Wire(Bool())
  val cp0_waddr       = Wire(CP0_ADDR_BUS)
  val cp0_wdata       = Wire(BUS)
  val zero32          = Wire(BUS)
  val allowin         = Wire(Bool())
  val ms_to_ws_valid  = Wire(Bool())
  val inst_is_mfc0    = Wire(Bool())
  val inst_is_mtc0    = Wire(Bool())
  val inst_is_eret    = Wire(Bool())
  val inst_is_syscall = Wire(Bool())
  val ms_fwd_valid    = Wire(Bool())
  val ms_blk_valid    = Wire(Bool())
  val inst_is_tlbp    = Wire(Bool())
  val inst_is_tlbr    = Wire(Bool())
  val inst_is_tlbwi   = Wire(Bool())
  val ex              = Wire(Bool())

  // output-ctrl
  io.ctrl.ex := ex

  // output-decoder
  io.decoder.reg_waddr    := reg_waddr
  io.decoder.reg_wen      := reg_wen
  io.decoder.reg_wdata    := reg_wdata
  io.decoder.inst_is_mfc0 := inst_is_mfc0
  io.decoder.ms_fwd_valid := ms_fwd_valid
  io.decoder.blk_valid    := ms_blk_valid

  // output-execute
  val ms_data_buff       = RegInit(BUS_INIT)
  val ms_data_buff_valid = RegInit(false.B)
  io.execute.hi          := hi
  io.execute.lo          := lo
  io.execute.whilo       := whilo && ms_to_ws_valid
  io.execute.allowin     := allowin
  io.execute.inst_unable := !ms_valid || ms_data_buff_valid || io.fromMemoryStage.data_ok

  when(io.fromMemoryStage.do_flush) {
    ms_data_buff_valid := false.B
    ms_data_buff       := 0.U
  }.elsewhen(
    !ms_data_buff_valid && ms_valid && io.fromDataMemory.data_ok && !io.fromWriteBackStage.allowin,
  ) {
    ms_data_buff_valid := true.B
    ms_data_buff       := io.fromDataMemory.rdata
  }.elsewhen(io.fromWriteBackStage.allowin) {
    ms_data_buff_valid := false.B
    ms_data_buff       := 0.U
  }

  val data_ok =
    io.fromMemoryStage.data_ok || ms_data_buff_valid || (io.fromMemoryStage.wait_mem && io.fromDataMemory.data_ok)
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
  io.writeBackStage.valid           := ms_to_ws_valid
  io.writeBackStage.inst_is_mfc0    := inst_is_mfc0
  io.writeBackStage.inst_is_mtc0    := inst_is_mtc0
  io.writeBackStage.inst_is_eret    := inst_is_eret
  io.writeBackStage.inst_is_syscall := inst_is_syscall
  io.writeBackStage.cp0_addr        := io.fromMemoryStage.cp0_addr
  io.writeBackStage.excode          := io.fromMemoryStage.excode
  io.writeBackStage.badvaddr        := io.fromMemoryStage.badvaddr
  io.writeBackStage.ex              := ex
  io.writeBackStage.bd              := io.fromMemoryStage.bd
  io.writeBackStage.inst_is_tlbp    := inst_is_tlbp
  io.writeBackStage.inst_is_tlbr    := inst_is_tlbr
  io.writeBackStage.inst_is_tlbwi   := inst_is_tlbwi
  io.writeBackStage.tlb_refill      := tlb_refill
  io.writeBackStage.after_tlb       := after_tlb
  io.writeBackStage.s1_found        := s1_found
  io.writeBackStage.s1_index        := s1_index
  // output-execute
  io.mov.cp0_wen   := cp0_wen
  io.mov.cp0_waddr := cp0_waddr
  io.mov.cp0_wdata := cp0_wdata

  // input-memory stage

  /*-------------------------------io finish-------------------------------*/
  inst_is_mfc0    := ms_valid && (aluop === EXE_MFC0_OP)
  inst_is_mtc0    := ms_valid && (aluop === EXE_MTC0_OP)
  inst_is_eret    := ms_valid && (aluop === EXE_ERET_OP)
  inst_is_syscall := ms_valid && (aluop === EXE_SYSCALL_OP)
  inst_is_tlbp    := ms_valid && (aluop === EXE_TLBP_OP)
  inst_is_tlbr    := ms_valid && (aluop === EXE_TLBR_OP)
  inst_is_tlbwi   := ms_valid && (aluop === EXE_TLBWI_OP)

  val ready_go = Mux(io.fromMemoryStage.wait_mem, data_ok, true.B)
  allowin        := !ms_valid || ready_go && io.fromWriteBackStage.allowin
  ms_to_ws_valid := ms_valid && ready_go && !io.fromMemoryStage.do_flush

  ms_fwd_valid := ms_to_ws_valid // p195 ms_to_ws_valid
  ms_blk_valid := ms_valid && io.fromMemoryStage.res_from_mem && !ready_go && !io.fromMemoryStage.do_flush

  zero32 := 0.U(32.W)

  when(reset.asBool === RST_ENABLE) {
    reg_waddr := NOP_REG_ADDR
    reg_wen   := REG_WRITE_DISABLE
    reg_wdata := ZERO_WORD
    hi        := ZERO_WORD
    lo        := ZERO_WORD
    whilo     := WRITE_DISABLE
    cp0_wen   := WRITE_DISABLE
    cp0_waddr := 0.U
    cp0_wdata := ZERO_WORD
  }.otherwise {
    // input-memory stage
    reg_waddr := io.fromMemoryStage.reg_waddr
    hi        := io.fromMemoryStage.hi
    lo        := io.fromMemoryStage.lo
    whilo     := io.fromMemoryStage.whilo

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
            "b00".U -> Util.signedExtend(data(7, 0)),
            "b01".U -> Util.signedExtend(data(15, 8)),
            "b10".U -> Util.signedExtend(data(23, 16)),
            "b11".U -> Util.signedExtend(data(31, 24)),
          ),
        ),
        EXE_LBU_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.zeroExtend(data(7, 0)),
            "b01".U -> Util.zeroExtend(data(15, 8)),
            "b10".U -> Util.zeroExtend(data(23, 16)),
            "b11".U -> Util.zeroExtend(data(31, 24)),
          ),
        ),
        EXE_LH_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.signedExtend(data(15, 0)),
            "b10".U -> Util.signedExtend(data(31, 16)),
          ),
        ),
        EXE_LHU_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Util.zeroExtend(data(15, 0)),
            "b10".U -> Util.zeroExtend(data(31, 16)),
          ),
        ),
        EXE_LW_OP -> data,
        EXE_LWL_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> Cat(data(7, 0), reg2_i(23, 0)),
            "b01".U -> Cat(data(15, 0), reg2_i(15, 0)),
            "b10".U -> Cat(data(23, 0), reg2_i(7, 0)),
            "b11".U -> data,
          ),
        ),
        EXE_LWR_OP -> MuxLookup(
          addrLowBit2,
          ZERO_WORD,
          Seq(
            "b00".U -> data,
            "b01".U -> Cat(reg2_i(31, 24), data(31, 8)),
            "b10".U -> Cat(reg2_i(31, 16), data(31, 16)),
            "b11".U -> Cat(reg2_i(31, 8), data(31, 24)),
          ),
        ),
        EXE_LL_OP -> data,
      ),
    ) // reg_wdata
  }
  ex := ms_valid && io.fromMemoryStage.ex
}
