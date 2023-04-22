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

//EJTAG TAP FSM
`define EJTAG_TAP_FSM_TEST          4'd0
`define EJTAG_TAP_FSM_IDLE          4'd1
`define EJTAG_TAP_FSM_SELDR         4'd2
`define EJTAG_TAP_FSM_CAPTUREDR     4'd3
`define EJTAG_TAP_FSM_SHIFTDR       4'd4
`define EJTAG_TAP_FSM_EXIT1DR       4'd5
`define EJTAG_TAP_FSM_PAUSEDR       4'd6
`define EJTAG_TAP_FSM_EXIT2DR       4'd7
`define EJTAG_TAP_FSM_UPDATADR      4'd8
`define EJTAG_TAP_FSM_SELIR         4'd9
`define EJTAG_TAP_FSM_CAPTUREIR     4'd10
`define EJTAG_TAP_FSM_SHIFTIR       4'd11
`define EJTAG_TAP_FSM_EXIT1IR       4'd12
`define EJTAG_TAP_FSM_PAUSEIR       4'd13
`define EJTAG_TAP_FSM_EXIT2IR       4'd14
`define EJTAG_TAP_FSM_UPDATAIR      4'd15

//EJTAG TAP instruction
`define EJTAG_TAP_INST_IDCODE       5'h01
`define EJTAG_TAP_INST_IMPCODE      5'h03
`define EJTAG_TAP_INST_ADDRESS      5'h08
`define EJTAG_TAP_INST_DATA         5'h09
`define EJTAG_TAP_INST_CONTROL      5'h0a
`define EJTAG_TAP_INST_ALL          5'h0b
`define EJTAG_TAP_INST_EJTAGBOOT    5'h0c
`define EJTAG_TAP_INST_NORMALBOOT   5'h0d
`define EJTAG_TAP_INST_FASTDATA     5'h0e
`define EJTAG_TAP_INST_TCBCONTROLA  5'h10
`define EJTAG_TAP_INST_TCBCONTROLB  5'h11
`define EJTAG_TAP_INST_TCBDATA      5'h12
`define EJTAG_TAP_INST_TCBCONTROLC  5'h13
`define EJTAG_TAP_INST_PCSAMPLE     5'h14
`define EJTAG_TAP_INST_TCBCONTROLD  5'h15
`define EJTAG_TAP_INST_TCBCONTROLE  5'h16
`define EJTAG_TAP_INST_FDC          5'h17
`define EJTAG_TAP_INST_DBTX         5'h1c //for fast download
`define EJTAG_TAP_INST_BYPASS       5'h1f



module ls132r_ejtag_tap(
  tck           ,
  trst          ,
  tms           ,
  tdi           ,
  tdo           ,

  dmseg_addr    ,
  dmseg_rdata   ,
  dmseg_wdata   ,

  rocc_in       ,
  dmseg_be_in   ,
  prnw          ,
  pracc_in      ,
  ejtagbrk_in   ,
  dm            ,

  pracc_out     ,
  prrst         ,
  proben        ,
  probtrap      ,
  ejtagbrk_out   
);

input           tck;
input           trst;
input           tms;
input           tdi;
output          tdo;
reg             tdo;

input   [31:0]  dmseg_addr;
input   [31:0]  dmseg_rdata;
output  [31:0]  dmseg_wdata;

input           rocc_in;
input   [ 1:0]  dmseg_be_in;
input           prnw;
input           pracc_in;
input           ejtagbrk_in;
input           dm;

output          pracc_out;
output          prrst;
output          proben;
output          probtrap;
output          ejtagbrk_out;

wire            ind; 
wire            cap_i;
wire            shift_i;
wire            updata_i;
wire            cap_d;
wire            tdo_i;
wire            shift_d;
wire            updata_d_fsm;
wire            tdo_d;
wire            tlr_sig;  //test-logic-reset

wire    [4:0]   inst;

wire    [13:0]  sel; //add fast upload and download, by xucp
//wire [11:0] sel;
wire            norboot;
wire            boot;

wire            pracc_en;


always @ (negedge tck or negedge trst)
  if(!trst)
    tdo <= 1'b0;
  else
    tdo <= ind ? tdo_i : tdo_d;


ejtag_tap_fsm fsm (.tck        (tck          ),
                   .trst       (trst         ),
                   .tms        (tms          ),
                   .ind        (ind          ),
                   .capture_ir (cap_i        ),
                   .shift_ir   (shift_i      ),
                   .updata_ir  (updata_i     ),
                   .capture_dr (cap_d        ),
                   .shift_dr   (shift_d      ),
                   .updata_dr  (updata_d_fsm ),
                   .tlr_sig    (tlr_sig      ),
                   .pracc_en   (pracc_en ));


ejtag_tap_instreg instreg (.clk    (tck      ),
                           .rst_n  (trst     ),
                           .s_rst  (tlr_sig  ),
                           .en     (1'b1     ),
                           .cap    (cap_i    ),
                           .shift  (shift_i  ),
                           .updata (updata_i ),
                           .si     (tdi      ),
                           .pi     (inst     ),
                           .so     (tdo_i    ),
                           .po     (inst     ));

ejtag_tap_decode decode (.inst    (inst    ),
                         .sel     (sel     ),
                         .norboot (norboot ),
                         .boot    (boot    ));



ejtag_tap_reg_group reggroup(.tck            (tck             ),
                             .trst           (trst            ),
                             .tdi            (tdi             ),
                             .tdo            (tdo_d           ),

                             .capture_dr     (cap_d           ),
                             .shift_dr       (shift_d         ),
                             .updata_dr_fsm  (updata_d_fsm    ),

                             .sel            (sel             ),
                             .norboot        (norboot|tlr_sig ),
                             .boot           (boot            ),

                             .dmseg_addr     (dmseg_addr      ),
                             .dmseg_rdata    (dmseg_rdata     ),
                             .dmseg_wdata    (dmseg_wdata     ),

                             .rocc_in        (rocc_in         ),
                             .dmseg_be_in    (dmseg_be_in     ),
                             .prnw           (prnw            ),
                             .pracc_in       (pracc_in        ),
                             .ejtagbrk_in    (ejtagbrk_in     ),
                             .dm             (dm              ),

                             .pracc_out      (pracc_out       ),
                             .prrst          (prrst           ),
                             .proben         (proben          ),
                             .probtrap       (probtrap        ),
                             .ejtagbrk_out   (ejtagbrk_out    ), 
                             .pracc_en       (pracc_en        )
                             );


endmodule

module ejtag_tap_instreg(
  clk   ,
  rst_n ,
  s_rst ,
  en    ,
  cap   ,
  shift ,
  updata,
  si    ,
  pi    ,
  so    ,
  po    
);

input           clk, rst_n, s_rst;
input           en;
input           cap, shift, updata;
input           si;
output          so;
input   [ 4:0]  pi;
output  [ 4:0]  po;
reg     [ 4:0]  po;


reg     [ 4:0]  sr;


assign so = sr[0];

always @(posedge clk or negedge rst_n)
begin
  if (!rst_n)
    sr <= `EJTAG_TAP_INST_IDCODE;
  else
  begin
    if (s_rst)
      sr <= `EJTAG_TAP_INST_IDCODE;
    else if (cap && en)
      sr <= pi;
    else if (shift && en)
      sr <= {si, sr[4:1]};
  end
end

always @(posedge clk or negedge rst_n)
begin
 if (!rst_n)
   po <= `EJTAG_TAP_INST_IDCODE;
 else begin
   if (s_rst)
     po <= `EJTAG_TAP_INST_IDCODE;
   else if (updata && en)
     po <= sr;
 end
end

endmodule

module ejtag_tap_reg_group(
  tck           ,
  trst          ,
  tdi           ,
  tdo           ,

  capture_dr    ,
  shift_dr      ,
  updata_dr_fsm ,

  sel           ,
  norboot       ,
  boot          ,

  dmseg_addr    ,
  dmseg_rdata   ,
  dmseg_wdata   ,

  rocc_in       ,
  dmseg_be_in   ,
  prnw          ,
  pracc_in      ,
  ejtagbrk_in   ,
  dm            ,

  pracc_out     ,
  prrst         ,
  proben        ,
  probtrap      ,
  ejtagbrk_out  ,

  pracc_en
  
);

input           tck, trst;
input           tdi;
output          tdo;
reg             tdo;

input           capture_dr, shift_dr, updata_dr_fsm;
input   [13:0]  sel;
input           norboot;
input           boot;

input   [31:0]  dmseg_addr;
input   [31:0]  dmseg_rdata;
output  [31:0]  dmseg_wdata;

input           rocc_in;
input   [ 1:0]  dmseg_be_in;
input           prnw;
input           pracc_in;
input           ejtagbrk_in;
input           dm;

output          pracc_out;
output          prrst;
output          proben;
output          probtrap;
output          ejtagbrk_out;
reg             ejtagbrk_out;

input           pracc_en;

wire            updata_dr;

reg     [31:0]  dmseg_addr_r;
reg     [31:0]  dmseg_rdata_r;
reg     [48:0]  pc_sample_r;


wire            tdo_id;
wire    [31:0]  temp_id;

wire            tdo_imp;
wire    [31:0]  temp_imp;

wire            tdo_addr;

wire            tdi_data;
wire            tdo_data;
wire    [31:0]  data_pi;

wire            tdi_rocc;
reg             tdo_rocc;

wire            tdi_ecr;
wire            tdo_ecr;
wire            tdo_ecr1;
reg     [31:0]  ecr_in;
reg             ecr_rocc_buffer_1, ecr_rocc_buffer_2;
reg             ecr_dm_buffer_1, ecr_dm_buffer_2;
reg             ecr_ejtagbrk_buffer_1, ecr_ejtagbrk_buffer_2;
reg             ecr_pracc_buffer_1, ecr_pracc_buffer_2;
reg             ecr_pracc_buffer_3;

//PrAcc ECR[18]
reg             tdo_pracc;  
wire            pracc_sin;  

//ECR[17:13]
wire            tdo_ecr2;
reg     [ 4:0]  ecr2;
reg     [ 4:0]  secr2;

//ECR[12]   EjtagBrk   
reg             tdo_brk;   


//Bypass
reg             tdo_bypass;


always @(posedge tck or negedge trst)
  if(!trst)
    dmseg_addr_r <= 32'h0000_0000;
  else
    begin
      if(ecr_pracc_buffer_2 & ~ecr_pracc_buffer_3)
         dmseg_addr_r <= dmseg_addr;
    end


ejtag_tap_basicreg #(32, 32'h2001_0819) ID     
    (.clk    (tck        ),
     .rst_n  (trst       ),
     .en     (sel[0]     ),
     .cap    (capture_dr ),
     .shift  (shift_dr   ),
     .updata (1'b0       ),
     .si     (tdo_id     ),
     .pi     (temp_id    ),
     .so     (tdo_id     ),
     .po     (temp_id    ));


ejtag_tap_basicreg #(32, 32'h4100_4000) IMP    
    (.clk    (tck        ),
     .rst_n  (trst       ),
     .en     (sel[1]     ),
     .cap    (capture_dr ),
     .shift  (shift_dr   ),
     .updata (1'b0       ),
     .si     (tdo_imp    ),
     .pi     (temp_imp   ),
     .so     (tdo_imp    ),
     .po     (temp_imp   ));


ejtag_tap_basicreg #(32, 32'h0000_0000) ADDR   
    (.clk    (tck          ),
     .rst_n  (trst         ),
     .en     (sel[2]       ),
     .cap    (capture_dr   ),
     .shift  (shift_dr     ),
     .updata (1'b0         ),
     .si     (tdi          ),
     .pi     (dmseg_addr_r ),
     .so     (tdo_addr     ),
     .po     (             ));


always @(posedge tck or negedge trst)
  if (!trst)
    dmseg_rdata_r <= 32'h0000_0000;
  else if (ecr_pracc_buffer_2 & ~ecr_pracc_buffer_3)
    dmseg_rdata_r <= dmseg_rdata;

assign data_pi  = ecr_in[18] ? dmseg_rdata_r : dmseg_wdata;  
assign tdi_data = sel[10] ? tdo_addr : tdi; //inst ALL

ejtag_tap_basicreg #(32, 32'h0000_0000) DATA   
    (.clk    (tck          ),
     .rst_n  (trst         ),
     .en     (sel[3]       ),
     .cap    (capture_dr   ),
     .shift  (shift_dr     ),
     .updata (updata_dr    ),
     .si     (tdi_data     ),
     .pi     (data_pi      ),
     .so     (tdo_data     ),
     .po     (dmseg_wdata  ));

//ECR
always @(posedge tck or negedge trst)
  if(!trst)
  begin
    ecr_rocc_buffer_1   <= 1'b0; 
    ecr_rocc_buffer_2   <= 1'b0; 
    ecr_dm_buffer_1     <= 1'b0; 
    ecr_dm_buffer_2     <= 1'b0; 
    ecr_ejtagbrk_buffer_1 <= 1'b0;
    ecr_ejtagbrk_buffer_2 <= 1'b0;
    ecr_pracc_buffer_1  <= 1'b0; 
    ecr_pracc_buffer_2  <= 1'b0; 
    ecr_pracc_buffer_3  <= 1'b0; 
  end
  else
  begin
    ecr_rocc_buffer_1   <= rocc_in; 
    ecr_rocc_buffer_2   <= ecr_rocc_buffer_1; 

    ecr_dm_buffer_1     <= dm; 
    ecr_dm_buffer_2     <= ecr_dm_buffer_1; 

    ecr_ejtagbrk_buffer_1 <= ejtagbrk_in;
    ecr_ejtagbrk_buffer_2 <= ecr_ejtagbrk_buffer_1;

    if (!(pracc_en && (sel[4] || sel[5]) && !proben))
        ecr_pracc_buffer_1 <= pracc_in; 
    else
        ecr_pracc_buffer_1 <= 1'b0; 

    ecr_pracc_buffer_2 <= ecr_pracc_buffer_1; 
    ecr_pracc_buffer_3 <= ecr_pracc_buffer_2; 
  end


always @(posedge tck or negedge trst)
  if (!trst)
    ecr_in <= 32'h0000_0000;
  else
  begin
    //reserved bits by sumh
    ecr_in[28:20]   <= 9'b0;
    ecr_in[17   ]   <= 1'b0;
    ecr_in[11: 4]   <= 8'b0;
    ecr_in[ 2: 0]   <= 3'b0;
    //end of reserved bits

    ecr_in[16:14]   <= {prrst, proben, probtrap};
    ecr_in[12]      <= ecr_ejtagbrk_buffer_2;
    ecr_in[3]       <= ecr_dm_buffer_2;

    if (ecr_pracc_buffer_2 & ~ecr_pracc_buffer_3)
    begin
      ecr_in[18]    <= 1'b1;
      ecr_in[30:29] <= dmseg_be_in;
      ecr_in[19]    <= prnw;
    end
    else if (updata_dr && (sel[4] || sel[5]) && !tdo_pracc)
      ecr_in[18]    <= 1'b0;
    
    if (ecr_rocc_buffer_2)
      ecr_in[31] <= 1'b1;
    else if (updata_dr_fsm && sel[4] && !tdo_rocc)
      ecr_in[31] <= 1'b0;
  end

//ECR[31]   Rocc
assign tdi_rocc = sel[10] ? tdo_data : tdi; //inst ALL

always @(posedge tck or negedge trst)
begin
  if (!trst)
    tdo_rocc <= 1'b0;
  else
  begin
    if (capture_dr && sel[4])
      tdo_rocc <= ecr_in[31];
    else if (shift_dr && sel[4])
      tdo_rocc <= tdi_rocc;
  end
end

//since "reset" changed slowly, we don't use handshake mechanism

assign updata_dr = updata_dr_fsm && (!ecr_in[31] || 
                                     updata_dr_fsm && sel[4] && !tdo_rocc);

//ECR[30:19] 
assign tdi_ecr = tdo_rocc;

ejtag_tap_basicreg #(12, 12'h000) ECR_30_19   
    (.clk    (tck           ),
     .rst_n  (trst          ),
     .en     (sel[4]        ),
     .cap    (capture_dr    ),
     .shift  (shift_dr      ),
     .updata (1'b0          ),
     .si     (tdi_ecr       ),
     .pi     (ecr_in[30:19] ),
     .so     (tdo_ecr1      ),
     .po     (              ));


//ECR[18]   PrAcc 
assign pracc_sin = sel[11] ? tdo_data : tdo_ecr1;

always@(posedge tck or negedge trst)
begin
  if (!trst)
    tdo_pracc <= 1'b0;
  else
  begin
    if (capture_dr && (sel[4] || sel[5]))
      tdo_pracc <= ecr_in[18];
    else if (shift_dr && (sel[4] || sel[5]))
      tdo_pracc <= pracc_sin;
  end
end

assign pracc_out = ecr_in[18];

//ECR[17:13]
assign prrst    = ecr2[3];
assign proben   = ecr2[2];
assign probtrap = ecr2[1];

assign tdo_ecr2 = secr2[0];

always@(posedge tck or negedge trst)
begin
  if (!trst)
    secr2 <= 5'b0_0000;
  else
  begin
    if (norboot)  
      secr2 <= {1'b0,secr2[3],3'b000}; 
    else if (boot)
      secr2 <= {1'b0,secr2[3],3'b110};  
    else if (capture_dr && sel[4])
      secr2 <= ecr2;
    else if (shift_dr && sel[4])
      secr2 <= {tdo_pracc, secr2[4:1]};
  end
end

always@(posedge tck or negedge trst)
begin
  if (!trst)
    ecr2 <= 5'b0_0000;
  else
  begin
    if (norboot)  //normalboot do clear ejtagboot indication
      ecr2 <= {1'b0,ecr2[3],3'b000}; 
    else if (boot)
      ecr2 <= {1'b0,ecr2[3],3'b110};  
    else if (updata_dr && sel[4])
      ecr2 <= secr2;
  end
end

//EjtagBrk   ECR[12]
always @(posedge tck or negedge trst)
begin
  if(!trst)
    tdo_brk <= 1'b0;
  else
  begin
    if(norboot)  
      tdo_brk <= 1'b0; 
    else if(boot)
      tdo_brk <= 1'b1;
    else if(capture_dr && sel[4])
      tdo_brk <= ejtagbrk_out; 
    else if(shift_dr && sel[4])
      tdo_brk <= tdo_ecr2;
  end
end

always @(posedge tck or negedge trst)
begin
  if (!trst)
    ejtagbrk_out <= 1'b0;
  else
  begin
    if (norboot)  
      ejtagbrk_out <= 1'b0;   
    else if (boot)
      ejtagbrk_out <= 1'b1;
    else
    begin
      if (updata_dr && sel[4])
      begin
        if(tdo_brk)
          ejtagbrk_out <= 1'b1;
      end
      else if (ecr_in[3]) 
        ejtagbrk_out <= 1'b0;     
    end
  end
end

//ECR[11:0]
ejtag_tap_basicreg #(12, 12'h000) ECR3   
    (.clk    (tck          ),
     .rst_n  (trst         ),
     .en     (sel[4]       ),
     .cap    (capture_dr   ),
     .shift  (shift_dr     ),
     .updata (1'b0         ),
     .si     (tdo_brk      ),
     .pi     (ecr_in[11:0] ),
     .so     (tdo_ecr      ),
     .po     (             ));


always@(posedge tck or negedge trst)
begin
  if(!trst)
    tdo_bypass <= 1'b0;
  else if(shift_dr && sel[6])
    tdo_bypass <= tdi;
end

always @(*)
  case(sel[13:0])
    14'b00_0000_0000_0001 : tdo = tdo_id;
    14'b00_0000_0000_0010 : tdo = tdo_imp;
    14'b00_0000_0100_0000 : tdo = tdo_bypass;
    14'b00_0000_0000_0100 : tdo = tdo_addr;
    14'b00_0000_0000_1000 : tdo = tdo_data;
    14'b00_0000_0001_0000 : tdo = tdo_ecr;
    14'b00_0100_0001_1100 : tdo = tdo_ecr;
    14'b00_1000_0010_1000 : tdo = tdo_pracc;
    default               : tdo = 1'b0;
  endcase

endmodule

module ejtag_tap_fsm(
  tck       ,
  trst      ,
  tms       ,
  ind       ,
  capture_ir,
  shift_ir  ,
  updata_ir ,
  capture_dr,
  shift_dr  ,
  updata_dr ,
  tlr_sig   ,
  pracc_en   
);

input           tck; 
input           trst;
input           tms;

output          ind;
reg             ind;

output          capture_ir, shift_ir, updata_ir;
reg             capture_ir, shift_ir, updata_ir;

output          capture_dr, shift_dr, updata_dr;
reg             capture_dr, shift_dr, updata_dr;

output          tlr_sig;

output          pracc_en;
reg             pracc_en;


reg     [ 3:0]  curr_state; 
reg     [ 3:0]  next_state;

assign tlr_sig = curr_state==`EJTAG_TAP_FSM_TEST;

always @(tms or curr_state)
  case(curr_state[3:0])
    `EJTAG_TAP_FSM_TEST      : next_state = tms ? `EJTAG_TAP_FSM_TEST     : `EJTAG_TAP_FSM_IDLE     ;
    `EJTAG_TAP_FSM_IDLE      : next_state = tms ? `EJTAG_TAP_FSM_SELDR    : `EJTAG_TAP_FSM_IDLE     ;
    `EJTAG_TAP_FSM_SELDR     : next_state = tms ? `EJTAG_TAP_FSM_SELIR    : `EJTAG_TAP_FSM_CAPTUREDR;
    `EJTAG_TAP_FSM_CAPTUREDR : next_state = tms ? `EJTAG_TAP_FSM_EXIT1DR  : `EJTAG_TAP_FSM_SHIFTDR  ;
    `EJTAG_TAP_FSM_SHIFTDR   : next_state = tms ? `EJTAG_TAP_FSM_EXIT1DR  : `EJTAG_TAP_FSM_SHIFTDR  ;
    `EJTAG_TAP_FSM_EXIT1DR   : next_state = tms ? `EJTAG_TAP_FSM_UPDATADR : `EJTAG_TAP_FSM_PAUSEDR  ;
    `EJTAG_TAP_FSM_PAUSEDR   : next_state = tms ? `EJTAG_TAP_FSM_EXIT2DR  : `EJTAG_TAP_FSM_PAUSEDR  ;
    `EJTAG_TAP_FSM_EXIT2DR   : next_state = tms ? `EJTAG_TAP_FSM_UPDATADR : `EJTAG_TAP_FSM_SHIFTDR  ;
    `EJTAG_TAP_FSM_UPDATADR  : next_state = tms ? `EJTAG_TAP_FSM_SELDR    : `EJTAG_TAP_FSM_IDLE     ;
    `EJTAG_TAP_FSM_SELIR     : next_state = tms ? `EJTAG_TAP_FSM_TEST     : `EJTAG_TAP_FSM_CAPTUREIR;
    `EJTAG_TAP_FSM_CAPTUREIR : next_state = tms ? `EJTAG_TAP_FSM_EXIT1IR  : `EJTAG_TAP_FSM_SHIFTIR  ;
    `EJTAG_TAP_FSM_SHIFTIR   : next_state = tms ? `EJTAG_TAP_FSM_EXIT1IR  : `EJTAG_TAP_FSM_SHIFTIR  ;
    `EJTAG_TAP_FSM_EXIT1IR   : next_state = tms ? `EJTAG_TAP_FSM_UPDATAIR : `EJTAG_TAP_FSM_PAUSEIR  ;
    `EJTAG_TAP_FSM_PAUSEIR   : next_state = tms ? `EJTAG_TAP_FSM_EXIT2IR  : `EJTAG_TAP_FSM_PAUSEIR  ;
    `EJTAG_TAP_FSM_EXIT2IR   : next_state = tms ? `EJTAG_TAP_FSM_UPDATAIR : `EJTAG_TAP_FSM_SHIFTIR  ;
    default                  : next_state = tms ? `EJTAG_TAP_FSM_SELDR    : `EJTAG_TAP_FSM_IDLE     ;
  //`EJTAG_TAP_FSM_UPDATAIR
  endcase

always@(posedge tck or negedge trst)
begin
  if(!trst)
  begin
    curr_state  <= `EJTAG_TAP_FSM_TEST;
    pracc_en    <= 1'b0;
    ind         <= 1'b0;
    capture_ir  <= 1'b0;
    shift_ir    <= 1'b0;
    updata_ir   <= 1'b0;
    capture_dr  <= 1'b0;
    shift_dr    <= 1'b0;
    updata_dr   <= 1'b0;
  end
  else
  begin
    curr_state  <= next_state;

    pracc_en    <= ((next_state==`EJTAG_TAP_FSM_CAPTUREDR) |
                    (next_state==`EJTAG_TAP_FSM_SHIFTDR  ) |
                    (next_state==`EJTAG_TAP_FSM_EXIT1DR  ) |    
                    (next_state==`EJTAG_TAP_FSM_PAUSEDR  ) |
                    (next_state==`EJTAG_TAP_FSM_EXIT2DR  ) |
                    (next_state==`EJTAG_TAP_FSM_UPDATADR ) ); 

    case(next_state[3:0])
      `EJTAG_TAP_FSM_TEST,    `EJTAG_TAP_FSM_IDLE,    `EJTAG_TAP_FSM_SELDR,
      `EJTAG_TAP_FSM_EXIT1DR, `EJTAG_TAP_FSM_PAUSEDR, `EJTAG_TAP_FSM_EXIT2DR,
      `EJTAG_TAP_FSM_SELIR    :
      begin
        ind         <= 1'b0;
        capture_ir  <= 1'b0;
        shift_ir    <= 1'b0;
        updata_ir   <= 1'b0;
        capture_dr  <= 1'b0;
        shift_dr    <= 1'b0;
        updata_dr   <= 1'b0;
      end

      `EJTAG_TAP_FSM_EXIT1IR, `EJTAG_TAP_FSM_PAUSEIR, `EJTAG_TAP_FSM_EXIT2IR  :
      begin
        ind         <= 1'b1;
        capture_ir  <= 1'b0;
        shift_ir    <= 1'b0;
        updata_ir   <= 1'b0;
        capture_dr  <= 1'b0;
        shift_dr    <= 1'b0;
        updata_dr   <= 1'b0;
      end

      `EJTAG_TAP_FSM_CAPTUREDR : 
      begin
        ind         <= 1'b0;
        capture_dr  <= 1'b1;
      end

      `EJTAG_TAP_FSM_SHIFTDR   : 
      begin
        capture_dr  <= 1'b0;
        shift_dr    <= 1'b1;
      end

      `EJTAG_TAP_FSM_UPDATADR  : 
      begin
        shift_dr    <= 1'b0;
        updata_dr   <= 1'b1;
      end

      `EJTAG_TAP_FSM_CAPTUREIR : 
      begin
        ind         <= 1'b1;
        capture_ir  <= 1'b1;
      end

      `EJTAG_TAP_FSM_SHIFTIR   : 
      begin
        capture_ir  <= 1'b0;
        shift_ir    <= 1'b1;
      end

      default                  : //`EJTAG_TAP_FSM_UPDATAIR
      begin  
        shift_ir    <= 1'b0;
        updata_ir   <= 1'b1;
      end
    endcase
  end
end

endmodule //ejtag_tap_fsm


module ejtag_tap_decode(
  inst   ,
  sel    ,
  norboot,
  boot   
);

input   [ 4:0]  inst;

output  [13:0]  sel;
reg     [13:0]  sel;
output          norboot;
reg             norboot;
output          boot;
reg             boot;

always @(inst)
  case(inst)
    `EJTAG_TAP_INST_IDCODE      : {sel,norboot,boot} = {14'b00_0000_0000_0001,1'b0,1'b0};
    `EJTAG_TAP_INST_IMPCODE     : {sel,norboot,boot} = {14'b00_0000_0000_0010,1'b0,1'b0};
    `EJTAG_TAP_INST_ADDRESS     : {sel,norboot,boot} = {14'b00_0000_0000_0100,1'b0,1'b0};
    `EJTAG_TAP_INST_DATA        : {sel,norboot,boot} = {14'b00_0000_0000_1000,1'b0,1'b0};
    `EJTAG_TAP_INST_CONTROL     : {sel,norboot,boot} = {14'b00_0000_0001_0000,1'b0,1'b0};
    `EJTAG_TAP_INST_ALL         : {sel,norboot,boot} = {14'b00_0100_0001_1100,1'b0,1'b0};
    `EJTAG_TAP_INST_FASTDATA    : {sel,norboot,boot} = {14'b00_1000_0010_1000,1'b0,1'b0};
    `EJTAG_TAP_INST_EJTAGBOOT   : {sel,norboot,boot} = {14'b00_0000_0100_0000,1'b0,1'b1};
    `EJTAG_TAP_INST_NORMALBOOT  : {sel,norboot,boot} = {14'b00_0000_0100_0000,1'b1,1'b0};
    `EJTAG_TAP_INST_TCBCONTROLA : {sel,norboot,boot} = {14'b00_0000_1000_0000,1'b0,1'b0};
    `EJTAG_TAP_INST_TCBCONTROLB : {sel,norboot,boot} = {14'b00_0001_0000_0000,1'b0,1'b0};
    `EJTAG_TAP_INST_TCBDATA     : {sel,norboot,boot} = {14'b00_0010_0000_0000,1'b0,1'b0};
    `EJTAG_TAP_INST_PCSAMPLE    : {sel,norboot,boot} = {14'b00_0100_0000_0000,1'b0,1'b0};
    `EJTAG_TAP_INST_DBTX        : {sel,norboot,boot} = {14'b01_0000_0000_0000,1'b0,1'b0};
    `EJTAG_TAP_INST_BYPASS      : {sel,norboot,boot} = {14'b00_0000_0100_0000,1'b0,1'b0};
     default                    : {sel,norboot,boot} = {14'b00_0000_0000_0000,1'b0,1'b0};
 endcase

endmodule



module ejtag_tap_basicreg(
  clk   ,
  rst_n ,
  en    ,
  cap   ,
  shift ,
  updata,
  si    ,
  pi    ,
  so    ,
  po    
);

parameter WIDTH     = 32;
parameter RST_VAL   = 32'h0000_0000;

input               clk, rst_n;
input               en;
input               cap, shift, updata;
input               si;
output              so;
input  [WIDTH-1:0]  pi;
output [WIDTH-1:0]  po;
reg    [WIDTH-1:0]  po;


reg    [WIDTH-1:0]  sr;

assign so = sr[0];

always @(posedge clk or negedge rst_n)
begin
  if (!rst_n)
    sr <= RST_VAL;
  else
  begin
    if (cap && en)
      sr <= pi;
    else if (shift && en)
      sr <= {si, sr[WIDTH-1:1]};
  end
end

always @(posedge clk or negedge rst_n)
begin
  if (!rst_n)
    po <= RST_VAL;
  else if(updata && en)
    po <= sr;
end

endmodule
