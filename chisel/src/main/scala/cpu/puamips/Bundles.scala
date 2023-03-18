package cpu.puamips

import chisel3._
import cpu.puamips.Const._

class Fetch_Decoder extends Bundle {
  val pc       = Output(RegBus)
}

class Fetch_InstMemory extends Bundle {
  val pc = Output(RegBus)
  val ce = Output(Bool())
}

class InstMemory_Decoder extends Bundle {
  val inst = Output(RegBus)
}

class Decoder_Execute extends Bundle {
  val aluop = Output(AluOpBus)
  val alusel = Output(AluSelBus)
  val reg1 = Output(RegBus)
  val reg2 = Output(RegBus)
  val wd = Output(RegAddrBus)
  val wreg = Output(Bool())
}

class Decoder_RegFile extends Bundle {
  val reg1_addr = Output(RegAddrBus)
  val reg1_read = Output(Bool())
  val reg2_addr = Output(RegAddrBus)
  val reg2_read = Output(Bool())
}

class RegFile_Decoder extends Bundle {
  val rdata1 = Output(RegBus)
  val rdata2 = Output(RegBus)
}

class Execute_Decoder extends Bundle {
  val wdata = Output(RegBus)
  val wd = Output(RegAddrBus)
  val wreg = Output(Bool())
}

class Execute_Memory extends Bundle {
  val wdata = Output(RegBus)
  val wd = Output(RegAddrBus)
  val wreg = Output(Bool())
}

class Memory_Decoder extends Bundle {
  val wdata = Output(RegBus)
  val wd = Output(RegAddrBus)
  val wreg = Output(Bool())
}

class Memory_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi = Output(RegBus)
  val lo = Output(RegBus)
}

class Memory_WriteBack extends Bundle {
  val wdata = Output(RegBus)
  val wd = Output(RegAddrBus)
  val wreg = Output(Bool())
  val whilo = Output(Bool())
  val hi = Output(RegBus)
  val lo = Output(RegBus)
}

class WriteBack_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi = Output(RegBus)
  val lo = Output(RegBus)
}

class WriteBack_RegFile extends Bundle {
  val wdata = Output(RegBus)
  val wd = Output(RegAddrBus)
  val wreg = Output(Bool())
}

class WriteBack_HILO extends Bundle {
  val we = Output(Bool())
  val hi = Output(RegBus)
  val lo = Output(RegBus)
}

class HILO_WriteBack extends Bundle {
  val hi = Output(RegBus)
  val lo = Output(RegBus)
}