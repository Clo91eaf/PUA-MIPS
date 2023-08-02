package cpu.pipeline.writeback

import chisel3._
import chisel3.util._

class CommitBuffer(
    depth: Int = 32,
) extends Module {
  val io = IO(new Bundle {
    val flush = Input(Bool())
    val enq = Flipped(
      Vec(
        2,
        new Bundle {
          val wb_pc       = Output(UInt(32.W))
          val wb_rf_wen   = Output(UInt(4.W))
          val wb_rf_wnum  = Output(UInt(5.W))
          val wb_rf_wdata = Output(UInt(32.W))
        },
      ),
    )
    val deq = new Bundle {
      val wb_pc       = Output(UInt(32.W))
      val wb_rf_wen   = Output(UInt(4.W))
      val wb_rf_wnum  = Output(UInt(5.W))
      val wb_rf_wdata = Output(UInt(32.W))
    }
  })

  val ram = RegInit(VecInit(Seq.fill(depth)(0.U.asTypeOf(new Bundle {
    val wb_pc       = UInt(32.W)
    val wb_rf_wen   = UInt(4.W)
    val wb_rf_wnum  = UInt(5.W)
    val wb_rf_wdata = UInt(32.W)
  }))))
  val enq_ptr    = RegInit(0.U(log2Ceil(depth).W))
  val deq_ptr    = RegInit(0.U(log2Ceil(depth).W))
  val maybe_full = RegInit(false.B)
  val ptr_match  = enq_ptr === deq_ptr
  val empty      = ptr_match && !maybe_full
  val full       = ptr_match && maybe_full
  val do_enq     = Wire(Vec(2, Bool()))
  val do_deq     = WireDefault(io.deq.wb_rf_wen.orR)

  for { i <- 0 until 2 } {
    do_enq(i) := io.enq(i).wb_rf_wen.orR
  }

  val next_enq_ptr = MuxCase(
    enq_ptr,
    Seq(
      io.flush                 -> 0.U,
      (do_enq(0) && do_enq(1)) -> (enq_ptr + 2.U),
      (do_enq(0) || do_enq(1)) -> (enq_ptr + 1.U),
    ),
  )

  when(do_enq(0)) {
    ram(enq_ptr) := io.enq(0)
  }

  val enq1_ptr = Mux(do_enq(0), enq_ptr + 1.U, enq_ptr)
  when(do_enq(1)) {
    ram(enq1_ptr) := io.enq(1)
  }

  val next_deq_ptr =
    Mux(do_deq, deq_ptr + 1.U, deq_ptr)

  when(do_enq(0) =/= do_deq) {
    maybe_full := do_enq(0)
  }

  when(do_enq(1)) {
    maybe_full := do_enq(1)
  }

  when(io.flush) {
    enq_ptr    := 0.U
    deq_ptr    := 0.U
    maybe_full := false.B
  }.otherwise {
    enq_ptr := next_enq_ptr
    deq_ptr := next_deq_ptr
  }

  when(do_deq) {
    ram(deq_ptr).wb_rf_wen := 0.U
  }

  when(empty) {
    do_deq           := false.B
    io.deq           := DontCare
    io.deq.wb_rf_wen := 0.U
  }.otherwise {
    io.deq := ram(deq_ptr)
  }

}
