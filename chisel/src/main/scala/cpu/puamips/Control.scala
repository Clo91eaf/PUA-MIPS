package cpu.puamips

import Const._
import chisel3._
import chisel3.util._

class Control extends Module {
  val io = IO(new Bundle {
    val fromDecoder = Flipped(new Decoder_Control())
    val fromExecute = Flipped(new Execute_Control())
    val fromMemory = Flipped(new Memory_Control())

    val fetch = new Control_Fetch()
    val decoderStage = new Control_DecoderStage()
    val executeStage = new Control_ExecuteStage()
    val memoryStage = new Control_MemoryStage()
    val writeBackStage = new Control_WriteBackStage()
    val llbitReg = new Control_LLbitReg()
    val divider = new Control_Divider()
  })
  val stall = Wire(STALL_BUS)
  io.fetch.stall := stall
  io.decoderStage.stall := stall
  io.executeStage.stall := stall
  io.memoryStage.stall := stall
  io.writeBackStage.stall := stall
  val new_pc = Wire(REG_BUS)
  io.fetch.new_pc := new_pc
  val flush = Wire(Bool())
  io.decoderStage.flush := flush
  io.divider.flush := flush
  io.executeStage.flush := flush
  io.fetch.flush := flush
  io.llbitReg.flush := flush
  io.memoryStage.flush := flush
  io.writeBackStage.flush := flush

  //INIT
  new_pc := PC_INIT // liphen

  when(reset.asBool === RST_ENABLE) {
    stall := "b000000".U
    flush := false.B
    new_pc := PC_INIT
  }.elsewhen(io.fromMemory.excepttype =/= ZERO_WORD) {
    flush := true.B
    stall := "b000000".U
    switch(io.fromMemory.excepttype) {
      is("h00000001".U) { // interrupt
        new_pc := "h00000020".U
      }
      is("h00000008".U) { // syscall
        new_pc := "h00000040".U
      }
      is("h0000000a".U) { // inst_invalid
        new_pc := "h00000040".U
      }
      is("h0000000d".U) { // trap
        new_pc := "h00000040".U
      }
      is("h0000000c".U) { // ov
        new_pc := "h00000040".U
      }
      is("h0000000e".U) { // eret
        new_pc := io.fromMemory.cp0_epc
      }
    }
  }.elsewhen(io.fromExecute.stallreq === STOP) {
    stall := "b001111".U
    flush := false.B
  }.elsewhen(io.fromDecoder.stallreq === STOP) {
    stall := "b000111".U
    flush := false.B
  }.otherwise {
    stall := "b000000".U
    flush := false.B
    new_pc := PC_INIT
  }

  // debug
  // printf(p"control :stall 0x${Hexadecimal(stall)}\n")
}
