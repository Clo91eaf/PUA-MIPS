import cache._
import cpu._
import chisel3._
import chisel3.stage.ChiselStage
import firrtl.options.TargetDirAnnotation

object Elaborate extends App {
  implicit val config = new CpuConfig(build = false)
  (new ChiselStage).emitVerilog(new PuaMips(), Array("--target-dir", "generated"))
}
