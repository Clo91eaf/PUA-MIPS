package cpu.pipeline.fetch

import chisel3._
import chisel3.util._

class BufferUnit extends Bundle {
  val tlb = new Bundle {
    val refill  = Bool()
    val invalid = Bool()
  }
  val data = UInt(32.W)
  val addr = UInt(32.W)
}

class InstBuffer(
    ninst: Int = 2,
    depth: Int = 16,
) extends Module {
  val io = IO(new Bundle {
    val fifo_rst                 = Input(Bool())
    val flush_delay_slot         = Input(Bool())
    val delay_sel_rst            = Input(Bool())
    val D_delay_rst              = Input(Bool())
    val E_delay_rst              = Input(Bool())
    val D_ena                    = Input(Bool())
    val i_stall                  = Input(Bool())
    val master_is_branch         = Input(Bool())
    val master_is_in_delayslot_o = Output(Bool())

    val read_en = Input(Vec(ninst, Bool()))
    val read    = Output(Vec(ninst, new BufferUnit()))

    val write_en = Input(Vec(ninst, Bool()))
    val write    = Input(Vec(ninst, new BufferUnit()))

    val empty        = Output(Bool())
    val almost_empty = Output(Bool())
    val full         = Output(Bool())
  })
  // fifo buffer
  val buffer = RegInit(VecInit(Seq.fill(depth)(0.U.asTypeOf(new BufferUnit()))))

  // fifo ptr
  val enq_ptr = RegInit(0.U(log2Ceil(depth).W))
  val deq_ptr = RegInit(0.U(log2Ceil(depth).W))
  val count   = Mux(enq_ptr - deq_ptr < 0.U, enq_ptr - deq_ptr + depth.U, enq_ptr - deq_ptr)

  // depth - 1 is the last element, depth - 2 is the last second element
  // the second last element's valid decide whether the fifo is full
  io.full         := count >= (depth - 2).U
  io.empty        := count === 0.U
  io.almost_empty := count === 1.U

  val master_is_in_delayslot = RegInit(false.B)
  io.master_is_in_delayslot_o := master_is_in_delayslot
  master_is_in_delayslot := MuxCase(
    false.B,
    Seq(
      io.flush_delay_slot                     -> false.B,
      !io.read_en(0)                          -> master_is_in_delayslot,
      (io.master_is_branch && !io.read_en(1)) -> true.B,
    ),
  )

  val delayslot_stall  = RegInit(false.B)
  val delayslot_enable = RegInit(false.B)
  val delayslot_line   = RegInit(0.U.asTypeOf(new BufferUnit()))
  when(
    io.fifo_rst && io.delay_sel_rst && !io.flush_delay_slot && io.i_stall && (deq_ptr + 1.U === enq_ptr || deq_ptr === enq_ptr),
  ) {
    delayslot_stall := true.B
  }.elsewhen(delayslot_stall && io.write_en(0)) {
    delayslot_stall := false.B
  }

  when(io.fifo_rst && !io.flush_delay_slot && io.delay_sel_rst) {
    when(io.E_delay_rst) {
      delayslot_enable := true.B
      delayslot_line   := Mux(deq_ptr === enq_ptr, io.write(0), buffer(deq_ptr))
    }.elsewhen(io.D_delay_rst) {
      delayslot_enable := true.B
      delayslot_line := Mux(
        deq_ptr + 1.U === enq_ptr,
        io.write(0),
        buffer(deq_ptr + 1.U),
      )
    }.otherwise {
      delayslot_enable := false.B
      delayslot_line   := 0.U.asTypeOf(new BufferUnit())
    }
  }.elsewhen(!delayslot_stall && io.read_en(0)) {
    delayslot_enable := false.B
    delayslot_line   := 0.U.asTypeOf(new BufferUnit())
  }

  // * deq * //
  io.read(0) := MuxCase(
    buffer(deq_ptr),
    Seq(
      delayslot_enable -> delayslot_line,
      io.empty         -> 0.U.asTypeOf(new BufferUnit()),
      io.almost_empty  -> buffer(deq_ptr),
    ),
  )
  io.read(1) := MuxCase(
    buffer(deq_ptr + 1.U),
    Seq(
      delayslot_enable -> 0.U.asTypeOf(new BufferUnit()),
      io.empty         -> 0.U.asTypeOf(new BufferUnit()),
      io.almost_empty  -> 0.U.asTypeOf(new BufferUnit()),
    ),
  )
  when(io.fifo_rst) {
    deq_ptr := 0.U
  }.elsewhen(io.empty || delayslot_enable) {
    deq_ptr := deq_ptr
  }.elsewhen(io.read_en(0) && io.read_en(1)) {
    deq_ptr := deq_ptr + 2.U
  }.elsewhen(io.read_en(0)) {
    deq_ptr := deq_ptr + 1.U
  }

  // * enq * //
  for { i <- 0 until ninst } { when(io.write_en(i)) { buffer(enq_ptr + i.U) := io.write(i) } }

  when(io.fifo_rst) {
    enq_ptr := 0.U
  }.elsewhen(io.write_en(0) && io.write_en(1)) {
    enq_ptr := enq_ptr + 2.U
  }.elsewhen(io.write_en(0)) {
    enq_ptr := enq_ptr + 1.U
  }
}