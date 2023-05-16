package cpu.axi

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class DataMemory extends Module {
  val io = IO(new Bundle {
    val fromExecute        = Flipped(new Execute_DataMemory())
    val fromMemory         = Flipped(new Memory_DataMemory())
    val fromCtrl           = Flipped(new Ctrl_DataMemory())
    val fromDataMMU        = Flipped(new MMU_Sram())

    val execute      = new DataMemory_Execute()
    val memory       = new DataMemory_Memory()
    val sramAXITrans = new DataMemory_SramAXITrans()
    val ctrl         = new DataMemory_Ctrl()
  })
  val req         = io.fromExecute.req
  val aluop       = io.fromExecute.aluop
  val addrLowBit2 = io.fromExecute.addrLowBit2
  val wr          = io.fromExecute.wr
  val size        = io.fromExecute.size
  val addr        = io.fromDataMMU.paddr
  val wdata       = io.fromExecute.wdata
  val wstrb       = io.fromExecute.wstrb
  val es_waiting  = io.fromExecute.waiting
  val ms_waiting  = io.fromMemory.waiting
  val addr_ok     = io.sramAXITrans.addr_ok
  val data_ok     = io.sramAXITrans.data_ok
  val rdata       = io.sramAXITrans.rdata

  val read_mask = MuxLookup(
    aluop,
    "hffffffff".U,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h000000ff".U,
          "b01".U -> "h0000ff00".U,
          "b10".U -> "h00ff0000".U,
          "b11".U -> "hff000000".U,
        ),
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h000000ff".U,
          "b01".U -> "h0000ff00".U,
          "b10".U -> "h00ff0000".U,
          "b11".U -> "hff000000".U,
        ),
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h0000ffff".U,
          "b10".U -> "hffff0000".U,
        ),
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        "hffffffff".U,
        Seq(
          "b00".U -> "h0000ffff".U,
          "b10".U -> "hffff0000".U,
        ),
      ),
    ),
  )
  val read_mask_next = RegNext(read_mask)

  val data_sram_discard         = RegInit(0.U(2.W))
  val data_sram_data_ok_discard = addr_ok && ~(data_sram_discard.orR)
  // data sram
  io.sramAXITrans.req       := req
  io.sramAXITrans.wr        := wr
  io.sramAXITrans.size      := size
  io.sramAXITrans.addr      := addr
  io.sramAXITrans.wstrb     := wstrb
  io.sramAXITrans.wdata     := wdata
  io.memory.rdata           := rdata & read_mask_next
  io.memory.data_ok         := ~data_sram_discard.orR && data_ok
  io.execute.rdata          := rdata & read_mask_next
  io.execute.addr_ok        := addr_ok
  io.execute.data_ok        := ~data_sram_discard.orR && data_ok
  io.ctrl.data_sram_discard := data_sram_discard

  when(io.fromCtrl.do_flush) {
    data_sram_discard := Cat(es_waiting, ms_waiting)
  }.elsewhen(data_ok) {
    when(data_sram_discard === 3.U) {
      data_sram_discard := 1.U
    }.elsewhen(data_sram_discard === 1.U || data_sram_discard === 2.U) {
      data_sram_discard := 0.U
    }
  }
}
