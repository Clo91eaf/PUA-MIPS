package cpu.pipeline.decoder

import chisel3._
import chisel3.util._

import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class ForwardCtrl(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val in = Input(new Bundle {
      val forward = Vec(config.fuNum, new DataForwardToDecoderUnit())
      val regfile = Vec(config.decoderNum, new Src12Read())
    })
    val out = Output(new Bundle {
      val inst = Vec(config.decoderNum, new Src12Read())
    })
  })

  // wb优先度最低
  for (i <- 0 until (config.decoderNum)) {
    io.out.inst(i).src1.raddr := DontCare
    io.out.inst(i).src2.raddr := DontCare
    io.out.inst(i).src1.rdata := io.in.regfile(i).src1.rdata
    io.out.inst(i).src2.rdata := io.in.regfile(i).src2.rdata
  }

  // mem优先度中
  for (i <- 0 until (config.decoderNum)) {
    for (j <- 0 until (config.fuNum)) {
      when(
        io.in.forward(j).mem.wen &&
          io.in.forward(j).mem.waddr === io.in.regfile(i).src1.raddr,
      ) {
        io.out.inst(i).src1.rdata := io.in.forward(j).mem.wdata
      }
      when(
        io.in.forward(j).mem.wen &&
          io.in.forward(j).mem.waddr === io.in.regfile(i).src2.raddr,
      ) {
        io.out.inst(i).src2.rdata := io.in.forward(j).mem.wdata
      }
    }
  }

  // exe优先度高
  for (i <- 0 until (config.decoderNum)) {
    for (j <- 0 until (config.fuNum)) {
      when(
        io.in.forward(j).exe.wen && !io.in.forward(j).mem_wreg &&
          io.in.forward(j).exe.waddr === io.in.regfile(i).src1.raddr,
      ) {
        io.out.inst(i).src1.rdata := io.in.forward(j).exe.wdata
      }
      when(
        io.in.forward(j).exe.wen && !io.in.forward(j).mem_wreg &&
          io.in.forward(j).exe.waddr === io.in.regfile(i).src2.raddr,
      ) {
        io.out.inst(i).src2.rdata := io.in.forward(j).exe.wdata
      }
    }
  }
}
