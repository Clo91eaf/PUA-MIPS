package cache.memoryBanks

import chisel3._
import chisel3.util._
import cache.memip.LUTRamIP
import cache.CacheConfig

/** LUT ram for XPM, one port for read/write, one port for read
  * @param depth
  *   how many lines there are in the bank
  * @param width
  *   how wide in bits each line is
  * @param cpuCFG
  *   implicit configuration to control generate ram for simulation or elaboration
  */
class LUTRam(depth: Int, width: Int, wayNum: Int) extends Module {
  require(isPow2(depth))
  val waddridth = log2Ceil(depth)
  val io = IO(new Bundle {
    val readAddr = Input(UInt(waddridth.W))
    val rdata    = Output(UInt(width.W))

    val writeAddr   = Input(UInt(waddridth.W))
    val wdata       = Input(UInt(width.W))
    val en          = Input(Bool())
    val writeOutput = Output(UInt(width.W))
  })

  if (false) {
    val bank = Module(
      new LUTRamIP(
        wdataidth = width,
        waddridth = waddridth,
        byteWriteWidth = width,
        numberOfLines = depth,
      ),
    )
    bank.io.clka := clock
    bank.io.clkb := clock
    bank.io.rsta := reset
    bank.io.rstb := reset

    bank.io.regcea := false.B
    bank.io.regceb := false.B
    bank.io.ena    := true.B
    bank.io.enb    := true.B

    bank.io.addra  := io.writeAddr
    bank.io.wea    := io.en
    bank.io.dina   := io.wdata
    io.writeOutput := bank.io.douta

    bank.io.addrb := io.readAddr
    io.rdata      := bank.io.doutb
  } else {
    if (false) {
      val bank = RegInit(VecInit(Seq.fill(depth)(0.U(width.W))))
      io.rdata       := bank(io.readAddr)
      io.writeOutput := DontCare
      when(io.en) {
        bank(io.writeAddr) := io.wdata
      }.otherwise {
        io.writeOutput := bank(io.writeAddr)
      }
    } else {
      val bank = RegInit(VecInit(Seq.tabulate(depth)(i => (wayNum * 2 + i).U(width.W))))
      io.rdata       := bank(io.readAddr)
      io.writeOutput := DontCare
      when(io.en) {
        bank(io.writeAddr) := io.wdata
      }.otherwise {
        io.writeOutput := bank(io.writeAddr)
      }
    }
  }
}
