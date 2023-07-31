package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.TlbEntry
import cpu.defines.Const._

class TlbL2 extends Module {
  val io = IO(new Bundle {
    val in = Input(new Bundle {
      val write = new Bundle {
        val en    = Bool()
        val index = UInt(log2Ceil(TLB_NUM).W)
        val entry = new TlbEntry()
      }
      val read = new Bundle {
        val index = UInt(log2Ceil(TLB_NUM).W)
      }
      val entry_hi = new Bundle {
        val vpn2 = UInt(VPN2_WID.W)
        val asid = UInt(ASID_WID.W)
      }
      val tlb1_vpn2 = UInt(VPN2_WID.W)
      val tlb2_vpn2 = UInt(VPN2_WID.W)
    })
    val out = Output(new Bundle {
      val read = new Bundle {
        val entry = new TlbEntry()
      }
      val tlb1_found      = Bool()
      val tlb2_found      = Bool()
      val tlb1_entry      = new TlbEntry()
      val tlb2_entry      = new TlbEntry()
      val tlb_found       = Bool()
      val tlb_match_index = UInt(log2Ceil(TLB_NUM).W)
    })
  })
  // tlb l2
  val tlb_l2 = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U.asTypeOf(new TlbEntry()))))

  val tlb_match       = Seq.fill(3)(Wire(Vec(TLB_NUM, Bool())))
  val tlb_find_vpn2   = Wire(Vec(3, UInt(VPN2_WID.W)))
  val tlb_match_index = Wire(Vec(3, UInt(log2Ceil(TLB_NUM).W)))

  tlb_find_vpn2(0) := io.in.entry_hi.vpn2
  tlb_find_vpn2(1) := io.in.tlb1_vpn2
  tlb_find_vpn2(2) := io.in.tlb2_vpn2

  io.out.tlb1_found      := tlb_match(1).asUInt.orR
  io.out.tlb2_found      := tlb_match(2).asUInt.orR
  io.out.tlb1_entry      := tlb_l2(tlb_match_index(1))
  io.out.tlb2_entry      := tlb_l2(tlb_match_index(2))
  io.out.tlb_found       := tlb_match(0).asUInt.orR
  io.out.tlb_match_index := tlb_match_index(0)
  io.out.read.entry      := tlb_l2(io.in.read.index)

  for (i <- 0 until (3)) {
    for (j <- 0 until (TLB_NUM)) {
      tlb_match(i)(j) := (tlb_l2(j).g || tlb_l2(j).asid === io.in.entry_hi.asid) &&
        (tlb_l2(j).vpn2 === tlb_find_vpn2(i))
    }
    tlb_match_index(i) := PriorityEncoder(tlb_match(i))
  }

  when(io.in.write.en) {
    tlb_l2(io.in.write.index) := io.in.write.entry
  }
}
