import cache._
import chisel3._
import chisel3.stage.ChiselStage
import firrtl.options.TargetDirAnnotation
import cpu.CpuConfig

object Elaborate extends App {
  (new ChiselStage).emitVerilog(new Cache(), Array("--target-dir", "generated"))
}
