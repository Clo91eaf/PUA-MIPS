package cpu.pipeline.decoder

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class Issue(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    // 输入
    val allow_to_go = Input(Bool())
    val instFifo = Input(new Bundle {
      val empty        = Bool()
      val almost_empty = Bool()
    })
    val decodeInst = Input(Vec(config.decoderNum, new InstInfo()))
    val execute    = Input(Vec(config.fuNum, new MemRead()))
    // 输出
    val inst1 = Output(new Bundle {
      val is_in_delayslot = Bool()
      val allow_to_go     = Bool()
    })
  })

  val inst0 = io.decodeInst(0)
  val inst1 = io.decodeInst(1)

  // inst buffer是否存有至少2条指令
  val instFifo_invalid = io.instFifo.empty || io.instFifo.almost_empty

  // 结构冲突
  val mem_conflict    = inst0.fusel === FU_MEM && inst1.fusel === FU_MEM
  val mul_conflict    = inst0.fusel === FU_MUL && inst1.fusel === FU_MUL
  val div_conflict    = inst0.fusel === FU_DIV && inst1.fusel === FU_DIV
  val struct_conflict = mem_conflict || mul_conflict || div_conflict

  // 写后读冲突
  val load_stall =
    io.execute(0).mem_wreg && (inst1.reg1_ren && inst1.reg1_raddr === io.execute(0).reg_waddr ||
      inst1.reg2_ren && inst1.reg2_raddr === io.execute(0).reg_waddr) ||
      io.execute(1).mem_wreg && (inst1.reg1_ren && inst1.reg1_raddr === io.execute(1).reg_waddr ||
        inst1.reg2_ren && inst1.reg2_raddr === io.execute(1).reg_waddr)
  val raw_reg =
    inst0.reg_wen && (inst0.reg_waddr === inst1.reg1_raddr && inst1.reg1_ren || inst0.reg_waddr === inst1.reg2_raddr && inst1.reg2_ren)
  val raw_hilo = VecInit(FU_DIV, FU_MUL, FU_MTHILO).contains(inst0.fusel) &&
    VecInit(FU_DIV, FU_MUL, FU_MFHILO, FU_MTHILO).contains(inst1.fusel)
  val raw_cp0 =
    inst0.op === EXE_MTC0 && inst1.op === EXE_MFC0 && inst0.cp0_addr === inst1.cp0_addr
  val data_conflict = raw_reg || raw_hilo || raw_cp0 || load_stall

  // 指令1是否在延迟槽中
  io.inst1.is_in_delayslot := inst0.fusel === FU_BR && io.inst1.allow_to_go
  // 指令1是否允许执行
  io.inst1.allow_to_go := io.allow_to_go &&
    !instFifo_invalid &&
    inst0.dual_issue &&
    inst1.dual_issue &&
    !struct_conflict &&
    !data_conflict &&
    !VecInit(FU_BR, FU_EX).contains(io.decodeInst(1).fusel)
}
