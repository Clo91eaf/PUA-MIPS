package cpu.pipeline.memory

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.decoder.RegWrite
import cpu.pipeline.writeback.MemoryUnitWriteBackUnit

class MemoryUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl           = new MemoryCtrl()
    val memoryUnit     = Input(new ExecuteUnitMemoryUnit())
    val decoderUnit    = Output(Vec(config.fuNum, new RegWrite()))
    val writeBackStage = Output(new MemoryUnitWriteBackUnit())
    val dataMemory = new Bundle {
      val out = Output(new Bundle {
       
      })
      val in = Input(
        Vec(
          config.fuNum,
          new Bundle {
            val tlb_invalid = Bool()
            val tlb_refill  = Bool()
          },
        ),
      )
    }
  })

  io.decoderUnit(0).wen   := io.memoryUnit.inst0.inst_info.reg_wen
  io.decoderUnit(0).waddr := io.memoryUnit.inst0.inst_info.reg_waddr
  io.decoderUnit(0).wdata := io.memoryUnit.inst0.rd_info.wdata
  io.decoderUnit(1).wen   := io.memoryUnit.inst1.inst_info.reg_wen
  io.decoderUnit(1).waddr := io.memoryUnit.inst1.inst_info.reg_waddr
  io.decoderUnit(1).wdata := io.memoryUnit.inst1.rd_info.wdata

  io.dataMemory := io.memoryUnit.mem

}
