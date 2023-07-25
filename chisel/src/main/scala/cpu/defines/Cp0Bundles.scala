package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines.Const._

class Cp0Index extends Bundle {
  val p     = Bool()
  val blank = UInt((32 - 1 - log2Ceil(TLB_NUM)).W)
  val index = UInt(log2Ceil(TLB_NUM).W)
}

class Cp0Random extends Bundle {
  val blank  = UInt((32 - log2Ceil(TLB_NUM)).W)
  val random = UInt(log2Ceil(TLB_NUM).W)
}

class Cp0EntryLo extends Bundle {
  val fill = UInt((32 - PFN_WID - C_WID - 3).W)
  val pfn  = UInt(PFN_WID.W)
  val c    = UInt(C_WID.W)
  val d    = Bool()
  val v    = Bool()
  val g    = Bool()
}

class Cp0Context extends Bundle {
  val ptebase = UInt(PTEBASE_WID.W)
  val badvpn2 = UInt(VPN2_WID.W)
  val blank   = UInt((32 - PTEBASE_WID - VPN2_WID).W)
}

class Cp0Wired extends Bundle {
  val blank = UInt((31 - log2Ceil(TLB_NUM)).W)
  val wired = UInt(log2Ceil(TLB_NUM).W)
}

class Cp0BadVAddr extends Bundle {
  val badvaddr = UInt(PC_WID.W)
}

class Cp0Count extends Bundle {
  val count = UInt(PC_WID.W)
}

class Cp0EntryHi extends Bundle {
  val vpn2  = UInt(VPN2_WID.W)
  val blank = UInt((32 - VPN2_WID - ASID_WID).W)
  val asid  = UInt(ASID_WID.W)
}
