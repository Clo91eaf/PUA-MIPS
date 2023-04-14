package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class Decoder extends Module {
  val io = IO(new Bundle {
    // 从各个流水线阶段传来的信号
    val fromDecoderStage   = Flipped(new DecoderStage_Decoder())
    val fromExecuteStage   = Flipped(new ExecuteStage_Decoder())
    val fromExecute        = Flipped(new Execute_Decoder())
    val fromRegfile        = Flipped(new RegFile_Decoder())
    val fromMemory         = Flipped(new Memory_Decoder())
    val fromWriteBackStage = Flipped(new WriteBackStage_Decoder())

    val decoderStage = new Decoder_DecoderStage()
    val fetchStage   = new Decoder_FetchStage()
    val executeStage = new Decoder_ExecuteStage()
    val regfile      = new Decoder_RegFile()
  })
  // input
  val pc                = Wire(INST_ADDR_BUS)
  val inst              = Wire(INST_BUS)
  val aluop_i           = Wire(ALU_OP_BUS)
  val reg1_data         = Wire(BUS)
  val reg2_data         = Wire(BUS)
  val is_in_delayslot_i = Wire(Bool())
  val ds_valid          = Wire(Bool())

  // input-decoder stage
  pc       := io.fromDecoderStage.pc
  inst     := io.fromDecoderStage.inst
  ds_valid := io.fromDecoderStage.valid

  // input-execute
  aluop_i := io.fromExecute.aluop

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
  val reg_wen                = Wire(REG_WRITE_BUS)
  val next_inst_in_delayslot = Wire(Bool())
  val branch_flag            = Wire(Bool())
  val branch_target_address  = Wire(BUS)
  val link_addr              = Wire(BUS)
  val is_in_delayslot        = Wire(Bool())
  val except_type            = Wire(UInt(32.W))
  val current_inst_addr      = Wire(BUS)
  val allowin                = Wire(Bool())
  val is_branch              = Wire(Bool())
  val valid                  = Wire(Bool())

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

  // output-fetchStage
  io.fetchStage.branch_stall          := false.B
  io.fetchStage.branch_flag           := branch_flag
  io.fetchStage.branch_target_address := branch_target_address
  io.fetchStage.allowin               := allowin
  io.fetchStage.is_branch             := is_branch

  // output-decoderStage
  io.decoderStage.allowin := allowin

  // output-execute stage
  io.executeStage.link_addr       := link_addr
  io.executeStage.is_in_delayslot := is_in_delayslot
  io.executeStage.valid           := valid

  // output-execute stage
  io.executeStage.except_type       := except_type
  io.executeStage.current_inst_addr := current_inst_addr

  // io-finish

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

  val imm                    = Wire(BUS)
  val inst_valid             = Wire(Bool())
  val pc_plus_4              = Wire(BUS)
  val pc_plus_8              = Wire(BUS)
  val imm_sll2_signedext     = Wire(BUS)
  val except_type_is_syscall = Wire(Bool())
  val except_type_is_eret    = Wire(Bool())

  // TODO
  val ready_go   = Wire(Bool())
  val mfc0_block = Wire(Bool())
  mfc0_block := (io.fromExecute.inst_is_mfc0 && (io.fromExecute.reg_waddr === rs || io.fromExecute.reg_waddr === rt)) ||
    (io.fromMemory.inst_is_mfc0 && (io.fromMemory.reg_waddr === rs || io.fromMemory.reg_waddr === rt)) ||
    (io.fromWriteBackStage.inst_is_mfc0 && (io.fromWriteBackStage.reg_waddr === rs || io.fromWriteBackStage.reg_waddr === rt))
  ready_go := !(mfc0_block || (io.fromExecute.blk_valid && (io.fromExecute.reg_waddr === rs || io.fromExecute.reg_waddr === rt)))
  allowin := !ds_valid || ready_go && io.fromExecute.allowin
  valid := ds_valid && ready_go && !io.fromWriteBackStage.eret && !io.fromWriteBackStage.ex

  pc_plus_4          := pc + 4.U
  pc_plus_8          := pc + 8.U
  imm_sll2_signedext := Cat(Util.signedExtend(imm16, to = 30), 0.U(2.W))

  // exceptiontype的低8bit留给外部中断，第9bit表示是否是syscall指令
  // 第10bit表示是否是无效指令，第11bit表示是否是trap指令
  except_type := Cat(
    "b0".U(19.W),
    except_type_is_eret,
    "b0".U(2.W),
    inst_valid,
    except_type_is_syscall,
    "b0".U(8.W),
  )

  current_inst_addr := pc;

  val BTarget = pc_plus_4 + imm_sll2_signedext
  val JTarget = Cat(pc_plus_4(31, 28), inst(25, 0), 0.U(2.W))

  // 对指令进行译码

  aluop                  := EXE_NOP_OP
  alusel                 := EXE_RES_NOP
  reg_waddr              := rd // inst(15, 11)
  reg_wen                := REG_WRITE_DISABLE
  inst_valid             := INST_INVALID
  reg1_ren               := READ_DISABLE
  reg2_ren               := READ_DISABLE
  reg1_raddr             := rs // inst(25, 21)
  reg2_raddr             := rt // inst(20, 16)
  imm                    := ZERO_WORD
  link_addr              := ZERO_WORD
  next_inst_in_delayslot := NOT_IN_DELAY_SLOT
  except_type_is_syscall := false.B
  except_type_is_eret    := false.B
  when(inst === SYSCALL) {
    except_type_is_syscall := true.B
  }
  when(inst === ERET) {
    except_type_is_eret := true.B
  }

  val signals: List[UInt] = ListLookup(
    inst,
  // @formatter:off
                   List(INST_INVALID, READ_DISABLE  , READ_DISABLE  , EXE_RES_NOP    , EXE_NOP_OP , REG_WRITE_DISABLE , WRA_X  , IMM_N),
    Array(         /*   inst_valid  | reg1_ren      | reg2_ren      | alusel         | aluop      | reg_wen           | reg_waddr | immType */
      // NOP
      NOP       -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE  , EXE_RES_NOP    , EXE_NOP_OP , REG_WRITE_DISABLE , WRA_X  , IMM_N  ),
      // 位操作    
      OR        -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_OR_OP  , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      AND       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_AND_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      XOR       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_XOR_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      NOR       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_NOR_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      // 移位    
      SLLV      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_SLL_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      SRLV      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_SRL_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      SRAV      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU    , EXE_SRA_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_N  ),
      SLL       -> List(INST_VALID  , READ_DISABLE  , READ_ENABLE   , EXE_RES_ALU    , EXE_SLL_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_SHT),
      SRL       -> List(INST_VALID  , READ_DISABLE  , READ_ENABLE   , EXE_RES_ALU    , EXE_SRL_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_SHT),
      SRA       -> List(INST_VALID  , READ_DISABLE  , READ_ENABLE   , EXE_RES_ALU    , EXE_SRA_OP , REG_WRITE_ENABLE  , WRA_T1 , IMM_SHT),
      // 立即数    
      ORI       -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU    , EXE_OR_OP  , REG_WRITE_ENABLE  , WRA_T2 , IMM_LZE),
      ANDI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU    , EXE_AND_OP , REG_WRITE_ENABLE  , WRA_T2 , IMM_LZE),
      XORI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU    , EXE_XOR_OP , REG_WRITE_ENABLE  , WRA_T2 , IMM_LZE),
      LUI       -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU    , EXE_OR_OP  , REG_WRITE_ENABLE  , WRA_T2 , IMM_HZE),

      // Move  
      MOVN      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MOV   , EXE_MOVN_OP , REG_WRITE_ENABLE , WRA_T1  , IMM_N  ),
      MOVZ      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MOV   , EXE_MOVZ_OP , REG_WRITE_ENABLE , WRA_T1  , IMM_N  ),

      // HI，LO的Move指令 
      MFHI      -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE  , EXE_RES_MOV   , EXE_MFHI_OP , REG_WRITE_ENABLE , WRA_T1  , IMM_N  ),
      MFLO      -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE  , EXE_RES_MOV   , EXE_MFLO_OP , REG_WRITE_ENABLE , WRA_T1  , IMM_N  ),
      MTHI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_MTHI_OP , REG_WRITE_DISABLE, WRA_X   , IMM_N  ),
      MTLO      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_MTLO_OP , REG_WRITE_DISABLE, WRA_X   , IMM_N  ),
      
      // C0的Move指令 
      MFC0      -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE  , EXE_RES_MOV   , EXE_MFC0_OP , REG_WRITE_ENABLE , WRA_T2    , IMM_N  ),
      MTC0      -> List(INST_VALID  , READ_DISABLE  , READ_ENABLE   , EXE_RES_NOP   , EXE_MTC0_OP , REG_WRITE_DISABLE, WRA_X     , IMM_N  ),

      // 比较指令   
      SLT       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU   , EXE_SLT_OP  , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      SLTU      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU   , EXE_SLTU_OP , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      // 立即数   
      SLTI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU   , EXE_SLT_OP  , REG_WRITE_ENABLE   , WRA_T2    , IMM_LSE),
      SLTIU     -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU   , EXE_SLTU_OP , REG_WRITE_ENABLE   , WRA_T2    , IMM_LSE),

      // Trap  
      TEQ       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_TEQ_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      TEQI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_TEQ_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_LSE),
      TGE       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_TGE_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      TGEI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_TGE_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_LSE),
      TGEIU     -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_TGEU_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_LSE),
      TGEU      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_TGEU_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      TLT       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_TLT_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      TLTI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_TLT_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_LSE),
      TLTU      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_TLTU_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      TLTIU     -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_TLTU_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_LSE),
      TNE       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_TNE_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      TNEI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_NOP   , EXE_TNE_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_LSE),

      // 算术指令 
      ADD       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU   , EXE_ADD_OP  , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      ADDU      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU   , EXE_ADDU_OP , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      SUB       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU   , EXE_SUB_OP  , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      SUBU      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_ALU   , EXE_SUBU_OP , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      MUL       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL   , EXE_MUL_OP  , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      MULT      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_MULT_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      MULTU     -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_MULTU_OP, REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      MADD      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL   , EXE_MADD_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      MADDU     -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL   , EXE_MADDU_OP, REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      MSUB      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL   , EXE_MSUB_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      MSUBU     -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_MUL   , EXE_MSUBU_OP, REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      DIV       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_DIV_OP  , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      DIVU      -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_NOP   , EXE_DIVU_OP , REG_WRITE_DISABLE  , WRA_X     , IMM_N  ),
      CLO       -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU   , EXE_CLO_OP  , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      CLZ       -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU   , EXE_CLZ_OP  , REG_WRITE_ENABLE   , WRA_T1    , IMM_N  ),
      // 立即数 
      ADDI      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU, EXE_ADD_OP  , REG_WRITE_ENABLE  , WRA_T2 , IMM_LSE),
      ADDIU     -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_ALU, EXE_ADDU_OP , REG_WRITE_ENABLE  , WRA_T2 , IMM_LSE),
      // 跳转指令 
      J         -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_J_OP     , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      JAL       -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_JAL_OP   , REG_WRITE_ENABLE , WRA_T3, IMM_N ),
      JR        -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_JR_OP    , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      JALR      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_JALR_OP  , REG_WRITE_ENABLE , WRA_T1, IMM_N ),
      BEQ       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_JUMP_BRANCH , EXE_BEQ_OP   , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      BNE       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE   , EXE_RES_JUMP_BRANCH , EXE_BNE_OP   , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      BGTZ      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_BGTZ_OP  , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      BLEZ      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_BLEZ_OP  , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      BGEZ      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_BGEZ_OP  , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      BGEZAL    -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_BGEZAL_OP, REG_WRITE_ENABLE , WRA_T3, IMM_N ),
      BLTZ      -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_BLTZ_OP  , REG_WRITE_DISABLE, WRA_X , IMM_N ),
      BLTZAL    -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE  , EXE_RES_JUMP_BRANCH , EXE_BLTZAL_OP, REG_WRITE_ENABLE , WRA_T3, IMM_N ),
      // BEQL      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_JUMP_BRANCH , EXE_EQ   , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BNEL      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_JUMP_BRANCH , EXE_NE   , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGTZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_GTZ  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLEZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_LEZ  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_GEZ  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZALL   -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_GEZAL, REG_WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // BLTZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_LTZ  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLTZALL   -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_JUMP_BRANCH , EXE_LTZAL, REG_WRITE_ENABLE   , WRA_T3 , IMM_N  ),

      // // TLB
      // TLBP      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_P   , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBR      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_R   , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBWI     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_WI  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBWR     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_WR  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // 例外指令
      SYSCALL   -> List(INST_VALID  , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXE_SYSCALL_OP  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BREAK     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXC_BR  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      ERET      -> List(INST_VALID  , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXE_ERET_OP  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // WAIT      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP, EXC_WAIT, REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // 访存指令
      LB        -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LB_OP  , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LBU       -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LBU_OP , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LH        -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LH_OP  , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LHU       -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LHU_OP , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LW        -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LW_OP  , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      SB        -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SB_OP  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      SH        -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SH_OP  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      SW        -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SW_OP  , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      LWL       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_LWL_OP , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      LWR       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_LWR_OP , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      SWL       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SWL_OP , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      SWR       -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SWR_OP , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      LL        -> List(INST_VALID  , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_LL_OP  , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      SC        -> List(INST_VALID  , READ_ENABLE   , READ_ENABLE     , EXE_RES_LOAD_STORE, EXE_SC_OP  , REG_WRITE_ENABLE   , WRA_T2 , IMM_N  ),


      SYNC      -> List(INST_VALID  , READ_DISABLE  , READ_ENABLE     , EXE_RES_NOP       , EXE_NOP_OP , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
      PREF      -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE    , EXE_RES_NOP       , EXE_NOP_OP , REG_WRITE_ENABLE   , WRA_X  , IMM_N  ),
      PREFX     -> List(INST_VALID  , READ_DISABLE  , READ_DISABLE    , EXE_RES_NOP       , EXE_NOP_OP , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // // Cache
      // CACHE     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOAD_STORE, EXE_CAC , REG_WRITE_DISABLE  , WRA_X  , IMM_N  ),
    )
  )
  // @formatter:on

  val (csinst_valid: Bool) :: (op1_type: Bool) :: (op2_type: Bool) :: csInstType :: csOpType :: csWReg :: wraType :: immType :: Nil =
    signals

  inst_valid := csinst_valid
  reg1_ren   := op1_type
  reg2_ren   := op2_type

  imm := MuxLookup(
    immType,
    Util.zeroExtend(sa), // default IMM_SHT
    Seq(
      IMM_LSE -> Util.signedExtend(imm16),
      IMM_LZE -> Util.zeroExtend(imm16),
      IMM_HZE -> Cat(imm16, Fill(16, 0.U)),
    ),
  )

  reg_waddr := MuxLookup(
    wraType,
    "b11111".U(5.W), // 取"b11111", 即31号寄存器
    Seq(
      WRA_T1 -> rd, // 取inst(15,11)
      WRA_T2 -> rt, // 取inst(20,16)
    ),
  )

  aluop  := csOpType
  alusel := csInstType
  reg_wen := MuxLookup(
    aluop,
    csWReg, // wreg默认为查找表中的结果
    Seq(
      EXE_MOVN_OP -> Fill(
        4,
        Mux(reg2 =/= ZERO_WORD, WRITE_ENABLE, REG_WRITE_DISABLE),
      ),
      EXE_MOVZ_OP -> Fill(
        4,
        Mux(reg2 === ZERO_WORD, WRITE_ENABLE, REG_WRITE_DISABLE),
      ),
    ),
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
    ),
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
    ),
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
    ),
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
    ),
  )

  val es_reg_wen   = io.fromExecute.reg_wen
  val es_reg_waddr = io.fromExecute.reg_waddr
  val es_fwd_valid = io.fromExecute.es_fwd_valid
  val ms_reg_wen   = io.fromMemory.reg_wen
  val ms_reg_waddr = io.fromMemory.reg_waddr
  val ms_fwd_valid = io.fromMemory.ms_fwd_valid

  val reg1_value = Wire(Vec(4, UInt(8.W)))
  val reg2_value = Wire(Vec(4, UInt(8.W)))

  reg1 := reg1_value.asUInt
  for (i <- 0 until 4) {
    when(reset.asBool === RST_ENABLE) {
      reg1_value(i) := ZERO_WORD
    }.elsewhen(
      reg1_ren && es_fwd_valid && es_reg_wen(i) && es_reg_waddr === reg1_raddr,
    ) {
      reg1_value(i) := io.fromExecute.reg_wdata(i * 8 + 7, i * 8)
    }.elsewhen(
      reg1_ren && ms_fwd_valid && ms_reg_wen(i) && ms_reg_waddr === reg1_raddr,
    ) {
      reg1_value(i) := io.fromMemory.reg_wdata(i * 8 + 7, i * 8)
    }.elsewhen(reg1_ren) {
      reg1_value(i) := reg1_data(i * 8 + 7, i * 8)
    }.elsewhen(!reg1_ren) {
      reg1_value(i) := imm(i * 8 + 7, i * 8)
    }.otherwise {
      reg1_value(i) := ZERO_WORD
    }
  }

  reg2 := reg2_value.asUInt
  for (i <- 0 until 4) {
    when(reset.asBool === RST_ENABLE) {
      reg2_value(i) := ZERO_WORD
    }.elsewhen(
      reg2_ren && es_fwd_valid && es_reg_wen(i) && es_reg_waddr === reg2_raddr,
    ) {
      reg2_value(i) := io.fromExecute.reg_wdata(i * 8 + 7, i * 8)
    }.elsewhen(
      reg2_ren && ms_fwd_valid && ms_reg_wen(i) && ms_reg_waddr === reg2_raddr,
    ) {
      reg2_value(i) := io.fromMemory.reg_wdata(i * 8 + 7, i * 8)
    }.elsewhen(reg2_ren) {
      reg2_value(i) := reg2_data(i * 8 + 7, i * 8)
    }.elsewhen(!reg2_ren) {
      reg2_value(i) := imm(i * 8 + 7, i * 8)
    }.otherwise {
      reg2_value(i) := ZERO_WORD
    }
  }

  when(reset.asBool === RST_ENABLE) {
    is_in_delayslot := NOT_IN_DELAY_SLOT
  }.otherwise {
    is_in_delayslot := is_in_delayslot_i
  }

  val is_branch_temp = Wire(Bool())
  is_branch_temp := MuxLookup(
    aluop,
    false.B,
    Seq(
      EXE_BEQ_OP    -> true.B,
      EXE_BNE_OP    -> true.B,
      EXE_JAL_OP    -> true.B,
      EXE_JR_OP     -> true.B,
      EXE_BGEZ_OP   -> true.B,
      EXE_BGTZ_OP   -> true.B,
      EXE_BLEZ_OP   -> true.B,
      EXE_BLTZ_OP   -> true.B,
      EXE_BGEZAL_OP -> true.B,
      EXE_BLTZAL_OP -> true.B,
      EXE_J_OP      -> true.B,
      EXE_JALR_OP   -> true.B,
    ),
  )
  is_branch := is_branch_temp && ds_valid

  // debug
  // printf(p"decoder :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
