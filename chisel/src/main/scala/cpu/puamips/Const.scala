package cpu.puamips

import chisel3._
import chisel3.util._

object Const {
  val RST_ENABLE = true.B // 复位使能
  val RST_DISABLE = false.B // 复位除能
  val WRITE_ENABLE = true.B // 写使能
  val WRITE_DISABLE = false.B // 写除能
  val READ_ENABLE = true.B // 读使能
  val READ_DISABLE = false.B // 读除能
  val INST_VALID = false.B // 指令有效
  val INST_INVALID = true.B // 指令无效
  val CHIP_ENABLE = true.B // 芯片使能
  val CHIP_DISABLE = false.B // 芯片禁止
  val ZERO_WORD = 0.U // 32位数字0
  val ALU_OP_BUS = UInt(8.W) // 译码阶段输出操作子类型数据宽度
  val ALU_OP_BUS_INIT = 0.U(8.W)
  val ALU_SEL_BUS = UInt(3.W) // 译码阶段输出操作类型数据宽度
  val ALU_SEL_BUS_INIT = 0.U(3.W) // 译码阶段输出操作类型数据宽度

  // 指令
  val EXE_AND = "b100100".U(6.W) // 指令 and 的功能码
  val EXE_OR = "b100101".U(6.W) // 指令 or 的功能码
  val EXE_XOR = "b100110".U(6.W) // 指令 xor 的功能码
  val EXE_NOR = "b100111".U(6.W) // 指令 nor 的功能码
  val EXE_ANDI = "b001100".U(6.W) // 指令 andi 的指令码
  val EXE_ORI = "b001101".U(6.W) // 指令 ori 的功能码
  val EXE_XORI = "b001110".U(6.W) // 指令 xori 的指令码
  val EXE_LUI = "b001111".U(6.W) // 指令 lui 的指令码

  val EXE_SLL = "b000000".U(6.W) // 指令 sll 的功能码
  val EXE_SLLV = "b000100".U(6.W) // 指令 sllv 的功能码
  val EXE_SRL = "b000010".U(6.W) // 指令 srl 的功能码
  val EXE_SRLV = "b000110".U(6.W) // 指令 srlv 的功能码
  val EXE_SRA = "b000011".U(6.W) // 指令 sra 的功能码
  val EXE_SRAV = "b000111".U(6.W) // 指令 srav 的功能码
  val EXE_SYNC = "b001111".U(6.W) // 指令 sync 的功能码
  val EXE_PREF = "b110011".U(6.W) // 指令 pref 的功能码

  val EXE_MOVZ = "b001010".U(6.W) // 指令MOVZ的功能码
  val EXE_MOVN = "b001011".U(6.W) // 指令MOVN的功能码
  val EXE_MFHI = "b010000".U(6.W) // 指令MFHI的功能码
  val EXE_MTHI = "b010001".U(6.W) // 指令MTHI的功能码
  val EXE_MFLO = "b010010".U(6.W) // 指令MFLO的功能码
  val EXE_MTLO = "b010011".U(6.W) // 指令MTLO的功能码

  val EXE_NOP = "b000000".U(6.W) // 指令 nop 的功能码
  val SSNOP = "b0000_0000_0000_0000_0000_0000_0100_0000".U(32.W) // 指令 SSNOP

  val EXE_SPECIAL_INST = "b000000".U(6.W) // 指令 special 的指令码

  // AluOp
  val EXE_AND_OP = "b00100100".U(8.W)
  val EXE_OR_OP = "b00100101".U(8.W)
  val EXE_XOR_OP = "b00100110".U(8.W)
  val EXE_NOR_OP = "b00100111".U(8.W)
  val EXE_ANDI_OP = "b01011001".U(8.W)
  val EXE_ORI_OP = "b01011010".U(8.W)
  val EXE_XORI_OP = "b01011011".U(8.W)
  val EXE_LUI_OP = "b01011100".U(8.W)

  val EXE_SLL_OP = "b01111100".U(8.W)
  val EXE_SLLV_OP = "b00000100".U(8.W)
  val EXE_SRL_OP = "b00000010".U(8.W)
  val EXE_SRLV_OP = "b00000110".U(8.W)
  val EXE_SRA_OP = "b00000011".U(8.W)
  val EXE_SRAV_OP = "b00000111".U(8.W)

  val EXE_MOVZ_OP = "b00001010".U(8.W)
  val EXE_MOVN_OP = "b00001011".U(8.W)
  val EXE_MFHI_OP = "b00010000".U(8.W)
  val EXE_MTHI_OP = "b00010001".U(8.W)
  val EXE_MFLO_OP = "b00010010".U(8.W)
  val EXE_MTLO_OP = "b00010011".U(8.W)

  val EXE_NOP_OP = "b00000000".U(8.W)

  // AluSel
  val EXE_RES_LOGIC = "b001".U(3.W)

  val EXE_RES_SHIFT = "b010".U(3.W)
  val EXE_RES_NOP = "b000".U(3.W)
  val EXE_RES_MOVE = "b011".U(3.W)

  val INST_ADDR_BUS = UInt(32.W)
  val INST_BUS = UInt(32.W)
  val INST_MEM_NUM_LOG2 = 17
  val INST_MEM_NUM = (1 << INST_MEM_NUM_LOG2) - 1

  val REG_ADDR_BUS = UInt(5.W)
  val REG_ADDR_BUS_INIT = 0.U(5.W)
  val REG_BUS = UInt(32.W)
  val REG_BUS_INIT = 0.U(32.W)
  val NOP_REG_ADDR = "b00000".U(5.W)
  val REG_NUM = 32
  val REG_NUM_LOG2 = 5
}
