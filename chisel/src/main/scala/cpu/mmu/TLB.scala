package cpu.mmu

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.defines._

class TLB extends Module {
  val io = IO(new Bundle {
    val fromInstMMU        = Flipped(new MMU_TLB())
    val fromDataMMU        = Flipped(new MMU_TLB())
    val fromWriteBackStage = Flipped(new WriteBackStage_TLB())
    val common             = new TLBCommon()

    val instMMU        = new TLB_MMU()
    val dataMMU        = new TLB_MMU()
    val execute        = new TLB_Execute()
    val writeBackStage = new TLB_WriteBackStage()
  })

  // io
  val s0_vpn2     = io.fromInstMMU.tlb_vpn2
  val s0_odd_page = io.fromInstMMU.tlb_odd_page
  val s0_asid     = io.fromInstMMU.tlb_asid
  val s0_found    = Wire(Bool())
  val s0_index    = Wire(UInt(log2Ceil(TLB_NUM).W))
  val s0_pfn      = Wire(UInt(20.W))
  val s0_c        = Wire(UInt(3.W))
  val s0_d        = Wire(Bool())
  val s0_v        = Wire(Bool())
  io.instMMU.tlb_found := s0_found
  io.common.s0_index   := s0_index
  io.instMMU.tlb_pfn   := s0_pfn
  io.instMMU.tlb_c     := s0_c
  io.instMMU.tlb_d     := s0_d
  io.instMMU.tlb_v     := s0_v

  // search port 1
  val s1_vpn2     = io.fromDataMMU.tlb_vpn2
  val s1_odd_page = io.fromDataMMU.tlb_odd_page
  val s1_asid     = io.fromDataMMU.tlb_asid
  val s1_found    = Wire(Bool())
  val s1_index    = Wire(UInt(log2Ceil(TLB_NUM).W))
  val s1_pfn      = Wire(UInt(20.W))
  val s1_c        = Wire(UInt(3.W))
  val s1_d        = Wire(Bool())
  val s1_v        = Wire(Bool())
  io.dataMMU.tlb_found := s1_found
  io.execute.s1_found  := s1_found
  io.execute.s1_index  := s1_index
  io.dataMMU.tlb_pfn   := s1_pfn
  io.dataMMU.tlb_c     := s1_c
  io.dataMMU.tlb_d     := s1_d
  io.dataMMU.tlb_v     := s1_v

  // write port
  val we      = io.fromWriteBackStage.we
  val w_index = io.fromWriteBackStage.w_index
  val w_vpn2  = io.fromWriteBackStage.w_vpn2
  val w_asid  = io.fromWriteBackStage.w_asid
  val w_g     = io.fromWriteBackStage.w_g
  val w_pfn0  = io.fromWriteBackStage.w_pfn0
  val w_c0    = io.fromWriteBackStage.w_c0
  val w_d0    = io.fromWriteBackStage.w_d0
  val w_v0    = io.fromWriteBackStage.w_v0
  val w_pfn1  = io.fromWriteBackStage.w_pfn1
  val w_c1    = io.fromWriteBackStage.w_c1
  val w_d1    = io.fromWriteBackStage.w_d1
  val w_v1    = io.fromWriteBackStage.w_v1

  // read port
  val r_index = io.fromWriteBackStage.r_index
  val r_vpn2  = Wire(UInt(19.W))
  val r_asid  = Wire(UInt(8.W))
  val r_g     = Wire(Bool())
  io.writeBackStage.r_vpn2 := r_vpn2
  io.writeBackStage.r_asid := r_asid
  io.writeBackStage.r_g    := r_g
  io.writeBackStage.r_vpn2 := r_vpn2
  io.writeBackStage.r_asid := r_asid
  io.writeBackStage.r_g    := r_g
  val r_pfn0 = Wire(UInt(20.W))
  val r_c0   = Wire(UInt(3.W))
  val r_d0   = Wire(Bool())
  val r_v0   = Wire(Bool())
  io.writeBackStage.r_pfn0 := r_pfn0
  io.writeBackStage.r_c0   := r_c0
  io.writeBackStage.r_d0   := r_d0
  io.writeBackStage.r_v0   := r_v0
  val r_pfn1 = Wire(UInt(20.W))
  val r_c1   = Wire(UInt(3.W))
  val r_d1   = Wire(Bool())
  val r_v1   = Wire(Bool())
  io.writeBackStage.r_pfn1 := r_pfn1
  io.writeBackStage.r_c1   := r_c1
  io.writeBackStage.r_d1   := r_d1
  io.writeBackStage.r_v1   := r_v1

  // io-finish

  val tlb_vpn2 = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U(19.W))))
  val tlb_asid = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U(8.W))))
  val tlb_g    = RegInit(VecInit(Seq.fill(TLB_NUM)(false.B)))
  val tlb_pfn0 = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U(20.W))))
  val tlb_c0   = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U(3.W))))
  val tlb_d0   = RegInit(VecInit(Seq.fill(TLB_NUM)(false.B)))
  val tlb_v0   = RegInit(VecInit(Seq.fill(TLB_NUM)(false.B)))
  val tlb_pfn1 = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U(20.W))))
  val tlb_c1   = RegInit(VecInit(Seq.fill(TLB_NUM)(0.U(3.W))))
  val tlb_d1   = RegInit(VecInit(Seq.fill(TLB_NUM)(false.B)))
  val tlb_v1   = RegInit(VecInit(Seq.fill(TLB_NUM)(false.B)))

  val match0 = Wire(Vec(TLB_NUM, Bool()))
  val match1 = Wire(Vec(TLB_NUM, Bool()))

  val s0_index_arr = Wire(Vec(TLB_NUM, UInt(log2Ceil(TLB_NUM).W)))
  val s1_index_arr = Wire(Vec(TLB_NUM, UInt(log2Ceil(TLB_NUM).W)))

  // search
  s0_found := match0.asUInt.orR
  s1_found := match1.asUInt.orR

  s0_index := s0_index_arr(TLB_NUM - 1)
  s1_index := s1_index_arr(TLB_NUM - 1)

  s0_pfn := Mux(s0_odd_page, tlb_pfn1(s0_index), tlb_pfn0(s0_index))
  s0_c   := Mux(s0_odd_page, tlb_c1(s0_index), tlb_c0(s0_index))
  s0_d   := Mux(s0_odd_page, tlb_d1(s0_index), tlb_d0(s0_index))
  s0_v   := Mux(s0_odd_page, tlb_v1(s0_index), tlb_v0(s0_index))

  s1_pfn := Mux(s1_odd_page, tlb_pfn1(s1_index), tlb_pfn0(s1_index))
  s1_c   := Mux(s1_odd_page, tlb_c1(s1_index), tlb_c0(s1_index))
  s1_d   := Mux(s1_odd_page, tlb_d1(s1_index), tlb_d0(s1_index))
  s1_v   := Mux(s1_odd_page, tlb_v1(s1_index), tlb_v0(s1_index))

  for (tlb_i <- 0 until TLB_NUM) {

    // 判断是否有匹配项
    match0(tlb_i) := (s0_vpn2 === tlb_vpn2(tlb_i)) &&
      ((s0_asid === tlb_asid(tlb_i)) || tlb_g(tlb_i))
    match1(tlb_i) := (s1_vpn2 === tlb_vpn2(tlb_i)) &&
      ((s1_asid === tlb_asid(tlb_i)) || tlb_g(tlb_i))

    if (tlb_i == 0) {
      s0_index_arr(tlb_i) := Mux(match0(tlb_i), tlb_i.U, 0.U)
      s1_index_arr(tlb_i) := Mux(match1(tlb_i), tlb_i.U, 0.U)
    } else {
      s0_index_arr(tlb_i) := s0_index_arr(tlb_i - 1) |
        Mux(match0(tlb_i), tlb_i.U, 0.U)
      s1_index_arr(tlb_i) := s1_index_arr(tlb_i - 1) |
        Mux(match1(tlb_i), tlb_i.U, 0.U)
    }

    // Write
    when(we && w_index === tlb_i.U) {
      tlb_vpn2(tlb_i) := w_vpn2
      tlb_asid(tlb_i) := w_asid
      tlb_g(tlb_i)    := w_g

      tlb_pfn0(tlb_i) := w_pfn0
      tlb_c0(tlb_i)   := w_c0
      tlb_d0(tlb_i)   := w_d0
      tlb_v0(tlb_i)   := w_v0

      tlb_pfn1(tlb_i) := w_pfn1
      tlb_c1(tlb_i)   := w_c1
      tlb_d1(tlb_i)   := w_d1
      tlb_v1(tlb_i)   := w_v1
    }
  } // 循环结束

  r_vpn2 := tlb_vpn2(r_index)
  r_asid := tlb_asid(r_index)
  r_g    := tlb_g(r_index)

  r_pfn0 := tlb_pfn0(r_index)
  r_c0   := tlb_c0(r_index)
  r_d0   := tlb_d0(r_index)
  r_v0   := tlb_v0(r_index)

  r_pfn1 := tlb_pfn1(r_index)
  r_c1   := tlb_c1(r_index)
  r_d1   := tlb_d1(r_index)
  r_v1   := tlb_v1(r_index)
}
