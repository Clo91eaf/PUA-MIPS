package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines.Instructions

trait Constants {
  // 全局
  val RST_ENABLE           = true.B
  val RST_DISABLE          = false.B
  val ZERO_WORD            = 0.U
  val WRITE_ENABLE         = true.B
  val WRITE_DISABLE        = false.B
  val REG_WRITE_ENABLE     = "b1111".U(4.W)
  val REG_WRITE_DISABLE    = "b0000".U(4.W)
  val REG_WRITE_BUS        = UInt(4.W)
  val READ_ENABLE          = true.B
  val READ_DISABLE         = false.B
  val INST_VALID           = false.B
  val INST_INVALID         = true.B
  val STOP                 = true.B
  val NOT_STOP             = false.B
  val IN_DELAY_SLOT        = true.B
  val NOT_IN_DELAY_SLOT    = false.B
  val BRANCH               = true.B
  val NOT_BRANCH           = false.B
  val INTERRUPT_ASSERT     = true.B
  val INTERRUPT_NOT_ASSERT = false.B
  val TRAP_ASSERT          = true.B
  val TRAP_NOT_ASSERT      = false.B
  val CHIP_ENABLE          = true.B
  val CHIP_DISABLE         = false.B

  // AluOp
  private val ALU_OP_LEN = 7
  val ALU_OP_BUS         = UInt(ALU_OP_LEN.W)
  val ALU_OP_BUS_INIT    = 0.U(ALU_OP_LEN.W)
  // NOP
  val EXE_NOP_OP = 0.U(ALU_OP_LEN.W)
  // 位操作
  val EXE_AND_OP = 1.U(ALU_OP_LEN.W)
  val EXE_OR_OP  = 2.U(ALU_OP_LEN.W)
  val EXE_XOR_OP = 3.U(ALU_OP_LEN.W)
  val EXE_NOR_OP = 4.U(ALU_OP_LEN.W)
  // 移位
  val EXE_SLL_OP  = 5.U(ALU_OP_LEN.W)
  val EXE_SLLV_OP = 6.U(ALU_OP_LEN.W)
  val EXE_SRL_OP  = 7.U(ALU_OP_LEN.W)
  val EXE_SRLV_OP = 8.U(ALU_OP_LEN.W)
  val EXE_SRA_OP  = 9.U(ALU_OP_LEN.W)
  val EXE_SRAV_OP = 10.U(ALU_OP_LEN.W)
  // Move
  val EXE_MOVZ_OP = 11.U(ALU_OP_LEN.W)
  val EXE_MOVN_OP = 12.U(ALU_OP_LEN.W)
  // HILO
  val EXE_MFHI_OP = 13.U(ALU_OP_LEN.W)
  val EXE_MTHI_OP = 14.U(ALU_OP_LEN.W)
  val EXE_MFLO_OP = 15.U(ALU_OP_LEN.W)
  val EXE_MTLO_OP = 16.U(ALU_OP_LEN.W)
  // CP0 Move
  val EXE_MFC0_OP = 17.U(ALU_OP_LEN.W)
  val EXE_MTC0_OP = 18.U(ALU_OP_LEN.W)
  // 比较
  val EXE_SLT_OP  = 19.U(ALU_OP_LEN.W)
  val EXE_SLTU_OP = 20.U(ALU_OP_LEN.W)
  // 算数
  val EXE_ADD_OP   = 21.U(ALU_OP_LEN.W)
  val EXE_ADDU_OP  = 22.U(ALU_OP_LEN.W)
  val EXE_SUB_OP   = 23.U(ALU_OP_LEN.W)
  val EXE_SUBU_OP  = 24.U(ALU_OP_LEN.W)
  val EXE_CLZ_OP   = 25.U(ALU_OP_LEN.W)
  val EXE_CLO_OP   = 26.U(ALU_OP_LEN.W)
  val EXE_MULT_OP  = 27.U(ALU_OP_LEN.W)
  val EXE_MULTU_OP = 28.U(ALU_OP_LEN.W)
  val EXE_MUL_OP   = 29.U(ALU_OP_LEN.W)
  val EXE_MADD_OP  = 30.U(ALU_OP_LEN.W)
  val EXE_MADDU_OP = 31.U(ALU_OP_LEN.W)
  val EXE_MSUB_OP  = 32.U(ALU_OP_LEN.W)
  val EXE_MSUBU_OP = 33.U(ALU_OP_LEN.W)
  val EXE_DIV_OP   = 34.U(ALU_OP_LEN.W)
  val EXE_DIVU_OP  = 35.U(ALU_OP_LEN.W)
  // 跳转
  val EXE_J_OP      = 36.U(ALU_OP_LEN.W)
  val EXE_JAL_OP    = 37.U(ALU_OP_LEN.W)
  val EXE_JALR_OP   = 38.U(ALU_OP_LEN.W)
  val EXE_JR_OP     = 39.U(ALU_OP_LEN.W)
  val EXE_BEQ_OP    = 40.U(ALU_OP_LEN.W)
  val EXE_BGEZ_OP   = 41.U(ALU_OP_LEN.W)
  val EXE_BGEZAL_OP = 42.U(ALU_OP_LEN.W)
  val EXE_BGTZ_OP   = 43.U(ALU_OP_LEN.W)
  val EXE_BLEZ_OP   = 44.U(ALU_OP_LEN.W)
  val EXE_BLTZ_OP   = 45.U(ALU_OP_LEN.W)
  val EXE_BLTZAL_OP = 46.U(ALU_OP_LEN.W)
  val EXE_BNE_OP    = 47.U(ALU_OP_LEN.W)
  // 访存
  val EXE_LB_OP  = 48.U(ALU_OP_LEN.W)
  val EXE_LBU_OP = 49.U(ALU_OP_LEN.W)
  val EXE_LH_OP  = 50.U(ALU_OP_LEN.W)
  val EXE_LHU_OP = 51.U(ALU_OP_LEN.W)
  val EXE_LL_OP  = 52.U(ALU_OP_LEN.W)
  val EXE_LW_OP  = 53.U(ALU_OP_LEN.W)
  val EXE_LWL_OP = 54.U(ALU_OP_LEN.W)
  val EXE_LWR_OP = 55.U(ALU_OP_LEN.W)
  val EXE_SB_OP  = 56.U(ALU_OP_LEN.W)
  val EXE_SC_OP  = 57.U(ALU_OP_LEN.W)
  val EXE_SH_OP  = 58.U(ALU_OP_LEN.W)
  val EXE_SW_OP  = 59.U(ALU_OP_LEN.W)
  val EXE_SWL_OP = 60.U(ALU_OP_LEN.W)
  val EXE_SWR_OP = 61.U(ALU_OP_LEN.W)
  // Trap
  val EXE_TEQ_OP  = 62.U(ALU_OP_LEN.W)
  val EXE_TGE_OP  = 63.U(ALU_OP_LEN.W)
  val EXE_TGEU_OP = 64.U(ALU_OP_LEN.W)
  val EXE_TLT_OP  = 65.U(ALU_OP_LEN.W)
  val EXE_TLTU_OP = 66.U(ALU_OP_LEN.W)
  val EXE_TNE_OP  = 67.U(ALU_OP_LEN.W)
  // 例外
  val EXE_SYSCALL_OP = 68.U(ALU_OP_LEN.W)
  val EXE_BREAK_OP   = 69.U(ALU_OP_LEN.W)
  val EXE_ERET_OP    = 70.U(ALU_OP_LEN.W)
  val EXE_WAIT_OP    = 71.U(ALU_OP_LEN.W)
  // tlb
  val EXE_TLBP_OP  = 72.U(ALU_OP_LEN.W)
  val EXE_TLBR_OP  = 73.U(ALU_OP_LEN.W)
  val EXE_TLBWI_OP = 74.U(ALU_OP_LEN.W)

  // AluSel
  val ALU_SEL_BUS      = UInt(3.W)
  val ALU_SEL_BUS_INIT = 0.U(3.W)

  val EXE_RES_ALU         = "b001".U(3.W)
  val EXE_RES_MOV         = "b011".U(3.W)
  val EXE_RES_MUL         = "b101".U(3.W)
  val EXE_RES_JUMP_BRANCH = "b110".U(3.W)
  val EXE_RES_LOAD_STORE  = "b111".U(3.W)

  val EXE_RES_NOP = "b000".U(3.W)

  // inst rom
  val INST_ADDR_BUS      = UInt(32.W)
  val INST_ADDR_BUS_INIT = 0.U(32.W)
  val INST_BUS           = UInt(32.W)
  val INST_BUS_INIT      = 0.U(32.W)
  val INST_MEM_NUM       = 131071 // 2^17-1
  val INST_MEM_NUM_LOG2  = 17

  // data ram
  val DATA_ADDR_BUS            = UInt(32.W)
  val DATA_ADDR_BUS_INIT       = 0.U(32.W)
  val DATA_BUS                 = UInt(32.W)
  val DATA_BUS_INIT            = 0.U(32.W)
  val DATA_MEM_NUM             = 131071 // 2^17-1
  val DATA_MEM_NUM_LOG2        = 17
  val BYTE_WIDTH               = UInt(8.W)
  val DATA_MEMORY_SEL_BUS      = UInt(4.W)
  val DATA_MEMORY_SEL_BUS_INIT = 0.U(4.W)

  // GPR RegFile
  val ADDR_BUS          = UInt(5.W)
  val ADDR_BUS_INIT     = 0.U(5.W)
  val BUS               = UInt(32.W)
  val BUS_INIT          = 0.U(32.W)
  val PC_INIT           = "hbfc00000".U(32.W)
  val WEN_BUS           = UInt(4.W)
  val WEN_BUS_INIT      = 0.U(4.W)
  val WRITE_SELECT_INIT = "b1111".U(4.W)
  val REG_WIDTH         = 32
  val DOUBLE_BUS        = UInt(64.W)
  val DOUBLE_BUS_INIT   = 0.U(64.W)
  val DOUBLE_REG_WIDTH  = 64
  val REG_NUM           = 32
  val REG_NUM_LOG2      = 5
  val NOP_REG_ADDR      = "b00000".U(5.W)

  // other
  val STALL_BUS      = UInt(6.W)
  val STALL_BUS_INIT = 0.U(6.W)
  val CNT_BUS        = UInt(2.W)
  val CNT_BUS_INIT   = 0.U(2.W)

  // DIV Instructions
  val DIV_FREE             = 0.U(2.W)
  val DIV_BY_ZERO          = 1.U(2.W)
  val DIV_ON               = 2.U(2.W)
  val DIV_END              = 3.U(2.W)
  val DIV_RESULT_READY     = true.B
  val DIV_RESULT_NOT_READY = false.B
  val DIV_START            = true.B
  val DIV_STOP             = false.B
  val SIGNED               = true.B
  val NOT_SIGNED           = false.B

  // CP0寄存器
  // CP0 Register (5.w), Select (3.w)
  val CP0_INDEX_ADDR          = "b00000_000".U(8.W) // 0,0
  val CP0_RANDOM_ADDR         = "b00001_000".U(8.W) // 1,0
  val CP0_ENTRYLO0_ADDR       = "b00010_000".U(8.W) // 2,0
  val CP0_ENTRYLO1_ADDR       = "b00011_000".U(8.W) // 3,0
  val CP0_CONTEXT_ADDR        = "b00100_000".U(8.W) // 4,0
  val CP0_CONTEXT_CONFIG_ADDR = "b00100_001".U(8.W) // 4,1
  val CP0_USER_LOCAL_ADDR     = "b00100_010".U(8.W) // 4,2
  val CP0_PAGE_MASK_ADDR      = "b00101_000".U(8.W) // 5,0
  val CP0_PAGE_GRAIN_ADDR     = "b00101_001".U(8.W) // 5,1
  val CP0_WIRED_ADDR          = "b00110_000".U(8.W) // 6,0
  val CP0_HWRENA_ADDR         = "b00111_000".U(8.W) // 7,0
  val CP0_BADV_ADDR           = "b01000_000".U(8.W) // 8,0
  val CP0_COUNT_ADDR          = "b01001_000".U(8.W) // 9,0  (sel保留 6or7)
  val CP0_ENTRYHI_ADDR        = "b01010_000".U(8.W) // 10,0
  val CP0_COMPARE_ADDR        = "b01011_000".U(8.W) // 11,0 (sel保留 6or7)
  val CP0_STATUS_ADDR         = "b01100_000".U(8.W) // 12,0
  val CP0_INTCTL_ADDR         = "b01100_001".U(8.W) // 12,1
  val CP0_SRSCTL_ADDR         = "b01100_010".U(8.W) // 12,2
  val CP0_SRSMAP_ADDR         = "b01100_011".U(8.W) // 12,3
  val CP0_CAUSE_ADDR          = "b01101_000".U(8.W) // 13,0
  val CP0_EPC_ADDR            = "b01110_000".U(8.W) // 14,0
  val CP0_PI_ADDR             = "b01111_000".U(8.W) // 15,0
  val CP0_EBASE_ADDR          = "b01111_001".U(8.W) // 15,1
  val CP0_CDMMBASE_ADDR       = "b01111_010".U(8.W) // 15,2
  val CP0_CMGCRBASE_ADDR      = "b01111_011".U(8.W) // 15,3
  val CP0_CONFIG_ADDR         = "b10000_000".U(8.W) // 16,0
  val CP0_CONFIG1_ADDR        = "b10000_001".U(8.W) // 16,1
  val CP0_CONFIG2_ADDR        = "b10000_010".U(8.W) // 16,2
  val CP0_CONFIG3_ADDR        = "b10000_011".U(8.W) // 16,3
  val CP0_CONFIG4_ADDR        = "b10000_100".U(8.W) // 16,4 (sel保留 6or7)
  val CP0_LOAD_LINKED_ADDR    = "b10001_000".U(8.W) // 17,0

  val CP0_ADDR_BUS      = UInt(8.W)
  val CP0_ADDR_BUS_INIT = 0.U(8.W)

  // 例外类型
  val EX_INT  = "h00".U(5.W) //  interrupt.
  val EX_MOD  = "h01".U(5.W) //  modification of a TLB entry.
  val EX_TLBL = "h02".U(5.W) //  TLB miss on a load or instruction fetch.
  val EX_TLBS = "h03".U(5.W) //  TLB miss on a store.
  val EX_ADEL = "h04".U(5.W) //  address error on a load or instruction fetch.
  val EX_ADES = "h05".U(5.W) //  address error on a store.
  val EX_SYS  = "h08".U(5.W) //  syscall instruction.
  val EX_BP   = "h09".U(5.W) //  breakpoint instruction.
  val EX_RI   = "h0a".U(5.W) //  reserved instruction.
  val EX_OV   = "h0c".U(5.W) //  arithmetic overflow.
  val EX_NO   = "h1f".U(5.W) //  unknown cause.

  val EX_ENTRY            = "h_bfc00380".U(32.W)
  val EX_TLB_REFILL_ENTRY = "h_bfc00200".U(32.W)
  // TLB MMU
  val TLB_NUM = 16
}
trait OptionConst {

  // 写寄存器目标 Write Register Address type
  val WRA_T1 = 0.U(2.W) // 取inst(15,11)
  val WRA_T2 = 1.U(2.W) // 取inst(20,16)
  val WRA_T3 = 2.U(2.W) // 取"b11111", 即31号寄存器
  val WRA_X  = 0.U(2.W) // not care

  // 立即数类型
  private val IL = 3
  val IMM_N      = 0.U(IL.W)
  val IMM_LSE    = 1.U(IL.W) // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
  val IMM_LZE    = 2.U(IL.W) // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
  val IMM_HZE    = 3.U(IL.W) // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
  val IMM_SHT    = 4.U(IL.W) // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
}

object Const extends Constants with Instructions with OptionConst
