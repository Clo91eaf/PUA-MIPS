package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Fetch_Decoder extends Bundle {
  val pc = Output(REG_BUS)
}

class Fetch_Top extends Bundle {
  val pc = Output(REG_BUS)
  val ce = Output(Bool())
}

class Top_InstMemory extends Bundle {
  val pc = Output(REG_BUS)
  val ce = Output(Bool())
}

class InstMemory_Top extends Bundle {
  val inst = Output(REG_BUS)
}

class Top_Decoder extends Bundle {
  val inst = Output(REG_BUS)
}

class Decoder_Execute extends Bundle {
  val aluop = Output(ALU_OP_BUS)
  val alusel = Output(ALU_SEL_BUS)
  val reg1 = Output(REG_BUS)
  val reg2 = Output(REG_BUS)
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
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
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
}

class Execute_Memory extends Bundle {
  val wdata = Output(REG_BUS)
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
}

class Memory_Decoder extends Bundle {
  val wdata = Output(REG_BUS)
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
}

class Memory_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

class Memory_WriteBack extends Bundle {
  val wdata = Output(REG_BUS)
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
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
  val wd = Output(REG_ADDR_BUS)
  val wreg = Output(Bool())
}

class WriteBack_HILO extends Bundle {
  val we = Output(Bool())
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}

class HILO_WriteBack extends Bundle {
  val hi = Output(REG_BUS)
  val lo = Output(REG_BUS)
}