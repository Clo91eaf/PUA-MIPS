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
  val count = UInt(DATA_WID.W)
}

class Cp0EntryHi extends Bundle {
  val vpn2  = UInt(VPN2_WID.W)
  val blank = UInt((32 - VPN2_WID - ASID_WID).W)
  val asid  = UInt(ASID_WID.W)
}

class Cp0Compare extends Bundle {
  val compare = UInt(DATA_WID.W)
}

class Cp0Status extends Bundle {
  val blank3 = UInt(3.W)
  val cu0    = Bool()
  val blank2 = UInt(5.W)
  val bev    = Bool()
  val blank1 = UInt(6.W)
  val im     = UInt(8.W)
  val blank0 = UInt(3.W)
  val um     = Bool()
  val r0     = Bool()
  val erl    = Bool()
  val exl    = Bool()
  val ie     = Bool()
}

class Cp0Cause extends Bundle {
  val bd     = Bool()
  val blank3 = UInt(7.W)
  val iv     = Bool()
  val blank2 = UInt(7.W)
  val ip     = UInt(8.W)
  val blank1 = Bool()
  val excode = UInt(5.W)
  val blank0 = UInt(2.W)
}

class Cp0Epc extends Bundle {
  val epc = UInt(PC_WID.W)
}

class Cp0Ebase extends Bundle {
  val fill   = Bool()
  val blank1 = Bool()
  val ebase  = UInt(18.W)
  val blank0 = UInt(2.W)
  val cpuNum = UInt(10.W)
}

class Cp0Config extends Bundle {
  val m     = Bool()
  val k23   = UInt(3.W)
  val ku    = UInt(3.W)
  val impl  = UInt(9.W)
  val be    = Bool()
  val at    = UInt(2.W)
  val ar    = UInt(3.W)
  val mt    = UInt(3.W)
  val blank = UInt(3.W)
  val vi    = Bool()
  val k0    = UInt(3.W)
}

class Cp0Config1 extends Bundle {
  val m  = Bool()
  val ms = UInt(6.W)
  val is = UInt(3.W)
  val il = UInt(3.W)
  val ia = UInt(3.W)
  val ds = UInt(3.W)
  val dl = UInt(3.W)
  val da = UInt(3.W)
  val c2 = Bool()
  val md = Bool()
  val pc = Bool()
  val wr = Bool()
  val ca = Bool()
  val ep = Bool()
  val fp = Bool()
}

class Cp0ErrorEpc extends Bundle {
  val errorEpc = UInt(PC_WID.W)
}
