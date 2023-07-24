package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class Fu(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = new Bundle {
      val allow_to_go = Input(Bool())
      val do_flush    = Input(Bool())
    }
    val inst = Vec(
      config.decoderNum,
      new Bundle {
        val pc        = Input(UInt(PC_WID.W))
        val hilo_wen  = Input(Bool())
        val mul_en    = Input(Bool())
        val div_en    = Input(Bool())
        val inst_info = Input(new InstInfo())
        val src_info  = Input(new SrcInfo())
        val ex        = Input(new ExceptionInfo())

        val overflow = Output(Bool())
        val result   = Output(UInt(DATA_WID.W))
      },
    )
    val cp0_rdata = Input(UInt(DATA_WID.W))
    val stall_req = Output(Bool())
  })

  val alu  = Seq.fill(config.decoderNum)(Module(new Alu()))
  val mul  = Module(new Mul()).io
  val div  = Module(new Div()).io
  val hilo = Module(new HiLo()).io

  for (i <- 0 until (config.fuNum)) {
    alu(i).io.inst_info  := io.inst(i).inst_info
    alu(i).io.src_info   := io.inst(i).src_info
    alu(i).io.hilo.rdata := hilo.rdata
    alu(i).io.mul.ready  := mul.ready
    alu(i).io.mul.result := mul.result
    alu(i).io.div.ready  := div.ready
    alu(i).io.div.result := div.result
    io.inst(i).overflow  := alu(i).io.overflow
  }
  alu(0).io.cp0_rdata := io.cp0_rdata
  alu(1).io.cp0_rdata := 0.U

  mul.src1   := Mux(io.inst(0).mul_en, io.inst(0).src_info.src1_data, io.inst(1).src_info.src1_data)
  mul.src2   := Mux(io.inst(0).mul_en, io.inst(0).src_info.src2_data, io.inst(1).src_info.src2_data)
  mul.signed := Mux(io.inst(0).mul_en, alu(0).io.mul.signed, alu(1).io.mul.signed)
  mul.start  := Mux(io.inst(0).mul_en, alu(0).io.mul.start, alu(1).io.mul.start)

  div.src1   := Mux(io.inst(0).div_en, io.inst(0).src_info.src1_data, io.inst(1).src_info.src1_data)
  div.src2   := Mux(io.inst(0).div_en, io.inst(0).src_info.src2_data, io.inst(1).src_info.src2_data)
  div.signed := Mux(io.inst(0).div_en, alu(0).io.div.signed, alu(1).io.div.signed)
  div.start  := Mux(io.inst(0).div_en, alu(0).io.div.start, alu(1).io.div.start)
  div.annul  := 0.U

  io.stall_req := (io.inst.map(_.mul_en).reduce(_ || _) && !mul.ready) ||
    (io.inst.map(_.div_en).reduce(_ || _) && !div.ready)

  io.inst(0).result := Mux(
    VecInit(EXE_JAL, EXE_JALR, EXE_BGEZAL, EXE_BLTZAL).contains(io.inst(0).inst_info.op),
    io.inst(0).pc + 8.U,
    alu(0).io.result,
  )
  io.inst(1).result := alu(1).io.result

  hilo.wen := ((io.inst(0).hilo_wen && !io.inst.map(_.ex.flush_req).reduce(_ || _)) ||
    (io.inst(1).hilo_wen && !io.inst(1).ex.flush_req)) && io.ctrl.allow_to_go && !io.ctrl.do_flush
  hilo.wdata := Mux(io.inst(1).hilo_wen, alu(1).io.hilo.wdata, alu(0).io.hilo.wdata)
}
