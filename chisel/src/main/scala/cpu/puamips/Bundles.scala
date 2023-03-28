package cpu.puamips

import Const._
import chisel3._

// fetch
class Fetch_DecoderStage extends Bundle {
  val pc = Output(REG_BUS)
}

class Fetch_InstMemory extends Bundle {
  val pc = Output(REG_BUS)
  val ce = Output(Bool())
}

// decoderStage
class DecoderStage_Decoder extends Bundle {
  val pc = Output(INST_ADDR_BUS)
  val inst = Output(INST_BUS)
}

// decoder
class Decoder_Fetch extends Bundle {
  val branch_flag = Output(Bool()) // 是否发生转移
  val branch_target_address = Output(REG_BUS) // 转移到的目标地址
}

class Decoder_ExecuteStage extends Bundle {
  val aluop = Output(ALU_OP_BUS)
  val alusel = Output(ALU_SEL_BUS)
  val inst = Output(REG_BUS)
  val is_in_delayslot = Output(Bool())
  val link_addr = Output(REG_BUS)
  val reg1 = Output(REG_BUS)
  val reg2 = Output(REG_BUS)
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
  val next_inst_in_delayslot = Output(Bool())
}

class Decoder_RegFile extends Bundle {
  val reg1_addr = Output(REG_ADDR_BUS)
  val reg1_read = Output(Bool())
  val reg2_addr = Output(REG_ADDR_BUS)
  val reg2_read = Output(Bool())
}

class Decoder_Control extends Bundle {
  val stallreq = Output(Bool())
}

// executeStage
class ExecuteStage_Decoder extends Bundle {
  val is_in_delayslot = Output(Bool())
}

// execute
class Execute_Decoder extends Bundle {
  val aluop = Output(ALU_OP_BUS)
  val wd = Output(REG_ADDR_BUS)
  val wdata = Output(REG_BUS)
  val wreg = Output(Bool())
}

class Execute_Memory extends Bundle {
  val pc = Output(REG_BUS)
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
  val aluop = Output(ALU_OP_BUS)
  val addr = Output(REG_BUS)
  val reg2 = Output(REG_BUS)
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
  val whilo = Output(Bool())
}

class Execute_Control extends Bundle {
  val stallreq = Output(Bool())
}

// memoryStage

// memory
class Memory_Decoder extends Bundle {
  val wd = Output(REG_ADDR_BUS)
  val wdata = Output(REG_BUS)
  val wreg = Output(Bool())
}

class Memory_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

class Memory_WriteBack extends Bundle {
  val pc = Output(REG_BUS)
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
  val whilo = Output(Bool())
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

class Memory_DataMemory extends Bundle {
  val addr = Output(REG_BUS)
  val wen = Output(Bool())
  val sel = Output(DATA_MEMORY_SEL_BUS)
  val data = Output(REG_BUS)
  val ce = Output(Bool())
}

// writeBackStage

// writeBack
class WriteBack_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

class WriteBack_RegFile extends Bundle {
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
}

class WriteBack_HILO extends Bundle {
  val whilo = Output(Bool())
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

// control
class Control_Fetch extends Bundle {
  val stall = Output(STALL_BUS)
}

class Control_DecoderStage extends Bundle {
  val stall = Output(STALL_BUS)
}

// instMemory
class InstMemory_DecoderStage extends Bundle {
  val inst = Output(REG_BUS)
}

// dataMemory
class DataMemory_Memory extends Bundle {
  val data = Output(REG_BUS)
}

// regFile
class RegFile_Decoder extends Bundle {
  val reg1_data = Output(REG_BUS)
  val reg2_data = Output(REG_BUS)
}

// HILO
class HILO_WriteBack extends Bundle {
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

// other
class INST_SRAM extends Bundle {
  val en = Output(Bool())
  val wen = Output(WEN_BUS)
  val addr = Output(REG_BUS)
  val wdata = Output(REG_BUS)
  val rdata = Input(REG_BUS)
}

class DATA_SRAM extends Bundle {
  val en = Output(Bool())
  val wen = Output(WEN_BUS)
  val addr = Output(REG_BUS)
  val wdata = Output(REG_BUS)
  val rdata = Input(REG_BUS)
}

class DEBUG extends Bundle {
  val pc = Output(REG_BUS)
  val wen = Output(WEN_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wdata = Output(REG_BUS)
}
