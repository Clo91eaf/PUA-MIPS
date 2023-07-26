package cache.memoryBanks

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cache.memip.SDPRamIP
import firrtl.options.TargetDirAnnotation

/** simple dual port ram, with a port for reading and a port for writing
  *
  * @param depth
  *   how many lines there are in the ram
  * @param width
  *   how wide in bits each line is
  * @param byteAddressable
  *   is it byte addressable?
  * @param cpuCfg
  *   the implicit configuration for simulation and elaboration
  */
class SimpleDualPortRam(depth: Int, width: Int, byteAddressable: Boolean) extends Module {
  require(isPow2(depth))
  require(
    width % 8 == 0 || !byteAddressable,
    "if memory is byte addressable, then the adderss width must be a multiple of 8",
  )
  val waddridth = log2Ceil(depth)

  val io = IO(new Bundle {
    val raddr = Input(UInt(waddridth.W))
    val ren   = Input(Bool())
    val rdata = Output(UInt(width.W))

    val waddr = Input(UInt(waddridth.W))
    val wen   = Input(Bool())
    val wstrb = Input(UInt((if (byteAddressable) width / 8 else 1).W))
    val wdata = Input(UInt(width.W))
  })

  if (false) {
    val memory = Module(
      new SDPRamIP(
        wdataidth = width,
        byteWriteWidth = if (byteAddressable) 8 else width,
        numberOfLines = depth,
        waddridth = waddridth,
      ),
    )
    memory.io.clka := clock
    memory.io.clkb := clock
    memory.io.rsta := reset
    memory.io.rstb := reset

    memory.io.addra := io.waddr
    memory.io.ena   := io.wen
    memory.io.dina  := io.wdata
    memory.io.wea   := io.wstrb

    memory.io.addrb  := io.raddr
    memory.io.enb    := io.ren
    memory.io.regceb := false.B
    io.rdata         := memory.io.doutb
  } else {
    assert(
      io.wstrb.orR || !io.wen,
      "when write port enable is high, write vector cannot be all 0",
    )
    if (byteAddressable) {
      val bank = SyncReadMem(depth, Vec(width / 8, UInt(8.W)))
      when(io.ren) {
        io.rdata := bank(io.raddr).asTypeOf(io.rdata)
      }.otherwise {
        io.rdata := DontCare
      }
      when(io.wen) {
        bank.write(io.waddr, io.wdata.asTypeOf(Vec(width / 8, UInt(8.W))), io.wstrb.asBools)
      }
    } else {
      val bank = SyncReadMem(depth, UInt(width.W))

      when(io.ren) {
        io.rdata := bank.read(io.raddr)
      }.otherwise {
        io.rdata := 0.U(32.W)
      }

      when(io.wen) {
        bank.write(io.waddr, io.wdata)
      }
    }
  }
}

object SimpleDualPortRamEla extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(
      ChiselGeneratorAnnotation(() => new SimpleDualPortRam(32, 32, false)),
      TargetDirAnnotation("generation"),
    ),
  )
}
