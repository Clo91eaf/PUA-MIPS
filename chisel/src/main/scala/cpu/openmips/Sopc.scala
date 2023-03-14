package cpu.openmips

import chisel3._
import chisel3.util._
import cpu.openmips.Constants._

class Sopc extends Module {
  val io = IO(new Bundle() {
    val success = Output(Bool())
  })
  io.success := DontCare

  val inst_addr = Wire(InstAddrBus)
  val inst = Wire(InstBus)
  val rom_ce = Wire(Bool())

  val openmips0 = Module(new OpenMips)
  inst_addr := openmips0.io.rom_addr_o
  openmips0.io.rom_data_i := inst
  rom_ce := openmips0.io.rom_ce_o

  val inst_rom0 = Module(new Inst_rom)
  inst_rom0.io.ce := rom_ce
  inst_rom0.io.addr := inst_addr
  inst := inst_rom0.io.inst

}
