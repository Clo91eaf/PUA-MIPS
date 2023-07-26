package cpu.defines

import chisel3._
import chisel3.util._
import Const._
import cpu.CpuConfig

class ExceptionInfo extends Bundle {
  val flush_req  = Bool()
  val tlb_refill = Bool()
  val eret       = Bool()
  val badvaddr   = UInt(PC_WID.W)
  val bd         = Bool()
  val excode     = UInt(EXCODE_WID.W)
}

class SrcInfo extends Bundle {
  val src1_data = UInt(DATA_WID.W)
  val src2_data = UInt(DATA_WID.W)
}

class RdInfo extends Bundle {
  val wdata = UInt(DATA_WID.W)
}

class InstInfo extends Bundle {
  val inst_valid = Bool()
  val reg1_ren   = Bool()
  val reg1_raddr = UInt(REG_ADDR_WID.W)
  val reg2_ren   = Bool()
  val reg2_raddr = UInt(REG_ADDR_WID.W)
  val fusel      = UInt(FU_SEL_WID.W)
  val op         = UInt(OP_WID.W)
  val reg_wen    = Bool()
  val reg_waddr  = UInt(REG_ADDR_WID.W)
  val imm32      = UInt(DATA_WID.W)
  val cp0_addr   = UInt(CP0_ADDR_WID.W)
  val dual_issue = Bool()
  val inst       = UInt(INST_WID.W)
}

class MemRead extends Bundle {
  val mem_ren   = Bool()
  val reg_waddr = UInt(REG_ADDR_WID.W)
}

class SrcReadSignal extends Bundle {
  val ren   = Bool()
  val raddr = UInt(REG_ADDR_WID.W)
}

class CacheCtrl extends Bundle {
  val iCache_stall = Output(Bool())
  val dCache_stall = Output(Bool())
}

class FetchUnitCtrl extends Bundle {
  val allow_to_go = Input(Bool())
  val do_flush    = Input(Bool())
}

class InstBufferCtrl extends Bundle {
  val delay_slot_do_flush = Input(Bool())
}

class DecoderUnitCtrl extends Bundle {
  val inst0 = Output(new Bundle {
    val src1 = new SrcReadSignal()
    val src2 = new SrcReadSignal()
  })
  val branch_flag = Output(Bool())

  val allow_to_go = Input(Bool())
  val do_flush    = Input(Bool())
}

class ExecuteCtrl(implicit val config: CpuConfig) extends Bundle {
  val inst        = Output(Vec(config.fuNum, new MemRead()))
  val fu_stall    = Output(Bool())
  val branch_flag = Output(Bool())

  val allow_to_go = Input(Bool())
  val do_flush    = Input(Bool())
}

class MemoryCtrl extends Bundle {
  val flush_req = Output(Bool())

  val allow_to_go = Input(Bool())
  val do_flush    = Input(Bool())
}

class WriteBackCtrl extends Bundle {
  val allow_to_go = Input(Bool())
  val do_flush    = Input(Bool())
}
