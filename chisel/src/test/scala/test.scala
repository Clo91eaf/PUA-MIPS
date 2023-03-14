package cpu.openmips
import chisel3._
import chisel3.stage.{ChiselStage,ChiselGeneratorAnnotation,ChiselMain}

object demotest extends App {
    (new ChiselStage).emitVerilog(new Sopc)
    ChiselMain.main(Array("-X","verilog","-e","verilog","--module","cpu.openmips.Sopc"))
    new ChiselStage().execute(Array.empty, Seq(ChiselGeneratorAnnotation(() => new Sopc)))
}