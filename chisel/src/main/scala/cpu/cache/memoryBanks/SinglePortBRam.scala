package cache.memoryBanks

import chisel3._
import chisel3.stage.{ChiselGeneratorAnnotation, ChiselStage}
import chisel3.util._
import cache.memip.SinglePortRamIP
import firrtl.options.TargetDirAnnotation

class SinglewRam(depth: Int, width: Int = 32, byteAddressable: Boolean) extends Module {
  require(isPow2(depth))
  require(
    width % 8 == 0 || !byteAddressable,
    "if memory is byte addressable, then the adderss width must be a multiple of 8",
  )
  val waddridth = log2Ceil(depth)

  val io = IO(new Bundle {
    val addr  = Input(UInt(waddridth.W))
    val en    = Input(Bool())
    val wstrb = Input(UInt((if (byteAddressable) width / 8 else 1).W))
    val wdata = Input(UInt(width.W))
    val rdata = Output(UInt(width.W))
  })

  if (false) {
    val bank = Module(
      new SinglePortRamIP(
        wdataidth = width,
        byteWriteWidth = if (byteAddressable) 8 else width,
        waddridth = waddridth,
        numberOfLines = depth,
      ),
    )
    bank.io.clka   := clock
    bank.io.rsta   := reset
    bank.io.addra  := io.addr
    bank.io.dina   := io.wdata
    bank.io.ena    := io.en
    bank.io.wea    := io.wstrb
    io.rdata       := bank.io.douta
    bank.io.regcea := false.B
  } else {
    if (byteAddressable) {
      val bank = SyncReadMem(depth, Vec(width / 8, UInt(8.W)))
      io.rdata := DontCare
      when(io.en) {
        when(io.wstrb.orR) {
          bank.write(
            io.addr,
            io.wdata.asTypeOf(Vec(width / 8, UInt(8.W))),
            io.wstrb.asBools(),
          )
        }.otherwise {
          io.rdata := bank(io.addr).asUInt
        }
      }
    } else {
      val bank = SyncReadMem(depth, UInt(width.W))
      io.rdata := DontCare
      when(io.en) {
        when(io.wstrb.asBool) {
          bank(io.addr) := io.wdata
        }.otherwise {
          io.rdata := bank(io.addr).asUInt
        }
      }
    }
  }

}

object SinglewRamElaborate extends App {
  (new ChiselStage).execute(
    Array(),
    Seq(
      ChiselGeneratorAnnotation(() => new SinglewRam(32, 32, false)),
      TargetDirAnnotation("generation"),
    ),
  )
}
