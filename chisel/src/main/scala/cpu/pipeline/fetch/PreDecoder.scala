package cpu.pipeline.fetch

import chisel3._
import chisel3.util._
import cpu.defines.Const._
import cpu.CpuConfig
import cpu.pipeline.fetch.BufferUnit

class BufferEnq extends Bundle {
  val valid            = Bool()
  val jump_branch_inst = Bool()
  val op               = UInt(OP_WID.W)
  val is_in_delayslot  = Bool()

  val tlb = new Bundle {
    val refill  = Bool()
    val invalid = Bool()
  }
  val inst = UInt(32.W)
  val pc   = UInt(32.W)
}

class PreDecoder(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val flush = Input(Bool())

    val full = new Bundle {
      val fromInstFifo = Input(Bool())
      val toIcache     = Output(Bool())
    }
    val read = Output(Vec(config.instFetchNum, new BufferEnq()))

    val wen   = Input(Vec(config.instFetchNum, Bool()))
    val write = Input(Vec(config.instFetchNum, new BufferUnit()))
  })

  val buffer = RegInit(VecInit(Seq.fill(config.instFetchNum)(0.U.asTypeOf(new BufferEnq()))))

  for (i <- 0 until config.instFetchNum) {
    when(io.wen(i) && !io.full.fromInstFifo) {
      buffer(i).tlb.refill  := io.write(i).tlb.refill
      buffer(i).tlb.invalid := io.write(i).tlb.invalid
      buffer(i).inst        := io.write(i).inst
      buffer(i).pc          := io.write(i).pc
    }
    when(!io.full.fromInstFifo) {
      buffer(i).valid := io.wen(i)
    }
  }
  io.full.toIcache := io.full.fromInstFifo

  for (i <- 0 until config.instFetchNum) {
    val signals: List[UInt] = ListLookup(
      buffer(i).inst,
      List(EXE_NOP, false.B),
      Array( // 跳转指令
        J      -> List(EXE_J, true.B),
        JAL    -> List(EXE_JAL, true.B),
        JR     -> List(EXE_JR, true.B),
        JALR   -> List(EXE_JALR, true.B),
        BEQ    -> List(EXE_BEQ, true.B),
        BNE    -> List(EXE_BNE, true.B),
        BGTZ   -> List(EXE_BGTZ, true.B),
        BLEZ   -> List(EXE_BLEZ, true.B),
        BGEZ   -> List(EXE_BGEZ, true.B),
        BGEZAL -> List(EXE_BGEZAL, true.B),
        BLTZ   -> List(EXE_BLTZ, true.B),
        BLTZAL -> List(EXE_BLTZAL, true.B),
      ),
    )
    val op :: jump_branch_inst :: Nil = signals

    io.read(i).tlb.refill       := buffer(i).tlb.refill
    io.read(i).tlb.invalid      := buffer(i).tlb.invalid
    io.read(i).inst             := buffer(i).inst
    io.read(i).pc               := buffer(i).pc
    io.read(i).valid            := buffer(i).valid
    io.read(i).jump_branch_inst := jump_branch_inst
    io.read(i).op               := op
  }

  val inst0_is_in_delayslot = RegNext(buffer(config.instFetchNum - 1).jump_branch_inst)

  for (i <- 1 until config.instFetchNum) {
    io.read(i).is_in_delayslot := buffer(i - 1).jump_branch_inst
  }
  io.read(0).is_in_delayslot := inst0_is_in_delayslot

  when(io.flush) {
    for (i <- 0 until config.instFetchNum) {
      buffer(i).valid := false.B
    }
  }
}
