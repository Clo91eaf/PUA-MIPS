package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Fetch_Decoder extends Bundle {
  val pc = Output(REG_BUS)
  val inst = Output(REG_BUS)
}

class Decoder_Fetch extends Bundle {
  val branch_flag = Output(Bool()) // 是否发生转移
  val branch_target_address = Output(REG_BUS) // 转移到的目标地址
}

class Fetch_InstMemory extends Bundle {
  val pc = Output(REG_BUS)
  val ce = Output(Bool())
}

class InstMemory_Fetch extends Bundle {
  val inst = Output(REG_BUS)
}

class Decoder_Execute extends Bundle {
  val pc = Output(REG_BUS)
  val aluop = Output(ALU_OP_BUS)
  val alusel = Output(ALU_SEL_BUS)
  val reg1 = Output(REG_BUS)
  val reg2 = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
  val link_addr = Output(REG_BUS)
}

class Decoder_RegFile extends Bundle {
  val reg1_addr = Output(REG_ADDR_BUS)
  val reg1_read = Output(Bool())
  val reg2_addr = Output(REG_ADDR_BUS)
  val reg2_read = Output(Bool())
}

class RegFile_Decoder extends Bundle {
  val rdata1 = Output(REG_BUS)
  val rdata2 = Output(REG_BUS)
}

class Execute_Decoder extends Bundle {
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
}

class Execute_Memory extends Bundle {
  val pc = Output(REG_BUS)
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
}

class Memory_Decoder extends Bundle {
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
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

class HILO_WriteBack extends Bundle {
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

class DEBUG extends Bundle {
  val pc = Output(REG_BUS)
  val wdata = Output(REG_BUS)
  val waddr = Output(REG_ADDR_BUS)
  val wen = Output(Bool())
}
