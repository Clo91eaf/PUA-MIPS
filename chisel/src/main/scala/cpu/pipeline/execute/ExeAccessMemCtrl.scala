package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.CpuConfig
import cpu.defines._
import cpu.defines.Const._

class ExeAccessMemCtrl(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val mem = new Bundle {
      val out = Output(new Bundle {
        val en        = Bool()
        val ren       = Bool()
        val wen       = Bool()
        val inst_info = new InstInfo()
        val addr      = UInt(DATA_ADDR_WID.W)
        val wdata     = UInt(DATA_WID.W)
      })
    }

    val inst = Vec(
      config.fuNum,
      new Bundle {
        val inst_info = Input(new InstInfo())
        val src_info  = Input(new SrcInfo())
        val ex = new Bundle {
          val in  = Input(new ExceptionInfo())
          val out = Output(new ExceptionInfo())
        }
        val mem_sel = Output(Bool())
      },
    )
  })
  io.mem.out.en := io.inst.map(_.mem_sel).reduce(_ || _)
  io.mem.out.ren := io.inst(0).mem_sel && io.inst(0).inst_info.rmem ||
    io.inst(1).mem_sel && io.inst(1).inst_info.rmem
  io.mem.out.wen := io.inst(0).mem_sel && io.inst(0).inst_info.wmem ||
    io.inst(1).mem_sel && io.inst(1).inst_info.wmem
  io.mem.out.inst_info := MuxCase(
    DontCare,
    Seq(
      (io.inst(0).inst_info.fusel === FU_MEM) -> io.inst(0).inst_info,
      (io.inst(1).inst_info.fusel === FU_MEM) -> io.inst(1).inst_info,
    ),
  )
  val mem_addr = Wire(Vec(config.fuNum, UInt(DATA_ADDR_WID.W)))
  mem_addr(0) := io.inst(0).inst_info.mem_addr
  mem_addr(1) := io.inst(1).inst_info.mem_addr
  io.mem.out.addr := MuxCase(
    0.U,
    Seq(
      (io.inst(0).inst_info.fusel === FU_MEM) -> mem_addr(0),
      (io.inst(1).inst_info.fusel === FU_MEM) -> mem_addr(1),
    ),
  )
  io.mem.out.wdata := MuxCase(
    0.U,
    Seq(
      (io.inst(0).inst_info.fusel === FU_MEM) ->
        io.inst(0).src_info.src2_data,
      (io.inst(1).inst_info.fusel === FU_MEM) ->
        io.inst(1).src_info.src2_data,
    ),
  )
  val mem_adel = Wire(Vec(config.fuNum, Bool()))
  for (i <- 0 until config.fuNum) {
    mem_adel(i) := VecInit(EXE_LW, EXE_LL).contains(io.inst(i).inst_info.op) && mem_addr(i)(1, 0) =/= 0.U ||
      VecInit(EXE_LH, EXE_LHU).contains(io.inst(i).inst_info.op) && mem_addr(i)(0) =/= 0.U
  }
  val mem_ades = Wire(Vec(config.fuNum, Bool()))
  for (i <- 0 until config.fuNum) {
    mem_ades(i) := VecInit(EXE_SW, EXE_SC).contains(io.inst(i).inst_info.op) && mem_addr(i)(1, 0) =/= 0.U ||
      io.inst(i).inst_info.op === EXE_SH && mem_addr(i)(0) =/= 0.U
  }

  for (i <- 0 until config.fuNum) {
    io.inst(i).ex.out := io.inst(i).ex.in
    io.inst(i).ex.out.excode := MuxCase(
      io.inst(i).ex.in.excode,
      Seq(
        (io.inst(i).ex.in.excode =/= EX_NO) -> io.inst(i).ex.in.excode,
        mem_adel(i)                         -> EX_ADEL,
        mem_ades(i)                         -> EX_ADES,
      ),
    )
    io.inst(i).ex.out.badvaddr := Mux(
      VecInit(EX_ADEL, EX_ADES).contains(io.inst(i).ex.in.excode),
      io.inst(i).ex.in.badvaddr,
      mem_addr(i),
    )
    io.inst(i).ex.out.flush_req := io.inst(i).ex.in.flush_req || io.inst(i).ex.out.excode =/= EX_NO
  }
  io.inst(0).mem_sel := (io.inst(0).inst_info.wmem || io.inst(0).inst_info.rmem) &&
    !io.inst(0).ex.out.flush_req
  io.inst(1).mem_sel := (io.inst(1).inst_info.wmem || io.inst(1).inst_info.rmem) &&
    !io.inst(0).ex.out.flush_req && !io.inst(1).ex.out.flush_req

}
