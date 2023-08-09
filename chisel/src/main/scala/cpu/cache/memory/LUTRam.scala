package cache.memory

import chisel3._
import chisel3.util._
import cpu.CacheConfig
import cpu.CpuConfig

/** LUT ram for XPM, one port for read/write, one port for read
  * @param depth
  *   how many lines there are in the bank
  * @param width
  *   how wide in bits each line is
  * @param config
  *   implicit configuration to control generate ram for simulation or elaboration
  */
class LUTRam(depth: Int, width: Int)(implicit val config: CpuConfig) extends Module {
  require(isPow2(depth))
  val waddridth = log2Ceil(depth)
  val io = IO(new Bundle {
    val raddr = Input(UInt(waddridth.W))
    val rdata = Output(UInt(width.W))

    val waddr       = Input(UInt(waddridth.W))
    val wdata       = Input(UInt(width.W))
    val wen         = Input(Bool())
    val writeOutput = Output(UInt(width.W))
  })

  if (config.build) {
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

    bank.io.addra  := io.waddr
    bank.io.wea    := io.wen
    bank.io.dina   := io.wdata
    io.writeOutput := DontCare

    bank.io.addrb := io.raddr
    io.rdata      := bank.io.doutb
  } else {
    val bank = RegInit(VecInit(Seq.fill(depth)(0.U(width.W))))
    io.rdata       := bank(io.raddr)
    io.writeOutput := DontCare
    when(io.wen) {
      bank(io.waddr) := io.wdata
    }.otherwise {
      io.writeOutput := bank(io.waddr)
    }
  }
}
