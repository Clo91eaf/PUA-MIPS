package cache.memoryBanks

import chisel3._
import chisel3.util._
import cache.memip.TDPRamIP

class TDPRamBundle(waddridth: Int, width: Int, byteAddressable: Boolean) extends Bundle {
  val portEnable = Input(Bool())
  val addr       = Input(UInt(waddridth.W))
  val wdata      = Input(UInt(width.W))
  val wstrb      = Input(UInt((if (byteAddressable) width / 8 else 1).W))
  val rdata      = Output(UInt(width.W))
}

/** true dual port ram from XPM 2019.2
  *
  * @param depth
  *   how many lines are in this ram
  * @param width
  *   how many bits per line
  * @param byteAddressable
  *   is it byte addressable or does it have a single write enable
  * @param cpuCfg
  *   whether to generate XPM for FPGA or to generate chisel memory for simualtion
  */
class TrueDualPortRam(depth: Int, width: Int, byteAddressable: Boolean) extends Module {
  require(isPow2(depth))
  require(
    width % 8 == 0 || !byteAddressable,
    "if memory is byte addressable, then the adderss width must be a multiple of 8",
  )
  val waddridth = log2Ceil(depth)

  val io = IO(new Bundle {
    val r = new TDPRamBundle(waddridth, width, byteAddressable)
    val w = new TDPRamBundle(waddridth, width, byteAddressable)
  })

  if (false) {
    val memory = Module(
      new TDPRamIP(
        wdataidth = width,
        byteWriteWidth = if (byteAddressable) 8 else width,
        waddridth = waddridth,
        numberOfLines = depth,
      ),
    )
    memory.io.clka := clock
    memory.io.clkb := clock
    memory.io.rsta := reset
    memory.io.rstb := reset

    memory.io.addra  := io.r.addr
    memory.io.dina   := io.r.wdata
    memory.io.ena    := io.r.portEnable
    memory.io.regcea := false.B
    memory.io.wea    := io.r.wstrb
    io.r.rdata       := memory.io.douta

    memory.io.addrb  := io.w.addr
    memory.io.dinb   := io.w.wdata
    memory.io.enb    := io.w.portEnable
    memory.io.regceb := false.B
    memory.io.web    := io.w.wstrb
    io.w.rdata       := memory.io.doutb
  } else {
    assert(
      io.r.wstrb.orR || !io.r.portEnable,
      "when write port enable is high, write vector cannot be all 0",
    )
    assert(
      !(io.r.addr === io.w.addr && io.r.portEnable && io.w.portEnable && (io.r.wstrb.orR || io.w.wstrb.orR)),
      "there has been an request collision",
    )
    if (byteAddressable) {
      val bank = SyncReadMem(depth, Vec(width / 8, UInt(8.W)))
      when(io.r.portEnable) {
        when(io.r.wstrb.orR) {
          bank.write(
            io.r.addr,
            io.r.wdata.asTypeOf(Vec(width / 8, UInt(8.W))),
            io.r.wstrb.asBools(),
          )
        }.otherwise {
          io.r.rdata := bank(io.r.addr)
        }
      }
      when(io.w.portEnable) {
        when(io.w.wstrb.orR) {
          bank.write(
            io.w.addr,
            io.w.wdata.asTypeOf(Vec(width / 8, UInt(8.W))),
            io.w.wstrb.asBools(),
          )
        }.otherwise {
          io.w.rdata := bank(io.w.addr)
        }
      }
    } else {
      val bank = SyncReadMem(depth, UInt(width.W))
      when(io.r.portEnable) {
        when(io.r.wstrb.asBool) {
          bank(io.r.addr) := io.r.wdata
        }.otherwise {
          io.r.rdata := bank(io.r.addr)
        }
      }
      when(io.w.portEnable) {
        when(io.w.wstrb.asBool) {
          bank(io.w.addr) := io.w.wdata
        }.otherwise {
          io.w.rdata := bank(io.w.addr)
        }
      }
    }
  }
}
