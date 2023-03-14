package cpu.openmips

import chisel3._
import chisel3.util._

object Constants {
  val RstEnable = true.B
  val RstDisable = false.B
  val WriteEnable = true.B
  val WriteDisable = false.B
  val ReadEnable = true.B
  val ReadDisable = false.B
  val InstValid = false.B
  val InstInvalid = true.B
  val ChipEnable = true.B
  val ChipDisable = false.B
  val ZeroWord = 0.U(32.W)
  val AluOpBus = UInt(8.W)
  val AluSelBus = UInt(3.W)
  // 指令
  val EXE_ORI = "b001101".U(6.W) // 指令 ori 的功能码
  val EXE_AND = "b100100".U(6.W) // 指令 and 的功能码
  val EXE_OR = "b100101".U(6.W) // 指令 or 的功能码
  val EXE_XOR = "b100110".U(6.W) // 指令 xor 的功能码
  val EXE_NOR = "b100111".U(6.W) // 指令 nor 的功能码
  val EXE_ANDI = "b001100".U(6.W) // 指令 andi 的指令码
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
  val EXE_NOP = "b000000".U(6.W) // 指令 nop 的功能码
  val SSNOP = "b0000_0000_0000_0000_0000_0000_0100_0000".U(32.W) // 指令 SSNOP

  // AluOp
  val EXE_OR_OP = "b00100101".U(8.W)
  val EXE_ORI_OP = "b01011010".U(8.W)
  val EXE_NOP_OP = "b00000000".U(8.W)
  val EXE_SPECIAL_INST = "b000000".U(6.W) // 指令 special 的指令码
  val EXE_AND_OP = "b00100100".U(8.W)
  val EXE_XOR_OP = "b00100110".U(8.W)
  val EXE_NOR_OP = "b00100111".U(8.W)
  val EXE_ANDI_OP = "b01011001".U(8.W)
  val EXE_XORI_OP = "b01011011".U(8.W)
  val EXE_LUI_OP = "b01011100".U(8.W)
  val EXE_SLL_OP = "b01111100".U(8.W)
  val EXE_SLLV_OP = "b00000100".U(8.W)
  val EXE_SRL_OP = "b00000010".U(8.W)
  val EXE_SRLV_OP = "b00000110".U(8.W)
  val EXE_SRA_OP = "b00000011".U(8.W)
  val EXE_SRAV_OP = "b00000111".U(8.W)

  // AluSel
  val EXE_RES_LOGIC = "b001".U(3.W)
  val EXE_RES_NOP = "b000".U(3.W)
  val EXE_RES_SHIFT = "b010".U(3.W)

  val InstAddrBus = UInt(32.W)
  val InstBus = UInt(32.W)
  val InstMemNumLog2 = 17
  val InstMemNum = (1 << InstMemNumLog2) - 1

  val RegAddrBus = UInt(5.W)
  val RegBus = UInt(32.W)
  val RegBusInit = 0.U(32.W)
  val NOPRegAddr = "b00000".U(5.W)
  val RegNum = 32
  val RegNumLog2 = 5
}
