package cache.memoryBanks.metaBanks

import chisel3._
import cache.CacheConfig
import cache.memoryBanks.metaBanks.ReadWritePort
import cache.memoryBanks.SinglewRam
import cache.memoryBanks.SimpleDualPortRam

class Bank(
    byteAddressable: Boolean = false,
)(implicit
    cacheConfig: CacheConfig,
) extends Module {
  val io = IO(new Bundle {
    val way = Vec(
      cacheConfig.nway,
      new Bundle {
        val r = new ReadOnlyPort(UInt(cacheConfig.bankWidthBits.W))
        val w = new WriteOnlyMaskPort(UInt(cacheConfig.bankWidthBits.W))
      },
    )
  })
  for { i <- 0 until cacheConfig.nway } yield {
    val bank = Module(
      new SimpleDualPortRam(cacheConfig.nset * cacheConfig.nbank, cacheConfig.bankWidthBits, byteAddressable),
    )
    bank.suggestName(s"bank_${i}")

    // portR
    bank.io.ren      := true.B // always read
    bank.io.raddr    := io.way(i).r.addr
    io.way(i).r.data := bank.io.rdata

    // portW
    bank.io.wen   := io.way(i).w.en.orR
    bank.io.waddr := io.way(i).w.addr
    bank.io.wdata := io.way(i).w.data
    bank.io.wstrb := io.way(i).w.en
  }
}
