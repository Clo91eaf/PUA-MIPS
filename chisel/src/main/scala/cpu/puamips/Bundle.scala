// package cpu.puamips

// import chisel3._
// import Const._

// class IF_ID extends Bundle {
//   val pc       = Output(UInt(WORD_LEN.W))
//   val pc_plus4 = Output(UInt(WORD_LEN.W))
// }

// class ID_EX extends Bundle {
//   val pc          = Output(UInt(WORD_LEN.W))
//   val pc_plus4    = Output(UInt(WORD_LEN.W))
//   val inst        = Output(UInt(INST_LEN.W))
//   val rs1_data    = Output(UInt(WORD_LEN.W))
//   val rs2_data    = Output(UInt(WORD_LEN.W))
//   val rs1_addr    = Output(UInt(RF_ADDR_LEN.W))
//   val rs2_addr    = Output(UInt(RF_ADDR_LEN.W))
//   val rd_addr     = Output(UInt(WORD_LEN.W))
//   val exe_signal  = Output(UInt(ALU_LEN.W))
//   val op1_data    = Output(UInt(WORD_LEN.W))
//   val op2_data    = Output(UInt(WORD_LEN.W))
//   val read_signal = Output(UInt(RS_LEN.W))
//   val wb_signal   = Output(UInt(WS_LEN.W))
//   val imm_i       = Output(UInt(WORD_LEN.W))
//   val imm_i_sext  = Output(UInt(WORD_LEN.W))
//   val imm_j       = Output(UInt(WORD_LEN.W))
//   val imm_j_sext  = Output(UInt(WORD_LEN.W))
//   val imm_u       = Output(UInt(WORD_LEN.W))
//   val imm_u_sext  = Output(UInt(WORD_LEN.W))
// }

// class RD_RF extends Bundle {
//   val addr = Output(UInt(RF_ADDR_LEN.W))
//   val data = Input(UInt(WORD_LEN.W))
// }

// class RD_RF2 extends Bundle {
//   val rd1 = new RD_RF()
//   val rd2 = new RD_RF()
// }

// class WR_RF extends Bundle {
//   val addr = Output(UInt(RF_ADDR_LEN.W))
//   val data = Output(UInt(WORD_LEN.W))
// }

// class EX_MEM extends Bundle {
//   val wb_data = Output(UInt(WORD_LEN.W))

//   val pc       = Output(UInt(WORD_LEN.W))
//   val pc_plus4 = Output(UInt(WORD_LEN.W))
//   val inst     = Output(UInt(INST_LEN.W))

//   val rs1_data = Output(UInt(WORD_LEN.W))
//   val rs2_data = Output(UInt(WORD_LEN.W))
//   val rd_addr  = Output(UInt(WORD_LEN.W))

//   val exe_signal  = Output(UInt(ALU_LEN.W))
//   val op1_data    = Output(UInt(WORD_LEN.W))
//   val op2_data    = Output(UInt(WORD_LEN.W))
//   val read_signal = Output(UInt(RS_LEN.W))
//   val wb_signal   = Output(UInt(WS_LEN.W))

//   val imm_i      = Output(UInt(WORD_LEN.W))
//   val imm_i_sext = Output(UInt(WORD_LEN.W))
//   val imm_j      = Output(UInt(WORD_LEN.W))
//   val imm_j_sext = Output(UInt(WORD_LEN.W))
//   val imm_u      = Output(UInt(WORD_LEN.W))
//   val imm_u_sext = Output(UInt(WORD_LEN.W))
// }

// class MEM_WB extends Bundle {
//   val wb_data = Output(UInt(WORD_LEN.W))

//   val pc       = Output(UInt(WORD_LEN.W))
//   val pc_plus4 = Output(UInt(WORD_LEN.W))
//   val inst     = Output(UInt(INST_LEN.W))

//   val rs1_data = Output(UInt(WORD_LEN.W))
//   val rs2_data = Output(UInt(WORD_LEN.W))
//   val rd_addr  = Output(UInt(WORD_LEN.W))

//   val exe_signal  = Output(UInt(ALU_LEN.W))
//   val op1_data    = Output(UInt(WORD_LEN.W))
//   val op2_data    = Output(UInt(WORD_LEN.W))
//   val read_signal = Output(UInt(RS_LEN.W))
//   val wb_signal   = Output(UInt(WS_LEN.W))

//   val imm_i      = Output(UInt(WORD_LEN.W))
//   val imm_i_sext = Output(UInt(WORD_LEN.W))
//   val imm_j      = Output(UInt(WORD_LEN.W))
//   val imm_j_sext = Output(UInt(WORD_LEN.W))
//   val imm_u      = Output(UInt(WORD_LEN.W))
//   val imm_u_sext = Output(UInt(WORD_LEN.W))
// }
// class EX_Ctrl extends Bundle {
//   val jump_addr   = Output(UInt(WORD_LEN.W))
//   val jump_flag   = Output(UInt(INST_LEN.W))
//   val flush       = Output(Bool())
//   val rs1_addr = Output(UInt(RF_ADDR_LEN.W))
//   val rs2_addr = Output(UInt(RF_ADDR_LEN.W))
// }

// class MEM_Ctrl extends Bundle {
//   val rd_addr = Output(UInt(RF_ADDR_LEN.W))
// }

// class MEM_EX extends Bundle {
//   val wb_data = Output(UInt(WORD_LEN.W))
// }

// class WB_Ctrl extends Bundle {
//   val rd_addr = Output(UInt(RF_ADDR_LEN.W))
// }

// class WB_EX extends Bundle {
//   val wb_data = Output(UInt(WORD_LEN.W))
// }

// class Ctrl_IF extends Bundle {
//   val jump_flag = Output(Bool())
//   val jump_addr = Output(UInt(WORD_LEN.W))
// }

// class Ctrl_ID extends Bundle {
//   val flush       = Output(Bool())
// }

// class Ctrl_EX extends Bundle {
//   val flush = Output(Bool())
//   val forward1 = Output(UInt(FORWARD_LEN.W))
//   val forward2 = Output(UInt(FORWARD_LEN.W))
// }