package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig

class SocStatistic extends Bundle {
  val cp0_count  = Output(UInt(32.W))
  val cp0_random = Output(UInt(32.W))
  val cp0_cause  = Output(UInt(32.W))
  val int        = Output(Bool())
  val commit     = Output(Bool())
}

class BranchPredictorUnitStatistic extends Bundle {
  val branch   = Output(UInt(32.W))
  val failed   = Output(UInt(32.W))
  val instInfo = Output(new InstInfo())
  val isBranch = Output(Bool())
  val success  = Output(Bool())
}

class CPUStatistic extends Bundle {
  val soc = new SocStatistic()
  val bpu = new BranchPredictorUnitStatistic()
}

class GlobalStatistic extends Bundle {
  val cpu   = new CPUStatistic()
  val cache = new CacheStatistic()
}

class ICacheStatistic extends Bundle {
  val request = Output(UInt(32.W))
  val hit    = Output(UInt(32.W))
}

class DCacheStatistic extends Bundle {
  val request = Output(UInt(32.W))
  val hit    = Output(UInt(32.W))
}

class CacheStatistic extends Bundle {
  val icache = new ICacheStatistic()
  val dcache = new DCacheStatistic()
}
