/*------------------------------------------------------------------------------
--------------------------------------------------------------------------------
Copyright (c) 2016, Loongson Technology Corporation Limited.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this 
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice, 
this list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

3. Neither the name of Loongson Technology Corporation Limited nor the names of 
its contributors may be used to endorse or promote products derived from this 
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
DISCLAIMED. IN NO EVENT SHALL LOONGSON TECHNOLOGY CORPORATION LIMITED BE LIABLE
TO ANY PARTY FOR DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE 
GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) 
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT 
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
--------------------------------------------------------------------------------
------------------------------------------------------------------------------*/

/****** Configuration ******/
`define LS132R_INST_SRAM_MASK     32'h0000_0000
//`define LS132R_INST_SRAM_WINDOW   32'hffff_ffff
`define LS132R_INST_SRAM_WINDOW   32'h0000_0000

`define LS132R_DATA_SRAM_MASK     32'h0000_0000
//`define LS132R_DATA_SRAM_WINDOW   32'hffff_ffff
`define LS132R_DATA_SRAM_WINDOW   32'h0000_0000

`define LS132R_IBKP_NUM     4'd1

/****** Bus Width ******/
`define Lcpustbus       26
`define Lirbus          68
`define Lissuebus       260
`define Lwbbus          109
`define Lexbus          47
`define Lbrbus          68
`define Ldecstbus       37
`define Lexestbus       4
`define Ltoifcbus       74
`define Lfromifcbus     35
`define Ldmsegdreqbus   68
`define Ldmsegireqbus   35
`define Ldmsegdresbus   34
`define Ldmsegiresbus   34
`define Licompbus       33
`define Ldrsegreqbus    66

/****** Internal bus width ******/
`define LS132R_WIDTH_BLOCK  4'b0001 
`define LS132R_WIDTH_64BIT  4'b1111 
`define LS132R_WIDTH_24BIT  4'b1011 
`define LS132R_WIDTH_32BIT  4'b1010 
`define LS132R_WIDTH_16BIT  4'b1001 
`define LS132R_WIDTH_8BIT   4'b1000 
`define LS132R_WIDTH_FULL   4'b0000 

/****** Internal Excode ******/
/* EX_RESET     */  /* EX_EJTAGBOOT */  
/* EX_SOFTRESET */ 
/* EX_NMI       */ 
`define LS132R_EX_INT          6'h00 //Interrupt
`define LS132R_EX_ADEL         6'h04 //Address Error (load or fetch)
`define LS132R_EX_ADES         6'h05 //Address Error (store)
`define LS132R_EX_IBE          6'h06 //Bus Error (instruction fetch)
`define LS132R_EX_DBE          6'h07 //Bus Error (load or store)
`define LS132R_EX_SYS          6'h08 //Syscall
`define LS132R_EX_BP           6'h09 //Breakpoint
`define LS132R_EX_RI           6'h0a //Reserved Instruction
`define LS132R_EX_CPU          6'h0b //Coprocessor Unusable
`define LS132R_EX_OV           6'h0c //Arithmatic Overflow
`define LS132R_EX_TRAP         6'h0d //Trap
`define LS132R_EX_FPE          6'h0f //Float Point Exception
`define LS132R_EX_WATCH        6'h17 //Reference to WatchHi/WatchLo Address
`define LS132R_EX_MCHECK       6'h18 //Machine Check
`define LS132R_EX_CACHEERR     6'h1e //Cache Error
`define LS132R_EX_NMI          6'h10 //NMI

`define LS132R_EX_DSS          6'h20 //Debug Sigle Step
`define LS132R_EX_DBP          6'h21 //Debug Breakpoint
`define LS132R_EX_DDBL         6'h22 //Debug Data Break Load
`define LS132R_EX_DDBS         6'h23 //Debug Data Break Store
`define LS132R_EX_DIB          6'h24 //Debug Instruction Break
`define LS132R_EX_DINT         6'h25 //Debug Interrupt
`define LS132R_EX_DDBLIMPR     6'h32 //Debug Data Break Load Imprecise
`define LS132R_EX_DDBSIMPR     6'h33 //Debug Data Break Store Imprecise

`define LS132R_EX_WAIT         6'h1f //internal exception for handling inst WAIT


/****** Vector Interrupt Entries ******/
`define LS132R_INT0_ENTRY_BEV0  32'h8000_0200
`define LS132R_INT1_ENTRY_BEV0  32'h8000_0220
`define LS132R_INT2_ENTRY_BEV0  32'h8000_0240
`define LS132R_INT3_ENTRY_BEV0  32'h8000_0260
`define LS132R_INT4_ENTRY_BEV0  32'h8000_0280
`define LS132R_INT5_ENTRY_BEV0  32'h8000_02a0
`define LS132R_INT6_ENTRY_BEV0  32'h8000_02c0
`define LS132R_INT7_ENTRY_BEV0  32'h8000_02e0

/****** Internal Op & Fmt Code ******/
/*Fix point operation*/
`define LS132R_OP_CLO      8'h00 
`define LS132R_OP_CLZ      8'h01 
`define LS132R_OP_EXT      8'h02
`define LS132R_OP_INS      8'h03
`define LS132R_OP_WSBH     8'h04
`define LS132R_OP_MOV      8'h05 //LUI,RDPGPR,WRPGPR
`define LS132R_OP_ROTR     8'h06 //ROTRV
`define LS132R_OP_SEB      8'h08
`define LS132R_OP_SEH      8'h09
`define LS132R_OP_MOVN     8'h0a 
`define LS132R_OP_MOVZ     8'h0b 
`define LS132R_OP_MFHI     8'h0c 
`define LS132R_OP_MFLO     8'h0d 
`define LS132R_OP_MTHI     8'h0e 
`define LS132R_OP_MTLO     8'h0f 
`define LS132R_OP_MUL      8'h10 
`define LS132R_OP_SLL      8'h11 //NOP,SSNOP,EHB,SLLV
`define LS132R_OP_SRL      8'h12 //SRLV
`define LS132R_OP_SRA      8'h13
`define LS132R_OP_MULT     8'h14 
`define LS132R_OP_MULTU    8'h15 
`define LS132R_OP_DIV      8'h16
`define LS132R_OP_DIVU     8'h17
`define LS132R_OP_ADD      8'h18 //ADDI
`define LS132R_OP_ADDU     8'h19 //ADDIU,
`define LS132R_OP_SUB      8'h1a
`define LS132R_OP_SUBU     8'h1b
`define LS132R_OP_AND      8'h1c //ANDI
`define LS132R_OP_OR       8'h1d //ORI
`define LS132R_OP_XOR      8'h1e //XORI
`define LS132R_OP_NOR      8'h1f
`define LS132R_OP_TEQ      8'h20 //TEQI
`define LS132R_OP_TNE      8'h21 //TNEI
`define LS132R_OP_TLT      8'h22 //TLTI
`define LS132R_OP_TLTU     8'h23 //TLTIU
`define LS132R_OP_TGE      8'h24 //TGEI`
`define LS132R_OP_TGEU     8'h25 //TEEI
`define LS132R_OP_SLT      8'h26 //SLTI
`define LS132R_OP_SLTU     8'h27 //SLTIU
`define LS132R_OP_MADD     8'h28 
`define LS132R_OP_MADDU    8'h29 
`define LS132R_OP_MSUB     8'h2a 
`define LS132R_OP_MSUBU    8'h2b 
`define LS132R_OP_J        8'h2c
`define LS132R_OP_JR       8'h2d //JR.HB
`define LS132R_OP_JAL      8'h2e 
`define LS132R_OP_JALR     8'h2f //JALR.HB
`define LS132R_OP_BEQ      8'h30
`define LS132R_OP_BNE      8'h31
`define LS132R_OP_BLEZ     8'h32
`define LS132R_OP_BGTZ     8'h33
`define LS132R_OP_BLTZ     8'h34
`define LS132R_OP_BGEZ     8'h35
`define LS132R_OP_BLTZAL   8'h36
`define LS132R_OP_BGEZAL   8'h37
`define LS132R_OP_BEQL     8'h38
`define LS132R_OP_BNEL     8'h39
`define LS132R_OP_BLEZL    8'h3a
`define LS132R_OP_BGTZL    8'h3b
`define LS132R_OP_BLTZL    8'h3c
`define LS132R_OP_BGEZL    8'h3d
`define LS132R_OP_BLTZALL  8'h3e
`define LS132R_OP_BGEZALL  8'h3f
 
/*CP0 operation*/
`define LS132R_OP_MFC1     8'h80 
`define LS132R_OP_CFC1     8'h81 
`define LS132R_OP_MTC1     8'h82 
`define LS132R_OP_CTC1     8'h83 
`define LS132R_OP_DI       8'h84 
`define LS132R_OP_EI       8'h85 
`define LS132R_OP_SYNC     8'h86
`define LS132R_OP_ERET     8'h87
`define LS132R_OP_MFC0     8'h8c //RDHWR
`define LS132R_OP_MTC0     8'h8d
`define LS132R_OP_SYNCI    8'h8e 
`define LS132R_OP_DERET    8'h8f
`define LS132R_OP_LB       8'h90
`define LS132R_OP_LH       8'h91
`define LS132R_OP_LW       8'h92 
`define LS132R_OP_LDC1     8'h93
`define LS132R_OP_LBU      8'h94
`define LS132R_OP_LHU      8'h95
`define LS132R_OP_LL       8'h96
`define LS132R_OP_PREF     8'h97
`define LS132R_OP_SB       8'h98
`define LS132R_OP_SH       8'h99
`define LS132R_OP_SW       8'h9a 
`define LS132R_OP_SDC1     8'h9b 
`define LS132R_OP_LBUX     8'h9c 
`define LS132R_OP_LHX      8'h9d 
`define LS132R_OP_SC       8'h9e
`define LS132R_OP_LWX      8'h9f 
`define LS132R_OP_LWC1     8'ha2
`define LS132R_OP_SWC1     8'ha3
`define LS132R_OP_PREFX    8'ha6
`define LS132R_OP_LWL      8'hb8
`define LS132R_OP_LWR      8'hb9
`define LS132R_OP_SWL      8'hba
`define LS132R_OP_SWR      8'hbb
 
`define LS132R_CCA_UNCACHE  3'b010
`define LS132R_CCA_CACHE    3'b011

/****** MIPS32 Op & Fmt Code ******/
`define MIPS32_FMT_S        5'h10
`define MIPS32_FMT_D        5'h11
`define MIPS32_FMT_W        5'h14
`define MIPS32_FMT_L        5'h15
`define MIPS32_FMT_PS       5'h16

`define MIPS32_COND_F       4'h0
`define MIPS32_COND_UN      4'h1
`define MIPS32_COND_EQ      4'h2
`define MIPS32_COND_UEQ     4'h3
`define MIPS32_COND_OLT     4'h4
`define MIPS32_COND_ULT     4'h5
`define MIPS32_COND_OLE     4'h6
`define MIPS32_COND_ULE     4'h7
`define MIPS32_COND_SF      4'h8
`define MIPS32_COND_NGLE    4'h9
`define MIPS32_COND_SEQ     4'ha
`define MIPS32_COND_NGL     4'hb
`define MIPS32_COND_LT      4'hc
`define MIPS32_COND_NGE     4'hd
`define MIPS32_COND_LE      4'he
`define MIPS32_COND_NGT     4'hf


/****** AXI Parameter ******/
// burst type
`define AXI_BURST_FIXED 2'b00
`define AXI_BURST_INCR  2'b01
`define AXI_BURST_WRAP  2'b10

// burst length
`define AXI_LEN_1       4'h0
`define AXI_LEN_2       4'h1
`define AXI_LEN_4       4'h3
`define AXI_LEN_8       4'h7

// burst size
`define AXI_SIZE_1B     3'b000 
`define AXI_SIZE_2B     3'b001
`define AXI_SIZE_4B     3'b010
`define AXI_SIZE_8B     3'b011
`define AXI_SIZE_16B    3'b100

// lock type
`define AXI_LOCK_NORMAL 2'b00

// cache & coherence attribute
`define AXI_NO_CACHE_NO_BUF 4'b0000 

// prot attribute
`define AXI_PROT0_NORMAL    1'b0
`define AXI_PROT1_NONSECURE 1'b1
`define AXI_PROT2_DATA      1'b1
`define AXI_PROT2_INST      1'b0



/****** EJTAG parameter ******/
`define EJTAG_ECR_PSZ_BYTE      2'd0
`define EJTAG_ECR_PSZ_HALFWORD  2'd1
`define EJTAG_ECR_PSZ_WORD      2'd2
`define EJTAG_ECR_PSZ_TRIPLE    2'd3

