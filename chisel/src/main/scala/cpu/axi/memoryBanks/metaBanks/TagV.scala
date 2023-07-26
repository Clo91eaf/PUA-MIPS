package cache.memoryBanks.metaBanks

import chisel3._
import chisel3.util._
import cache.CacheConfig
import cache.memoryBanks.SimpleDualPortRam

class TagV(implicit cacheConfig: CacheConfig) extends Module {
  val nset: Int = cacheConfig.nset
  val nway: Int = cacheConfig.nway
  val io = IO(new Bundle {
    val way = Vec(
      nway,
      new Bundle {
        val r = new ReadOnlyPort(UInt(cacheConfig.tagvWidth.W))
        val w = new WriteOnlyPort(UInt(cacheConfig.tagvWidth.W))
      },
    )
  })

  for (i <- 0 until nway) {
    val tagvBank = Module(new SimpleDualPortRam(nset, 21, false))
    tagvBank.suggestName(s"tag_bank_way_$i")

    // port R
    tagvBank.io.ren   := true.B
    tagvBank.io.raddr := io.way(i).r.addr
    io.way(i).r.data  := tagvBank.io.rdata
    // port W
    tagvBank.io.waddr := io.way(i).w.addr
    tagvBank.io.wen   := io.way(i).w.en
    tagvBank.io.wdata := io.way(i).w.data
    tagvBank.io.wstrb := io.way(i).w.en
  }
}
