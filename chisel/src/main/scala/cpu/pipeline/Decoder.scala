package cpu.pipeline

import cpu.defines.Const._
import chisel3._
import chisel3.util._
import UInt._

class Decoder extends Module {
  val io = IO(new Bundle {
    // 从各个流水线阶段传来的信号
    val fromInstMemory   = Flipped(new InstMemory_Decoder())
    val fromDecoderStage = Flipped(new DecoderStage_Decoder())
    val fromExecuteStage = Flipped(new ExecuteStage_Decoder())
    val fromExecute      = Flipped(new Execute_Decoder())
    val fromRegfile      = Flipped(new RegFile_Decoder())
    val fromMemory       = Flipped(new Memory_Decoder())
    val fromControl      = Flipped(new Control_Decoder())

    val fetch        = new Decoder_Fetch()
    val executeStage = new Decoder_ExecuteStage()
    val regfile      = new Decoder_RegFile()
    val control      = new Decoder_Control()
  })
  // input
  val pc                = Wire(INST_ADDR_BUS)
  val inst              = Wire(INST_BUS)
  val aluop_i           = Wire(ALU_OP_BUS)
  val waddr_i           = Wire(ADDR_BUS) // TODO:未被使用
  val reg1_data         = Wire(BUS)
  val reg2_data         = Wire(BUS)
  val is_in_delayslot_i = Wire(Bool())

  // input-decoder stage
  pc := io.fromDecoderStage.pc

  // input-inst memory
  inst := io.fromInstMemory.inst

  // input-execute
  aluop_i := io.fromExecute.aluop
  waddr_i := io.fromExecute.reg_waddr // TODO:未被使用

  // input-memory
  waddr_i := io.fromMemory.reg_waddr // TODO:未被使用

  // input-regfile
  reg1_data := io.fromRegfile.reg1_data
  reg2_data := io.fromRegfile.reg2_data

  // input-execute stage
  is_in_delayslot_i := io.fromExecuteStage.is_in_delayslot

  // output
  val reg1_ren               = Wire(Bool())
  val reg2_ren               = Wire(Bool())
  val reg1_raddr             = Wire(ADDR_BUS)
  val reg2_raddr             = Wire(ADDR_BUS)
  val aluop                  = Wire(ALU_OP_BUS)
  val alusel                 = Wire(ALU_SEL_BUS)
  val reg1                   = Wire(BUS)
  val reg2                   = Wire(BUS)
  val reg_waddr              = Wire(ADDR_BUS)
  val reg_wen                = Wire(Bool())
  val next_inst_in_delayslot = Wire(Bool())
  val branch_flag            = Wire(Bool())
  val branch_target_address  = Wire(BUS)
  val link_addr              = Wire(BUS)
  val is_in_delayslot        = Wire(Bool())
  val stallreq               = Wire(Bool())
  val excepttype             = Wire(UInt(32.W))
  val current_inst_addr      = Wire(BUS)

  // output-execute stage
  io.executeStage.pc := pc

  // output-regfile
  io.regfile.reg1_ren   := reg1_ren
  io.regfile.reg2_ren   := reg2_ren
  io.regfile.reg1_raddr := reg1_raddr
  io.regfile.reg2_raddr := reg2_raddr

  // output-execute stage
  io.executeStage.aluop                  := aluop
  io.executeStage.alusel                 := alusel
  io.executeStage.reg1                   := reg1
  io.executeStage.reg2                   := reg2
  io.executeStage.reg_waddr              := reg_waddr
  io.executeStage.reg_wen                := reg_wen
  io.executeStage.inst                   := inst
  io.executeStage.next_inst_in_delayslot := next_inst_in_delayslot

  // output-fetch
  io.fetch.branch_flag           := branch_flag
  io.fetch.branch_target_address := branch_target_address

  // output-execute stage
  io.executeStage.link_addr       := link_addr
  io.executeStage.is_in_delayslot := is_in_delayslot

  // output-control
  io.control.stallreq := stallreq

  // output-execute stage
  io.executeStage.excepttype        := excepttype
  io.executeStage.current_inst_addr := current_inst_addr

  // io-finish

  // flush时pc为0，此时不应该读到inst，将inst置为0
  when(io.fromControl.flush === true.B) {
    inst := ZERO_WORD
  }

  // 取得的指令码功能码
  val op  = Wire(UInt(6.W))
  val op2 = Wire(UInt(5.W))
  val op3 = Wire(UInt(6.W))
  val op4 = Wire(UInt(5.W))
  op  := inst(31, 26)
  op2 := inst(10, 6)
  op3 := inst(5, 0)
  op4 := inst(20, 16)

  val rt    = Wire(UInt(5.W))
  val rd    = Wire(UInt(5.W))
  val sa    = Wire(UInt(5.W))
  val rs    = Wire(UInt(5.W))
  val imm16 = Wire(UInt(16.W))

  rt    := inst(20, 16)
  rd    := inst(15, 11)
  sa    := inst(10, 6)
  rs    := inst(25, 21)
  imm16 := inst(15, 0)

  val imm                          = Wire(BUS)
  val instValid                    = Wire(Bool())
  val pc_plus_4                    = Wire(BUS)
  val pc_plus_8                    = Wire(BUS)
  val imm_sll2_signedext           = Wire(BUS)
  val stallreq_for_reg1_loadrelate = Wire(Bool())
  val stallreq_for_reg2_loadrelate = Wire(Bool())
  val pre_inst_is_load             = Wire(Bool())
  val excepttype_is_syscall        = Wire(Bool())
  val excepttype_is_eret           = Wire(Bool())

  pc_plus_4          := pc + 4.U
  pc_plus_8          := pc + 8.U
  imm_sll2_signedext := Cat(Util.signedExtend(imm16, to = 30), 0.U(2.W))
  stallreq := stallreq_for_reg1_loadrelate || stallreq_for_reg2_loadrelate
  when(
    aluop_i === EXE_LB_OP || aluop_i === EXE_LBU_OP ||
      aluop_i === EXE_LH_OP || aluop_i === EXE_LHU_OP ||
      aluop_i === EXE_LW_OP || aluop_i === EXE_LWR_OP ||
      aluop_i === EXE_LWL_OP || aluop_i === EXE_LL_OP ||
      aluop_i === EXE_SC_OP
  ) {
    pre_inst_is_load := true.B
  }.otherwise {
    pre_inst_is_load := false.B
  }

  // exceptiontype的低8bit留给外部中断，第9bit表示是否是syscall指令
  // 第10bit表示是否是无效指令，第11bit表示是否是trap指令
  excepttype := Cat(
    "b0".U(19.W),
    excepttype_is_eret,
    "b0".U(2.W),
    instValid,
    excepttype_is_syscall,
    "b0".U(8.W)
  )

  current_inst_addr := pc;

  val BTarget = pc_plus_4 + imm_sll2_signedext
  val JTarget = Cat(pc_plus_4(31, 28), inst(25, 0), 0.U(2.W))

  // 对指令进行译码
  when(reset.asBool === RST_ENABLE) {
    aluop                  := EXE_NOP_OP
    alusel                 := EXE_RES_NOP
    reg_waddr              := rd // inst(15, 11)
    reg_wen                := WRITE_DISABLE
    instValid              := INST_INVALID
    reg1_ren               := READ_DISABLE
    reg2_ren               := READ_DISABLE
    reg1_raddr             := rs // inst(25, 21)
    reg2_raddr             := rt // inst(20, 16)
    imm                    := ZERO_WORD
    link_addr              := ZERO_WORD
    branch_target_address  := ZERO_WORD
    branch_flag            := NOT_BRANCH
    next_inst_in_delayslot := NOT_IN_DELAY_SLOT
    excepttype_is_syscall  := false.B
    excepttype_is_eret     := false.B
  }

  aluop                  := EXE_NOP_OP
  alusel                 := EXE_RES_NOP
  reg_waddr              := rd // inst(15, 11)
  reg_wen                := WRITE_DISABLE
  instValid              := INST_INVALID
  reg1_ren               := READ_DISABLE
  reg2_ren               := READ_DISABLE
  reg1_raddr             := rs // inst(25, 21)
  reg2_raddr             := rt // inst(20, 16)
  imm                    := ZERO_WORD
  link_addr              := ZERO_WORD
  branch_target_address  := ZERO_WORD
  branch_flag            := NOT_BRANCH
  next_inst_in_delayslot := NOT_IN_DELAY_SLOT
  excepttype_is_syscall  := false.B
  excepttype_is_eret     := false.B
  when(inst === SYSCALL) {
    excepttype_is_syscall := true.B
  }
  when(inst === ERET) {
    excepttype_is_eret := true.B
  }

  val signals: List[UInt] = ListLookup(
    inst,
  // @formatter:off
    List(INST_INVALID, READ_DISABLE  , READ_DISABLE  , EXE_RES_NOP, EXE_NOP_OP, WRITE_DISABLE, WRA_X, IMM_N),
    Array(         /*   instValid  | reg1_ren     | reg2_ren     | alusel       | aluop      | reg_wen           | reg_waddr     | immType */
      // NOP
      NOP       -> List(INST_VALID , READ_DISABLE  , READ_DISABLE  , EXE_RES_NOP  , EXE_NOP_OP , WRITE_DISABLE  , WRA_X  , IMM_N),
      // 位操作
      OR        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_OR_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      AND       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_AND_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      XOR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_XOR_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      NOR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_NOR_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // 移位
      SLLV      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_SHIFT, EXE_SLL_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SRLV      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRL_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SRAV      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRA_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SLL       -> List(INST_VALID , READ_DISABLE  , READ_ENABLE   , EXE_RES_SHIFT, EXE_SLL_OP , WRITE_ENABLE   , WRA_T1 , IMM_SHT),
      SRL       -> List(INST_VALID , READ_DISABLE  , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRL_OP , WRITE_ENABLE   , WRA_T1 , IMM_SHT),
      SRA       -> List(INST_VALID , READ_DISABLE  , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRA_OP , WRITE_ENABLE   , WRA_T1 , IMM_SHT),
      // 立即数
      ORI       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_OR_OP  , WRITE_ENABLE   , WRA_T2 , IMM_LZE),
      ANDI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_AND_OP , WRITE_ENABLE   , WRA_T2 , IMM_LZE),
      XORI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_XOR_OP , WRITE_ENABLE   , WRA_T2 , IMM_LZE),
      LUI       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_OR_OP  , WRITE_ENABLE   , WRA_T2 , IMM_HZE),

      // Move
      MOVN      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MOVE , EXE_MOVN_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MOVZ      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MOVE , EXE_MOVZ_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),

      // HI，LO的Move指令
      MFHI      -> List(INST_VALID , READ_DISABLE  , READ_DISABLE    , EXE_RES_MOVE , EXE_MFHI_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MFLO      -> List(INST_VALID , READ_DISABLE  , READ_DISABLE    , EXE_RES_MOVE , EXE_MFLO_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MTHI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_NOP , EXE_MTHI_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MTLO      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_NOP , EXE_MTLO_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      
      // C0的Move指令
      MFC0      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_MOVE , EXE_MFC0_OP , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      MTC0      -> List(INST_VALID , READ_DISABLE    , READ_ENABLE     , EXE_RES_NOP  , EXE_MTC0_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // 比较指令
      SLT       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_ARITHMETIC, EXE_SLT_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SLTU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_ARITHMETIC, EXE_SLTU_OP, WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // 立即数
      SLTI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_ARITHMETIC, EXE_SLT_OP , WRITE_ENABLE   , WRA_T2 , IMM_LSE),
      SLTIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_ARITHMETIC, EXE_SLTU_OP, WRITE_ENABLE   , WRA_T2 , IMM_LSE),

      // Trap
      TEQ       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP, EXE_TEQ_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      TEQI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP, EXE_TEQ_OP, WRITE_DISABLE  , WRA_X  , IMM_LSE),
      TGE       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP, EXE_TGE_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      TGEI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP, EXE_TGE_OP, WRITE_DISABLE  , WRA_X  , IMM_LSE),
      TGEIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP, EXE_TGEU_OP, WRITE_DISABLE , WRA_X  , IMM_LSE),
      TGEU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP, EXE_TGEU_OP, WRITE_DISABLE , WRA_X  , IMM_N  ),
      TLT       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP, EXE_TLT_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      TLTI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP, EXE_TLT_OP, WRITE_DISABLE  , WRA_X  , IMM_LSE),
      TLTIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP, EXE_TLTU_OP, WRITE_DISABLE , WRA_X  , IMM_LSE),
      TLTU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP, EXE_TLTU_OP, WRITE_DISABLE , WRA_X  , IMM_N  ),
      TNE       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP, EXE_TNE_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      TNEI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP, EXE_TNE_OP, WRITE_DISABLE  , WRA_X  , IMM_LSE),

      // 算术指令
      ADD       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_ARITHMETIC, EXE_ADD_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      ADDU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_ARITHMETIC, EXE_ADDU_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SUB       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_ARITHMETIC, EXE_SUB_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SUBU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_ARITHMETIC, EXE_SUBU_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MUL       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL       , EXE_MUL_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MULT      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP       , EXE_MULT_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MULTU     -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP       , EXE_MULTU_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MADD      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL       , EXE_MADD_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MADDU     -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL       , EXE_MADDU_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MSUB      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL       , EXE_MSUB_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MSUBU     -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL       , EXE_MSUBU_OP, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      DIV       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP       , EXE_DIV_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      DIVU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP       , EXE_DIVU_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      CLO       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_ARITHMETIC, EXE_CLO_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      CLZ       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_ARITHMETIC, EXE_CLZ_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // 立即数
      ADDI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_ARITHMETIC, EXE_ADDI_OP  , WRITE_ENABLE   , WRA_T2 , IMM_LSE),
      ADDIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_ARITHMETIC, EXE_ADDIU_OP , WRITE_ENABLE   , WRA_T2 , IMM_LSE),
      // 跳转指令
      J         -> List(INST_VALID , READ_DISABLE  , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_J_OP     , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      JAL       -> List(INST_VALID , READ_DISABLE  , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_JAL_OP   , WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      JR        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_JR_OP    , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      JALR      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_JALR_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      BEQ       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_JUMP_BRANCH , EXE_BEQ_OP   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      BNE       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_JUMP_BRANCH , EXE_BNE_OP   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      BGTZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_BGTZ_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      BLEZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_BLEZ_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      BGEZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_BGEZ_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      BGEZAL    -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_BGEZAL_OP, WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      BLTZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_BLTZ_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      BLTZAL    -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_BLTZAL_OP, WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // BEQL      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_JUMP_BRANCH , EXE_EQ   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BNEL      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_JUMP_BRANCH , EXE_NE   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGTZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_GTZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLEZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_LEZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_GEZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZALL   -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_GEZAL, WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // BLTZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_LTZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLTZALL   -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_LTZAL, WRITE_ENABLE   , WRA_T3 , IMM_N  ),

      // // TLB
      // TLBP      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_P   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBR      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_R   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBWI     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_WI  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBWR     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_WR  , WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // 例外指令
      SYSCALL   -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXE_SYSCALL_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BREAK     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXC_BR  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      ERET      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXE_ERET_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // WAIT      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXC_WAIT, WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // 访存指令
      LB        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LB_OP  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LBU       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LBU_OP , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LH        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LH_OP  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LHU       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LHU_OP , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LW        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LW_OP  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      SB        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SB_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      SH        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SH_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      SW        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SW_OP  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      LWL       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_LWL_OP , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LWR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_LWR_OP , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      SWL       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SWL_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      SWR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SWR_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      LL        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LL_OP  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      SC        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SC_OP  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),


      SYNC      -> List(INST_VALID , READ_DISABLE    , READ_ENABLE     , EXE_RES_NOP  , EXE_SRL_OP    , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      PREF      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP  , EXE_NOP_OP    , WRITE_ENABLE    , WRA_X  , IMM_N  ),
      PREFX     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP  , EXE_NOP_OP    , WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // // Cache
      // CACHE     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_CAC , WRITE_DISABLE  , WRA_X  , IMM_N  ),
    )
  )
  // @formatter:on

  val (csInstValid: Bool) :: (op1_type: Bool) :: (op2_type: Bool) :: csInstType :: cs0 =
    signals
  val csOpType :: (csWReg: Bool) :: csWRType :: csIMMType :: Nil = cs0

  val wraType = Wire(UInt(2.W))
  val immType = Wire(UInt(3.W))

  instValid := csInstValid
  wraType   := csWRType
  immType   := csIMMType

  reg1_ren := op1_type
  reg2_ren := op2_type

  imm := MuxLookup(
    immType,
    Util.zeroExtend(sa), // default IMM_SHT
    Seq(
      IMM_LSE -> Util.signedExtend(imm16),
      IMM_LZE -> Util.zeroExtend(imm16),
      IMM_HZE -> Cat(imm16, Fill(16, 0.U))
    )
  )

  reg_waddr := MuxLookup(
    wraType,
    "b11111".U(5.W), // 取"b11111", 即31号寄存器
    Seq(
      WRA_T1 -> rd, // 取inst(15,11)
      WRA_T2 -> rt  // 取inst(20,16)
    )
  )

  aluop   := csOpType
  alusel  := csInstType
  reg_wen := csWReg
  reg_wen := MuxLookup(
    aluop,
    csWReg, // wreg默认为查找表中的结果
    Seq(
      EXE_MOVN_OP -> Mux(reg2 =/= ZERO_WORD, WRITE_ENABLE, WRITE_DISABLE),
      EXE_MOVZ_OP -> Mux(reg2 === ZERO_WORD, WRITE_ENABLE, WRITE_DISABLE)
    )
  )

  link_addr := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      // @formatter:off
      EXE_JR_OP     -> ZERO_WORD,
      EXE_JALR_OP   -> pc_plus_8,
      EXE_J_OP      -> ZERO_WORD,
      EXE_JAL_OP    -> pc_plus_8,
      EXE_BGEZAL_OP -> pc_plus_8,
      EXE_BLTZAL_OP -> pc_plus_8
      // @formatter:on
    )
  )

  branch_flag := MuxLookup(
    aluop,
    NOT_BRANCH,
    Seq(
      // @formatter:off
      EXE_JR_OP     -> BRANCH,
      EXE_JALR_OP   -> BRANCH,
      EXE_J_OP      -> BRANCH,
      EXE_JAL_OP    -> BRANCH,
      EXE_BEQ_OP    -> (reg1 === reg2),
      EXE_BNE_OP    -> (reg1 =/= reg2),
      EXE_BGTZ_OP   -> (!reg1(31) && (reg1 =/= 0.U)),
      EXE_BGEZ_OP   -> (!reg1(31)),
      EXE_BGEZAL_OP -> (!reg1(31)),
      EXE_BLTZ_OP   -> reg1(31),
      EXE_BLTZAL_OP -> reg1(31),
      EXE_BLEZ_OP   -> (!(!reg1(31) && (reg1 =/= 0.U)))
      // @formatter:on
    )
  )

  branch_target_address := MuxLookup(
    aluop,
    BTarget,
    Seq(
      // @formatter:off
      EXE_JR_OP   -> reg1,
      EXE_JALR_OP -> reg1,
      EXE_J_OP    -> JTarget,
      EXE_JAL_OP  -> JTarget
      // @formatter:on
    )
  )

  next_inst_in_delayslot := MuxLookup(
    aluop,
    NOT_IN_DELAY_SLOT,
    Seq(
      // @formatter:off
      EXE_JR_OP     -> IN_DELAY_SLOT,
      EXE_JALR_OP   -> IN_DELAY_SLOT,
      EXE_J_OP      -> IN_DELAY_SLOT,
      EXE_JAL_OP    -> IN_DELAY_SLOT,
      EXE_BEQ_OP    -> (reg1 === reg2),
      EXE_BNE_OP    -> (reg1 =/= reg2),
      EXE_BGTZ_OP   -> (!reg1(31) && (reg1 =/= 0.U)),
      EXE_BGEZ_OP   -> (!reg1(31)),
      EXE_BGEZAL_OP -> (!reg1(31)),
      EXE_BLTZ_OP   -> reg1(31),
      EXE_BLTZAL_OP -> reg1(31),
      EXE_BLEZ_OP   -> (!(!reg1(31) && (reg1 =/= 0.U)))
      // @formatter:on
    )
  )

  stallreq_for_reg1_loadrelate := NOT_STOP
  when(reset.asBool === RST_ENABLE) {
    reg1 := ZERO_WORD
  }.elsewhen(
    pre_inst_is_load && io.fromExecute.reg_waddr === reg1_raddr && reg1_ren
  ) {
    stallreq_for_reg1_loadrelate := STOP
    reg1                         := ZERO_WORD // liphen
  }.elsewhen(
    reg1_ren && io.fromExecute.reg_wen && io.fromExecute.reg_waddr === reg1_raddr
  ) {

    // input-execute
    reg1 := io.fromExecute.reg_wdata
  }.elsewhen(
    reg1_ren && io.fromMemory.reg_wen && io.fromMemory.reg_waddr === reg1_raddr
  ) {

    // input-memory
    reg1 := io.fromMemory.reg_wdata
  }.elsewhen(reg1_ren) {
    reg1 := reg1_data
  }.elsewhen(!reg1_ren) {
    reg1 := imm
  }.otherwise {
    reg1 := ZERO_WORD
  }

  stallreq_for_reg2_loadrelate := NOT_STOP
  when(reset.asBool === RST_ENABLE) {
    reg2 := ZERO_WORD
  }.elsewhen(
    pre_inst_is_load && io.fromExecute.reg_waddr === reg2_raddr && reg2_ren
  ) {
    stallreq_for_reg2_loadrelate := STOP
    reg2                         := ZERO_WORD // liphen
  }.elsewhen(
    (reg2_ren) && (io.fromExecute.reg_wen) && (io.fromExecute.reg_waddr === reg2_raddr)
  ) {

    // input-execute
    reg2 := io.fromExecute.reg_wdata
  }.elsewhen(
    (reg2_ren) && (io.fromMemory.reg_wen) && (io.fromMemory.reg_waddr === reg2_raddr)
  ) {

    // input-memory
    reg2 := io.fromMemory.reg_wdata
  }.elsewhen(reg2_ren) {
    reg2 := reg2_data
  }.elsewhen(!reg2_ren) {
    reg2 := imm
  }.otherwise {
    reg2 := ZERO_WORD
  }

  when(reset.asBool === RST_ENABLE) {
    is_in_delayslot := NOT_IN_DELAY_SLOT
  }.otherwise {
    is_in_delayslot := is_in_delayslot_i
  }

  // debug
  // printf(p"decoder :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
