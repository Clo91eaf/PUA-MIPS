package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines.Instructions

  // @formatter:off
trait Constants {
  // 全局
  val RST_ENABLE               = true.B  
  val RST_DISABLE              = false.B 
  val ZERO_WORD                = 0.U     
  val WRITE_ENABLE             = true.B  
  val WRITE_DISABLE            = false.B 
  val READ_ENABLE              = true.B  
  val READ_DISABLE             = false.B 
  val ALU_OP_BUS               = UInt(8.W) 
  val ALU_OP_BUS_INIT          = 0.U(8.W)
  val ALU_SEL_BUS              = UInt(3.W) 
  val ALU_SEL_BUS_INIT         = 0.U(3.W)
  val INST_VALID               = false.B 
  val INST_INVALID             = true.B  
  val STOP                     = true.B
  val NOT_STOP                 = false.B
  val IN_DELAY_SLOT            = true.B
  val NOT_IN_DELAY_SLOT        = false.B 
  val BRANCH                   = true.B 
  val NOT_BRANCH               = false.B
  val INTERRUPT_ASSERT         = true.B
  val INTERRUPT_NOT_ASSERT     = false.B
  val TRAP_ASSERT              = true.B
  val TRAP_NOT_ASSERT          = false.B
  val CHIP_ENABLE              = true.B 
  val CHIP_DISABLE             = false.B

  // 指令
  val EXE_AND                  = "b100100".U(6.W)
  val EXE_OR                   = "b100101".U(6.W)
  val EXE_XOR                  = "b100110".U(6.W)
  val EXE_NOR                  = "b100111".U(6.W)
  val EXE_ANDI                 = "b001100".U(6.W)
  val EXE_ORI                  = "b001101".U(6.W)
  val EXE_XORI                 = "b001110".U(6.W)
  val EXE_LUI                  = "b001111".U(6.W)

  val EXE_SLL                  = "b000000".U(6.W)
  val EXE_SLLV                 = "b000100".U(6.W)
  val EXE_SRL                  = "b000010".U(6.W)
  val EXE_SRLV                 = "b000110".U(6.W)
  val EXE_SRA                  = "b000011".U(6.W)
  val EXE_SRAV                 = "b000111".U(6.W)
  val EXE_SYNC                 = "b001111".U(6.W)
  val EXE_PREF                 = "b110011".U(6.W)

  val EXE_MOVZ                 = "b001010".U(6.W)
  val EXE_MOVN                 = "b001011".U(6.W)
  val EXE_MFHI                 = "b010000".U(6.W)
  val EXE_MTHI                 = "b010001".U(6.W)
  val EXE_MFLO                 = "b010010".U(6.W)
  val EXE_MTLO                 = "b010011".U(6.W)

  val EXE_SLT                  = "b101010".U(6.W)
  val EXE_SLTU                 = "b101011".U(6.W)
  val EXE_SLTI                 = "b001010".U(6.W)
  val EXE_SLTIU                = "b001011".U(6.W)
  val EXE_ADD                  = "b100000".U(6.W)
  val EXE_ADDU                 = "b100001".U(6.W)
  val EXE_SUB                  = "b100010".U(6.W)
  val EXE_SUBU                 = "b100011".U(6.W)
  val EXE_ADDI                 = "b001000".U(6.W)
  val EXE_ADDIU                = "b001001".U(6.W)
  val EXE_CLZ                  = "b100000".U(6.W)
  val EXE_CLO                  = "b100001".U(6.W)

  val EXE_MULT                 = "b011000".U(6.W)
  val EXE_MULTU                = "b011001".U(6.W)
  val EXE_MUL                  = "b000010".U(6.W)
  val EXE_MADD                 = "b000000".U(6.W)
  val EXE_MADDU                = "b000001".U(6.W)
  val EXE_MSUB                 = "b000100".U(6.W)
  val EXE_MSUBU                = "b000101".U(6.W)

  val EXE_DIV                  = "b011010".U(6.W)
  val EXE_DIVU                 = "b011011".U(6.W)

  val EXE_J                    = "b000010".U(6.W)
  val EXE_JAL                  = "b000011".U(6.W)
  val EXE_JALR                 = "b001001".U(6.W)
  val EXE_JR                   = "b001000".U(6.W)
  val EXE_BEQ                  = "b000100".U(6.W)
  val EXE_BGEZ                 = "b00001".U(5.W) 
  val EXE_BGEZAL               = "b10001".U(5.W) 
  val EXE_BGTZ                 = "b000111".U(6.W)
  val EXE_BLEZ                 = "b000110".U(6.W)
  val EXE_BLTZ                 = "b00000".U(5.W) 
  val EXE_BLTZAL               = "b10000".U(5.W) 
  val EXE_BNE                  = "b000101".U(6.W)

  val EXE_LB                   = "b100000".U(6.W) 
  val EXE_LBU                  = "b100100".U(6.W) 
  val EXE_LH                   = "b100001".U(6.W) 
  val EXE_LHU                  = "b100101".U(6.W) 
  val EXE_LL                   = "b110000".U(6.W)
  val EXE_LW                   = "b100011".U(6.W)
  val EXE_LWL                  = "b100010".U(6.W)
  val EXE_LWR                  = "b100110".U(6.W)
  val EXE_SB                   = "b101000".U(6.W)
  val EXE_SC                   = "b111000".U(6.W)
  val EXE_SH                   = "b101001".U(6.W)
  val EXE_SW                   = "b101011".U(6.W)
  val EXE_SWL                  = "b101010".U(6.W)
  val EXE_SWR                  = "b101110".U(6.W)

  val EXE_SYSCALL              = "b001100".U(6.W)

  val EXE_TEQ                  = "b110100".U(6.W)
  val EXE_TEQI                 = "b01100".U(5.W)
  val EXE_TGE                  = "b110000".U(6.W)
  val EXE_TGEI                 = "b01000".U(5.W)
  val EXE_TGEIU                = "b01001".U(5.W)
  val EXE_TGEU                 = "b110001".U(6.W)
  val EXE_TLT                  = "b110010".U(6.W)
  val EXE_TLTI                 = "b01010".U(5.W)
  val EXE_TLTIU                = "b01011".U(5.W)
  val EXE_TLTU                 = "b110011".U(6.W)
  val EXE_TNE                  = "b110110".U(6.W)
  val EXE_TNEI                 = "b01110".U(5.W)
  val EXE_ERET                 = "b01000010_00000000_00000000_00011000".U(32.W)

  val EXE_NOP                  = "b000000".U(6.W) 
  val SSNOP                    = "b00000000_00000000_00000000_01000000".U(32.W)

  val EXE_SPECIAL_INST         = "b000000".U(6.W)
  val EXE_REGIMM_INST          = "b000001".U(6.W)
  val EXE_SPECIAL2_INST        = "b011100".U(6.W)

  // AluOp
  val EXE_AND_OP               = "b00100100".U(8.W)
  val EXE_OR_OP                = "b00100101".U(8.W)
  val EXE_XOR_OP               = "b00100110".U(8.W)
  val EXE_NOR_OP               = "b00100111".U(8.W)

  val EXE_SLL_OP               = "b01111100".U(8.W)
  val EXE_SLLV_OP              = "b00000100".U(8.W)
  val EXE_SRL_OP               = "b00000010".U(8.W)
  val EXE_SRLV_OP              = "b00000110".U(8.W)
  val EXE_SRA_OP               = "b00000011".U(8.W)
  val EXE_SRAV_OP              = "b00000111".U(8.W)

  val EXE_MOVZ_OP              = "b00001010".U(8.W)
  val EXE_MOVN_OP              = "b00001011".U(8.W)
  val EXE_MFHI_OP              = "b00010000".U(8.W)
  val EXE_MTHI_OP              = "b00010001".U(8.W)
  val EXE_MFLO_OP              = "b00010010".U(8.W)
  val EXE_MTLO_OP              = "b00010011".U(8.W)

  val EXE_SLT_OP               = "b00101010".U(8.W)
  val EXE_SLTU_OP              = "b00101011".U(8.W)
  val EXE_ADD_OP               = "b00100000".U(8.W)
  val EXE_ADDU_OP              = "b00100001".U(8.W)
  val EXE_SUB_OP               = "b00100010".U(8.W)
  val EXE_SUBU_OP              = "b00100011".U(8.W)
  val EXE_CLZ_OP               = "b10110000".U(8.W)
  val EXE_CLO_OP               = "b10110001".U(8.W)

  val EXE_MULT_OP              = "b00011000".U(8.W)
  val EXE_MULTU_OP             = "b00011001".U(8.W)
  val EXE_MUL_OP               = "b10101001".U(8.W)
  val EXE_MADD_OP              = "b10100110".U(8.W)
  val EXE_MADDU_OP             = "b10101000".U(8.W)
  val EXE_MSUB_OP              = "b10101010".U(8.W)
  val EXE_MSUBU_OP             = "b10101011".U(8.W)

  val EXE_DIV_OP               = "b00011010".U(8.W)
  val EXE_DIVU_OP              = "b00011011".U(8.W)

  val EXE_J_OP                 = "b01001111".U(8.W)
  val EXE_JAL_OP               = "b01010000".U(8.W)
  val EXE_JALR_OP              = "b00001001".U(8.W)
  val EXE_JR_OP                = "b00001000".U(8.W)
  val EXE_BEQ_OP               = "b01010001".U(8.W)
  val EXE_BGEZ_OP              = "b01000001".U(8.W)
  val EXE_BGEZAL_OP            = "b01001011".U(8.W)
  val EXE_BGTZ_OP              = "b01010100".U(8.W)
  val EXE_BLEZ_OP              = "b01010011".U(8.W)
  val EXE_BLTZ_OP              = "b01000000".U(8.W)
  val EXE_BLTZAL_OP            = "b01001010".U(8.W)
  val EXE_BNE_OP               = "b01010010".U(8.W)

  val EXE_LB_OP                = "b11100000".U(8.W)
  val EXE_LBU_OP               = "b11100100".U(8.W)
  val EXE_LH_OP                = "b11100001".U(8.W)
  val EXE_LHU_OP               = "b11100101".U(8.W)
  val EXE_LL_OP                = "b11110000".U(8.W)
  val EXE_LW_OP                = "b11100011".U(8.W)
  val EXE_LWL_OP               = "b11100010".U(8.W)
  val EXE_LWR_OP               = "b11100110".U(8.W)
  val EXE_PREF_OP              = "b11110011".U(8.W)
  val EXE_SB_OP                = "b11101000".U(8.W)
  val EXE_SC_OP                = "b11111000".U(8.W)
  val EXE_SH_OP                = "b11101001".U(8.W)
  val EXE_SW_OP                = "b11101011".U(8.W)
  val EXE_SWL_OP               = "b11101010".U(8.W)
  val EXE_SWR_OP               = "b11101110".U(8.W)
  val EXE_SYNC_OP              = "b00001111".U(8.W)

  val EXE_MFC0_OP              = "b01011101".U(8.W)
  val EXE_MTC0_OP              = "b01100000".U(8.W)

  val EXE_SYSCALL_OP           = "b00001100".U(8.W)

  val EXE_TEQ_OP               = "b00110100".U(8.W)
  val EXE_TGE_OP               = "b00110000".U(8.W)
  val EXE_TGEU_OP              = "b00110001".U(8.W)
  val EXE_TLT_OP               = "b00110010".U(8.W)
  val EXE_TLTU_OP              = "b00110011".U(8.W)
  val EXE_TNE_OP               = "b00110110".U(8.W)
  
  val EXE_ERET_OP              = "b01101011".U(8.W)

  val EXE_NOP_OP               = "b00000000".U(8.W)

  // AluSel
  val EXE_RES_ALU              = "b001".U(3.W)
  val EXE_RES_MOV              = "b011".U(3.W)
  val EXE_RES_MUL              = "b101".U(3.W)
  val EXE_RES_JUMP_BRANCH      = "b110".U(3.W)
  val EXE_RES_LOAD_STORE       = "b111".U(3.W)
  
  val EXE_RES_NOP              = "b000".U(3.W)
  
  // inst rom
  val INST_ADDR_BUS            = UInt(32.W)
  val INST_ADDR_BUS_INIT       = 0.U(32.W)
  val INST_BUS                 = UInt(32.W)
  val INST_BUS_INIT            = 0.U(32.W)
  val INST_MEM_NUM             = 131071        // 2^17-1
  val INST_MEM_NUM_LOG2        = 17

  // data ram
  val DATA_ADDR_BUS            = UInt(32.W)
  val DATA_ADDR_BUS_INIT       = 0.U(32.W)
  val DATA_BUS                 = UInt(32.W)
  val DATA_BUS_INIT            = 0.U(32.W)
  val DATA_MEM_NUM             = 131071     // 2^17-1
  val DATA_MEM_NUM_LOG2        = 17          
  val BYTE_WIDTH               = UInt(8.W)
  val DATA_MEMORY_SEL_BUS      = UInt(4.W)
  val DATA_MEMORY_SEL_BUS_INIT = 0.U(4.W)

  // GPR RegFile
  val ADDR_BUS                 = UInt(5.W)
  val ADDR_BUS_INIT            = 0.U(5.W)
  val BUS                      = UInt(32.W)
  val BUS_INIT                 = 0.U(32.W)
  val PC_INIT                  = "hbfc00000".U(32.W)
  val WEN_BUS                  = UInt(4.W)
  val WEN_BUS_INIT             = 0.U(4.W)
  val WRITE_SELECT_INIT        = "b1111".U(4.W)
  val REG_WIDTH                = 32
  val DOUBLE_BUS               = UInt(64.W)
  val DOUBLE_BUS_INIT          = 0.U(64.W)
  val DOUBLE_REG_WIDTH         = 64
  val REG_NUM                  = 32
  val REG_NUM_LOG2             = 5
  val NOP_REG_ADDR             = "b00000".U(5.W)

  // other
  val STALL_BUS                = UInt(6.W)
  val STALL_BUS_INIT           = 0.U(6.W)
  val CNT_BUS                  = UInt(2.W)
  val CNT_BUS_INIT             = 0.U(2.W)

  // DIV Instructions
  val DIV_FREE                 = 0.U(2.W)
  val DIV_BY_ZERO              = 1.U(2.W)
  val DIV_ON                   = 2.U(2.W)
  val DIV_END                  = 3.U(2.W)
  val DIV_RESULT_READY         = true.B
  val DIV_RESULT_NOT_READY     = false.B
  val DIV_START                = true.B
  val DIV_STOP                 = false.B
  val SIGNED                   = true.B
  val NOT_SIGNED               = false.B

  // CP0寄存器
  val CP0_REG_COUNT            = "b01001".U(5.W)
  val CP0_REG_COMPARE          = "b01011".U(5.W)
  val CP0_REG_STATUS           = "b01100".U(5.W)
  val CP0_REG_CAUSE            = "b01101".U(5.W)
  val CP0_REG_EPC              = "b01110".U(5.W)
  val CP0_REG_PRID             = "b01111".U(5.W)
  val CP0_REG_CONFIG           = "b10000".U(5.W)

  val CP0_ADDR_BUS             = UInt(5.W)
  val CP0_ADDR_BUS_INIT        = 0.U(5.W)
}
trait OptionConst {

  // 写寄存器目标 Write Register Address type
  val WRA_T1                   = 0.U(2.W)    // 取inst(15,11)
  val WRA_T2                   = 1.U(2.W)    // 取inst(20,16)
  val WRA_T3                   = 2.U(2.W)    // 取"b11111", 即31号寄存器
  val WRA_X                    = 0.U(2.W)    // not care

  // 立即数类型
  private val IL               = 3
  val IMM_N                    = 0.U(IL.W)
  val IMM_LSE                  = 1.U(IL.W)   // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
  val IMM_LZE                  = 2.U(IL.W)   // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
  val IMM_HZE                  = 3.U(IL.W)   // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
  val IMM_SHT                  = 4.U(IL.W)   // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
}
  // @formatter:on

object Const extends Constants with Instructions with OptionConst
