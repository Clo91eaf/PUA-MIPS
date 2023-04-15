package cpu.defines

import chisel3._
import Const._

// fetch
class FetchStage_DecoderStage extends Bundle {
  val pc       = Output(BUS)
  val inst     = Output(BUS)
  val ex       = Output(Bool())
  val bd       = Output(Bool())
  val badvaddr = Output(Bool())
  val valid    = Output(Bool())
}

class FetchStage_InstMemory extends Bundle {
  val en    = Output(Bool())
  val addr  = Output(BUS)
  val wen   = Output(UInt(4.W))
  val wdata = Output(BUS)
}

// decoderStage
class DecoderStage_Decoder extends Bundle {
  val pc       = Output(BUS)
  val inst     = Output(BUS)
  val ex       = Output(Bool())
  val bd       = Output(Bool())
  val badvaddr = Output(Bool())
  val valid    = Output(Bool())
}

// decoder
class Decoder_FetchStage extends Bundle {
  val branch_stall          = Output(Bool())
  val branch_flag           = Output(Bool()) // 是否发生转移
  val branch_target_address = Output(BUS)    // 转移到的目标地址
  val allowin               = Output(Bool())
  val is_branch             = Output(Bool())
}

class Decoder_DecoderStage extends Bundle {
  val allowin = Output(Bool())
}

class Decoder_ExecuteStage extends Bundle {
  val aluop                  = Output(ALU_OP_BUS)
  val alusel                 = Output(ALU_SEL_BUS)
  val inst                   = Output(BUS)
  val is_in_delayslot        = Output(Bool())
  val link_addr              = Output(BUS)
  val reg1                   = Output(BUS)
  val reg2                   = Output(BUS)
  val reg_waddr              = Output(ADDR_BUS)
  val reg_wen                = Output(REG_WRITE_BUS)
  val next_inst_in_delayslot = Output(Bool())
  val pc                     = Output(INST_ADDR_BUS)
  val valid                  = Output(Bool())
  val ex                     = Output(Bool())
  val bd                     = Output(Bool())
  val badvaddr               = Output(Bool())
  val cp0_addr               = Output(UInt(8.W))
  val excode                 = Output(UInt(5.W))
  val overflow_inst          = Output(Bool())
  val fs_to_ds_ex            = Output(Bool())
}

class Decoder_RegFile extends Bundle {
  val reg1_raddr = Output(ADDR_BUS)
  val reg1_ren   = Output(Bool())
  val reg2_raddr = Output(ADDR_BUS)
  val reg2_ren   = Output(Bool())
}

class Decoder_Control extends Bundle {
  val stallreq = Output(Bool())
}

// executeStage
class ExecuteStage_Decoder extends Bundle {
  val is_in_delayslot = Output(Bool())
}

class ExecuteStage_Execute extends Bundle {
  val aluop           = Output(ALU_OP_BUS)
  val alusel          = Output(ALU_SEL_BUS)
  val inst            = Output(BUS)
  val is_in_delayslot = Output(Bool())
  val link_addr       = Output(BUS)
  val reg1            = Output(BUS)
  val reg2            = Output(BUS)
  val reg_waddr       = Output(ADDR_BUS)
  val reg_wen         = Output(REG_WRITE_BUS)
  val pc              = Output(INST_ADDR_BUS)
  val valid           = Output(Bool())
  val bd              = Output(Bool())
  val badvaddr        = Output(Bool())
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val overflow_inst   = Output(Bool())
  val fs_to_ds_ex     = Output(Bool())
  val ds_to_es_ex     = Output(Bool())
}

// execute
class Execute_ALU extends Bundle {
  val op  = Output(ALU_OP_BUS)
  val in1 = Output(BUS)
  val in2 = Output(BUS)
}

class ALU_Execute extends Bundle {
  val out  = Output(BUS)
  val ov   = Output(Bool())
  val trap = Output(Bool())
}

class Execute_Mul extends Bundle {
  val op  = Output(ALU_OP_BUS)
  val in1 = Output(BUS)
  val in2 = Output(BUS)
}

class Mul_Execute extends Bundle {
  val out = Output(DOUBLE_BUS)
}

class Execute_Div extends Bundle {
  val op       = Output(ALU_OP_BUS)
  val divisor  = Output(BUS)
  val dividend = Output(BUS)
}

class Div_Execute extends Bundle {
  val quotient  = Output(BUS)
  val remainder = Output(BUS)
}

class CP0_Mov extends Bundle {
  val cp0_rdata = Output(BUS)
}

class Memory_Mov extends Bundle {
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(Bool())
  val cp0_wdata = Output(Bool())
}

class WriteBackStage_Mov extends Bundle {
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(Bool())
  val cp0_wdata = Output(Bool())
}

class Execute_Mov extends Bundle {
  val op   = Output(ALU_OP_BUS)
  val inst = Output(BUS)
  val in   = Output(BUS)
  val hi   = Output(BUS)
  val lo   = Output(BUS)
}

class Mov_Execute extends Bundle {
  val out = Output(BUS)
}

class Execute_Decoder extends Bundle {
  val aluop        = Output(ALU_OP_BUS)
  val reg_waddr    = Output(ADDR_BUS)
  val reg_wdata    = Output(BUS)
  val reg_wen      = Output(REG_WRITE_BUS)
  val allowin      = Output(Bool())
  val blk_valid    = Output(Bool())
  val inst_is_mfc0 = Output(Bool())
  val es_fwd_valid = Output(Bool())
}

class Execute_ExecuteStage extends Bundle {
  val allowin = Output(Bool())
}

class Execute_MemoryStage extends Bundle {
  val aluop           = Output(ALU_OP_BUS)
  val cnt             = Output(CNT_BUS)
  val hi              = Output(BUS)
  val hilo            = Output(DOUBLE_BUS)
  val lo              = Output(BUS)
  val reg2            = Output(BUS)
  val reg_waddr       = Output(ADDR_BUS)
  val reg_wdata       = Output(BUS)
  val whilo           = Output(Bool())
  val reg_wen         = Output(REG_WRITE_BUS)
  val is_in_delayslot = Output(Bool())
  val pc              = Output(BUS)
  val valid           = Output(Bool())
  val mem_addr        = Output(BUS)
  val bd              = Output(Bool())
  val badvaddr        = Output(Bool())
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val overflow_inst   = Output(Bool())
  val ex              = Output(Bool())
}

class Execute_CP0 extends Bundle {
  val cp0_raddr = Output(CP0_ADDR_BUS)
}

// dataMemory
class Execute_DataMemory extends Bundle {
  val valid = Output(Bool())
  val op    = Output(ALU_OP_BUS)
  val addr  = Output(BUS)
  val data  = Output(BUS)
}

class DataMemory_DataSram extends Bundle {
  val en    = Output(Bool())
  val wen   = Output(WEN_BUS)
  val addr  = Output(BUS)
  val wdata = Output(BUS)
}

class DataSram_DataMemory extends Bundle {
  val rdata = Output(BUS)
}

class DataMemory_Memory extends Bundle {
  val rdata = Output(BUS)
}

class DataMemory_DataStage extends Bundle {
  val addr = Output(BUS)
}

// memoryStage
class MemoryStage_Execute extends Bundle {
  val cnt  = Output(CNT_BUS)
  val hilo = Output(DOUBLE_BUS)
}

class MemoryStage_Memory extends Bundle {
  val aluop           = Output(ALU_OP_BUS)
  val hi              = Output(BUS)
  val lo              = Output(BUS)
  val whilo           = Output(Bool())
  val mem_addr        = Output(BUS)
  val reg2            = Output(BUS)
  val reg_waddr       = Output(ADDR_BUS)
  val reg_wen         = Output(REG_WRITE_BUS)
  val reg_wdata       = Output(BUS)
  val is_in_delayslot = Output(Bool())
  val pc              = Output(BUS)
  val valid           = Output(Bool())
  val bd              = Output(Bool())
  val badvaddr        = Output(Bool())
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val ex              = Output(Bool())
}

// memory
class Memory_MemoryStage extends Bundle {
  val allowin = Output(Bool())
}
class Memory_WriteBackStage extends Bundle {
  val pc              = Output(BUS)
  val LLbit_value     = Output(Bool())
  val LLbit_wen       = Output(Bool())
  val reg_wdata       = Output(BUS)
  val reg_waddr       = Output(ADDR_BUS)
  val reg_wen         = Output(REG_WRITE_BUS)
  val whilo           = Output(Bool())
  val hi              = Output(BUS)
  val lo              = Output(BUS)
  val valid           = Output(Bool())
  val inst_is_mfc0    = Output(Bool())
  val inst_is_mtc0    = Output(Bool())
  val inst_is_eret    = Output(Bool())
  val inst_is_syscall = Output(Bool())
  val bd              = Output(Bool())
  val badvaddr        = Output(Bool())
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val ex              = Output(Bool())
}

class Memory_Decoder extends Bundle {
  val reg_waddr    = Output(ADDR_BUS)
  val reg_wdata    = Output(BUS)
  val reg_wen      = Output(REG_WRITE_BUS)
  val inst_is_mfc0 = Output(Bool())
  val ms_fwd_valid = Output(Bool())
}

class Memory_Execute extends Bundle {
  val whilo   = Output(Bool())
  val hi      = Output(BUS)
  val lo      = Output(BUS)
  val allowin = Output(Bool())
  val eret    = Output(Bool())
  val ex      = Output(Bool())
}

class Memory_CP0 extends Bundle {
  val current_inst_addr = Output(BUS)
  val is_in_delayslot   = Output(Bool())
  val except_type       = Output(UInt(32.W))
}

class Memory_Control extends Bundle {
  val except_type = Output(UInt(32.W))
  val cp0_epc     = Output(BUS)
}

// writeBackStage
class WriteBackStage_Decoder extends Bundle {
  val inst_is_mfc0 = Output(Bool())
  val reg_waddr    = Output(ADDR_BUS)
  val eret         = Output(Bool())
  val ex           = Output(Bool())
  val cp0_cause    = Output(UInt(32.W))
  val cp0_status   = Output(UInt(32.W))
}

class WriteBackStage_LLbitReg extends Bundle {
  val LLbit_value = Output(Bool())
  val LLbit_wen   = Output(Bool())
}

class WriteBackStage_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi    = Output(BUS)
  val lo    = Output(BUS)
  val eret  = Output(Bool())
  val ex    = Output(Bool())
}

class WriteBackStage_HILO extends Bundle {
  val whilo = Output(Bool())
  val hi    = Output(BUS)
  val lo    = Output(BUS)
}

class WriteBackStage_Memory extends Bundle {
  val LLbit_value = Output(Bool())
  val LLbit_wen   = Output(Bool())
  val allowin     = Output(Bool())
  val eret        = Output(Bool())
  val ex          = Output(Bool())
}

class WriteBackStage_RegFile extends Bundle {
  val reg_wdata = Output(BUS)
  val reg_waddr = Output(ADDR_BUS)
  val reg_wen   = Output(REG_WRITE_BUS)
}

class WriteBackStage_CP0 extends Bundle {
  val wb_ex       = Output(Bool())
  val wb_bd       = Output(Bool())
  val eret_flush  = Output(Bool())
  val wb_excode   = Output(UInt(5.W))
  val wb_pc       = Output(UInt(32.W))
  val wb_badvaddr = Output(UInt(32.W))
  val ext_int_in  = Output(UInt(6.W))
  val cp0_addr    = Output(UInt(8.W))
  val mtc0_we     = Output(Bool())
  val cp0_wdata   = Output(UInt(32.W))
}

class WriteBackStage_FetchStage extends Bundle {
  val eret    = Output(Bool())
  val ex      = Output(Bool())
  val cp0_epc = Output(UInt(32.W))
}

// writeBack

// instMemory
class InstMemory_FetchStage extends Bundle {
  val rdata = Output(BUS)
}

// regFile
class RegFile_Decoder extends Bundle {
  val reg1_data = Output(BUS)
  val reg2_data = Output(BUS)
}

// HILO
class HILO_Execute extends Bundle {
  val hi = Output(BUS)
  val lo = Output(BUS)
}

// LLbitReg
class LLbitReg_Memory extends Bundle {
  val LLbit = Output(Bool())
}

// CP0
class CP0_WriteBackStage extends Bundle {
  val cp0_rdata    = Output(UInt(32.W))
  val cp0_status   = Output(UInt(32.W))
  val cp0_cause    = Output(UInt(32.W))
  val cp0_epc      = Output(UInt(32.W))
  val cp0_badvaddr = Output(UInt(32.W))
  val cp0_count    = Output(UInt(32.W))
  val cp0_compare  = Output(UInt(32.W))
}
// other
class INST_SRAM extends Bundle {
  val en    = Output(Bool())
  val wen   = Output(WEN_BUS)
  val addr  = Output(BUS)
  val wdata = Output(BUS)
  val rdata = Output(BUS)
}

class DATA_SRAM extends Bundle {
  val en    = Output(Bool())
  val wen   = Output(WEN_BUS)
  val addr  = Output(BUS)
  val wdata = Output(BUS)
  val rdata = Output(BUS)
}

class DEBUG extends Bundle {
  val pc    = Output(BUS)
  val wen   = Output(WEN_BUS)
  val waddr = Output(ADDR_BUS)
  val wdata = Output(BUS)
}
