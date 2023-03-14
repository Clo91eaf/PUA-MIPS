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
  val EXE_ORI = "b001101".U(6.W)

  //AluOp
  val EXE_OR_OP = "b00100101".U(8.W)
  val EXE_ORI_OP = "b01011010".U(8.W)
  val EXE_NOP_OP = "b00000000".U(8.W)

  //AluSel
  val EXE_RES_LOGIC = "b001".U(3.W)
  val EXE_RES_NOP = "b000".U(3.W)

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
