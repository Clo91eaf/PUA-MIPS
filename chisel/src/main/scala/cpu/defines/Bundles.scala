package cpu.defines

import chisel3._
import chisel3.util._
import Const._

// pre fetch stage
class PreFetchStage_FetchStage extends Bundle {
  val valid      = Output(Bool())
  val inst_ok    = Output(Bool())
  val inst       = Output(BUS)
  val pc         = Output(BUS)
  val tlb_refill = Output(Bool())
  val ex         = Output(Bool())
  val badvaddr   = Output(UInt(32.W))
  val excode     = Output(UInt(5.W))
}

class PreFetchStage_InstMemory extends Bundle {
  val req     = Output(Bool())
  val waiting = Output(Bool())
}

class PreFetchStage_InstMMU extends Bundle {
  val vaddr = Output(UInt(32.W))
}

// fetch stage
class FetchStage_PreFetchStage extends Bundle {
  val valid       = Output(Bool())
  val allowin     = Output(Bool())
  val inst_unable = Output(Bool())
}

class FetchStage_DecoderStage extends Bundle {
  val valid      = Output(Bool())
  val tlb_refill = Output(Bool())
  val excode     = Output(UInt(5.W))
  val ex         = Output(Bool())
  val badvaddr   = Output(UInt(32.W))
  val inst       = Output(BUS)
  val pc         = Output(BUS)
}

class FetchStage_InstMemory extends Bundle {
  val waiting = Output(Bool())
}

// decoderStage
class DecoderStage_Decoder extends Bundle {
  // wire
  val do_flush = Output(Bool())
  val after_ex = Output(Bool())
  // reg
  val valid      = Output(Bool())
  val tlb_refill = Output(Bool())
  val excode     = Output(UInt(5.W))
  val ex         = Output(Bool())
  val badvaddr   = Output(UInt(32.W))
  val inst       = Output(BUS)
  val pc         = Output(BUS)
}

// decoder
class Decoder_PreFetchStage extends Bundle {
  val br_leaving_ds         = Output(Bool())
  val branch_stall          = Output(Bool())
  val branch_flag           = Output(Bool()) // 是否发生转移
  val branch_target_address = Output(BUS)    // 转移到的目标地址
}

class Decoder_FetchStage extends Bundle {
  val allowin = Output(Bool())
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
  val badvaddr               = Output(UInt(32.W))
  val cp0_addr               = Output(UInt(8.W))
  val excode                 = Output(UInt(5.W))
  val overflow_inst          = Output(Bool())
  val fs_to_ds_ex            = Output(Bool())
  val tlb_refill             = Output(Bool())
  val after_tlb              = Output(Bool())
  val mem_re                 = Output(Bool())
  val mem_we                 = Output(Bool())
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
  // wire
  val do_flush = Output(Bool())
  val after_ex = Output(Bool())
  // reg
  val valid           = Output(Bool())
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
  val bd              = Output(Bool())
  val badvaddr        = Output(UInt(32.W))
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val overflow_inst   = Output(Bool())
  val fs_to_ds_ex     = Output(Bool())
  val ds_to_es_ex     = Output(Bool())
  val tlb_refill      = Output(Bool())
  val after_tlb       = Output(Bool())
  val mem_re          = Output(Bool())
  val mem_we          = Output(Bool())
}

// execute
class Execute_ALU extends Bundle {
  val op  = Output(ALU_OP_BUS)
  val in1 = Output(BUS)
  val in2 = Output(BUS)
}

class Execute_Mul extends Bundle {
  val op  = Output(ALU_OP_BUS)
  val in1 = Output(BUS)
  val in2 = Output(BUS)
}

class Execute_Div extends Bundle {
  val op       = Output(ALU_OP_BUS)
  val divisor  = Output(BUS)
  val dividend = Output(BUS)
}

class Execute_Mov extends Bundle {
  val op   = Output(ALU_OP_BUS)
  val inst = Output(BUS)
  val in   = Output(BUS)
  val hi   = Output(BUS)
  val lo   = Output(BUS)
}

// execute and its component
class ALU_Execute extends Bundle {
  val out  = Output(BUS)
  val ov   = Output(Bool())
  val trap = Output(Bool())
}

class Mul_Execute extends Bundle {
  val out = Output(DOUBLE_BUS)
}

class Div_Execute extends Bundle {
  val quotient  = Output(BUS)
  val remainder = Output(BUS)
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
  val badvaddr        = Output(UInt(32.W))
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val ex              = Output(Bool())
  val data_ok         = Output(Bool())
  val data            = Output(BUS)
  val wait_mem        = Output(Bool())
  val res_from_mem    = Output(Bool())
  val tlb_refill      = Output(Bool())
  val after_tlb       = Output(Bool())
  val s1_found        = Output(Bool())
  val s1_index        = Output(UInt(log2Ceil(TLB_NUM).W))
  val cnt             = Output(CNT_BUS)
}

class Execute_CP0 extends Bundle {
  val cp0_raddr = Output(CP0_ADDR_BUS)
}

// dataMemory
class Execute_DataMemory extends Bundle {
  val aluop       = Output(ALU_OP_BUS)
  val addrLowBit2 = Output(UInt(2.W))
  val req         = Output(Bool())
  val wr          = Output(Bool())
  val size        = Output(UInt(2.W))
  val wdata       = Output(BUS)
  val wstrb       = Output(UInt(4.W))
  val waiting     = Output(Bool())
}

class DataMemory_SramAXITrans extends Bundle {
  val req     = Output(Bool())
  val wr      = Output(Bool())
  val size    = Output(UInt(2.W))
  val addr    = Output(BUS)
  val wstrb   = Output(UInt(4.W))
  val wdata   = Output(BUS)
  val addr_ok = Input(Bool())
  val data_ok = Input(Bool())
  val rdata   = Input(BUS)
}

class DataMemory_Execute extends Bundle {
  val addr_ok = Output(Bool())
  val rdata   = Output(BUS)
  val data_ok = Output(Bool())
}

class DataMemory_Memory extends Bundle {
  val data_ok = Output(Bool())
  val rdata   = Output(BUS)
}

// memoryStage
class MemoryStage_Execute extends Bundle {
  val cnt  = Output(CNT_BUS)
  val hilo = Output(DOUBLE_BUS)
}

class MemoryStage_Memory extends Bundle {
  // wire
  val do_flush = Output(Bool())
  val after_ex = Output(Bool())
  // reg
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
  val badvaddr        = Output(UInt(32.W))
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val ex              = Output(Bool())
  val data_ok         = Output(Bool())
  val data            = Output(BUS)
  val wait_mem        = Output(Bool())
  val res_from_mem    = Output(Bool())
  val tlb_refill      = Output(Bool())
  val after_tlb       = Output(Bool())
  val s1_found        = Output(Bool())
  val s1_index        = Output(UInt(log2Ceil(TLB_NUM).W))
}

// memory
class Memory_Mov extends Bundle {
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(Bool())
  val cp0_wdata = Output(Bool())
}

class Memory_MemoryStage extends Bundle {
  val allowin = Output(Bool())
}

class Memory_WriteBackStage extends Bundle {
  val pc              = Output(BUS)
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
  val badvaddr        = Output(UInt(32.W))
  val cp0_addr        = Output(UInt(8.W))
  val excode          = Output(UInt(5.W))
  val ex              = Output(Bool())
  val inst_is_tlbp    = Output(Bool())
  val inst_is_tlbr    = Output(Bool())
  val inst_is_tlbwi   = Output(Bool())
  val tlb_refill      = Output(Bool())
  val after_tlb       = Output(Bool())
  val s1_found        = Output(Bool())
  val s1_index        = Output(UInt(log2Ceil(TLB_NUM).W))
}

class Memory_DataMemory extends Bundle {
  val waiting = Output(Bool())
}

class Memory_Decoder extends Bundle {
  val reg_waddr    = Output(ADDR_BUS)
  val reg_wdata    = Output(BUS)
  val reg_wen      = Output(REG_WRITE_BUS)
  val inst_is_mfc0 = Output(Bool())
  val ms_fwd_valid = Output(Bool())
  val blk_valid    = Output(Bool())
}

class Memory_Execute extends Bundle {
  val whilo       = Output(Bool())
  val hi          = Output(BUS)
  val lo          = Output(BUS)
  val allowin     = Output(Bool())
  val inst_unable = Output(Bool())
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

class WriteBackStage_Mov extends Bundle {
  val cp0_wen   = Output(Bool())
  val cp0_waddr = Output(Bool())
  val cp0_wdata = Output(Bool())
  val cp0_rdata = Output(BUS)
}

class WriteBackStage_Decoder extends Bundle {
  val inst_is_mfc0 = Output(Bool())
  val reg_waddr    = Output(ADDR_BUS)
  val cp0_cause    = Output(UInt(32.W))
  val cp0_status   = Output(UInt(32.W))
}

class WriteBackStage_Execute extends Bundle {
  val whilo = Output(Bool())
  val hi    = Output(BUS)
  val lo    = Output(BUS)
}

class WriteBackStage_HILO extends Bundle {
  val whilo = Output(Bool())
  val hi    = Output(BUS)
  val lo    = Output(BUS)
}

class WriteBackStage_Memory extends Bundle {
  val allowin = Output(Bool())
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
  val tlbp        = Output(Bool())
  val tlbr        = Output(Bool())
  val tlbwi       = Output(Bool())
  val s1_found    = Output(Bool())
  val s1_index    = Output(UInt(4.W))
  val r_vpn2      = Output(UInt(19.W))
  val r_asid      = Output(UInt(8.W))
  val r_g         = Output(Bool())
  val r_pfn0      = Output(UInt(20.W))
  val r_c0        = Output(UInt(3.W))
  val r_d0        = Output(Bool())
  val r_v0        = Output(Bool())
  val r_pfn1      = Output(UInt(20.W))
  val r_c1        = Output(UInt(3.W))
  val r_d1        = Output(Bool())
  val r_v1        = Output(Bool())
}

class WriteBackStage_MMU extends Bundle {
  val cp0_entryhi = Output(UInt(32.W))
}

class WriteBackStage_TLB extends Bundle {
  val we      = Output(Bool())
  val w_index = Output(UInt(log2Ceil(TLB_NUM).W))
  val w_vpn2  = Output(UInt(19.W))
  val w_asid  = Output(UInt(8.W))
  val w_g     = Output(Bool())
  val w_pfn0  = Output(UInt(20.W))
  val w_c0    = Output(UInt(3.W))
  val w_d0    = Output(Bool())
  val w_v0    = Output(Bool())
  val w_pfn1  = Output(UInt(20.W))
  val w_c1    = Output(UInt(3.W))
  val w_d1    = Output(Bool())
  val w_v1    = Output(Bool())
  val r_index = Output(UInt(log2Ceil(TLB_NUM).W))
}

// instMemory

class InstMemory_PreFetchStage extends Bundle {
  val addr_ok = Output(Bool())
  val rdata   = Output(BUS)
  val data_ok = Output(Bool())
}

class InstMemory_FetchStage extends Bundle {
  val data_ok = Output(Bool())
  val rdata   = Output(BUS)
}

class InstMemory_SramAXITrans extends Bundle {
  val req     = Output(Bool())
  val wr      = Output(Bool())
  val size    = Output(UInt(2.W))
  val addr    = Output(BUS)
  val wstrb   = Output(UInt(4.W))
  val wdata   = Output(BUS)
  val data_ok = Input(Bool())
  val addr_ok = Input(Bool())
  val rdata   = Input(BUS)
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

// CP0
class CP0_WriteBackStage extends Bundle {
  val cp0_rdata    = Output(UInt(32.W))
  val cp0_status   = Output(UInt(32.W))
  val cp0_cause    = Output(UInt(32.W))
  val cp0_epc      = Output(UInt(32.W))
  val cp0_badvaddr = Output(UInt(32.W))
  val cp0_count    = Output(UInt(32.W))
  val cp0_compare  = Output(UInt(32.W))
  val cp0_entryhi  = Output(UInt(32.W))
  val cp0_entrylo0 = Output(UInt(32.W))
  val cp0_entrylo1 = Output(UInt(32.W))
  val cp0_index    = Output(UInt(32.W))
}

class AXI extends Bundle {
  // read request
  val arid    = Output(UInt(4.W))
  val araddr  = Output(UInt(32.W))
  val arlen   = Output(UInt(8.W))
  val arsize  = Output(UInt(3.W))
  val arburst = Output(UInt(2.W))
  val arlock  = Output(UInt(2.W))
  val arcache = Output(UInt(4.W))
  val arprot  = Output(UInt(3.W))
  val arvalid = Output(Bool())
  val arready = Input(Bool())

  // read response
  val rid    = Input(UInt(4.W))
  val rdata  = Input(UInt(32.W))
  val rresp  = Input(UInt(2.W))
  val rlast  = Input(Bool())
  val rvalid = Input(Bool())
  val rready = Output(Bool())

  // write request
  val awid    = Output(UInt(4.W))
  val awaddr  = Output(UInt(32.W))
  val awlen   = Output(UInt(8.W))
  val awsize  = Output(UInt(3.W))
  val awburst = Output(UInt(2.W))
  val awlock  = Output(UInt(2.W))
  val awcache = Output(UInt(4.W))
  val awprot  = Output(UInt(3.W))
  val awvalid = Output(Bool())
  val awready = Input(Bool())

  // write data
  val wid    = Output(UInt(4.W))
  val wdata  = Output(UInt(32.W))
  val wstrb  = Output(UInt(4.W))
  val wlast  = Output(Bool())
  val wvalid = Output(Bool())
  val wready = Input(Bool())

  // write response
  val bid    = Input(UInt(4.W))
  val bresp  = Input(UInt(2.W))
  val bvalid = Input(Bool())
  val bready = Output(Bool())
}

class DEBUG extends Bundle {
  val pc    = Output(BUS)
  val wen   = Output(WEN_BUS)
  val waddr = Output(ADDR_BUS)
  val wdata = Output(BUS)
}

//TLB MMU
class TLBCommon extends Bundle {
  val s0_index = Output(UInt(log2Ceil(TLB_NUM).W))
}

class TLB_WriteBackStage extends Bundle {
  val r_vpn2 = Output(UInt(19.W))
  val r_asid = Output(UInt(8.W))
  val r_g    = Output(Bool())
  val r_pfn0 = Output(UInt(20.W))
  val r_c0   = Output(UInt(3.W))
  val r_d0   = Output(Bool())
  val r_v0   = Output(Bool())
  val r_pfn1 = Output(UInt(20.W))
  val r_c1   = Output(UInt(3.W))
  val r_d1   = Output(Bool())
  val r_v1   = Output(Bool())
}

class TLB_MMU extends Bundle {
  val tlb_found = Output(Bool())
  val tlb_pfn   = Output(UInt(20.W))
  val tlb_c     = Output(UInt(3.W))
  val tlb_d     = Output(Bool())
  val tlb_v     = Output(Bool())
}

class TLB_Execute extends Bundle {
  val s1_found = Output(Bool())
  val s1_index = Output(UInt(log2Ceil(TLB_NUM).W))
}

// MMU
class MMU_TLB extends Bundle {
  val tlb_vpn2     = Output(UInt(19.W))
  val tlb_odd_page = Output(Bool())
  val tlb_asid     = Output(UInt(8.W))
}

class MMU_Common extends Bundle {
  val tlb_refill   = Output(Bool())
  val tlb_invalid  = Output(Bool())
  val tlb_modified = Output(Bool())
}

class Execute_DataMMU extends Bundle {
  val vaddr        = Output(UInt(32.W))
  val inst_is_tlbp = Output(Bool())
}

class MMU_Sram extends Bundle {
  val paddr = Output(UInt(32.W))
}

//Ctrl
class Ctrl_PreFetchStage extends Bundle {
  val after_ex = Output(Bool())
  val do_flush = Output(Bool())
  val flush_pc = Output(UInt(32.W))
  val block    = Output(Bool())
}

class Ctrl_FetchStage extends Bundle {
  val after_ex = Output(Bool())
  val do_flush = Output(Bool())
}

class Ctrl_InstMemory extends Bundle {
  val do_flush = Output(Bool())
}

class Ctrl_DataMemory extends Bundle {
  val do_flush = Output(Bool())
}

class Ctrl_DecoderStage extends Bundle {
  val do_flush = Output(Bool())
  val after_ex = Output(Bool())
}

class Ctrl_ExecuteStage extends Bundle {
  val do_flush = Output(Bool())
  val after_ex = Output(Bool())
}

class Ctrl_MemoryStage extends Bundle {
  val do_flush = Output(Bool())
  val after_ex = Output(Bool())
}

class InstMemory_Ctrl extends Bundle {
  val inst_sram_discard = Output(UInt(2.W))
}

class DataMemory_Ctrl extends Bundle {
  val data_sram_discard = Output(UInt(2.W))
}

class FetchStage_Ctrl extends Bundle {
  val ex = Output(Bool())
}

class Decoder_Ctrl extends Bundle {
  val ex = Output(Bool())
}

class Execute_Ctrl extends Bundle {
  val ex = Output(Bool())
}

class Memory_Ctrl extends Bundle {
  val ex = Output(Bool())
}

class WriteBackStage_Ctrl extends Bundle {
  val ex       = Output(Bool())
  val do_flush = Output(Bool())
  val flush_pc = Output(BUS)
}
