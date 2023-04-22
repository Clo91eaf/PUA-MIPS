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

`include "ls132r_define.h"

module ls132r_execute_stage(
  clock,
  reset,
  
  issuebus_i,
  dec_status_i,
  hw_int_i,
  cpu_status_o,
  exbus_o,
  brbus_o,
  ex_int_o,
  wbbus_o,
  exe_status_o,
  status_nmi_o,

  ifc_data_i,
  data_ifc_o 

 ,ejtagboot_i //set DM during ejtagboot
 ,ejtagbrk_i
 ,proben_i
 ,dmseg_dres_i
 ,dmseg_dreq_o
 ,drseg_res_dcr_i
 ,dcr_req_o
 ,drseg_res_hb_i
 ,hb_req_o

 //debug interface
 ,debug_wb_pc,
 ,debug_wb_rf_wen,
 ,debug_wb_rf_wnum,
 ,debug_wb_rf_wdata
);

input                       clock;
input                       reset;

input  [`Lissuebus-1:0]     issuebus_i;
input  [`Ldecstbus-1:0]     dec_status_i;
input  [5:0]                hw_int_i;
output [`Lcpustbus-1:0]     cpu_status_o;
output [`Lexbus-1:0]        exbus_o;
output [`Lbrbus-1:0]        brbus_o;
output                      ex_int_o;
output [`Lwbbus-1:0]        wbbus_o;
output [`Lexestbus-1:0]     exe_status_o;
output                      status_nmi_o;

input  [`Lfromifcbus-1:0]   ifc_data_i;
output [`Ltoifcbus-1:0]     data_ifc_o;

input                       ejtagboot_i;
input                       ejtagbrk_i;
input                       proben_i;
input  [`Ldmsegdresbus-1:0] dmseg_dres_i;
output [`Ldmsegdreqbus-1:0] dmseg_dreq_o;
input  [              31:0] drseg_res_dcr_i;
output [ `Ldrsegreqbus-1:0] dcr_req_o;
input  [              31:0] drseg_res_hb_i;
output [ `Ldrsegreqbus-1:0] hb_req_o;

//debug interface
output  [31:0]   debug_wb_pc;
output  [3 :0]   debug_wb_rf_wen;
output  [4 :0]   debug_wb_rf_wnum;
output  [31:0]   debug_wb_rf_wdata;

wire        issue_valid      = issuebus_i[  0:  0];
wire        issue_bd         = issuebus_i[  1:  1];
wire        issue_ex         = issuebus_i[  2:  2];
wire [ 5:0] issue_excode     = issuebus_i[  8:  3];
wire [31:0] issue_pc         = issuebus_i[ 40:  9];
wire [ 7:0] issue_op         = issuebus_i[ 48: 41];
wire [ 4:0] issue_fmt        = issuebus_i[ 53: 49];
wire        issue_float_mult = issuebus_i[ 54: 54];
wire [ 6:0] issue_dest       = issuebus_i[ 61: 55];
wire        issue_v1_en      = issuebus_i[ 62: 62];
wire [31:0] issue_v1         = issuebus_i[ 94: 63];
wire        issue_v2_en      = issuebus_i[ 95: 95];
wire [31:0] issue_v2         = issuebus_i[127: 96];
wire        issue_v3_en      = issuebus_i[128:128];
wire [31:0] issue_v3         = issuebus_i[160:129];
wire        issue_v1_h_en    = issuebus_i[161:161];
wire [31:0] issue_v1_h       = issuebus_i[193:162];
wire        issue_v2_h_en    = issuebus_i[194:194];
wire [31:0] issue_v2_h       = issuebus_i[226:195];
wire        issue_v3_h_en    = issuebus_i[227:227];
wire [31:0] issue_v3_h       = issuebus_i[259:228];


wire        inst_stall_in_ir = dec_status_i[ 0: 0];
wire        br_in_ir         = dec_status_i[ 1: 1];
wire        wait_sleep       = dec_status_i[ 2: 2];
//wire        bd_in_ir         = dec_status_i[ 3: 3];
wire [31:0] bd_pc            = dec_status_i[35: 4];
wire        idle_in_ir       = dec_status_i[36:36];


wire        kernel_mode;
wire        super_mode ;
wire        user_mode  ;
wire        cause_iv   ;
wire        status_bev ;
wire        status_erl ;
wire [ 2:0] status_cu  ;
wire [ 3:0] HWREna     ;
wire        debug_mode ;
wire        dss_enable ;
wire        nodcr      ;
wire        dint       ;
wire        lsnm       ;
wire [ 7:0] int_vec    ;

assign cpu_status_o[ 0: 0] = kernel_mode;
assign cpu_status_o[ 1: 1] = super_mode ;
assign cpu_status_o[ 2: 2] = user_mode  ;
assign cpu_status_o[ 3: 3] = cause_iv   ;
assign cpu_status_o[ 4: 4] = status_bev ;
assign cpu_status_o[ 5: 5] = status_erl ;
assign cpu_status_o[ 8: 6] = status_cu  ;
assign cpu_status_o[12: 9] = HWREna     ;
assign cpu_status_o[13:13] = debug_mode ;
assign cpu_status_o[14:14] = dss_enable ;
assign cpu_status_o[15:15] = nodcr      ;
assign cpu_status_o[16:16] = dint       ;
assign cpu_status_o[17:17] = lsnm       ;
assign cpu_status_o[25:18] = int_vec    ;


wire        ex_valid ;
wire        ex_delay1;
wire [ 5:0] excode_delay1;
wire [ 5:0] ex_excode;
wire [31:0] ex_pc;
reg         ex_nmi_delay1;

assign exbus_o[ 0: 0] = ex_valid ;
assign exbus_o[ 1: 1] = ex_delay1 ;
assign exbus_o[ 7: 2] = excode_delay1;
assign exbus_o[13: 8] = ex_excode;
assign exbus_o[45:14] = ex_pc;
assign exbus_o[46:46] = ex_nmi_delay1;

wire        isa_ex_valid;

wire        br_taken    ; 
wire        br_not_taken;
wire        br_likely   ;
wire [31:0] br_base     ;
wire [31:0] br_offset   ;
wire        br_eret     ;

assign brbus_o[ 0: 0] = br_taken    ; 
assign brbus_o[ 1: 1] = br_not_taken;
assign brbus_o[ 2: 2] = br_likely   ;
assign brbus_o[34: 3] = br_base     ;
assign brbus_o[66:35] = br_offset   ;
assign brbus_o[67:67] = br_eret     ;


wire        wb_gr_wen    ;
wire [ 4:0] wb_gr_num    ;
wire [31:0] wb_gr_value  ;
wire        wb_fr_wen    ;
wire [ 4:0] wb_fr_num    ;
wire [31:0] wb_fr_value  ;
wire        wb_fr_wen_h  ;
wire [31:0] wb_fr_value_h;

assign wbbus_o[  0: 0] = wb_gr_wen    ;
assign wbbus_o[  5: 1] = wb_gr_num    ;
assign wbbus_o[ 37: 6] = wb_gr_value  ;
assign wbbus_o[ 38:38] = wb_fr_wen    ;
assign wbbus_o[ 43:39] = wb_fr_num    ;
assign wbbus_o[ 75:44] = wb_fr_value  ;
assign wbbus_o[ 76:76] = wb_fr_wen_h  ;
assign wbbus_o[108:77] = wb_fr_value_h;


wire        rs_allowin;
wire        ctrl_stall_in_rs;
wire        idle_in_rs;
wire        deret_complete;

assign exe_status_o[ 0: 0]  = rs_allowin;
assign exe_status_o[ 1: 1]  = ctrl_stall_in_rs;
assign exe_status_o[ 2: 2]  = idle_in_rs;
assign exe_status_o[ 3: 3]  = deret_complete;


wire        data_ifc_rack   = ifc_data_i[ 0: 0];
wire        data_ifc_rrdy   = ifc_data_i[ 1: 1];
wire [31:0] data_ifc_rdata  = ifc_data_i[33: 2];
wire        data_ifc_wack   = ifc_data_i[34:34];


wire        data_ifc_valid;
wire [ 3:0] data_ifc_width;
wire [ 3:0] data_ifc_ben  ;
wire        data_ifc_wr   ;
wire [31:0] data_ifc_addr ;
wire [31:0] data_ifc_wdata;

assign data_ifc_o[ 0: 0] = data_ifc_valid;
assign data_ifc_o[ 4: 1] = data_ifc_width;
assign data_ifc_o[ 8: 5] = data_ifc_ben  ;
assign data_ifc_o[ 9: 9] = data_ifc_wr   ;
assign data_ifc_o[41:10] = data_ifc_addr ;
assign data_ifc_o[73:42] = data_ifc_wdata;


// dmseg_dres_i
wire        dmseg_dres_valid = dmseg_dres_i[ 0: 0];
wire [31:0] dmseg_dres_value = dmseg_dres_i[32: 1];
wire        dmseg_dreq_ack   = dmseg_dres_i[33:33];

// dmseg_dreq_o;
wire        dmseg_dreq_valid;
wire        dmseg_dreq_wr   ;
wire [ 1:0] dmseg_dreq_width;
wire [31:0] dmseg_dreq_addr ;
wire [31:0] dmseg_dreq_wdata;

assign dmseg_dreq_o[ 0: 0] = dmseg_dreq_valid;
assign dmseg_dreq_o[ 1: 1] = dmseg_dreq_wr   ;
assign dmseg_dreq_o[ 3: 2] = dmseg_dreq_width;
assign dmseg_dreq_o[35: 4] = dmseg_dreq_addr ;
assign dmseg_dreq_o[67:36] = dmseg_dreq_wdata;

// dcr_req_o
wire        dcr_req_valid;
wire        dcr_req_wr   ;
wire [31:0] dcr_req_addr ;
wire [31:0] dcr_req_wdata;

assign dcr_req_o[ 0: 0] = dcr_req_valid;
assign dcr_req_o[ 1: 1] = dcr_req_wr   ;
assign dcr_req_o[33: 2] = dcr_req_addr ;
assign dcr_req_o[65:34] = dcr_req_wdata;

// hb_req_o
wire        hb_req_valid;
wire        hb_req_wr   ;
wire [31:0] hb_req_addr ;
wire [31:0] hb_req_wdata;

assign hb_req_o[ 0: 0] = hb_req_valid;
assign hb_req_o[ 1: 1] = hb_req_wr   ;
assign hb_req_o[33: 2] = hb_req_addr ;
assign hb_req_o[65:34] = hb_req_wdata;

/*
 *   Internal Wires & Registers 
 */
// RS
reg         rs_valid_r;
reg         rs_bd_r;
reg         rs_ex_r;
reg  [ 5:0] rs_excode_r;
reg  [31:0] rs_pc_r;
reg  [ 7:0] rs_op_r;
reg  [ 7:0] rs_dest_r;
reg  [31:0] rs_v1_r;
reg  [31:0] rs_v2_r;
reg  [31:0] rs_v3_r;

wire        rs_valid;
wire        rs_bd;
wire        rs_ex_in;
wire [ 5:0] rs_excode_in;
wire        rs_ex_dss_in;
wire        rs_ex_dint_in;
wire        rs_ex;
wire [ 5:0] rs_excode;
wire        rs_ex_ejtag;
wire [31:0] rs_pc;
wire [ 7:0] rs_op;
wire [ 7:0] rs_dest;
wire [ 1:0] rs_ex_ce;
wire [31:0] rs_v1;
wire [31:0] rs_v2;
wire [31:0] rs_v3;
wire [31:0] rs_v1_neg;
wire [31:0] rs_v2_neg;
wire [31:0] rs_v1_abs;
wire [31:0] rs_v2_abs;
wire        rs_v1_v2_signdiff;

wire        rs_datain_en;
wire        datain_brlky_cancel;

// EXECUTION
wire        exe_complete;
wire        br_complete;
wire        mul_complete;
wire        mac_complete;
wire        div_complete;
wire        fp_complete;
wire        load_complete;
wire        store_complete;
wire        fdiv_complete;
wire        fsqrt_complete;

wire        data_rack;
wire        data_wack;
wire        data_rrdy;

wire [31:0] fix_res;

wire [31:0] float_res_l;
wire [31:0] float_res_h;
wire [31:0] falu_res_l;
wire [31:0] falu_res_h;
wire [ 5:0] falu_evzoui;


reg  [31:0] reg_hi, reg_lo;

reg  [96:0] tbuf_r;
wire        tbuf_wen_31_0;
wire        tbuf_wen_64_32;
wire        tbuf_wen_96_65;

 // CP0 register

// BadVAddr     reg: 8, sel: 0
reg  [31:0] cr_badvaddr;

wire [31:0] badvaddr_value;
assign badvaddr_value = cr_badvaddr;

// Count        reg: 9, sel: 0
reg  [31:0] cr_count;

wire [31:0] count_value;
assign count_value = cr_count;

// Compare      reg: 11, sel: 0
reg  [31:0] cr_compare;

wire [31:0] compare_value;
assign compare_value = cr_compare;

// Status       reg: 12, sel: 0
wire        cr_status_CU3   = 1'b0;
wire        cr_status_CU2   = 1'b0;
wire        cr_status_CU1   = 1'b0;
reg         cr_status_CU0;
wire        cr_status_RP    = 1'b0;
wire        cr_status_FR    = 1'b0;
wire        cr_status_RE    = 1'b0;
wire        cr_status_MX    = 1'b0;
reg         cr_status_BEV;
wire        cr_status_TS    = 1'b0;
wire        cr_status_SR    = 1'b0;
reg         cr_status_NMI;
wire        cr_status_ASE   = 1'b0;
reg         cr_status_IM7;
reg         cr_status_IM6;
reg         cr_status_IM5;
reg         cr_status_IM4;
reg         cr_status_IM3;
reg         cr_status_IM2;
reg         cr_status_IM1;
reg         cr_status_IM0;
reg  [ 1:0] cr_status_KSU;
reg         cr_status_ERL;
reg         cr_status_EXL;
reg         cr_status_IE;

wire [31:0] status_value;
assign status_value = {cr_status_CU3, cr_status_CU2, cr_status_CU1, cr_status_CU0,
        cr_status_RP, cr_status_FR, cr_status_RE, cr_status_MX, 1'b0, cr_status_BEV,
        cr_status_TS, cr_status_SR, cr_status_NMI, cr_status_ASE, 2'b00, 
        cr_status_IM7, cr_status_IM6, cr_status_IM5, cr_status_IM4, cr_status_IM3,
        cr_status_IM2, cr_status_IM1, cr_status_IM0, 3'b000, cr_status_KSU,
        cr_status_ERL, cr_status_EXL, cr_status_IE};

// Cause        reg: 13, sel: 0
reg         cr_cause_BD;
reg  [1:0]  cr_cause_CE;
reg         cr_cause_IV;
reg         cr_cause_IP7;
reg         cr_cause_IP6;
reg         cr_cause_IP5;
reg         cr_cause_IP4;
reg         cr_cause_IP3;
reg         cr_cause_IP2;
reg         cr_cause_IP1;
reg         cr_cause_IP0;
reg  [4:0]  cr_cause_ExcCode;
reg         cr_cause_TI;
reg         cr_cause_DC;
wire        cr_cause_PCI    = 1'b0;
reg         cr_cause_FDCI;
wire        cr_cause_WP     = 1'b0;

wire [31:0] cause_value;
assign cause_value = {cr_cause_BD, cr_cause_TI, cr_cause_CE, cr_cause_DC, 
        cr_cause_PCI, 2'b00, cr_cause_IV, cr_cause_WP, cr_cause_FDCI, 3'b000,
        2'b00, cr_cause_IP7, cr_cause_IP6, cr_cause_IP5, cr_cause_IP4, 
        cr_cause_IP3, cr_cause_IP2, cr_cause_IP1, cr_cause_IP0, 1'b0,
        cr_cause_ExcCode, 2'b00};

// EPC          reg: 14, sel: 0
reg  [31:0] cr_epc;

wire [31:0] epc_value;
assign epc_value = cr_epc;

// PID          reg: 15, sel: 0
wire [ 7:0] cr_pid_Company_Options  = 8'h00;
wire [ 7:0] cr_pid_Company_ID       = 8'h00;
wire [ 7:0] cr_pid_Processor_ID     = 8'h42;
wire [ 7:0] cr_pid_Revision         = 8'h10;

wire [31:0] pid_value;
assign pid_value = {cr_pid_Company_Options, cr_pid_Company_ID, 
                    cr_pid_Processor_ID, cr_pid_Revision};

// Config       reg: 16, sel: 0
wire        cr_config_M     = 1'b1;
wire [ 2:0] cr_config_K23   = `LS132R_CCA_UNCACHE; 
wire [ 2:0] cr_config_KU    = `LS132R_CCA_UNCACHE;
wire        cr_config_BE    = 1'b0;
wire [ 1:0] cr_config_AT    = 2'b00;
wire [ 2:0] cr_config_AR    = 3'b000;
wire [ 2:0] cr_config_MT    = 3'h3;
wire        cr_config_VI    = 1'b0;
wire [ 2:0] cr_config_K0    = `LS132R_CCA_UNCACHE;

wire [31:0] config_value;
assign config_value = {cr_config_M, cr_config_K23, cr_config_KU, 9'h000, 
        cr_config_BE, cr_config_AT, cr_config_AR, cr_config_MT, 3'b000, 
        cr_config_VI, cr_config_K0};

// Config1      reg: 16, sel: 1
wire        cr_config1_M    = 1'b0;
wire [ 5:0] cr_config1_MMUSize_minus1 = 6'h00;
wire [ 2:0] cr_config1_IS   = 3'h0;
wire [ 2:0] cr_config1_IL   = 3'h0;
wire [ 2:0] cr_config1_IA   = 3'h0;
wire [ 2:0] cr_config1_DS   = 3'h0;
wire [ 2:0] cr_config1_DL   = 3'h0;
wire [ 2:0] cr_config1_DA   = 3'h0;
wire        cr_config1_C2   = 1'b0;
wire        cr_config1_MD   = 1'b0;
wire        cr_config1_PC   = 1'b0;
wire        cr_config1_WR   = 1'b0;
wire        cr_config1_CA   = 1'b0;
wire        cr_config1_EP   = 1'b1;
wire        cr_config1_FP   = 1'b0;

wire [31:0] config1_value;
assign config1_value = {cr_config1_M, cr_config1_MMUSize_minus1, cr_config1_IS,
        cr_config1_IL, cr_config1_IA, cr_config1_DS, cr_config1_DL, 
        cr_config1_DA, cr_config1_C2, cr_config1_MD, cr_config1_PC,
        cr_config1_WR, cr_config1_CA, cr_config1_EP, cr_config1_FP};

// ErrorEPC     reg: 30, sel: 0
reg  [31:0] cr_errorepc;

wire [31:0] errorepc_value;
assign errorepc_value = cr_errorepc;

// Debug        reg: 23, sel: 0        
reg         cr_debug_DBD;
reg         cr_debug_DM;
wire        cr_debug_NoDCR      = 1'b0;
reg         cr_debug_LSNM;
reg         cr_debug_Doze;
reg         cr_debug_Halt;
reg         cr_debug_CountDM;
wire        cr_debug_IBusEP     = 1'b0;
wire        cr_debug_MCheckP    = 1'b0;
wire        cr_debug_CacheEP    = 1'b0;
wire        cr_debug_DBusEP     = 1'b0;
wire        cr_debug_IEXI       = 1'b0;
wire        cr_debug_DDBSImpr   = 1'b0;
wire        cr_debug_DDBLImpr   = 1'b0;
wire [ 2:0] cr_debug_EJTAGver   = 3'd2; //ver2.6
reg  [ 4:0] cr_debug_DExcCode;
wire        cr_debug_NoSSt      = 1'b0;
reg         cr_debug_SSt;
wire        cr_debug_OffLine    = 1'b0;
wire        cr_debug_DIBImpr    = 1'b0;
reg         cr_debug_DINT;
reg         cr_debug_DIB;
wire        cr_debug_DDBS       = 1'b0;
wire        cr_debug_DDBL       = 1'b0;
reg         cr_debug_DBp;
reg         cr_debug_DSS;

wire [31:0] cr_debug_value;
assign cr_debug_value = {cr_debug_DBD, cr_debug_DM, cr_debug_NoDCR, 
            cr_debug_LSNM, cr_debug_Doze, cr_debug_Halt, cr_debug_CountDM, 
            cr_debug_IBusEP, cr_debug_MCheckP, cr_debug_CacheEP, 
            cr_debug_DBusEP, cr_debug_IEXI, cr_debug_DDBSImpr, 
            cr_debug_DDBLImpr, cr_debug_EJTAGver, cr_debug_DExcCode,
            cr_debug_NoSSt, cr_debug_SSt, cr_debug_OffLine, 
            cr_debug_DIBImpr, cr_debug_DINT, cr_debug_DIB, cr_debug_DDBS,
            cr_debug_DDBL, cr_debug_DBp, cr_debug_DSS};

// Debug2       reg: 23, sel: 6


// DEPC         reg: 24, sel: 0
reg  [31:0] cr_depc_DEPC;

wire [31:0] depc_value;
assign depc_value = cr_depc_DEPC;

// DESAVE       reg: 31, sel: 0
reg  [31:0] cr_desave;

wire [31:0] desave_value = cr_desave;

 // Fix 32-bit ADD 0
wire [32:0] adder0_a;
wire [32:0] adder0_b;
wire        adder0_cin;
wire [31:0] adder0_res;
wire        adder0_cout;
wire        adder0_lt;
wire        adder0_ltu;
wire        adder0_ge;
wire        adder0_geu;
wire        adder0_ov;

 // Fix 32-bit ADD 1
wire [32:0] adder1_a;
wire [32:0] adder1_b;
wire        adder1_cin;
wire [32:0] adder1_res;

 // 32-bit COMPARATOR
wire        cmp_en;
wire [31:0] cmp0_a;
wire [31:0] cmp0_b;
wire        cmp0_eq;
wire        cmp0_ne;
wire        cmp0_a_eqz;
wire        cmp0_b_eqz;

 // Branch Condition Determination
wire        br_gez;
wire        br_gtz;
wire        br_lez;
wire        br_ltz;
wire        br_cond_taken;
wire        br_cond_not_taken;

 // Shifter 
wire [31:0] shft0_a;
wire [ 4:0] shft0_b;
wire [31:0] shft0_l_res;
wire        shft0_r_sign; //1'b1--arithmetic, 1'b0--logic
wire        shftr_adding_bit;
wire [31:0] shft0_r_res;
wire [31:0] st_v_l;
wire [31:0] st_v_h;
wire [ 4:0] st_sa;

 // CLO, CLZ
wire [31:0] inv_v1;
wire [31:0] find_1st_one_in;
wire [ 4:0] find_1st_one_out;
wire        find_1st_one_nonzero;
wire [31:0] cloz_res;

 // Bitwise Logic
wire [31:0] logic_ina, logic_inb;
wire [31:0] and_out;
wire [31:0] or_out;
wire [31:0] nor_out;
wire [31:0] xor_out;

 // Fix 32x32->64 MUL
wire [31:0] mul_ina;
wire [31:0] mul_inb;
wire        mul_sign;
wire [31:0] mul_prod_l, mul_prod_h;
wire [31:0] latch_prod_l;
wire [31:0] latch_prod_h;

reg         mac_complete_r;

wire [31:0] mul_a;
wire [31:0] mul_a_in;
wire [31:0] mul_b;
wire [32:0] mul_adder_in_a;
wire [32:0] mul_adder_in_b;
wire [64:0] mul_sum_in;
wire [64:0] mul_sum;

 // Fix 32/32->32,32 DIV
wire [31:0] div_ina;
wire [31:0] div_inb;
wire        div_sign;
wire [31:0] div_quot; //LO 
wire [31:0] div_rem;  //HI
wire [31:0] div_a;
wire [31:0] div_b;
wire [32:0] div_P;
wire [31:0] div_a_in;
wire [31:0] div_b_in;
wire [32:0] div_P_in;
wire [32:0] div_adder_in_a;
wire [32:0] div_adder_in_b;
wire        div_adder_in_cin;
wire [31:0] div_rem_abs;
wire [31:0] div_quot_abs;

 // Serail MUL & DIV
wire        cnt_incr_en;
wire        cnt_clear;
wire        cnt_0;
wire        cnt_32;
wire        cnt_33;
wire        cnt_1_31;
wire        rs_v1_en;
wire        rs_v2_en;
wire        rs_v3_en;
wire        rs_pc_en;
wire        rs_bd_en;
wire [31:0] incr0_a;
wire        incr0_cin;
wire [32:0] incr0_res;
wire [31:0] incr1_a;
wire        incr1_cin;
wire [31:0] incr1_res;
wire        mul_res_neg;
wire        div_quot_neg;
wire        div_rem_neg;
reg  [ 5:0] mul_div_cnt_r;

reg         mul_div_neg_r;
reg         div_rem_neg_r;

 // Load & Store
wire [ 3:0] mem_width;
wire [ 3:0] mem_ben;
reg         fetching_data_r;
reg         cr_llbit;
  
wire [31:0] mem_vaddr;
wire [31:0] mem_paddr;
wire        d_useg;
wire        d_kseg0;
wire        d_kseg1;
wire        d_kseg2;
wire        d_kseg3;

wire        adel;
wire        ades;
wire        ade_priv;

wire [31:0] st_v_l_sll;
wire [31:0] st_v_h_sll;
wire [ 3:0] st_ben_l;
wire [ 3:0] st_ben_h;

wire [31:0] ld_v_l;
wire [31:0] ld_v_h;
wire [ 3:0] ld_ben_l;
wire [ 3:0] ld_ben_h;
wire [31:0] ld_v_l_align;
wire [31:0] ld_v_h_align;
wire [31:0] ld_v_l_align_f;
wire [31:0] ld_v_h_align_f;
 
 // Interrupt 
wire        count_cmp_eq;
wire        timer_int;
wire [ 5:0] int_pending;

 // Cp0 Operation
wire        cp0_ren;
wire        cp0_wen;
wire [ 7:0] cp0_addr;
wire [ 7:0] cp0_raddr;
wire [ 7:0] cp0_waddr;
wire [31:0] cp0_rd_value;
wire [31:0] cp0_wr_value;

 // Fcr Operation
wire        fcr_ren;
wire        fcr_wen;
wire [ 7:0] fcr_addr;
wire [ 7:0] fcr_raddr;
wire [ 7:0] fcr_waddr;
wire [31:0] fcr_rd_value;
wire [31:0] fcr_wr_value;
wire [ 2:0] cc_num;
wire        sel_cc = 1'b0;
wire [ 2:0] cc_dest;



 // Ejtag
wire        sel_dmseg;
wire        sel_drseg;
wire        sel_dcr;
wire        sel_hb;
wire        dmseg_ack;
wire        dmseg_rrdy;
wire        drseg_ack;
wire        drseg_rrdy;

// EXCEPTION
wire        exe_ex_valid;
wire        exe_ex;
wire        exe_ex_gr;
wire        exe_ex_fr;
wire [ 5:0] exe_excode;
wire        exe_ex_ov;
wire        exe_ex_trap;
wire        exe_ex_fpe;
wire        exe_ex_ades;
wire        exe_ex_adel;
wire        exe_ex_ddbl;
wire        exe_ex_ddbs;
wire        exe_ex_ddblimpr;
wire        exe_ex_ddbsimpr;
wire        exe_ex_ejtag;

wire [31:0] epc;

reg         wb_ex_r;
reg  [ 5:0] wb_excode_r;

wire        wb_ex;
wire [ 5:0] wb_excode;

wire        ex_ejtag;
wire        ex_nmi;


/*
 *   Internal Logic 
 */

// RS
assign rs_valid     = rs_valid_r;
assign rs_bd        = rs_bd_r;
assign rs_ex        = rs_ex_r;
assign rs_excode    = rs_excode_r;
assign rs_ex_ejtag  = rs_excode_r[5];
assign rs_pc        = rs_pc_r;
assign rs_op        = rs_op_r;
assign rs_dest      = rs_dest_r;
assign rs_ex_ce     = rs_dest[1:0];
assign rs_v1        = rs_v1_r;
assign rs_v2        = rs_v2_r;
assign rs_v3        = rs_v3_r;

assign rs_allowin   = ~rs_valid | exe_complete; 

assign rs_datain_en = issue_valid && rs_allowin;

assign datain_brlky_cancel = (issue_bd && br_not_taken && br_likely) || (rs_valid_r && br_eret);

reg         dss_flag_r;

always @(posedge clock)
begin
  if (reset)
    dss_flag_r <= 1'b0;
  else if (!debug_mode && dss_enable && rs_datain_en && !datain_brlky_cancel
           && !exe_ex_valid)
    dss_flag_r <= ~dss_flag_r;
end

assign rs_ex_dss_in  = !debug_mode && dss_flag_r && dss_enable && !issue_bd;

assign rs_ex_dint_in = !debug_mode && ejtagbrk_i;

assign rs_ex_in     = issue_ex || rs_ex_dss_in || rs_ex_dint_in;
assign rs_excode_in = rs_ex_dss_in  ? `LS132R_EX_DSS  :
                      rs_ex_dint_in ? `LS132R_EX_DINT : issue_excode;

always @(posedge clock)
begin
  if (reset || ex_valid)
    rs_valid_r <= 1'b0;
  else if (rs_datain_en && !datain_brlky_cancel)
    rs_valid_r <= 1'b1;
  else if (rs_valid && exe_complete)
    rs_valid_r <= 1'b0;

  if (rs_datain_en)
  begin
    rs_bd_r     <= issue_bd;
    rs_ex_r     <= rs_ex_in;
    rs_excode_r <= rs_excode_in;
    rs_pc_r     <= issue_pc;
    rs_op_r     <= issue_op;
    rs_dest_r   <= issue_dest;
    if (issue_v1_en)
      rs_v1_r   <= issue_v1;
    if (issue_v2_en)
      rs_v2_r   <= issue_v2;
    if (issue_v3_en)
      rs_v3_r   <= issue_v3;
  end
end

// EXECUTION
wire op_add     = rs_op==`LS132R_OP_ADD      ;
wire op_addu    = rs_op==`LS132R_OP_ADDU     ;
wire op_sub     = rs_op==`LS132R_OP_SUB      ;
wire op_subu    = rs_op==`LS132R_OP_SUBU     ;

wire op_slt     = rs_op==`LS132R_OP_SLT      ;
wire op_sltu    = rs_op==`LS132R_OP_SLTU     ;

wire op_teq     = rs_op==`LS132R_OP_TEQ      ;
wire op_tne     = rs_op==`LS132R_OP_TNE      ;
wire op_tlt     = rs_op==`LS132R_OP_TLT      ;
wire op_tltu    = rs_op==`LS132R_OP_TLTU     ;
wire op_tge     = rs_op==`LS132R_OP_TGE      ;
wire op_tgeu    = rs_op==`LS132R_OP_TGEU     ;

wire op_sll     = rs_op==`LS132R_OP_SLL      ;
wire op_srl     = rs_op==`LS132R_OP_SRL      ;
wire op_sra     = rs_op==`LS132R_OP_SRA      ;

wire op_clo     = rs_op==`LS132R_OP_CLO      ;
wire op_clz     = rs_op==`LS132R_OP_CLZ      ;

wire op_and     = rs_op==`LS132R_OP_AND      ;
wire op_or      = rs_op==`LS132R_OP_OR       ;
wire op_xor     = rs_op==`LS132R_OP_XOR      ;
wire op_nor     = rs_op==`LS132R_OP_NOR      ;

wire op_mul     = rs_op==`LS132R_OP_MUL      ;
wire op_mult    = rs_op==`LS132R_OP_MULT     ;
wire op_multu   = rs_op==`LS132R_OP_MULTU    ;
wire op_madd    = rs_op==`LS132R_OP_MADD     ;
wire op_maddu   = rs_op==`LS132R_OP_MADDU    ;
wire op_msub    = rs_op==`LS132R_OP_MSUB     ;
wire op_msubu   = rs_op==`LS132R_OP_MSUBU    ;
wire op_div     = rs_op==`LS132R_OP_DIV      ;
wire op_divu    = rs_op==`LS132R_OP_DIVU     ;

wire op_mfhi    = rs_op==`LS132R_OP_MFHI     ;
wire op_mflo    = rs_op==`LS132R_OP_MFLO     ;
wire op_mthi    = rs_op==`LS132R_OP_MTHI     ;
wire op_mtlo    = rs_op==`LS132R_OP_MTLO     ;
wire op_movn    = rs_op==`LS132R_OP_MOVN     ;
wire op_movz    = rs_op==`LS132R_OP_MOVZ     ;
wire op_mov     = rs_op==`LS132R_OP_MOV      ;

wire op_j       = rs_op==`LS132R_OP_J        ;
wire op_jr      = rs_op==`LS132R_OP_JR       ;
wire op_jal     = rs_op==`LS132R_OP_JAL      ;
wire op_jalr    = rs_op==`LS132R_OP_JALR     ;

wire op_beq     = rs_op==`LS132R_OP_BEQ      ;
wire op_bne     = rs_op==`LS132R_OP_BNE      ;
wire op_blez    = rs_op==`LS132R_OP_BLEZ     ;
wire op_bgtz    = rs_op==`LS132R_OP_BGTZ     ;
wire op_bltz    = rs_op==`LS132R_OP_BLTZ     ;
wire op_bgez    = rs_op==`LS132R_OP_BGEZ     ;
wire op_bltzal  = rs_op==`LS132R_OP_BLTZAL   ;
wire op_bgezal  = rs_op==`LS132R_OP_BGEZAL   ;

wire op_beql    = rs_op==`LS132R_OP_BEQL     ;
wire op_bnel    = rs_op==`LS132R_OP_BNEL     ;
wire op_blezl   = rs_op==`LS132R_OP_BLEZL    ;
wire op_bgtzl   = rs_op==`LS132R_OP_BGTZL    ;
wire op_bltzl   = rs_op==`LS132R_OP_BLTZL    ;
wire op_bgezl   = rs_op==`LS132R_OP_BGEZL    ;
wire op_bltzall = rs_op==`LS132R_OP_BLTZALL  ;
wire op_bgezall = rs_op==`LS132R_OP_BGEZALL  ;

wire op_lb      = rs_op==`LS132R_OP_LB       ;
wire op_lh      = rs_op==`LS132R_OP_LH       ;
wire op_lw      = rs_op==`LS132R_OP_LW       ;
wire op_lbu     = rs_op==`LS132R_OP_LBU      ;
wire op_lhu     = rs_op==`LS132R_OP_LHU      ;
wire op_ll      = rs_op==`LS132R_OP_LL       ;
wire op_sb      = rs_op==`LS132R_OP_SB       ;
wire op_sh      = rs_op==`LS132R_OP_SH       ;
wire op_sw      = rs_op==`LS132R_OP_SW       ;
wire op_sc      = rs_op==`LS132R_OP_SC       ;
wire op_lwl     = rs_op==`LS132R_OP_LWL      ;
wire op_lwr     = rs_op==`LS132R_OP_LWR      ;
wire op_swl     = rs_op==`LS132R_OP_SWL      ;
wire op_swr     = rs_op==`LS132R_OP_SWR      ;
wire op_sync    = rs_op==`LS132R_OP_SYNC     ;

wire op_eret    = rs_op==`LS132R_OP_ERET     ;
wire op_mfc0    = rs_op==`LS132R_OP_MFC0     ;
wire op_mtc0    = rs_op==`LS132R_OP_MTC0     ;

wire op_deret   = rs_op==`LS132R_OP_DERET    ;
wire op_ext     = 1'b0;
wire op_ins     = 1'b0;
wire op_wsbh    = 1'b0;
wire op_rotr    = 1'b0;
wire op_seb     = 1'b0;
wire op_seh     = 1'b0;
wire op_di      = 1'b0;
wire op_ei      = 1'b0;
wire op_tlbp    = 1'b0;
wire op_tlbr    = 1'b0;
wire op_tlbwi   = 1'b0;
wire op_tlbwr   = 1'b0;

wire op_synci   = 1'b0;
wire op_pref    = 1'b0;
wire op_prefx   = 1'b0;
wire op_cache0  = 1'b0;
wire op_cache1  = 1'b0;
wire op_cache4  = 1'b0;
wire op_cache5  = 1'b0;
wire op_cache7  = 1'b0;
wire op_cache8  = 1'b0;
wire op_cache9  = 1'b0;
wire op_cache10 = 1'b0;
wire op_cache11 = 1'b0;
wire op_cache12 = 1'b0;
wire op_cache13 = 1'b0;
wire op_cache14 = 1'b0;
wire op_cache15 = 1'b0;
wire op_cache16 = 1'b0;
wire op_cache17 = 1'b0;
wire op_cache18 = 1'b0;
wire op_cache19 = 1'b0;
wire op_cache20 = 1'b0;
wire op_cache21 = 1'b0;
wire op_cache22 = 1'b0;
wire op_cache23 = 1'b0;
wire op_cache28 = 1'b0;
wire op_cache29 = 1'b0;
wire op_cache30 = 1'b0;
wire op_cache31 = 1'b0;

wire op_mfc1    = 1'b0;
wire op_cfc1    = 1'b0;
wire op_mtc1    = 1'b0;
wire op_ctc1    = 1'b0;
wire op_ldc1    = 1'b0;
wire op_sdc1    = 1'b0;
wire op_lwc1    = 1'b0;
wire op_swc1    = 1'b0;
wire op_fadd    = 1'b0;
wire op_fsub    = 1'b0;
wire op_fmul    = 1'b0;
wire op_fdiv    = 1'b0;
wire op_fsqrt   = 1'b0;
wire op_fabs    = 1'b0;
wire op_fmov    = 1'b0;
wire op_fneg    = 1'b0;
wire op_roundl  = 1'b0;
wire op_truncl  = 1'b0;
wire op_ceill   = 1'b0;
wire op_floorl  = 1'b0;
wire op_roundw  = 1'b0;
wire op_truncw  = 1'b0;
wire op_ceilw   = 1'b0;
wire op_floorw  = 1'b0;
wire op_recip   = 1'b0;
wire op_rsqrt   = 1'b0;
wire op_cvts    = 1'b0;
wire op_cvtd    = 1'b0;
wire op_cvtw    = 1'b0;
wire op_cvtl    = 1'b0;
wire op_movf    = 1'b0;
wire op_movt    = 1'b0;
wire op_bc1f    = 1'b0;
wire op_bc1t    = 1'b0;
wire op_bc1fl   = 1'b0;
wire op_bc1tl   = 1'b0;
wire op_fmovf   = 1'b0;
wire op_fmovt   = 1'b0;
wire op_fmovz   = 1'b0;
wire op_fmovn   = 1'b0;
wire op_cf      = 1'b0;
wire op_cun     = 1'b0;
wire op_ceq     = 1'b0;
wire op_cueq    = 1'b0;
wire op_colt    = 1'b0;
wire op_cult    = 1'b0;
wire op_cole    = 1'b0;
wire op_cule    = 1'b0;
wire op_csf     = 1'b0;
wire op_cngle   = 1'b0;
wire op_cseq    = 1'b0;
wire op_cngl    = 1'b0;
wire op_clt     = 1'b0;
wire op_cnge    = 1'b0;
wire op_cle     = 1'b0;
wire op_cngt    = 1'b0;

wire fmt_s      = 1'b0;
wire fmt_d      = 1'b0;
wire fmt_w      = 1'b0;
wire fmt_l      = 1'b0;

wire link_op    = op_bgezal | op_bltzal | op_jal | op_jalr | op_bgezall 
                | op_bltzall;

wire sub_op     = op_sub | op_subu | op_slt | op_sltu | op_teq | op_tne 
                | op_tlt | op_tltu | op_tge | op_tgeu ;

wire load_fix   = op_lb   | op_lbu | op_lh | op_lhu | op_lw | op_ll 
                | op_lwl  | op_lwr;

wire load_float = op_lwc1 | op_ldc1;

wire load_op    = load_fix | load_float;

wire store_op   = op_sb   | op_sh  | op_sw | op_sc | op_swl | op_swr 
                | op_swc1 | op_sdc1;

wire cache_op   =  op_pref   | op_prefx   | op_synci
                | op_cache0  | op_cache1  | op_cache4  | op_cache5
                | op_cache7  | op_cache8  | op_cache9  | op_cache10 
                | op_cache11 | op_cache12 | op_cache13 | op_cache14 
                | op_cache15 | op_cache16 | op_cache17 | op_cache18 
                | op_cache19 | op_cache20 | op_cache21 | op_cache22 
                | op_cache23 | op_cache28 | op_cache29 | op_cache30
                | op_cache31 ;

wire mem_op     = load_op | store_op | cache_op;

wire add_op     = op_add | op_addu | mem_op ;

wire cmp_op     = op_teq | op_tne | op_beq | op_beql | op_bne | op_bnel; 

wire v1_eqz_op  = op_bgtz  | op_blez | op_bgtzl | op_blezl ; 

wire v2_eqz_op  = op_movn  | op_movz | op_fmovn | op_fmovz;

wire shft_op    = op_sll | op_srl | op_sra;

wire cloz_op    = op_clo | op_clz; 

wire logic_op   = op_and | op_or | op_nor | op_xor;

wire mac_add_op = op_madd | op_maddu;

wire mac_sub_op = op_msub | op_msubu;

wire mac_op     = mac_add_op | mac_sub_op;

wire mult_op    = op_mul | op_mult | op_multu;

wire mul_op     = mult_op | mac_op;

wire div_op     = op_div | op_divu;

wire br_lkly_op = op_beql    | op_bgezall | op_bgezl | op_bgtzl | op_blezl 
                | op_bltzall | op_bltzl   | op_bnel
                | op_bc1fl   | op_bc1tl;

wire br_op      = op_beq  | op_bgez   | op_bgezal | op_bgtz | op_blez 
                | op_bltz | op_bltzal | op_bne
                | op_bc1f | op_bc1t
                | br_lkly_op ;

wire jump_op    = op_j | op_jal | op_jalr | op_jr | op_eret | op_deret;

wire addsub_op  = op_add | op_addu | op_sub | op_subu;

// Fix 32-bit ADD 0
assign adder0_a = (add_op | sub_op) ? {1'b0, rs_v1} : 
            (mac_op & mul_complete) ? {1'b0, reg_lo} :
                            link_op ? {1'b0, rs_pc} : 33'h0;

assign adder0_b = add_op ? {1'b0,  rs_v2} :
                  sub_op ? {1'b0, ~rs_v2} :
              mac_add_op ? {1'b0,  latch_prod_l} :
              mac_sub_op ? {1'b0, ~latch_prod_l} :
                 link_op ? 33'h8 : 33'h0;

assign adder0_cin = sub_op | mac_sub_op;

assign {adder0_cout, adder0_res} = adder0_a + adder0_b + adder0_cin;

assign adder0_lt  = rs_v1[31]&adder0_res[31]  |
                    rs_v1[31]&(~rs_v2[31])    |
                    adder0_res[31]&(~rs_v2[31]) ;

assign adder0_ltu = ~adder0_cout;

assign adder0_ge  = ~adder0_lt;

assign adder0_geu = adder0_cout;

assign adder0_ov  = (~rs_v1[31]&~rs_v2[31]& adder0_res[31] |
                      rs_v1[31]& rs_v2[31]&~adder0_res[31] ) & op_add |
                    (~rs_v1[31]& rs_v2[31]& adder0_res[31] |
                      rs_v1[31]&~rs_v2[31]&~adder0_res[31] ) & op_sub;

// Fix 32-bit ADD 1
assign adder1_a   =     
   (div_op & (cnt_0 | cnt_1_31)) ? div_adder_in_a      :
   (div_op & cnt_32            ) ? {1'b0, div_P[31:0]} :
   (mul_op & (cnt_0 | cnt_1_31)) ? mul_adder_in_a      :
   (mac_op & mul_complete      ) ? {1'b0, reg_hi}      : 
                                   33'h0;

assign adder1_b   =    
   (div_op & (cnt_0 | cnt_1_31)) ? div_adder_in_b        :
   (div_op & cnt_32            ) ? {1'b0, div_b[31:0]}   :
   (mul_op & (cnt_0 | cnt_1_31)) ? mul_adder_in_b        :
   (mac_add_op & mul_complete  ) ? {1'b0,  latch_prod_h} :
   (mac_sub_op & mul_complete  ) ? {1'b0, ~latch_prod_h} : 
                                   33'h0;

assign adder1_cin = (mac_op & mul_complete) ? adder0_cout      : 
              (div_op & (cnt_0 | cnt_1_31)) ? div_adder_in_cin :
                                              1'b0;

assign adder1_res = adder1_a + adder1_b + adder1_cin;

// 32-bit COMPARATOR
assign cmp0_a  = (cmp_op | v1_eqz_op) ? rs_v1 : 32'h0;
assign cmp0_b  = (cmp_op | v2_eqz_op) ? rs_v2 : 32'h0;

assign cmp0_eq = cmp0_a==cmp0_b;

assign cmp0_ne = !cmp0_eq;

assign cmp0_a_eqz = cmp0_eq;

assign cmp0_b_eqz = cmp0_eq;

// Branch Condition Determination
assign br_gez = ~rs_v1[31];
assign br_gtz = br_gez & !cmp0_a_eqz;
assign br_ltz = rs_v1[31];
assign br_lez = br_ltz | cmp0_a_eqz;

assign br_cond_taken = jump_op 
                    || (op_beq  || op_beql)  &&  cmp0_eq
                    || (op_bne  || op_bnel)  && !cmp0_eq
                    || (op_blez || op_blezl) && br_lez
                    || (op_bgtz || op_bgtzl) && br_gtz
                    || (op_bltz || op_bltzal || op_bltzl || op_bltzall) && br_ltz
                    || (op_bgez || op_bgezal || op_bgezl || op_bgezall) && br_gez
                     ;

assign br_cond_not_taken = 
                       (op_beq  || op_beql)  && !cmp0_eq
                    || (op_bne  || op_bnel)  &&  cmp0_eq
                    || (op_blez || op_blezl) && !br_lez
                    || (op_bgtz || op_bgtzl) && !br_gtz
                    || (op_bltz || op_bltzal || op_bltzl || op_bltzall) && !br_ltz
                    || (op_bgez || op_bgezal || op_bgezl || op_bgezall) && !br_gez
                     ;

// Shifter 
assign shft0_a =  shft_op ? rs_v1 :
                 store_op ? st_v_l : 32'h0;

assign shft0_b =  shft_op ? rs_v2[4:0] :
                 store_op ? st_sa : 5'h0;

assign shft0_l_res = shft0_a << shft0_b;

assign shft0_r_sign= op_sra;

assign shftr_adding_bit = shft0_r_sign & shft0_a[31];

wire [31:0] shftr_tmp1 = {32{shft0_b[1:0]==2'b00}}&{                      shft0_a[31:0]} |
                         {32{shft0_b[1:0]==2'b01}}&{{1{shftr_adding_bit}},shft0_a[31:1]} |
                         {32{shft0_b[1:0]==2'b10}}&{{2{shftr_adding_bit}},shft0_a[31:2]} |
                         {32{shft0_b[1:0]==2'b11}}&{{3{shftr_adding_bit}},shft0_a[31:3]} ;
 
wire [31:0] shftr_tmp2 = {32{shft0_b[3:2]==2'b00}}&{                       shftr_tmp1[31: 0]} |
                         {32{shft0_b[3:2]==2'b01}}&{{ 4{shftr_adding_bit}},shftr_tmp1[31: 4]} |
                         {32{shft0_b[3:2]==2'b10}}&{{ 8{shftr_adding_bit}},shftr_tmp1[31: 8]} |
                         {32{shft0_b[3:2]==2'b11}}&{{12{shftr_adding_bit}},shftr_tmp1[31:12]} ;
 
wire [31:0] shftr_tmp3 = shft0_b[4] ? {{16{shftr_adding_bit}},shftr_tmp2[31:16]} : shftr_tmp2;

assign shft0_r_res = shftr_tmp3;


// CLO, CLZ
assign inv_v1  = {rs_v1[ 0],rs_v1[ 1],rs_v1[ 2],rs_v1[ 3],rs_v1[ 4],
                  rs_v1[ 5],rs_v1[ 6],rs_v1[ 7],rs_v1[ 8],rs_v1[ 9],
                  rs_v1[10],rs_v1[11],rs_v1[12],rs_v1[13],rs_v1[14],
                  rs_v1[15],rs_v1[16],rs_v1[17],rs_v1[18],rs_v1[19],
                  rs_v1[20],rs_v1[21],rs_v1[22],rs_v1[23],rs_v1[24],
                  rs_v1[25],rs_v1[26],rs_v1[27],rs_v1[28],rs_v1[29],
                  rs_v1[30],rs_v1[31]} & {32{cloz_op}};

assign find_1st_one_in = op_clo ? ~inv_v1 : inv_v1;

ls132r_first_one_32_5 
    u0_1st_one_32_5(.in(find_1st_one_in), .out(find_1st_one_out), 
                    .nonzero(find_1st_one_nonzero));

assign cloz_res = find_1st_one_nonzero ? {27'h0, find_1st_one_out} : {26'h0, 6'd32};

// Bitwise Logic
assign logic_ina = logic_op ? rs_v1 : 32'h0;
assign logic_inb = logic_op ? rs_v2 : 32'h0;

assign and_out = logic_ina & logic_inb;

assign or_out  = logic_ina | logic_inb;

assign xor_out = logic_ina ^ logic_inb;

assign nor_out = ~or_out;

// Fix 32x32->64 MUL
assign mul_sign = op_mul | op_mult | op_madd | op_msub;

assign mul_res_neg = mul_sign & rs_v1_v2_signdiff;

assign mul_a    = cnt_0 ? rs_v1_abs : tbuf_r[31:0];
assign mul_a_in = {1'b0, mul_a[31:1]};
assign mul_b    = rs_v2_abs;
assign mul_sum  = tbuf_r[96:32]; 

assign mul_adder_in_a = cnt_0    ? 33'h0 : mul_sum[64:32];
assign mul_adder_in_b = mul_a[0] ? {1'b0, mul_b} : 33'h0;
assign mul_sum_in     = cnt_0    ? {1'b0, adder1_res[32:0], 31'h0}        :
                                   {1'b0, adder1_res[32:0], mul_sum[31:1]};
assign mul_complete   = cnt_32;

assign {mul_prod_h, mul_prod_l} = mul_res_neg ? {incr1_res[31:0], incr0_res[31:0]} : mul_sum[63:0];

assign {latch_prod_h, latch_prod_l} = {mul_prod_h, mul_prod_l};

// MAC
always @(posedge clock)
begin 
 if (reset)
    mac_complete_r <= 1'b0;
  else if (rs_valid && !rs_ex && mac_op && !mac_complete_r && mul_complete)
    mac_complete_r <= 1'b1;
  else if (mac_complete_r)
    mac_complete_r <= 1'b0;
end
assign mac_complete = mac_complete_r;

// Fix 32/32->32,32 DIV
assign div_sign = op_div;

assign div_quot_neg = div_sign & rs_v1_v2_signdiff;
assign div_rem_neg  = div_sign & rs_v1[31];

assign div_a    = cnt_0  ? rs_v1_abs : tbuf_r[31:0];
assign div_a_in = cnt_0  ? {rs_v1_abs[30:0], 1'b0} : 
                           {tbuf_r[30:0], ~div_P[32]};
assign div_b    = rs_v2_abs;
assign div_P    = tbuf_r[64:32]; 

assign div_adder_in_a   = cnt_0     ? {32'h0, rs_v1_abs[31]} : 
                                      {div_P[31:0], tbuf_r[31]};
assign div_adder_in_b   = cnt_0     ? {1'b1, ~rs_v2_abs} : 
                          div_P[32] ? {1'b0,  div_b}   :
                                      {1'b1, ~div_b};
assign div_adder_in_cin = cnt_0     ? 1'b1 : ~div_P[32];
assign div_P_in         = (cnt_32 && !div_P[32]) ? {1'b0, div_P[31:0]} : 
                                                   adder1_res[32:0];

assign div_rem_abs  = div_P[31:0];
assign div_quot_abs = tbuf_r[31:0];

assign div_rem      = div_rem_neg  ? incr0_res[31:0] : div_rem_abs;
assign div_quot     = div_quot_neg ? incr1_res[31:0] : div_quot_abs;

assign div_complete = cnt_33;


assign cnt_0        = mul_div_cnt_r==6'h00;
assign cnt_32       = mul_div_cnt_r==6'd32;
assign cnt_33       = mul_div_cnt_r==6'd33;
assign cnt_1_31     = |mul_div_cnt_r[4:0] && !mul_div_cnt_r[5];
assign cnt_incr_en  = rs_valid && !rs_ex && (mul_op || div_op) && cnt_0 || 
                      cnt_1_31 ||
                      cnt_32 && div_op;

always @(posedge clock)
begin
  if (reset)
    mul_div_cnt_r <= 6'h00;
  else if (cnt_incr_en)
    mul_div_cnt_r <= mul_div_cnt_r + 1'b1;
  else if (!cnt_0 && (mult_op && mul_complete ||
                      mac_op  && mac_complete ||
                      div_op  && div_complete ))
    mul_div_cnt_r <= 6'h00;
end

assign incr0_a      = div_op&cnt_33 ? ~div_rem_abs : 
                      mul_op&cnt_32 ? ~mul_sum[31: 0] : 32'h0;
assign incr0_cin    = 1'b1;
assign incr0_res    = {1'b0, incr0_a} + incr0_cin;
assign incr1_a      = div_op&cnt_33 ? ~div_quot_abs :
                      mul_op&cnt_32 ? ~mul_sum[63:32] : 32'h0;
assign incr1_cin    = mul_op ? incr0_res[32] : 1'b1;
assign incr1_res    = incr1_a + incr1_cin;

// Reg HI/LO
always @(posedge clock)
begin
  if (rs_valid && !rs_ex && exe_complete)
  begin
    if (div_op)
      {reg_hi, reg_lo} <= {div_rem, div_quot};
    else if (mac_op)
      {reg_hi, reg_lo} <= {adder1_res[31:0], adder0_res[31:0]};
    else if (op_mult || op_multu)
      {reg_hi, reg_lo} <= {mul_prod_h, mul_prod_l};
    else if (op_mtlo)
      reg_lo <= rs_v1;
    else if (op_mthi)
      reg_hi <= rs_v1;
  end
end



// Load & Store
  // address caculation and mapping
assign mem_vaddr = adder0_res;

assign d_useg    = ~mem_vaddr[31];
assign d_kseg0   = mem_vaddr[31:29]==3'b100;
assign d_kseg1   = mem_vaddr[31:29]==3'b101;
assign d_kseg2   = mem_vaddr[31:29]==3'b110;
assign d_kseg3   = mem_vaddr[31:29]==3'b111;

assign mem_paddr[28: 0] = mem_vaddr[28: 0];
assign mem_paddr[31:29] = 
                      d_useg  ? {( status_erl && !mem_vaddr[30]) ? 2'b00 :
                                 ( status_erl &&  mem_vaddr[30]) ? 2'b01 :
                                 (!status_erl && !mem_vaddr[30]) ? 2'b01 : 2'b10
                                 , mem_vaddr[29]} :
         (d_kseg0 || d_kseg1) ? 3'b000 :
                                mem_vaddr[31:29]; //kseg2 or kseg3

//EXPLANATION:
//assign mem_paddr[31:29] = 
//       d_useg  ? (status_erl ? mem_vaddr[31:29] : mem_vaddr[31:29]+2'b10) :
//       d_kseg0 ? 3'b000 : 
//       d_kseg1 ? 3'b000 : 
//                 mem_vaddr[31:29]; //kseg2 or kseg3
                          
// address error check
assign adel = (op_lh|op_lhu)&mem_vaddr[0] 
           || (op_lw|op_lwc1|op_ll)&(|mem_vaddr[1:0])
           || op_ldc1&(|mem_vaddr[2:0])
           || load_op&ade_priv;

assign ades = op_sh&mem_vaddr[0] 
           || (op_swc1|op_sw|op_sc)&(|mem_vaddr[1:0]) 
           || op_sdc1&(|mem_vaddr[2:0])
           || store_op&ade_priv; 

assign ade_priv = user_mode && !d_useg ||
                  super_mode && (d_kseg0 || d_kseg1 || d_kseg3);

// store write value aligned
assign st_v_l     = rs_v3;
assign st_sa      = (op_swl ? ~mem_paddr[1:0] : mem_paddr[1:0])<<3;
assign st_v_l_sll = op_swl ? shft0_r_res : shft0_l_res;

wire addr00 = mem_paddr[1:0]==2'b00;
wire addr01 = mem_paddr[1:0]==2'b01;
wire addr10 = mem_paddr[1:0]==2'b10;
wire addr11 = mem_paddr[1:0]==2'b11;

assign st_ben_l[0]= op_sb&addr00 || op_sh&addr00 || op_sc&cr_llbit || op_sw || op_swc1 || op_sdc1
                  ||op_swl
                  ||op_swr&addr00;

assign st_ben_l[1]= op_sb&addr01 || op_sh&addr00 || op_sc&cr_llbit || op_sw || op_swc1 || op_sdc1
                  ||op_swl&~addr00
                  ||op_swr&(addr00|addr01);

assign st_ben_l[2]= op_sb&addr10 || op_sh&addr10 || op_sc&cr_llbit || op_sw || op_swc1 || op_sdc1
                  ||op_swl&(addr10|addr11)
                  ||op_swr&~addr11;

assign st_ben_l[3]= op_sb&addr11 || op_sh&addr10 || op_sc&cr_llbit || op_sw || op_swc1 || op_sdc1
                  ||op_swl&addr11
                  ||op_swr;

// load return value aligned and merged for lwl/lwr
assign ld_v_l = !(sel_dmseg || sel_drseg) ? data_ifc_rdata   :
                               sel_dmseg  ? dmseg_dres_value :
                               sel_dcr    ? drseg_res_dcr_i  : 
                                            drseg_res_hb_i   ;
assign ld_v_h = 32'h0;

assign ld_v_l_align = 
        {32{op_lb &addr00}}&{{24{ld_v_l[ 7]}}, ld_v_l[ 7: 0]} |
        {32{op_lb &addr01}}&{{24{ld_v_l[15]}}, ld_v_l[15: 8]} |
        {32{op_lb &addr10}}&{{24{ld_v_l[23]}}, ld_v_l[23:16]} |
        {32{op_lb &addr11}}&{{24{ld_v_l[31]}}, ld_v_l[31:24]} |
        {32{op_lbu&addr00}}&{ 24'h0,           ld_v_l[ 7: 0]} |
        {32{op_lbu&addr01}}&{ 24'h0,           ld_v_l[15: 8]} |
        {32{op_lbu&addr10}}&{ 24'h0,           ld_v_l[23:16]} |
        {32{op_lbu&addr11}}&{ 24'h0,           ld_v_l[31:24]} |
        {32{op_lh &addr00}}&{{16{ld_v_l[15]}}, ld_v_l[15: 0]} |
        {32{op_lh &addr10}}&{{16{ld_v_l[31]}}, ld_v_l[31:16]} |
        {32{op_lhu&addr00}}&{ 16'h0,           ld_v_l[15: 0]} |
        {32{op_lhu&addr10}}&{ 16'h0,           ld_v_l[31:16]} |
        {32{op_lw|op_ll  }}&{                  ld_v_l[31: 0]} |
        {32{op_lwl&addr00}}&{ ld_v_l[ 7: 0],    rs_v3[23: 0]} |
        {32{op_lwl&addr01}}&{ ld_v_l[15: 0],    rs_v3[15: 0]} |
        {32{op_lwl&addr10}}&{ ld_v_l[23: 0],    rs_v3[ 7: 0]} |
        {32{op_lwl&addr11}}&{ ld_v_l[31: 0]                 } |
        {32{op_lwr&addr00}}&{                  ld_v_l[31: 0]} |
        {32{op_lwr&addr01}}&{ rs_v3[31:24],    ld_v_l[31: 8]} |
        {32{op_lwr&addr10}}&{ rs_v3[31:16],    ld_v_l[31:16]} |
        {32{op_lwr&addr11}}&{ rs_v3[31: 8],    ld_v_l[31:24]} ;
        
assign ld_v_h_align = 32'h0;

assign ld_v_l_align_f = ld_v_l[31: 0];

assign ld_v_h_align_f = ld_v_h;

assign ld_ben_l[0]= (op_lb|op_lbu)&addr00 || (op_lh|op_lhu)&addr00 
                  ||op_ll || op_lw || op_lwc1 || op_ldc1
                  ||op_lwl
                  ||op_lwr&addr00;

assign ld_ben_l[1]= (op_lb|op_lbu)&addr01 || (op_lh|op_lhu)&addr00 
                  ||op_ll || op_lw || op_lwc1 || op_ldc1
                  ||op_lwl&~addr00
                  ||op_lwr&(addr00|addr01);

assign ld_ben_l[2]= (op_lb|op_lbu)&addr10 || (op_lh|op_lhu)&addr10 
                  ||op_ll || op_lw || op_lwc1 || op_ldc1
                  ||op_lwl&(addr10|addr11)
                  ||op_lwr&~addr11;

assign ld_ben_l[3]= (op_lb|op_lbu)&addr11 || (op_lh|op_lhu)&addr10 
                  ||op_ll || op_lw || op_lwc1 || op_ldc1
                  ||op_lwl&addr11
                  ||op_lwr;


assign mem_width = 
        (op_lb || op_lbu ||
         op_sb || op_swl&&addr00 || op_swr&&addr11) ? `LS132R_WIDTH_8BIT  :
        (op_lh || op_lhu ||
         op_sh || op_swl&&addr01 || op_swr&&addr10) ? `LS132R_WIDTH_16BIT :
        (         op_swl&&addr10 || op_swr&&addr01) ? `LS132R_WIDTH_24BIT :
                                                      `LS132R_WIDTH_32BIT;
      //(op_lw || op_ll || op_lwc1 || 
      // op_lwl&&addr11 || op_lwr&&addr00 ||
      // op_sw || op_sc || op_swc1 ||
      // op_swl&&addr11 || op_swr&&addr00)        --> `LS132R_WIDTH_32BIT 

assign mem_ben   = store_op ? st_ben_l : ld_ben_l;

always @(posedge clock)
begin
  if (reset)
    fetching_data_r <= 1'b0;
  else if (rs_valid && !rs_ex && load_op && !adel && !fetching_data_r && data_rack)
    fetching_data_r <= 1'b1;
  else if (fetching_data_r && data_rrdy)
    fetching_data_r <= 1'b0;
end

always @(posedge clock)
begin
  if (reset)
    cr_llbit <= 1'b0;
  else if (rs_valid && !rs_ex && op_ll && !adel && exe_complete)
    cr_llbit <= 1'b1;
  else if (rs_valid && !rs_ex && op_eret && exe_complete)
    cr_llbit <= 1'b0;
end

// Interrupt 
assign count_cmp_eq = cr_compare==cr_count;
assign timer_int    = cr_cause_TI;

assign int_pending[  5] = hw_int_i[5] | timer_int; 
assign int_pending[4:0] = hw_int_i[4:0];

// Cp0 Operation
assign cp0_ren      = op_mfc0;
assign cp0_wen      = rs_valid && !rs_ex && op_mtc0;
assign cp0_addr     = rs_v2[7:0];
assign cp0_raddr    = op_mfc0 ? cp0_addr : 8'h0;
assign cp0_waddr    = op_mtc0 ? cp0_addr : 8'h0;
assign cp0_wr_value = rs_v1[31:0];

assign cp0_rd_value = 
        {32{cp0_raddr=={5'd8 , 3'd0}}}&badvaddr_value    
      | {32{cp0_raddr=={5'd12, 3'd0}}}&status_value     
      | {32{cp0_raddr=={5'd13, 3'd0}}}&cause_value      
      | {32{cp0_raddr=={5'd14, 3'd0}}}&epc_value        
      | {32{cp0_raddr=={5'd15, 3'd0}}}&pid_value        
      | {32{cp0_raddr=={5'd16, 3'd0}}}&config_value     
      | {32{cp0_raddr=={5'd16, 3'd1}}}&config1_value    
      | {32{cp0_raddr=={5'd30, 3'd0}}}&errorepc_value   
      | {32{cp0_raddr=={5'd23, 3'd0}}}&cr_debug_value
      | {32{cp0_raddr=={5'd24, 3'd0}}}&depc_value
      | {32{cp0_raddr=={5'd31, 3'd0}}}&desave_value
      | {32{cp0_raddr=={5'd9,  3'd0}}}&count_value
      | {32{cp0_raddr=={5'd11, 3'd0}}}&compare_value
      ;

reg count_add_en;
always @(posedge clock)
  count_add_en <= (reset || cr_cause_DC || debug_mode&&!cr_debug_CountDM) ? 1'b0 : ~count_add_en;

always @(posedge clock)
begin
// BadVAddr     reg: 8, sel: 0
  if (rs_valid && 
      (rs_ex && rs_excode==`LS132R_EX_ADEL ||
       exe_ex && (exe_excode==`LS132R_EX_ADEL || exe_excode==`LS132R_EX_ADES)))
    cr_badvaddr <= rs_ex ? rs_pc_r : mem_vaddr;

// Count        reg: 9, sel: 0
  if (reset)
    cr_count <= 32'h0;
  else if (cp0_wen && cp0_waddr=={5'd9, 3'd0})
    cr_count <= cp0_wr_value[31:0];
  else if (count_add_en)
    cr_count <= cr_count + 1'b1;

// Compare      reg: 11, sel: 0
  if (reset)
    cr_compare <= 32'h0;
  else if (cp0_wen && cp0_waddr=={5'd11, 3'd0})
    cr_compare <= cp0_wr_value[31:0];


// Status       reg: 12, sel: 0
  if (reset)
  begin
    cr_status_CU0   <= 1'b1;
    cr_status_BEV   <= 1'b1;
    cr_status_NMI   <= 1'b0;
    cr_status_IM7   <= 1'b0;
    cr_status_IM6   <= 1'b0;
    cr_status_IM5   <= 1'b0;
    cr_status_IM4   <= 1'b0;
    cr_status_IM3   <= 1'b0;
    cr_status_IM2   <= 1'b0;
    cr_status_IM1   <= 1'b0;
    cr_status_IM0   <= 1'b0;
    cr_status_KSU   <= 2'b00; 
    cr_status_ERL   <= 1'b1;
    cr_status_EXL   <= 1'b0;
    cr_status_IE    <= 1'b0;
  end
  else begin
    if (isa_ex_valid && !ex_ejtag && !debug_mode)
      cr_status_EXL <= 1'b1;
    else if (rs_valid && !rs_ex && op_eret)
    begin
      cr_status_EXL <= status_erl ? cr_status_EXL : 1'b0;
    end
    else if (cp0_wen && cp0_waddr=={5'd12, 3'd0})
    begin
      cr_status_EXL <= cp0_wr_value[  1];
    end

    if (ex_nmi && !debug_mode)
      cr_status_ERL <= 1'b1;
    else if (rs_valid && !rs_ex && op_eret)
    begin
      cr_status_ERL <= 1'b0;
    end
    else if (cp0_wen && cp0_waddr=={5'd12, 3'd0})
    begin
      cr_status_ERL <= cp0_wr_value[  2];
    end

    if (ex_nmi && !debug_mode)
    begin
      cr_status_NMI <= 1'b1;
      cr_status_BEV <= 1'b1;
    end
    else if (cp0_wen && cp0_waddr=={5'd12, 3'd0})
    begin
      cr_status_NMI <= cp0_wr_value[ 19];
      cr_status_BEV <= cp0_wr_value[ 22];
    end

    if (cp0_wen && cp0_waddr=={5'd12, 3'd0})
    begin
      cr_status_CU0 <= cp0_wr_value[ 28];
      cr_status_IM7 <= cp0_wr_value[ 15];
      cr_status_IM6 <= cp0_wr_value[ 14];
      cr_status_IM5 <= cp0_wr_value[ 13];
      cr_status_IM4 <= cp0_wr_value[ 12];
      cr_status_IM3 <= cp0_wr_value[ 11];
      cr_status_IM2 <= cp0_wr_value[ 10];
      cr_status_IM1 <= cp0_wr_value[  9];
      cr_status_IM0 <= cp0_wr_value[  8];
      cr_status_KSU <= cp0_wr_value[4:3]; 
      cr_status_IE  <= cp0_wr_value[  0];
    end
  end

// Cause        reg: 13, sel: 0
  if (reset)
  begin
    cr_cause_TI <= 1'b0;
  end
  else if (cp0_wen && cp0_waddr=={5'd11, 3'd0})  //compare_wen
  begin
    cr_cause_TI <= 1'b0;
  end
  else if (count_cmp_eq)
  begin
    cr_cause_TI <= 1'b1;
  end

  if (reset)
  begin
    cr_cause_BD     <= 1'b0;
    cr_cause_CE     <= 2'b00;
    cr_cause_IV     <= 1'b0;
    cr_cause_IP7    <= 1'b0;
    cr_cause_IP6    <= 1'b0;
    cr_cause_IP5    <= 1'b0;
    cr_cause_IP4    <= 1'b0;
    cr_cause_IP3    <= 1'b0;
    cr_cause_IP2    <= 1'b0;
    cr_cause_IP1    <= 1'b0;
    cr_cause_IP0    <= 1'b0;
    cr_cause_ExcCode<= 5'h1f;
    cr_cause_FDCI   <= 1'b0;
    cr_cause_DC     <= 1'b0;
  end
  else begin
    if (isa_ex_valid && !ex_ejtag && !debug_mode)
    begin
      cr_cause_ExcCode <= rs_ex ? rs_excode[4:0] : exe_excode[4:0];
      
      if (!cr_status_EXL)
        cr_cause_BD <= rs_bd;

      if (ex_excode==`LS132R_EX_CPU)
        cr_cause_CE <= rs_ex_ce;
    end

    if (cp0_wen && cp0_waddr=={5'd13, 3'd0})
    begin
      cr_cause_DC   <= cp0_wr_value[27];
      cr_cause_IV   <= cp0_wr_value[23];
      cr_cause_IP1  <= cp0_wr_value[ 9];
      cr_cause_IP0  <= cp0_wr_value[ 8];
    end
    
    cr_cause_IP7    <= int_pending[5];
    cr_cause_IP6    <= int_pending[4];
    cr_cause_IP5    <= int_pending[3];
    cr_cause_IP4    <= int_pending[2];
    cr_cause_IP3    <= int_pending[1];
    cr_cause_IP2    <= int_pending[0];
  end

// EPC          reg: 14, sel: 0
  if (isa_ex_valid && !ex_ejtag && !cr_status_EXL && !debug_mode)
    cr_epc          <= epc;
  else if (cp0_wen && cp0_waddr=={5'd14, 3'd0})
    cr_epc          <= cp0_wr_value[31:0];

// Config       reg: 16, sel: 0

// ErrorEPC     reg: 30, sel: 0
  if (reset)
    cr_errorepc     <= 32'hbfc0_0000;
  else if (ex_valid && ex_nmi)
    cr_errorepc     <= epc;
  else if (cp0_wen && cp0_waddr=={5'd30, 3'd0})
    cr_errorepc     <= cp0_wr_value[31:0];

// Debug        reg: 23, sel: 0        
  if (reset)
  begin
    cr_debug_DBD    <= 1'b0;
    cr_debug_DM     <= ejtagboot_i ? 1'b1 : 1'b0;
    cr_debug_LSNM   <= 1'b0;
    cr_debug_SSt    <= 1'b0;
    cr_debug_DINT   <= ejtagboot_i ? 1'b1 : 1'b0;
    cr_debug_Doze   <= 1'b0;
    cr_debug_Halt   <= 1'b0;
    cr_debug_CountDM<= 1'b0;
    cr_depc_DEPC    <= ejtagboot_i ? 32'hbfc0_0000 : 32'h0;
  end
  else
  begin
    if (isa_ex_valid && (ex_ejtag || debug_mode))
      cr_debug_DBD  <= rs_bd;
    
    if (ex_valid && ex_ejtag)
      cr_debug_DM   <= 1'b1;
    else if (rs_valid && !rs_ex && op_deret)
      cr_debug_DM   <= 1'b0;

    if (cp0_wen && cp0_waddr=={5'd23, 3'd0})
    begin
      cr_debug_LSNM    <= cp0_wr_value[28];
      cr_debug_CountDM <= cp0_wr_value[25];
      cr_debug_SSt     <= cp0_wr_value[ 8];
    end

    if (isa_ex_valid && debug_mode)
      cr_debug_DExcCode <= ex_excode;

    if (ex_valid && ex_excode==`LS132R_EX_DINT)
      cr_debug_DINT <= 1'b1;
    else if (isa_ex_valid && debug_mode || rs_valid && !rs_ex && op_deret)
      cr_debug_DINT <= 1'b0;

    if (ex_valid && ex_excode==`LS132R_EX_DIB)
      cr_debug_DIB  <= 1'b1;
    else if (isa_ex_valid && debug_mode || rs_valid && !rs_ex && op_deret)
      cr_debug_DIB  <= 1'b0;

    if (ex_valid && ex_excode==`LS132R_EX_DBP)
      cr_debug_DBp  <= 1'b1;
    else if (isa_ex_valid && debug_mode || rs_valid && !rs_ex && op_deret)
      cr_debug_DBp  <= 1'b0;

    if (ex_valid && ex_excode==`LS132R_EX_DSS)
      cr_debug_DSS  <= 1'b1;
    else if (isa_ex_valid && debug_mode || rs_valid && !rs_ex && op_deret)
      cr_debug_DSS  <= 1'b0;

    if (isa_ex_valid && (ex_ejtag || debug_mode))
      cr_depc_DEPC <= epc;
    else if (cp0_wen && cp0_waddr=={5'd24, 3'd0})
      cr_depc_DEPC <= cp0_wr_value[31:0];

    if (cp0_wen && cp0_waddr=={5'd31, 3'd0})
      cr_desave    <= cp0_wr_value[31:0];

  end
end



// Float Operation
assign rs_v1_neg    = {32{mul_sign|div_sign}}&~rs_v1 + 1'b1;
assign rs_v2_neg    = {32{mul_sign|div_sign}}&~rs_v2 + 1'b1;
assign rs_v1_abs    = ((mul_sign|div_sign)&rs_v1[31]) ? rs_v1_neg : (mul_op|div_op) ? rs_v1 : 32'h0;
assign rs_v2_abs    = ((mul_sign|div_sign)&rs_v2[31]) ? rs_v2_neg : (mul_op|div_op) ? rs_v2 : 32'h0;
assign rs_v1_v2_signdiff = rs_v1[31]^rs_v2[31];

assign tbuf_wen_31_0  = cnt_incr_en;
assign tbuf_wen_64_32 = cnt_incr_en;
assign tbuf_wen_96_65 = cnt_incr_en & mul_op;

always @(posedge clock)
begin
  if (tbuf_wen_31_0)
    tbuf_r[31:0]  <= div_op ? div_a_in : mul_a_in;

  if (tbuf_wen_64_32)
    tbuf_r[64:32] <= div_op ? div_P_in[32:0] : mul_sum_in[32:0];

  if (tbuf_wen_96_65)
    tbuf_r[96:65] <= mul_sum_in[64:33];

end



// Execution result generation
wire bd_in_ir = issue_valid;    //issue_valid == ir_valid_r

assign br_complete   = bd_in_ir || op_eret || op_deret;

assign load_complete = adel || fetching_data_r && data_rrdy;

assign store_complete= ades || op_sc&&(!cr_llbit) || data_wack;

assign exe_complete  = rs_ex ||
                       (br_op || jump_op) && br_complete ||
                       mult_op  && mul_complete          ||
                       mac_op   && mac_complete          ||
                       div_op   && div_complete          ||
                       load_op  && load_complete         ||
                       store_op && store_complete        ||
                       !(br_op  || jump_op || mult_op || 
                         mac_op || div_op  || load_op ||
                         store_op
                        );

assign fix_res      = {32{addsub_op}}&adder0_res 
                    | {32{op_slt   }}&{31'h0, adder0_lt} 
                    | {32{op_sltu  }}&{31'h0, adder0_ltu} 
                    | {32{op_sll   }}&shft0_l_res 
                    | {32{op_sra   }}&shft0_r_res 
                    | {32{op_srl   }}&shft0_r_res 
                    | {32{cloz_op  }}&cloz_res 
                    | {32{op_and   }}&and_out 
                    | {32{op_or    }}&or_out 
                    | {32{op_xor   }}&xor_out 
                    | {32{op_nor   }}&nor_out 
                    | {32{op_mul   }}&mul_prod_l 
                    | {32{op_mfhi  }}&reg_hi 
                    | {32{op_mflo  }}&reg_lo 
                    | {32{op_movn  }}&rs_v1
                    | {32{op_movz  }}&rs_v1
                    | {32{op_mov   }}&rs_v1
                    | {32{link_op  }}&adder0_res 
                    | {32{load_fix }}&ld_v_l_align 
                    | {32{op_sc    }}&{31'h0, cr_llbit} 
                    | {32{op_mfc0  }}&cp0_rd_value 
                    ;

assign float_res_l  = 32'h0;
assign float_res_h  = 32'h0;

// EXCEPTION
assign exe_ex_ov    = (op_add || op_sub) && adder0_ov; 

assign exe_ex_trap  = op_teq  && cmp0_eq    ||
                      op_tne  && cmp0_ne    ||
                      op_tlt  && adder0_lt  ||
                      op_tltu && adder0_ltu ||
                      op_tge  && adder0_ge  ||
                      op_tgeu && adder0_geu ;

assign exe_ex_fpe   = 1'b0;

assign exe_ex_adel = load_op  && adel;

assign exe_ex_ades = store_op && ades;

assign exe_ex_ddbl      = 1'b0;
assign exe_ex_ddbs      = 1'b0;
assign exe_ex_ddblimpr  = 1'b0;
assign exe_ex_ddbsimpr  = 1'b0;

assign exe_ex_ejtag = exe_ex_ddbl     ||
                      exe_ex_ddbs     ||
                      exe_ex_ddblimpr ||
                      exe_ex_ddbsimpr ;

assign exe_ex_gr    = exe_ex_ov       ||
                      exe_ex_trap     ||
                      exe_ex_adel     ||
                      exe_ex_ades     ||
                      exe_ex_ejtag    ;

assign exe_ex_fr    = exe_ex_fpe      ||
                      exe_ex_adel     ||
                      exe_ex_ades     ||
                      exe_ex_ejtag    ;

assign exe_ex       = exe_ex_ov       ||
                      exe_ex_trap     ||
                      exe_ex_fpe      ||
                      exe_ex_adel     ||
                      exe_ex_ades     ||
                      exe_ex_ejtag    ;

assign exe_excode   = exe_ex_ov       ? `LS132R_EX_OV       :
                      exe_ex_trap     ? `LS132R_EX_TRAP     : 
                      exe_ex_fpe      ? `LS132R_EX_FPE      :
                      exe_ex_ddbl     ? `LS132R_EX_DDBL     :
                      exe_ex_ddbs     ? `LS132R_EX_DDBS     :
                      exe_ex_adel     ? `LS132R_EX_ADEL     : 
                      exe_ex_ades     ? `LS132R_EX_ADES     :
                      exe_ex_ddblimpr ? `LS132R_EX_DDBLIMPR :
                      exe_ex_ddbsimpr ? `LS132R_EX_DDBSIMPR :
                                         6'h00              ; //mod                  
assign exe_ex_valid = rs_valid && exe_complete && exe_ex;

assign wb_ex        = wb_ex_r;
assign wb_excode    = wb_excode_r;

wire [29:0] pc_w_m4 = rs_pc[31:2] - 1'b1;
assign epc          = rs_bd ? {pc_w_m4, rs_pc[1:0]} : rs_pc;

always @(posedge clock)
begin
  if (reset)
  begin
    wb_ex_r       <= 1'b0;
    ex_nmi_delay1 <= 1'b0;
  end
  else if (ex_valid)
  begin
    wb_ex_r       <= 1'b1;
    wb_excode_r   <= ex_excode;
    ex_nmi_delay1 <= ex_nmi;
  end
  else if (wb_ex)
  begin
    wb_ex_r       <= 1'b0;
    ex_nmi_delay1 <= 1'b0;
  end
end



// OUTPUT
// cpu_status_o
assign kernel_mode  = cr_status_EXL || cr_status_ERL || debug_mode ||
                      cr_status_KSU==2'b00;
assign super_mode   = !cr_status_EXL && !cr_status_ERL && !debug_mode &&
                      cr_status_KSU==2'b01;
assign user_mode    = !cr_status_EXL && !cr_status_ERL && !debug_mode &&
                      cr_status_KSU==2'b10;
assign cause_iv     = cr_cause_IV;
assign status_bev   = cr_status_BEV;
assign status_erl   = cr_status_ERL;
assign status_cu    = {cr_status_CU2,cr_status_CU1,cr_status_CU0};
assign HWREna        = 4'h0;
assign debug_mode    = cr_debug_DM;
assign dss_enable    = ~cr_debug_NoSSt & cr_debug_SSt;
assign nodcr         = cr_debug_NoDCR;
assign dint          = cr_debug_DINT;
assign lsnm          = cr_debug_LSNM;
assign int_vec       = {cr_cause_IP7 & cr_status_IM7,
                        cr_cause_IP6 & cr_status_IM6,
                        cr_cause_IP5 & cr_status_IM5,
                        cr_cause_IP4 & cr_status_IM4,
                        cr_cause_IP3 & cr_status_IM3,
                        cr_cause_IP2 & cr_status_IM2,
                        cr_cause_IP1 & cr_status_IM1,
                        cr_cause_IP0 & cr_status_IM0};


// exbus_o
assign ex_valid      = rs_valid && rs_ex || exe_ex_valid;
assign ex_delay1     = wb_ex;
assign excode_delay1 = wb_excode;
assign ex_excode     = (rs_valid && rs_ex) ? rs_excode : exe_excode;
assign ex_pc         = rs_pc;
assign ex_ejtag      = ex_excode[5];
assign ex_nmi        = ex_excode==`LS132R_EX_NMI;

assign isa_ex_valid  = rs_valid && rs_ex && (rs_excode!=`LS132R_EX_WAIT) || exe_ex_valid;

// brbus_o
assign br_taken     = rs_valid && br_complete && br_cond_taken;
assign br_not_taken = rs_valid && br_complete && br_cond_not_taken;
assign br_likely    = br_lkly_op; 

assign br_base      = br_op ? bd_pc :
           (op_j || op_jal) ? {bd_pc[31:28], rs_v3[25:0], 2'b00} :
         (op_jr || op_jalr) ? rs_v3[31:0] : 32'h0;

assign br_offset    = br_op ? rs_v3 :
                    op_eret ? (status_erl ? cr_errorepc : cr_epc) :
                   op_deret ? depc_value : 32'h0;

assign br_eret      = op_eret || op_deret;

// ex_int_o
assign ex_int_o = cr_status_IE && !cr_status_ERL && !cr_status_EXL &&
                 (cr_cause_IP0 && cr_status_IM0 ||
                  cr_cause_IP1 && cr_status_IM1 ||
                  cr_cause_IP2 && cr_status_IM2 ||
                  cr_cause_IP3 && cr_status_IM3 ||
                  cr_cause_IP4 && cr_status_IM4 ||
                  cr_cause_IP5 && cr_status_IM5 ||
                  cr_cause_IP6 && cr_status_IM6 ||
                  cr_cause_IP7 && cr_status_IM7 );

// wbbus_o
assign wb_gr_wen     = !exe_ex_gr && 
                         (exe_complete &&
                           (!(op_movn && !cmp0_ne ||
                              op_movz && !cmp0_eq ||
                              op_movf &&  sel_cc  ||
                              op_movt && !sel_cc   ) &&
                            rs_valid && !rs_ex && rs_dest[6:5]==2'b00));

assign wb_gr_num     = rs_dest[4:0];

assign wb_gr_value   = fix_res;

assign wb_fr_wen     = !exe_ex_fr &&
                         (exe_complete && 
                           (!(op_fmovn && !cmp0_ne ||
                              op_fmovz && !cmp0_eq ||
                              op_fmovf &&  sel_cc  ||
                              op_fmovt && !sel_cc  ) && 
                            rs_valid && !rs_ex && rs_dest[6:5]==2'b01));

assign wb_fr_num     = rs_dest[4:0];

assign wb_fr_value   = float_res_l;

assign wb_fr_wen_h   = 1'b0;
assign wb_fr_value_h = 32'h0;

// exe_status_o 
assign ctrl_stall_in_rs = rs_valid && 
       (op_eret || op_deret ||
        op_mtc0 && (cp0_addr=={5'd12, 3'd0} || cp0_addr=={5'd16, 3'd0})
       );                     /*cr_status*/              /*cr_config*/
       
assign idle_in_rs     = !rs_valid;

assign deret_complete = rs_valid && !rs_ex && op_deret;


// data_ifc_o
assign data_ifc_valid = rs_valid && !rs_ex && !adel && !ades && 
                       !sel_dmseg && !sel_drseg && 
                       (store_op && !(op_sc&&(!cr_llbit)) ||
                        load_op && !fetching_data_r);
assign data_ifc_width = mem_width;
assign data_ifc_ben   = mem_ben;
assign data_ifc_wr    = rs_valid && store_op;
assign data_ifc_addr  = mem_paddr;
assign data_ifc_wdata = st_v_l_sll;

assign sel_dmseg = debug_mode && !nodcr && !lsnm && mem_vaddr[31:20]==12'hff2;
assign sel_drseg = debug_mode && !nodcr && !lsnm && mem_vaddr[31:20]==12'hff3;

wire mem_offset0 = mem_vaddr[15:0]==16'h0000;
assign sel_dcr   = sel_drseg &&  mem_offset0;
assign sel_hb    = sel_drseg && !mem_offset0;

assign dmseg_ack = dmseg_dreq_ack && proben_i; 
assign dmseg_rrdy= dmseg_dres_valid;

assign drseg_ack = 1'b1;
assign drseg_rrdy= 1'b1;

assign data_rack = !(sel_dmseg || sel_drseg) ? data_ifc_rack :
                                  sel_dmseg  ? dmseg_ack     : drseg_ack;

assign data_wack = !(sel_dmseg || sel_drseg) ? data_ifc_wack :
                                  sel_dmseg  ? dmseg_rrdy    : drseg_ack;

assign data_rrdy = !(sel_dmseg || sel_drseg) ? data_ifc_rrdy :
                                  sel_dmseg  ? dmseg_rrdy    : drseg_rrdy;


// dmseg_dreq_o
assign dmseg_dreq_valid = rs_valid && !rs_ex && !adel && !ades && sel_dmseg &&
                         (store_op && !(op_sc&&(!cr_llbit)) ||
                          load_op && !fetching_data_r);
assign dmseg_dreq_wr    = store_op;
assign dmseg_dreq_width = mem_width[1:0];
assign dmseg_dreq_addr  = {mem_vaddr[31:2], 
                          (op_lwl|op_lwr|op_swl) ? 2'b00 : mem_vaddr[1:0]};
assign dmseg_dreq_wdata = st_v_l_sll;

// dcr_req_o
assign dcr_req_valid    = rs_valid && !rs_ex && !adel && !ades && sel_dcr &&
                         (op_lw || op_sw);
assign dcr_req_wr       = op_sw;
assign dcr_req_addr     = mem_vaddr;
assign dcr_req_wdata    = st_v_l;

// hb_req_o
assign hb_req_valid     = rs_valid && !rs_ex && !adel && !ades && sel_hb && 
                         (op_lw || op_sw);
assign hb_req_wr        = op_sw;
assign hb_req_addr      = mem_vaddr;
assign hb_req_wdata     = st_v_l;

assign status_nmi_o = cr_status_NMI;

//debug interface
assign debug_wb_pc       = rs_pc_r;
assign debug_wb_rf_wen   = {4{wb_gr_wen}};
assign debug_wb_rf_wnum  = wb_gr_num;
assign debug_wb_rf_wdata = wb_gr_value;

endmodule //ls132r_execute_stage
