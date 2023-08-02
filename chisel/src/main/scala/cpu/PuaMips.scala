import chisel3._
import chisel3.util._
import cache._
import cpu._
import cpu.defines._

class PuaMips extends Module {
  implicit val cpuConfig = new CpuConfig(hasCommitBuffer = true)
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
    val axi     = new AXI()
    val debug   = new DEBUG()
  })
  val core  = Module(new Core())
  val cache = Module(new Cache())

  core.io.inst <> cache.io.inst
  core.io.data <> cache.io.data

  io.ext_int <> core.io.ext_int
  io.debug <> core.io.debug
  io.axi <> cache.io.axi
}
