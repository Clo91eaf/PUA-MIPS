package cpu.axi

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

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

class SramAXITrans extends Module {
  val io = IO(new Bundle {
    val instMemory = Flipped(new InstMemory_SramAXITrans())
    val dataMemory = Flipped(new DataMemory_SramAXITrans())
    val axi        = new AXI()
  })
  // some constant
  // ID
  val INST_ID = 0.U(4.W)
  val DATA_ID = 1.U(4.W)

  // read request
  io.axi.ar.len   := 0.U
  io.axi.ar.burst := "b01".U(2.W)
  io.axi.ar.lock  := "b00".U(2.W)
  io.axi.ar.cache := "b0000".U(4.W)
  io.axi.ar.prot  := "b000".U(3.W)

  // write request
  io.axi.aw.id    := "b0001".U(4.W)
  io.axi.aw.len   := "b00000000".U(8.W)
  io.axi.aw.burst := "b01".U(2.W)
  io.axi.aw.lock  := "b00".U(2.W)
  io.axi.aw.cache := "b0000".U(4.W)
  io.axi.aw.prot  := "b000".U(3.W)

  // write data
  io.axi.w.id   := "b0001".U(4.W)
  io.axi.w.last := 1.U

  // AXI read request
  val axi_ar_busy = RegInit(false.B)
  val axi_ar_id   = RegInit(0.U(4.W))
  val axi_ar_addr = RegInit(BUS_INIT)
  val axi_ar_size = RegInit(0.U(3.W))

  // AXI read response
  val axi_r_data_ok = Wire(Bool())
  val axi_r_inst_ok = Wire(Bool())
  val axi_r_data    = Wire(BUS)

  // AXI write request
  val axi_aw_busy = RegInit(false.B)
  val axi_aw_addr = RegInit(BUS_INIT)
  val axi_aw_size = RegInit(0.U(3.W))
  val axi_w_busy  = RegInit(false.B)
  val axi_w_data  = RegInit(BUS_INIT)
  val axi_w_strb  = RegInit(0.U(4.W))

  // AXI write response
  val axi_b_ok = Wire(Bool())

  // middle read request
  val read_req_sel_data = Wire(Bool())
  val read_req_sel_inst = Wire(Bool())

  val read_req_valid = Wire(Bool())
  val read_req_id    = Wire(UInt(4.W))
  val read_req_addr  = Wire(BUS)
  val read_req_size  = Wire(UInt(3.W))

  val read_data_req_ok = Wire(Bool())
  val read_inst_req_ok = Wire(Bool())

  // middle read inst response
  val read_inst_resp_wen    = Wire(Bool())
  val read_inst_resp_ren    = Wire(Bool())
  val read_inst_resp_empty  = Wire(Bool())
  val read_inst_resp_full   = Wire(Bool())
  val read_inst_resp_input  = Wire(BUS)
  val read_inst_resp_output = Wire(BUS)

  val read_inst_resp_buff = Module(new FifoBuffer())
  read_inst_resp_buff.io.wen <> read_inst_resp_wen
  read_inst_resp_buff.io.ren <> read_inst_resp_ren
  read_inst_resp_buff.io.empty <> read_inst_resp_empty
  read_inst_resp_buff.io.full <> read_inst_resp_full
  read_inst_resp_buff.io.input <> read_inst_resp_input
  read_inst_resp_buff.io.output <> read_inst_resp_output

  // middle read data response
  val read_data_resp_wen    = Wire(Bool())
  val read_data_resp_ren    = Wire(Bool())
  val read_data_resp_empty  = Wire(Bool())
  val read_data_resp_full   = Wire(Bool())
  val read_data_resp_input  = Wire(BUS)
  val read_data_resp_output = Wire(BUS)

  val read_data_resp_buff = Module(new FifoBuffer(32, 6))
  read_data_resp_buff.io.wen <> read_data_resp_wen
  read_data_resp_buff.io.ren <> read_data_resp_ren
  read_data_resp_buff.io.empty <> read_data_resp_empty
  read_data_resp_buff.io.full <> read_data_resp_full
  read_data_resp_buff.io.input <> read_data_resp_input
  read_data_resp_buff.io.output <> read_data_resp_output

  // middle write request
  val write_req_valid   = Wire(Bool())
  val write_req_addr    = Wire(BUS)
  val write_req_size    = Wire(UInt(3.W))
  val write_req_data    = Wire(BUS)
  val write_req_strb    = Wire(UInt(4.W))
  val write_data_req_ok = Wire(Bool())

  // middle write response
  val write_data_resp_wen   = Wire(Bool())
  val write_data_resp_ren   = Wire(Bool())
  val write_data_resp_empty = Wire(Bool())
  val write_data_resp_full  = Wire(Bool())

  val write_data_resp_count = Module(new FifoCount(6, 3))
  write_data_resp_count.io.wen <> write_data_resp_wen
  write_data_resp_count.io.ren <> write_data_resp_ren
  write_data_resp_count.io.empty <> write_data_resp_empty
  write_data_resp_count.io.full <> write_data_resp_full

  // SRAM inst request
  val inst_read_valid = Wire(Bool())
  val inst_related    = Wire(Bool()) // TODO

  // SRAM inst response
  val inst_read_ready = Wire(Bool())

  // SRAM data request
  val data_read_valid  = Wire(Bool())
  val data_write_valid = Wire(Bool())
  val data_related     = Wire(Bool())

  // request record
  val data_req_record_wen       = Wire(Bool())
  val data_req_record_ren       = Wire(Bool())
  val data_req_record_empty     = Wire(Bool())
  val data_req_record_full      = Wire(Bool())
  val data_req_record_related_1 = Wire(Bool())
  val data_req_record_input     = Wire(UInt(33.W)) // {wr, addr}
  val data_req_record_output    = Wire(UInt(33.W))

  // FIFO buffer for request record
  val data_req_record = Module(new FifoBufferValid(33, 6, 3, 32))
  data_req_record.io.wen <> data_req_record_wen
  data_req_record.io.ren <> data_req_record_ren
  data_req_record.io.empty <> data_req_record_empty
  data_req_record.io.full <> data_req_record_full
  data_req_record.io.related_1 <> data_req_record_related_1
  data_req_record.io.input <> data_req_record_input
  data_req_record.io.output <> data_req_record_output
  data_req_record.io.related_data_1 <> io.dataMemory.addr

  // SRAM data response
  val data_read_ready  = Wire(Bool())
  val data_write_ready = Wire(Bool())

  // AXI read request
  when(!axi_ar_busy && read_req_valid) {
    axi_ar_busy := true.B
    axi_ar_id   := read_req_id
    axi_ar_addr := read_req_addr
    axi_ar_size := read_req_size
  }.elsewhen(axi_ar_busy && io.axi.ar.valid && io.axi.ar.ready) {
    axi_ar_busy := false.B
    axi_ar_id   := 0.U
    axi_ar_addr := 0.U
    axi_ar_size := 0.U
  }

  io.axi.ar.valid := axi_ar_busy
  io.axi.ar.id    := axi_ar_id
  io.axi.ar.addr  := axi_ar_addr
  io.axi.ar.size  := axi_ar_size

  // AXI read response
  axi_r_data_ok := io.axi.r.valid && io.axi.r.ready && io.axi.r.id === DATA_ID
  axi_r_inst_ok := io.axi.r.valid && io.axi.r.ready && io.axi.r.id === INST_ID
  axi_r_data    := io.axi.r.data

  io.axi.r.ready := !read_inst_resp_full && !read_data_resp_full

  // AXI write request
  when(!axi_aw_busy && !axi_w_busy && write_req_valid) {
    axi_aw_busy := true.B
    axi_aw_addr := write_req_addr
    axi_aw_size := write_req_size
  }.elsewhen(axi_aw_busy && io.axi.aw.valid && io.axi.aw.ready) {
    axi_aw_busy := false.B
    axi_aw_addr := 0.U(32.W)
    axi_aw_size := 3.U(3.W)
  }

  when(!axi_aw_busy && !axi_w_busy && write_req_valid) {
    axi_w_busy := true.B
    axi_w_data := write_req_data
    axi_w_strb := write_req_strb
  }.elsewhen(axi_w_busy && io.axi.w.valid && io.axi.w.ready) {
    axi_w_busy := false.B
    axi_w_data := 0.U(32.W)
    axi_w_strb := 0.U(4.W)
  }
  io.axi.aw.valid := axi_aw_busy
  io.axi.w.valid  := axi_w_busy
  io.axi.aw.addr  := axi_aw_addr
  io.axi.aw.size  := axi_aw_size
  io.axi.w.data   := axi_w_data
  io.axi.w.strb   := axi_w_strb

  // AXI write response
  axi_b_ok       := io.axi.b.valid && io.axi.b.ready
  io.axi.b.ready := !write_data_resp_full

  // Middle read request
  read_req_sel_data := data_read_valid
  read_req_sel_inst := !data_read_valid && inst_read_valid

  // To AXI
  read_req_valid := inst_read_valid || data_read_valid
  read_req_id    := Mux(read_req_sel_data, DATA_ID, INST_ID)
  read_req_addr  := Mux(read_req_sel_data, io.dataMemory.addr, io.instMemory.addr)
  read_req_size  := Mux(read_req_sel_data, io.dataMemory.size, io.instMemory.size)

  // To SRAM
  read_data_req_ok := read_req_sel_data && !axi_ar_busy
  read_inst_req_ok := read_req_sel_inst && !axi_ar_busy

  // Middle read inst response
  read_inst_resp_ren   := inst_read_ready
  read_inst_resp_wen   := axi_r_inst_ok
  read_inst_resp_input := axi_r_data

  // Middle read data response
  read_data_resp_ren   := data_read_ready
  read_data_resp_wen   := axi_r_data_ok
  read_data_resp_input := axi_r_data

  // middle write request
  // to axi
  write_req_valid := data_write_valid
  write_req_addr  := io.dataMemory.addr
  write_req_size  := io.dataMemory.size
  write_req_data  := io.dataMemory.wdata
  write_req_strb  := io.dataMemory.wstrb

  // to sram
  write_data_req_ok := data_write_valid && !axi_aw_busy && !axi_w_busy

  // middle write response
  write_data_resp_ren := data_write_ready
  write_data_resp_wen := axi_b_ok

  // SRAM inst request
  inst_read_valid       := io.instMemory.req && !io.instMemory.wr && !inst_related
  io.instMemory.addr_ok := read_inst_req_ok
  inst_related          := false.B

  // SRAM inst response
  inst_read_ready       := true.B
  io.instMemory.data_ok := !read_inst_resp_empty
  io.instMemory.rdata   := read_inst_resp_output

  // SRAM data request
  data_related          := data_req_record_related_1
  data_read_valid       := io.dataMemory.req && !io.dataMemory.wr && !data_related
  data_write_valid      := io.dataMemory.req && io.dataMemory.wr && !data_related
  io.dataMemory.addr_ok := read_data_req_ok || write_data_req_ok

  // request record
  data_req_record_ren   := io.dataMemory.data_ok
  data_req_record_wen   := io.dataMemory.req && io.dataMemory.addr_ok
  data_req_record_input := Cat(io.dataMemory.wr, io.dataMemory.addr)

  // SRAM data response
  io.dataMemory.rdata := read_data_resp_output
  data_read_ready     := !data_req_record_empty && !data_req_record_output(32)
  data_write_ready    := !data_req_record_empty && data_req_record_output(32)

  io.dataMemory.data_ok := (data_read_ready && !read_data_resp_empty) || (data_write_ready && !write_data_resp_empty)
}
