package cpu

import chisel3.stage.ChiselStage
import firrtl.options.TargetDirAnnotation

object elaborateCPU extends App {
  (new ChiselStage).emitVerilog(new CPUTop(), args)
}