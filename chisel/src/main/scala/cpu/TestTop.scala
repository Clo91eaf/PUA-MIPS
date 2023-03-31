import chisel3._
import chisel3.util._
import cpu.puamips._
import cpu.puamips.Const._

class CPUTop extends Module {
  // val inst_addr = Wire(INST_ADDR_BUS)
  // val inst = Wire(INST_BUS)
  // val rom_ce = Wire(Bool())

  val puamips = Module(new PuaMips())
  // inst_addr := puamips0.io.rom_addr_o
  // puamips0.io.rom_data_i := inst
  // rom_ce := puamips0.io.rom_ce_o

  val instMemory = Module(new InstMemory())
  // inst_rom0.io.ce := rom_ce
  // inst_rom0.io.addr := inst_addr
  // inst := inst_rom0.io.inst

}
