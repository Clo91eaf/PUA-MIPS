package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.CpuConfig

class BufferUnit extends Bundle {
  val tlb = new Bundle {
    val refill  = Bool()
    val invalid = Bool()
  }
  val data = UInt(32.W)
  val addr = UInt(32.W)
}

class InstBuffer(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val do_flush              = Input(Bool())
    val flush_delay_slot      = Input(Bool())
    val delay_sel_rst         = Input(Bool())
    val decoder_delay_rst     = Input(Bool())
    val execute_delay_rst     = Input(Bool())
    val icache_stall          = Input(Bool())
    val jump_branch_inst      = Input(Bool()) // 译码阶段的inst0是否为跳转指令
    val inst0_is_in_delayslot = Output(Bool())

    val ren  = Input(Vec(config.decoderNum, Bool()))
    val read = Output(Vec(config.decoderNum, new BufferUnit()))

    val wen   = Input(Vec(config.instFetchNum, Bool()))
    val write = Input(Vec(config.instFetchNum, new BufferUnit()))

    val empty        = Output(Bool())
    val almost_empty = Output(Bool())
    val full         = Output(Bool())
  })
  // fifo buffer
  val buffer = RegInit(VecInit(Seq.fill(config.instBufferDepth)(0.U.asTypeOf(new BufferUnit()))))

  // fifo ptr
  val enq_ptr = RegInit(0.U(log2Ceil(config.instBufferDepth).W))
  val deq_ptr = RegInit(0.U(log2Ceil(config.instBufferDepth).W))
  val count   = RegInit(0.U(log2Ceil(config.instBufferDepth).W))

  // config.instBufferDepth - 1 is the last element, config.instBufferDepth - 2 is the last second element
  // the second last element's valid decide whether the fifo is full
  io.full         := count >= (config.instBufferDepth - config.instFetchNum).U
  io.empty        := count === 0.U
  io.almost_empty := count === 1.U

  val inst0_is_in_delayslot = RegInit(false.B)
  io.inst0_is_in_delayslot := inst0_is_in_delayslot
  inst0_is_in_delayslot := MuxCase(
    false.B,
    Seq(
      io.flush_delay_slot                 -> false.B,
      !io.ren(0)                          -> inst0_is_in_delayslot,
      (io.jump_branch_inst && !io.ren(1)) -> true.B,
    ),
  )

  val delayslot_stall  = RegInit(false.B)
  val delayslot_enable = RegInit(false.B)
  val delayslot_line   = RegInit(0.U.asTypeOf(new BufferUnit()))
  when(
    io.do_flush && io.delay_sel_rst && !io.flush_delay_slot && io.icache_stall && (deq_ptr + 1.U === enq_ptr || deq_ptr === enq_ptr),
  ) {
    delayslot_stall := true.B
  }.elsewhen(delayslot_stall && io.wen(0)) {
    delayslot_stall := false.B
  }

  when(io.do_flush && !io.flush_delay_slot && io.delay_sel_rst) {
    when(io.execute_delay_rst) {
      delayslot_enable := true.B
      delayslot_line   := Mux(deq_ptr === enq_ptr, io.write(0), buffer(deq_ptr))
    }.elsewhen(io.decoder_delay_rst) {
      delayslot_enable := true.B
      delayslot_line := Mux(
        deq_ptr + 1.U === enq_ptr,
        io.write(0),
        buffer(deq_ptr + 1.U),
      )
    }.otherwise {
      delayslot_enable := false.B
    }
  }.elsewhen(!delayslot_stall && io.ren(0)) {
    delayslot_enable := false.B
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
  when(io.do_flush) {
    deq_ptr := 0.U
  }.elsewhen(io.empty || delayslot_enable) {
    deq_ptr := deq_ptr
  }.elsewhen(io.ren(1)) {
    deq_ptr := deq_ptr + 2.U
  }.elsewhen(io.ren(0)) {
    deq_ptr := deq_ptr + 1.U
  }

  // * enq * //
  val enq_num = Wire(UInt(log2Ceil(config.instFetchNum + 1).W))

  for { i <- 0 until config.instFetchNum } { when(io.wen(i)) { buffer(enq_ptr + i.U) := io.write(i) } }

  when(io.do_flush) {
    enq_ptr := 0.U
  }.otherwise {
    enq_ptr := enq_ptr + enq_num
  }

  enq_num := 0.U
  for (i <- 0 until config.instFetchNum) {
    when(io.wen(i)) {
      enq_num := (i + 1).U
    }
  }

  val deq_num = MuxCase(
    0.U,
    Seq(
      (io.empty || delayslot_enable) -> 0.U,
      io.ren(1)                      -> 2.U,
      io.ren(0)                      -> 1.U,
    ),
  )

  count := Mux(io.do_flush, 0.U, count + enq_num + config.instBufferDepth.U - deq_num)
}
