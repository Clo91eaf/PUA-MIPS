package cpu.puamips

import Const._
import chisel3._
import chisel3.util._
import UInt._

class Decoder extends Module {
  val io = IO(new Bundle {
    // 从各个流水线阶段传来的信号
    val  fromInstMemory    =  Flipped(new  InstMemory_Decoder())
    val  fromDecoderStage  =  Flipped(new  DecoderStage_Decoder())
    val  fromExecuteStage  =  Flipped(new  ExecuteStage_Decoder())
    val  fromExecute       =  Flipped(new  Execute_Decoder())
    val  fromRegfile       =  Flipped(new  RegFile_Decoder())
    val  fromMemory        =  Flipped(new  Memory_Decoder())
    val  fromControl       =  Flipped(new  Control_Decoder())

    val  fetch         =  new  Decoder_Fetch()
    val  executeStage  =  new  Decoder_ExecuteStage()
    val  regfile       =  new  Decoder_RegFile()
    val  control       =  new  Decoder_Control()
})                               
  // input
  val pc_i = Wire(INST_ADDR_BUS)
  val inst_i = Wire(INST_BUS)
  val aluop_i = Wire(ALU_OP_BUS)
  val wd_i = Wire(ADDR_BUS)
  val reg1_data_i = Wire(BUS)
  val reg2_data_i = Wire(BUS)
  val is_in_delayslot_i = Wire(Bool())
  pc_i := io.fromDecoderStage.pc
  inst_i := io.fromInstMemory.inst
  aluop_i := io.fromExecute.aluop
  wd_i := io.fromExecute.waddr
  wd_i := io.fromMemory.waddr
  reg1_data_i := io.fromRegfile.reg1_data
  reg2_data_i := io.fromRegfile.reg2_data
  is_in_delayslot_i := io.fromExecuteStage.is_in_delayslot

  // output
  io.executeStage.pc := pc_i
  val reg1_read = Wire(Bool())
  io.regfile.reg1_read := reg1_read
  val reg2_read = Wire(Bool())
  io.regfile.reg2_read := reg2_read
  val reg1_addr = Wire(ADDR_BUS)
  io.regfile.reg1_addr := reg1_addr
  val reg2_addr = Wire(ADDR_BUS)
  io.regfile.reg2_addr := reg2_addr
  val aluop = Wire(ALU_OP_BUS)
  io.executeStage.aluop := aluop
  val alusel = Wire(ALU_SEL_BUS)
  io.executeStage.alusel := alusel
  val reg1 = Wire(BUS)
  io.executeStage.reg1 := reg1
  val reg2 = Wire(BUS)
  io.executeStage.reg2 := reg2
  val waddr = Wire(ADDR_BUS)
  io.executeStage.waddr := waddr
  val wen = Wire(Bool())
  io.executeStage.wen := wen
  io.executeStage.inst := inst_i
  val next_inst_in_delayslot = Wire(Bool())
  io.executeStage.next_inst_in_delayslot := next_inst_in_delayslot
  val branch_flag = Wire(Bool())
  io.fetch.branch_flag := branch_flag
  val branch_target_address = Wire(BUS)
  io.fetch.branch_target_address := branch_target_address
  val link_addr = Wire(BUS)
  io.executeStage.link_addr := link_addr
  val is_in_delayslot = Wire(Bool())
  io.executeStage.is_in_delayslot := is_in_delayslot
  val stallreq = Wire(Bool())
  io.control.stallreq := stallreq
  val excepttype = Wire(UInt(32.W))
  io.executeStage.excepttype := excepttype
  val current_inst_addr = Wire(BUS)
  io.executeStage.current_inst_addr := current_inst_addr

  // flush时pc为0，此时不应该读到inst，将inst置为0
  when(io.fromControl.flush === true.B) {
    inst_i := ZERO_WORD
  }

  // 取得的指令码功能码
  val op = Wire(UInt(6.W))
  val op2 = Wire(UInt(5.W))
  val op3 = Wire(UInt(6.W))
  val op4 = Wire(UInt(5.W))
  op := inst_i(31, 26)
  op2 := inst_i(10, 6)
  op3 := inst_i(5, 0)
  op4 := inst_i(20, 16)

  val rt = Wire(UInt(5.W))
  val rd = Wire(UInt(5.W))
  val sa = Wire(UInt(5.W))
  val rs = Wire(UInt(5.W))
  val imm16 = Wire(UInt(16.W))

  rt := inst_i(20, 16)
  rd := inst_i(15, 11)
  sa := inst_i(10, 6)
  rs := inst_i(25, 21)
  imm16 := inst_i(15, 0)

  val imm = Wire(BUS)
  val instValid = Wire(Bool())
  val pc_plus_4 = Wire(BUS)
  val pc_plus_8 = Wire(BUS)
  val imm_sll2_signedext = Wire(BUS)
  val stallreq_for_reg1_loadrelate = Wire(Bool())
  val stallreq_for_reg2_loadrelate = Wire(Bool())
  val pre_inst_is_load = Wire(Bool())
  val excepttype_is_syscall = Wire(Bool())
  val excepttype_is_eret = Wire(Bool())

  pc_plus_4 := pc_i + 4.U
  pc_plus_8 := pc_i + 8.U
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

  current_inst_addr := pc_i;

  val BTarget = pc_plus_4 + imm_sll2_signedext
  val JTarget = Cat(pc_plus_4(31, 28), inst_i(25, 0), 0.U(2.W))

  // 对指令进行译码
  when(reset.asBool === RST_ENABLE) {
    aluop := EXE_NOP_OP
    alusel := EXE_RES_NOP
    waddr := rd // inst_i(15, 11)
    wen := WRITE_DISABLE
    instValid := INST_INVALID
    reg1_read := READ_DISABLE
    reg2_read := READ_DISABLE
    reg1_addr := rs // inst_i(25, 21)
    reg2_addr := rt // inst_i(20, 16)
    imm := ZERO_WORD
    link_addr := ZERO_WORD
    branch_target_address := ZERO_WORD
    branch_flag := NOT_BRANCH
    next_inst_in_delayslot := NOT_IN_DELAY_SLOT
    excepttype_is_syscall := false.B
    excepttype_is_eret := false.B
  }

  aluop := EXE_NOP_OP
  alusel := EXE_RES_NOP
  waddr := rd // inst_i(15, 11)
  wen := WRITE_DISABLE
  instValid := INST_INVALID
  reg1_read := READ_DISABLE
  reg2_read := READ_DISABLE
  reg1_addr := rs // inst_i(25, 21)
  reg2_addr := rt // inst_i(20, 16)
  imm := ZERO_WORD
  link_addr := ZERO_WORD
  branch_target_address := ZERO_WORD
  branch_flag := NOT_BRANCH
  next_inst_in_delayslot := NOT_IN_DELAY_SLOT
  excepttype_is_syscall := false.B
  excepttype_is_eret := false.B
  when(inst_i === SYSCALL) {
    excepttype_is_syscall := true.B
  }
  when(inst_i === ERET) {
    excepttype_is_eret := true.B
  }

  val signals: List[UInt] = ListLookup(
    inst_i,
  // @formatter:off
    List(INST_INVALID, READ_DISABLE  , READ_DISABLE  , EXE_RES_NOP, EXE_NOP_OP, WRITE_DISABLE, WRA_X, IMM_N),
    Array(         /*   instValid  | reg1_read     | reg2_read     | alusel       | aluop      | wen           | waddr     | immType */
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
  wraType := csWRType
  immType := csIMMType

  reg1_read := op1_type
  reg2_read := op2_type

  imm := MuxLookup(
    immType,
    Util.zeroExtend(sa), // default IMM_SHT
    Seq(
      IMM_LSE -> Util.signedExtend(imm16),
      IMM_LZE -> Util.zeroExtend(imm16),
      IMM_HZE -> Cat(imm16, Fill(16, 0.U))
    )
  )

  waddr := MuxLookup(
    wraType,
    "b11111".U(5.W), // 取"b11111", 即31号寄存器
    Seq(
      WRA_T1 -> rd, // 取inst(15,11)
      WRA_T2 -> rt // 取inst(20,16)
    )
  )

  aluop := csOpType
  alusel := csInstType
  wen := csWReg
  wen := MuxLookup(
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
  }.elsewhen(pre_inst_is_load && io.fromExecute.waddr === reg1_addr && reg1_read) {
    stallreq_for_reg1_loadrelate := STOP
    reg1 := ZERO_WORD // liphen
  }.elsewhen(
    reg1_read && io.fromExecute.wen && io.fromExecute.waddr === reg1_addr
  ) {
    reg1 := io.fromExecute.wdata
  }.elsewhen(
    reg1_read && io.fromMemory.wen && io.fromMemory.waddr === reg1_addr
  ) {
    reg1 := io.fromMemory.wdata
  }.elsewhen(reg1_read) {
    reg1 := reg1_data_i
  }.elsewhen(!reg1_read) {
    reg1 := imm
  }.otherwise {
    reg1 := ZERO_WORD
  }

  stallreq_for_reg2_loadrelate := NOT_STOP
  when(reset.asBool === RST_ENABLE) {
    reg2 := ZERO_WORD
  }.elsewhen(pre_inst_is_load && io.fromExecute.waddr === reg2_addr && reg2_read) {
    stallreq_for_reg2_loadrelate := STOP
    reg2 := ZERO_WORD // liphen
  }.elsewhen(
    (reg2_read) && (io.fromExecute.wen) && (io.fromExecute.waddr === reg2_addr)
  ) {
    reg2 := io.fromExecute.wdata
  }.elsewhen(
    (reg2_read) && (io.fromMemory.wen) && (io.fromMemory.waddr === reg2_addr)
  ) {
    reg2 := io.fromMemory.wdata
  }.elsewhen(reg2_read) {
    reg2 := reg2_data_i
  }.elsewhen(!reg2_read) {
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
  // printf(p"decoder :pc 0x${Hexadecimal(pc_i)}, inst 0x${Hexadecimal(inst_i)}\n")
}
