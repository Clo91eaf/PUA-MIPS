package cpu.puamips

import chisel3._
import chisel3.util._
import Const._

class DataMemory extends Module {
  val io = IO(new Bundle {
    val fromMemory = Flipped(new Memory_DataMemory())
    val memory = new DataMemory_Memory()
  })
  // input-memory
  val ce = Wire(Bool())
  val wen = Wire(Bool())
  val addr = Wire(DATA_ADDR_BUS)
  val sel = Wire(DATA_MEMORY_SEL_BUS)
  val data = RegInit(DATA_BUS_INIT)
  ce := io.fromMemory.ce
  wen := io.fromMemory.wen
  addr := io.fromMemory.addr
  sel := io.fromMemory.sel
  
  // output-memory
  io.memory.data := data

  val data_mem0 = Mem(DATA_MEM_NUM, BYTE_WIDTH)
  val data_mem1 = Mem(DATA_MEM_NUM, BYTE_WIDTH)
  val data_mem2 = Mem(DATA_MEM_NUM, BYTE_WIDTH)
  val data_mem3 = Mem(DATA_MEM_NUM, BYTE_WIDTH)

  when(ce === CHIP_DISABLE) {}.elsewhen(wen === WRITE_ENABLE) {
    when(sel(3) === true.B) {
      data_mem3(addr(DATA_MEM_NUM_LOG2 + 1, 2)) := io.fromMemory.data(31, 24)
    }
    when(sel(2) === true.B) {
      data_mem2(addr(DATA_MEM_NUM_LOG2 + 1, 2)) := io.fromMemory.data(23, 16)
    }
    when(sel(1) === true.B) {
      data_mem1(addr(DATA_MEM_NUM_LOG2 + 1, 2)) := io.fromMemory.data(15, 8)
    }
    when(sel(0) === true.B) {
      data_mem0(addr(DATA_MEM_NUM_LOG2 + 1, 2)) := io.fromMemory.data(7, 0)
    }
  }

  when(ce === CHIP_DISABLE) {
    data := ZERO_WORD
  }.elsewhen(wen === WRITE_DISABLE) {
    data := Cat(
      data_mem3(addr(DATA_MEM_NUM_LOG2 + 1, 2)),
      data_mem2(addr(DATA_MEM_NUM_LOG2 + 1, 2)),
      data_mem1(addr(DATA_MEM_NUM_LOG2 + 1, 2)),
      data_mem0(addr(DATA_MEM_NUM_LOG2 + 1, 2))
    )
  }.otherwise {
    data := ZERO_WORD
  }
}
