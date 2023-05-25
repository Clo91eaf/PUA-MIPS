package cpu.axi

import chisel3._
import chisel3.util._

/** A simple FIFO buffer implemented using Chisel's built-in Queue module.
  *
  * @param dataWidth
  *   The width of the data to be stored in the buffer.
  * @param buffDepth
  *   The depth of the buffer (i.e. the number of elements it can hold).
  * @param addrWidth
  *   The width of the address used to access the buffer.
  */
class FifoBuffer(
    val dataWidth: Int = 32,
    val buffDepth: Int = 4,
    val addrWidth: Int = 2,
) extends Module {
  val io = IO(new Bundle {
    val wen    = Input(Bool())             // Write enable signal.
    val ren    = Input(Bool())             // Read enable signal.
    val input  = Input(UInt(dataWidth.W))  // Data to be written to the buffer.
    val output = Output(UInt(dataWidth.W)) // Data read from the buffer.
    val empty  = Output(Bool())            // Output signal indicating whether the buffer is empty.
    val full   = Output(Bool())            // Output signal indicating whether the buffer is full.
  })

  // Instantiate a Queue module with the given data width and buffer depth.
  val queue = Module(new Queue(UInt(dataWidth.W), buffDepth))

  // Connect the input and output signals to the Queue module.
  queue.io.enq.valid := io.wen
  queue.io.enq.bits  := io.input
  io.full            := queue.io.enq.ready === false.B
  queue.io.deq.ready := io.ren
  io.output          := queue.io.deq.bits
  io.empty           := queue.io.count === 0.U
}

/** A simple counter that keeps track of the number of elements in a FIFO buffer.
  *
  * @param buffDepth
  *   The depth of the buffer (i.e. the number of elements it can hold).
  * @param addrWidth
  *   The width of the address used to access the buffer.
  */
class FifoCount(
    val buffDepth: Int = 4,
    val addrWidth: Int = 2,
) extends Module {
  val io = IO(new Bundle {
    val wen   = Input(Bool())
    val ren   = Input(Bool())
    val empty = Output(Bool())
    val full  = Output(Bool())
  })

  val count = RegInit(0.U(addrWidth.W))

  io.empty := count === 0.U
  io.full  := count === buffDepth.U

  when(io.ren && !io.empty) {
    count := count - 1.U
  }.elsewhen(io.wen && !io.full) {
    count := count + 1.U
  }
}

/** A FIFO buffer with a valid signal that checks if the output data is related to a specific value.
  *
  * @param dataWidth
  *   The width of the data to be stored in the buffer.
  * @param buffDepth
  *   The depth of the buffer (i.e. the number of elements it can hold).
  * @param addrWidth
  *   The width of the address used to access the buffer.
  * @param relatedDataWidth
  *   The width of the related data used to check if the output data is related to a specific value.
  */
class FifoBufferValid(
    val dataWidth: Int = 33,
    val buffDepth: Int = 6,
    val addrWidth: Int = 3,
    val relatedDataWidth: Int = 32,
) extends Module {
  val io = IO(new Bundle {
    val wen   = Input(Bool())  // Write enable signal.
    val ren   = Input(Bool())  // Read enable signal.
    val empty = Output(Bool()) // Output signal indicating whether the buffer is empty.
    val full  = Output(Bool()) // Output signal indicating whether the buffer is full.
    val related_1 = Output(
      Bool(),
    ) // Output signal indicating whether the output data is related to a specific value.
    val input  = Input(UInt(dataWidth.W))  // Data to be written to the buffer.
    val output = Output(UInt(dataWidth.W)) // Data read from the buffer.
    val related_data_1 = Input(
      UInt(relatedDataWidth.W),
    ) // Related data used to check if the output data is related to a specific value.
  })

  // Instantiate a Queue module with the given data width and buffer depth.
  val queue = Module(new Queue(UInt(dataWidth.W), buffDepth))

  // Connect the input and output signals to the Queue module.
  queue.io.enq.valid := io.wen
  queue.io.enq.bits  := io.input
  io.full            := queue.io.count === buffDepth.U
  io.empty           := queue.io.count === 0.U
  io.output          := queue.io.deq.bits

  // Connect the ready signal to the read enable input.
  queue.io.deq.ready := io.ren

  // Check if the output data is related to a specific value.
  io.related_1 := queue.io.deq.valid && io.related_data_1 === queue.io.deq
    .bits(relatedDataWidth - 1, 0)
}