package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class CP0Reg extends Module {
  val io = IO(new Bundle {
    val fromWriteBackStage = Flipped(new WriteBackStage_CP0())

    val writeBackStage = new CP0_WriteBackStage()
  })
  // input-writeBack stage
  val wb_ex       = io.fromWriteBackStage.wb_ex
  val wb_bd       = io.fromWriteBackStage.wb_bd
  val eret_flush  = io.fromWriteBackStage.eret_flush
  val wb_excode   = io.fromWriteBackStage.wb_excode
  val wb_pc       = io.fromWriteBackStage.wb_pc
  val wb_badvaddr = io.fromWriteBackStage.wb_badvaddr
  val ext_int_in  = io.fromWriteBackStage.ext_int_in
  val cp0_addr    = io.fromWriteBackStage.cp0_addr
  val mtc0_we     = io.fromWriteBackStage.mtc0_we
  val cp0_wdata   = io.fromWriteBackStage.cp0_wdata

  // output-writeBack stage
  val cp0_rdata    = Wire(UInt(32.W))
  val cp0_status   = Wire(UInt(32.W))
  val cp0_cause    = Wire(UInt(32.W))
  val cp0_epc      = Wire(UInt(32.W))
  val cp0_badvaddr = Wire(UInt(32.W))
  val cp0_count    = Wire(UInt(32.W))
  val cp0_compare  = Wire(UInt(32.W))
  io.writeBackStage.cp0_rdata    := cp0_rdata
  io.writeBackStage.cp0_status   := cp0_status
  io.writeBackStage.cp0_cause    := cp0_cause
  io.writeBackStage.cp0_epc      := cp0_epc
  io.writeBackStage.cp0_badvaddr := cp0_badvaddr
  io.writeBackStage.cp0_count    := cp0_count
  io.writeBackStage.cp0_compare  := cp0_compare

  // CP0_STATUS
  val cp0_status_bev = true.B

  val cp0_status_im = RegInit(0.U(8.W))
  when(mtc0_we && cp0_addr === CP0_STATUS_ADDR) {
    cp0_status_im := cp0_wdata(15, 8)
  }

  val cp0_status_exl = RegInit(false.B)
  when(wb_ex) {
    cp0_status_exl := true.B
  }.elsewhen(eret_flush) {
    cp0_status_exl := false.B
  }.elsewhen(
    mtc0_we && (cp0_addr === CP0_STATUS_ADDR),
  ) {
    cp0_status_exl := cp0_wdata(1)
  }

  val cp0_status_ie = RegInit(false.B)
  when(mtc0_we && (cp0_addr === CP0_STATUS_ADDR)) {
    cp0_status_ie := cp0_wdata(0)
  }

  cp0_status := Cat(
    0.U(9.W),       // 31:23
    cp0_status_bev, // 22:22
    0.U(6.W),       // 21:16
    cp0_status_im,  // 15:8
    0.U(6.W),       // 7:2
    cp0_status_exl, // 1:1
    cp0_status_ie,  // 0:0
  )

  val cp0_cause_bd = RegInit(false.B)
  when(wb_ex && !cp0_status_exl) {
    cp0_cause_bd := wb_bd
  }

  val cp0_cause_ti     = RegInit(false.B)
  val count_eq_compare = (cp0_count === cp0_compare)

  when(mtc0_we && (cp0_addr === CP0_COMP_ADDR)) {
    cp0_cause_ti := false.B
  }.elsewhen(count_eq_compare) {
    cp0_cause_ti := true.B
  }

  val cp0_cause_ip = RegInit(0.U(8.W))
  cp0_cause_ip := Cat(
    (ext_int_in(5) || cp0_cause_ti), // 7:7
    ext_int_in(4, 0),                // 6:2
    cp0_cause_ip(1, 0),              // 1:0
  )

  when(mtc0_we && cp0_addr === CP0_CAUSE_ADDR) {
    cp0_cause_ip := Cat(
      cp0_cause_ip(7, 2), // 7:2
      cp0_wdata(9, 8),    // 1:0
    )
  }

  val cp0_cause_excode = RegInit(0.U(5.W))
  when(wb_ex) {
    cp0_cause_excode := wb_excode
  }

  cp0_cause := Cat(
    cp0_cause_bd,     // 31:31
    cp0_cause_ti,     // 30:30
    0.U(14.W),        // 29:16
    cp0_cause_ip,     // 15:8
    false.B,          // 7:7
    cp0_cause_excode, // 6:2
    0.U(2.W),         // 1:0
  )

  // EPC
  val c0_epc = RegInit(0.U(32.W))
  when(wb_ex && !cp0_status_exl) {
    c0_epc := Mux(wb_bd, wb_pc - 4.U, wb_pc)
  }.elsewhen(mtc0_we && cp0_addr === CP0_EPC_ADDR) {
    c0_epc := cp0_wdata
  }

  cp0_epc := c0_epc

  // BADVADDR
  val c0_badvaddr = RegInit(0.U(32.W))
  when(wb_ex && wb_excode === CP0_BADV_ADDR) {
    c0_badvaddr := wb_badvaddr
  }

  cp0_badvaddr := c0_badvaddr

  // COUNT
  val tick = RegInit(false.B)
  tick := !tick

  val c0_count = RegInit(0.U(32.W))
  when(mtc0_we && cp0_addr === CP0_COUNT_ADDR) {
    c0_count := cp0_wdata
  }.elsewhen(tick) {
    c0_count := c0_count + 1.U
  }

  cp0_count := c0_count

  // COMPARE
  val c0_compare = RegInit(0.U(32.W))
  when(mtc0_we && cp0_addr === CP0_COMP_ADDR) {
    c0_compare := cp0_wdata
  }

  cp0_compare := c0_compare

  cp0_rdata := MuxLookup(
    cp0_addr,
    ZERO_WORD,
    Seq(
      CP0_STATUS_ADDR -> cp0_status,
      CP0_CAUSE_ADDR  -> cp0_cause,
      CP0_EPC_ADDR    -> cp0_epc,
      CP0_BADV_ADDR   -> cp0_badvaddr,
      CP0_COUNT_ADDR  -> cp0_count,
      CP0_COMP_ADDR   -> cp0_compare,
    ),
  )
}
