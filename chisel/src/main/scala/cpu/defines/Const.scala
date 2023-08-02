package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines.Instructions

trait Constants {
  // 全局
  val PC_WID  = 32
  val PC_INIT = "hbfc00000".U(PC_WID.W)

  val EXT_INT_WID = 6

  val WRITE_ENABLE  = true.B
  val WRITE_DISABLE = false.B
  val READ_ENABLE   = true.B
  val READ_DISABLE  = false.B
  val INST_VALID    = false.B
  val INST_INVALID  = true.B
  val SINGLE_ISSUE  = false.B
  val DUAL_ISSUE    = true.B

  // AluOp
  private val OP_NUM = 77
  val OP_WID         = log2Ceil(OP_NUM)
  // NOP
  val EXE_NOP = 0.U(OP_WID.W)
  // 位操作
  val EXE_AND = 1.U(OP_WID.W)
  val EXE_OR  = 2.U(OP_WID.W)
  val EXE_XOR = 3.U(OP_WID.W)
  val EXE_NOR = 4.U(OP_WID.W)
  // 移位
  val EXE_SLL  = 5.U(OP_WID.W)
  val EXE_SLLV = 6.U(OP_WID.W)
  val EXE_SRL  = 7.U(OP_WID.W)
  val EXE_SRLV = 8.U(OP_WID.W)
  val EXE_SRA  = 9.U(OP_WID.W)
  val EXE_SRAV = 10.U(OP_WID.W)
  // Move
  val EXE_MOVZ = 11.U(OP_WID.W)
  val EXE_MOVN = 12.U(OP_WID.W)
  // HILO
  val EXE_MFHI = 13.U(OP_WID.W)
  val EXE_MTHI = 14.U(OP_WID.W)
  val EXE_MFLO = 15.U(OP_WID.W)
  val EXE_MTLO = 16.U(OP_WID.W)
  // CP0 Move
  val EXE_MFC0 = 17.U(OP_WID.W)
  val EXE_MTC0 = 18.U(OP_WID.W)
  // 比较
  val EXE_SLT  = 19.U(OP_WID.W)
  val EXE_SLTU = 20.U(OP_WID.W)
  // 算数
  val EXE_ADD   = 21.U(OP_WID.W)
  val EXE_ADDU  = 22.U(OP_WID.W)
  val EXE_SUB   = 23.U(OP_WID.W)
  val EXE_SUBU  = 24.U(OP_WID.W)
  val EXE_CLZ   = 25.U(OP_WID.W)
  val EXE_CLO   = 26.U(OP_WID.W)
  val EXE_MULT  = 27.U(OP_WID.W)
  val EXE_MULTU = 28.U(OP_WID.W)
  val EXE_MUL   = 29.U(OP_WID.W)
  val EXE_MADD  = 30.U(OP_WID.W)
  val EXE_MADDU = 31.U(OP_WID.W)
  val EXE_MSUB  = 32.U(OP_WID.W)
  val EXE_MSUBU = 33.U(OP_WID.W)
  val EXE_DIV   = 34.U(OP_WID.W)
  val EXE_DIVU  = 35.U(OP_WID.W)
  // 跳转
  val EXE_J      = 36.U(OP_WID.W)
  val EXE_JAL    = 37.U(OP_WID.W)
  val EXE_JALR   = 38.U(OP_WID.W)
  val EXE_JR     = 39.U(OP_WID.W)
  val EXE_BEQ    = 40.U(OP_WID.W)
  val EXE_BGEZ   = 41.U(OP_WID.W)
  val EXE_BGEZAL = 42.U(OP_WID.W)
  val EXE_BGTZ   = 43.U(OP_WID.W)
  val EXE_BLEZ   = 44.U(OP_WID.W)
  val EXE_BLTZ   = 45.U(OP_WID.W)
  val EXE_BLTZAL = 46.U(OP_WID.W)
  val EXE_BNE    = 47.U(OP_WID.W)
  // 访存
  val EXE_LB  = 48.U(OP_WID.W)
  val EXE_LBU = 49.U(OP_WID.W)
  val EXE_LH  = 50.U(OP_WID.W)
  val EXE_LHU = 51.U(OP_WID.W)
  val EXE_LL  = 52.U(OP_WID.W)
  val EXE_LW  = 53.U(OP_WID.W)
  val EXE_LWL = 54.U(OP_WID.W)
  val EXE_LWR = 55.U(OP_WID.W)
  val EXE_SB  = 56.U(OP_WID.W)
  val EXE_SC  = 57.U(OP_WID.W)
  val EXE_SH  = 58.U(OP_WID.W)
  val EXE_SW  = 59.U(OP_WID.W)
  val EXE_SWL = 60.U(OP_WID.W)
  val EXE_SWR = 61.U(OP_WID.W)
  // Trap
  val EXE_TEQ  = 62.U(OP_WID.W)
  val EXE_TGE  = 63.U(OP_WID.W)
  val EXE_TGEU = 64.U(OP_WID.W)
  val EXE_TLT  = 65.U(OP_WID.W)
  val EXE_TLTU = 66.U(OP_WID.W)
  val EXE_TNE  = 67.U(OP_WID.W)
  // 例外
  val EXE_SYSCALL = 68.U(OP_WID.W)
  val EXE_BREAK   = 69.U(OP_WID.W)
  val EXE_ERET    = 70.U(OP_WID.W)
  val EXE_WAIT    = 71.U(OP_WID.W)
  // tlb
  val EXE_TLBP  = 72.U(OP_WID.W)
  val EXE_TLBR  = 73.U(OP_WID.W)
  val EXE_TLBWI = 74.U(OP_WID.W)
  val EXE_TLBWR = 75.U(OP_WID.W)
  // cache
  val EXE_CACHE = 76.U(OP_WID.W)

  // FUSel
  val FU_SEL_NUM = 10
  val FU_SEL_WID = log2Ceil(FU_SEL_NUM)

  val FU_NOP    = 0.U(FU_SEL_WID.W)
  val FU_ALU    = 1.U(FU_SEL_WID.W) // TODO:可能可以删除的用-表示,-
  val FU_MTC0   = 2.U(FU_SEL_WID.W) // -
  val FU_MFC0   = 3.U(FU_SEL_WID.W) // -
  val FU_MEM    = 4.U(FU_SEL_WID.W)
  val FU_BR     = 5.U(FU_SEL_WID.W)
  val FU_TLB    = 6.U(FU_SEL_WID.W) // -
  val FU_EX     = 7.U(FU_SEL_WID.W)
  val FU_TRAP   = 8.U(FU_SEL_WID.W) // -
  val FU_MTHILO = 9.U(FU_SEL_WID.W)
  val FU_MFHILO = 10.U(FU_SEL_WID.W)
  val FU_MUL    = 11.U(FU_SEL_WID.W)
  val FU_DIV    = 12.U(FU_SEL_WID.W)

  // div
  val DIV_CTRL_WID         = 2
  val DIV_FREE             = 0.U(DIV_CTRL_WID.W)
  val DIV_BY_ZERO          = 1.U(DIV_CTRL_WID.W)
  val DIV_ON               = 2.U(DIV_CTRL_WID.W)
  val DIV_END              = 3.U(DIV_CTRL_WID.W)
  val DIV_RESULT_READY     = true.B
  val DIV_RESULT_NOT_READY = false.B
  val DIV_START            = true.B
  val DIV_STOP             = false.B

  // inst rom
  val INST_WID = 32

  // data ram
  val DATA_ADDR_WID = 32

  // GPR RegFile
  val AREG_NUM     = 32
  val REG_ADDR_WID = 5
  val DATA_WID     = 32
  val HILO_WID     = 64

  // CP0寄存器
  // CP0 Register (5.w), Select (3.w)
  val CP0_INDEX_ADDR    = "b00000_000".U(8.W) // 0,0
  val CP0_RANDOM_ADDR   = "b00001_000".U(8.W) // 1,0
  val CP0_ENTRYLO0_ADDR = "b00010_000".U(8.W) // 2,0
  val CP0_ENTRYLO1_ADDR = "b00011_000".U(8.W) // 3,0
  val CP0_CONTEXT_ADDR  = "b00100_000".U(8.W) // 4,0
  // val CP0_CONTEXT_CONFIG_ADDR = "b00100_001".U(8.W) // 4,1
  // val CP0_USER_LOCAL_ADDR     = "b00100_010".U(8.W) // 4,2
  val CP0_PAGE_MASK_ADDR = "b00101_000".U(8.W) // 5,0
  // val CP0_PAGE_GRAIN_ADDR     = "b00101_001".U(8.W) // 5,1
  val CP0_WIRED_ADDR = "b00110_000".U(8.W) // 6,0
  // val CP0_HWRENA_ADDR         = "b00111_000".U(8.W) // 7,0
  val CP0_BADV_ADDR    = "b01000_000".U(8.W) // 8,0
  val CP0_COUNT_ADDR   = "b01001_000".U(8.W) // 9,0  (sel保留 6or7)
  val CP0_ENTRYHI_ADDR = "b01010_000".U(8.W) // 10,0
  val CP0_COMPARE_ADDR = "b01011_000".U(8.W) // 11,0 (sel保留 6or7)
  val CP0_STATUS_ADDR  = "b01100_000".U(8.W) // 12,0
  // val CP0_INTCTL_ADDR         = "b01100_001".U(8.W) // 12,1
  // val CP0_SRSCTL_ADDR         = "b01100_010".U(8.W) // 12,2
  // val CP0_SRSMAP_ADDR         = "b01100_011".U(8.W) // 12,3
  val CP0_CAUSE_ADDR = "b01101_000".U(8.W) // 13,0
  val CP0_EPC_ADDR   = "b01110_000".U(8.W) // 14,0
  val CP0_PRID_ADDR  = "b01111_000".U(8.W) // 15,0
  val CP0_EBASE_ADDR = "b01111_001".U(8.W) // 15,1
  // val CP0_CDMMBASE_ADDR    = "b01111_010".U(8.W) // 15,2
  // val CP0_CMGCRBASE_ADDR   = "b01111_011".U(8.W) // 15,3
  val CP0_CONFIG_ADDR  = "b10000_000".U(8.W) // 16,0
  val CP0_CONFIG1_ADDR = "b10000_001".U(8.W) // 16,1
  // val CP0_CONFIG2_ADDR     = "b10000_010".U(8.W) // 16,2
  // val CP0_CONFIG3_ADDR     = "b10000_011".U(8.W) // 16,3
  // val CP0_CONFIG4_ADDR     = "b10000_100".U(8.W) // 16,4 (sel保留 6or7)
  // val CP0_LOAD_LINKED_ADDR = "b10001_000".U(8.W) // 17,0
  val CP0_TAGLO_ADDR     = "b11100_000".U(8.W) // 28,0
  val CP0_TAGHI_ADDR     = "b11101_000".U(8.W) // 29,0
  val CP0_ERROR_EPC_ADDR = "b11110_000".U(8.W) // 30,0

  val CP0_ADDR_WID = 8

  val PTEBASE_WID = 9

  // 例外类型
  val EXCODE_WID = 5

  val EX_NO   = 0.U(EXCODE_WID.W)  // 无异常
  val EX_INT  = 1.U(EXCODE_WID.W)  // 中断异常
  val EX_MOD  = 2.U(EXCODE_WID.W)  // TLB 条目修改异常
  val EX_TLBL = 3.U(EXCODE_WID.W)  // TLB 非法取指令或访问异常
  val EX_TLBS = 4.U(EXCODE_WID.W)  // TLB 非法存储访问异常
  val EX_ADEL = 5.U(EXCODE_WID.W)  // 地址未对齐异常（取指令或访问异常）
  val EX_ADES = 6.U(EXCODE_WID.W)  // 地址未对齐异常（存储访问异常）
  val EX_SYS  = 7.U(EXCODE_WID.W)  // 系统调用异常
  val EX_BP   = 8.U(EXCODE_WID.W)  // 断点异常
  val EX_RI   = 9.U(EXCODE_WID.W)  // 保留指令异常
  val EX_CPU  = 10.U(EXCODE_WID.W) // 协处理器不可用异常
  val EX_OV   = 11.U(EXCODE_WID.W) // 算术溢出异常

  val EXC_INT  = "h00".U(EXCODE_WID.W) // 中断异常
  val EXC_MOD  = "h01".U(EXCODE_WID.W) // TLB 条目修改异常
  val EXC_TLBL = "h02".U(EXCODE_WID.W) // TLB 非法取指令或访问异常
  val EXC_TLBS = "h03".U(EXCODE_WID.W) // TLB 非法存储访问异常
  val EXC_ADEL = "h04".U(EXCODE_WID.W) // 地址未对齐异常（取指令或访问异常）
  val EXC_ADES = "h05".U(EXCODE_WID.W) // 地址未对齐异常（存储访问异常）
  val EXC_SYS  = "h08".U(EXCODE_WID.W) // 系统调用异常
  val EXC_BP   = "h09".U(EXCODE_WID.W) // 断点异常
  val EXC_RI   = "h0a".U(EXCODE_WID.W) // 保留指令异常
  val EXC_CPU  = "h0b".U(EXCODE_WID.W) // 协处理器不可用异常
  val EXC_OV   = "h0c".U(EXCODE_WID.W) // 算术溢出异常
  val EXC_NO   = "h1f".U(EXCODE_WID.W) // 无异常

  val EX_ENTRY            = "h_bfc00380".U(32.W)
  val EX_TLB_REFILL_ENTRY = "h_bfc00200".U(32.W)

  // TLB MMU
  val TLB_NUM  = 8
  val PFN_WID  = 20
  val C_WID    = 3
  val ASID_WID = 8
  val VPN2_WID = 19
}
trait OptionConst {

  // 写寄存器目标 Write Register Address type
  val WRA_T1  = 0.U(2.W) // 取inst(15,11)
  val WRA_T2  = 1.U(2.W) // 取inst(20,16)
  val WRA_T3  = 2.U(2.W) // 取"b11111", 即31号寄存器
  val WRA_X   = 0.U(2.W) // not care
  val AREG_31 = "b11111".U(5.W)

  // 立即数类型
  private val IL = 3
  val IMM_N      = 0.U(IL.W)
  val IMM_LSE    = 1.U(IL.W) // 立即数取inst(15,0)作为低16位，符号扩展，适用于ADDI，ADDIU，SLTI，和SLTIU
  val IMM_LZE    = 2.U(IL.W) // 立即数取inst(15,0)作为低16位，零扩展，适用于位操作指令
  val IMM_HZE    = 3.U(IL.W) // 立即数取inst(15,0)作为高16位，零扩展，适用于LUI （是否有必要？）
  val IMM_SHT    = 4.U(IL.W) // 立即数取inst(10,6)作为低5位，不关心扩展，适用于SLL，SRL，SRA
}

object Const extends Constants with Instructions with OptionConst
