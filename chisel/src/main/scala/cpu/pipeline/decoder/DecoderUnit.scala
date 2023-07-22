package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class DataForward extends Bundle {
  val valid = Bool()
  val wen   = Bool()
  val waddr = UInt(REG_ADDR_WID.W)
  val wdata = UInt(DATA_WID.W)
}

class DecoderUnit(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    // 输入
    val instBuffer = Flipped(new Bundle {
      val inst = Vec(
        config.decoderNum,
        Decoupled(new Bundle {
          val pc   = UInt(PC_WID.W)
          val inst = UInt(INST_WID.W)
        }),
      )
      val info = new Bundle {
        val empty        = Bool()
        val almost_empty = Bool()
      }
    })
    val regfile = Vec(config.decoderNum, new Src12Read())
    val forward = Input(
      Vec(
        config.fuNum,
        new Bundle {
          val exe = new Bundle {
            val forward = new DataForward()
            val mem_ren = Bool()
          }
          val mem = new DataForward()
        },
      ),
    )
    // 输出
    val bpu = Output(new Bundle {
      val valid = Bool()
      val pc    = UInt(PC_WID.W)
      val inst0 = UInt(INST_WID.W)
    })
    val executeStage = Vec(
      config.decoderNum,
      Decoupled(new Bundle {
        val pc           = UInt(PC_WID.W)
        val inst         = UInt(INST_WID.W)
        val decoded_inst = new DecodedInst()
        val ex           = new ExceptionInfo()
      }),
    )
    val ctrl = new DecoderUnitCtrl()
  })

  val issue = Module(new Issue()).io
  issue.inst0_allow_to_go := io.ctrl.allow_to_go
  issue.instBuffer        := io.instBuffer.info

  for (i <- 0 until (config.decoderNum)) {
    val decoder = Module(new Decoder()).io

    val pc   = io.instBuffer.inst(i).bits.pc
    val inst = io.instBuffer.inst(i).bits.inst
    decoder.in.inst := inst
    val decoded_inst = decoder.out

    issue.decodeInst(i)           := decoded_inst
    issue.execute.inst(i).mem_ren := io.forward(i).exe.mem_ren
  }
}
