package cpu.defines

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig

class SocStatic extends Bundle {
  val cp0_count  = Output(UInt(32.W))
  val cp0_random = Output(UInt(32.W))
  val cp0_cause  = Output(UInt(32.W))
  val int        = Output(Bool())
  val commit     = Output(Bool())
}

class BranchPredictorUnitStatic extends Bundle {
  val branch = Output(Bool())
  val failed = Output(Bool())
}

class GlobalStatic extends Bundle {
  val soc = new SocStatic()
  val bpu = new BranchPredictorUnitStatic()
}