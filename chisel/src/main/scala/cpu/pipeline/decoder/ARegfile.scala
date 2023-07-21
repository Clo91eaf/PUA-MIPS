package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class SrcRead extends Bundle {
  val raddr = Output(UInt(REG_ADDR_WID.W))
  val rdata = Input(UInt(DATA_WID.W))
}

class Src12Read extends Bundle {
  val src1 = new SrcRead()
  val src2 = new SrcRead()
}

class RegWrite extends Bundle {
  val wen   = Output(Bool())
  val waddr = Output(UInt(REG_ADDR_WID.W))
  val wdata = Output(UInt(DATA_WID.W))
}

class ARegFile(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val read  = Flipped(Vec(config.decoderNum, new Src12Read()))
    val write = Flipped(Vec(config.commitNum, new RegWrite()))
  })

  // 定义32个32位寄存器
  val regs = RegInit(VecInit(Seq.fill(AREG_NUM)(0.U(DATA_WID.W))))

  // 写寄存器堆
  for (i <- 0 until (config.commitNum)) {
    when(io.write(i).wen && io.write(i).waddr =/= 0.U) {
      regs(io.write(i).waddr) := io.write(i).wdata
    }
  }

  // 读寄存器堆
  for (i <- 0 until (config.decoderNum)) {
    // src1
    when(io.read(i).src1.raddr === 0.U) {
      io.read(i).src1.rdata := 0.U
    }.otherwise {
      io.read(i).src1.rdata := regs(io.read(i).src1.raddr)
      for (j <- 0 until (config.commitNum)) {
        when(io.write(j).wen && io.read(i).src1.raddr === io.write(j).waddr) {
          io.read(i).src1.rdata := io.write(j).wdata
        }
      }
    }
    // src2
    when(io.read(i).src2.raddr === 0.U) {
      io.read(i).src2.rdata := 0.U
    }.otherwise {
      io.read(i).src2.rdata := regs(io.read(i).src2.raddr)
      for (j <- 0 until (config.commitNum)) {
        when(io.write(j).wen && io.read(i).src2.raddr === io.write(j).waddr) {
          io.read(i).src2.rdata := io.write(j).wdata
        }
      }
    }
  }
}
