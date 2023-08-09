package cache.memory

import chisel3._
import chisel3.util._
import cpu.CacheConfig

class ReadOnlyPort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.nset * cacheConfig.nbank).W))
  val data = Output(gen)
}

class WriteOnlyPort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.nset * cacheConfig.nbank).W))
  val en   = Input(Bool())
  val data = Input(gen)
}

class WriteOnlyMaskPort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr = Input(UInt(log2Ceil(cacheConfig.nset * cacheConfig.nbank).W))
  val en   = Input(UInt(cacheConfig.bankWidth.W))
  val data = Input(gen)
}


class ReadWritePort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr  = Input(UInt(log2Ceil(cacheConfig.nset * cacheConfig.nbank).W))
  val en    = Input(Bool())
  val wdata = Input(gen)
  val rdata = Output(gen)
}

class MaskedReadWritePort[+T <: Data](gen: T)(implicit cacheConfig: CacheConfig) extends Bundle {
  val addr      = Input(UInt(log2Ceil(cacheConfig.nset * cacheConfig.nbank).W))
  val writeMask = Input(UInt(cacheConfig.bankWidth.W))
  val wdata     = Input(gen)
  val rdata     = Output(gen)
}
