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
  val reg_wen                = Output(Bool())
  val next_inst_in_delayslot = Output(Bool())
  val current_inst_addr      = Output(BUS)
  val except_type            = Output(UInt(32.W))
  val pc                     = Output(INST_ADDR_BUS)
  val stall                  = Output(Bool())
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
  val aluop             = Output(ALU_OP_BUS)
  val alusel            = Output(ALU_SEL_BUS)
  val inst              = Output(BUS)
  val is_in_delayslot   = Output(Bool())
  val link_addr         = Output(BUS)
  val reg1              = Output(BUS)
  val reg2              = Output(BUS)
  val reg_waddr         = Output(ADDR_BUS)
  val reg_wen           = Output(Bool())
  val current_inst_addr = Output(BUS)
  val except_type       = Output(UInt(32.W))
  val pc                = Output(INST_ADDR_BUS)
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

class Execute_Decoder extends Bundle {
  val aluop     = Output(ALU_OP_BUS)
  val reg_waddr = Output(ADDR_BUS)
  val reg_wdata = Output(BUS)
  val reg_wen   = Output(Bool())
  // val allowin   = Output(Bool())
  // val blk_valid = Output(Bool())
}

class Execute_ExecuteStage extends Bundle {
  val allowin = Output(Bool())
}

class Execute_MemoryStage extends Bundle {
  val aluop             = Output(ALU_OP_BUS)
  val cnt               = Output(CNT_BUS)
  val hi                = Output(BUS)
  val hilo              = Output(DOUBLE_BUS)
  val lo                = Output(BUS)
  val mem_addr          = Output(BUS)
  val reg2              = Output(BUS)
  val reg_waddr         = Output(ADDR_BUS)
  val reg_wdata         = Output(BUS)
  val whilo             = Output(Bool())
  val reg_wen           = Output(Bool())
  val cp0_wen           = Output(Bool())
  val cp0_waddr         = Output(CP0_ADDR_BUS)
  val cp0_wdata         = Output(BUS)
  val current_inst_addr = Output(BUS)
  val is_in_delayslot   = Output(Bool())
  val except_type       = Output(UInt(32.W))
  val pc                = Output(BUS)
}

class Execute_Control extends Bundle {
  val stallreq = Output(Bool())
}

class Execute_Divider extends Bundle {
  val opdata1    = Output(BUS)
  val opdata2    = Output(BUS)
  val start      = Output(Bool())
  val signed_div = Output(Bool())
}

class Execute_CP0 extends Bundle {
  val cp0_raddr = Output(CP0_ADDR_BUS)
}

class Execute_DataMemory extends Bundle {
  val mem_addr  = Output(BUS)
  val mem_wen   = Output(Bool())
  val mem_wsel  = Output(DATA_MEMORY_SEL_BUS)
  val mem_wdata = Output(BUS)
  val mem_ce    = Output(Bool())
}

// memoryStage
class MemoryStage_Execute extends Bundle {
  val cnt  = Output(CNT_BUS)
  val hilo = Output(DOUBLE_BUS)
}

class MemoryStage_Memory extends Bundle {
  val aluop             = Output(ALU_OP_BUS)
  val hi                = Output(BUS)
  val lo                = Output(BUS)
  val whilo             = Output(Bool())
  val mem_addr          = Output(BUS)
  val reg2              = Output(BUS)
  val reg_waddr         = Output(ADDR_BUS)
  val reg_wen           = Output(Bool())
  val reg_wdata         = Output(BUS)
  val cp0_wen           = Output(Bool())
  val cp0_waddr         = Output(CP0_ADDR_BUS)
  val cp0_wdata         = Output(BUS)
  val current_inst_addr = Output(BUS)
  val is_in_delayslot   = Output(Bool())
  val except_type       = Output(UInt(32.W))
  val pc                = Output(BUS)
}

// memory
class Memory_WriteBackStage extends Bundle {
  val pc          = Output(BUS)
  val LLbit_value = Output(Bool())
  val LLbit_wen   = Output(Bool())
  val reg_wdata   = Output(BUS)
  val reg_waddr   = Output(ADDR_BUS)
  val reg_wen     = Output(Bool())
  val whilo       = Output(Bool())
  val hi          = Output(BUS)
  val lo          = Output(BUS)
  val cp0_wen     = Output(Bool())
  val cp0_waddr   = Output(CP0_ADDR_BUS)
  val cp0_wdata   = Output(BUS)
}

class Memory_Decoder extends Bundle {
  val reg_waddr    = Output(ADDR_BUS)
  val reg_wdata    = Output(BUS)
  val reg_wen      = Output(Bool())
  // val inst_is_mfc0 = Output(Bool())
}

class Memory_Execute extends Bundle {
  val whilo     = Output(Bool())
  val hi        = Output(BUS)
  val lo        = Output(BUS)
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(CP0_ADDR_BUS)
  val cp0_wdata = Output(BUS)
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
}

class WriteBackStage_LLbitReg extends Bundle {
  val LLbit_value = Output(Bool())
  val LLbit_wen   = Output(Bool())
}

class WriteBackStage_Execute extends Bundle {
  val whilo     = Output(Bool())
  val hi        = Output(BUS)
  val lo        = Output(BUS)
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(CP0_ADDR_BUS)
  val cp0_wdata = Output(BUS)
}

class WriteBackStage_HILO extends Bundle {
  val whilo = Output(Bool())
  val hi    = Output(BUS)
  val lo    = Output(BUS)
}

class WriteBackStage_Memory extends Bundle {
  val LLbit_value = Output(Bool())
  val LLbit_wen   = Output(Bool())
  val cp0_wdata   = Output(BUS)
  val cp0_wen     = Output(Bool())
  val cp0_waddr   = Output(CP0_ADDR_BUS)
}

class WriteBackStage_RegFile extends Bundle {
  val reg_wdata = Output(BUS)
  val reg_waddr = Output(ADDR_BUS)
  val reg_wen   = Output(Bool())
}

class WriteBackStage_CP0 extends Bundle {
  val cp0_wdata = Output(BUS)
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(CP0_ADDR_BUS)
}

class WriteBackStage_FetchStage extends Bundle {
  val eret = Output(Bool())
  val ex   = Output(Bool())
}

// writeBack

// control
class Control_FetchStage extends Bundle {
  val stall  = Output(STALL_BUS)
  val new_pc = Output(BUS)
  val flush  = Output(Bool())
}

class Control_DecoderStage extends Bundle {
  val stall = Output(STALL_BUS)
  val flush = Output(Bool())
}

class Control_Decoder extends Bundle {
  val flush = Output(Bool())
}

class Control_ExecuteStage extends Bundle {
  val stall = Output(STALL_BUS)
  val flush = Output(Bool())
}

class Control_MemoryStage extends Bundle {
  val stall = Output(STALL_BUS)
  val flush = Output(Bool())
}

class Control_WriteBackStage extends Bundle {
  val stall = Output(STALL_BUS)
  val flush = Output(Bool())
}

class Control_Divider extends Bundle {
  val flush = Output(Bool())
}

class Control_LLbitReg extends Bundle {
  val flush = Output(Bool())
}

// instMemory
class InstMemory_FetchStage extends Bundle {
  val rdata = Output(BUS)
}

// dataMemory
class DataMemory_Memory extends Bundle {
  val mem_rdata = Output(BUS)
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

// divider
class Divider_Execute extends Bundle {
  val ready  = Output(Bool())
  val result = Output(DOUBLE_BUS)
}

// LLbitReg
class LLbitReg_Memory extends Bundle {
  val LLbit = Output(Bool())
}

// CP0
class CP0_Execute extends Bundle {
  val cp0_rdata = Output(BUS)
}

class CP0_Memory extends Bundle {
  val cause  = Output(BUS)
  val epc    = Output(BUS)
  val status = Output(BUS)
}

class CP0_Output extends Bundle {
  val count   = Output(BUS)
  val compare = Output(BUS)
  val config  = Output(BUS)
  val prid    = Output(BUS)
}

class CP0_FetchStage extends Bundle {
  val epc = Output(BUS)
}
// other
class INST_SRAM extends Bundle {
  val en    = Output(Bool())
  val wen   = Output(WEN_BUS)
  val addr  = Output(BUS)
  val wdata = Output(BUS)
  val rdata = Input(BUS)
}

class DATA_SRAM extends Bundle {
  val en    = Output(Bool())
  val wen   = Output(WEN_BUS)
  val addr  = Output(BUS)
  val wdata = Output(BUS)
  val rdata = Input(BUS)
}

class DEBUG extends Bundle {
  val pc    = Output(BUS)
  val wen   = Output(WEN_BUS)
  val waddr = Output(ADDR_BUS)
  val wdata = Output(BUS)
}
