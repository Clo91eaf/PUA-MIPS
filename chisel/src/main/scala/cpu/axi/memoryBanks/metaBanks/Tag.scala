package cache.memoryBanks.metaBanks

import chisel3._
import chisel3.util._
import cache.CacheConfig
import cache.memoryBanks.SimpleDualPortRam

class Tag(implicit cacheConfig: CacheConfig) extends Module {
  val io = IO(new Bundle {
    val way = Vec(
      cacheConfig.nway,
      new Bundle {
        val r = new ReadOnlyPort(UInt(cacheConfig.tagWidth.W))
        val w = new WriteOnlyPort(UInt(cacheConfig.tagWidth.W))
      },
    )
  })

  for (i <- 0 until cacheConfig.nway) {
    val tag = Module(new SimpleDualPortRam(cacheConfig.nset, cacheConfig.tagWidth, false))
    tag.suggestName(s"tag_$i")

    // port R
    tag.io.ren       := true.B
    tag.io.raddr     := io.way(i).r.addr
    io.way(i).r.data := tag.io.rdata
    // port W
    tag.io.waddr := io.way(i).w.addr
    tag.io.wen   := io.way(i).w.en
    tag.io.wdata := io.way(i).w.data
    tag.io.wstrb := io.way(i).w.en
  }
}
