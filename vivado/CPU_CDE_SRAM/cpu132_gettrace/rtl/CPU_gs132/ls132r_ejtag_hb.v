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

module ls132r_ejtag_hb(
  clock,
  reset,
  debug_mode,
  exbus_i,
  hb_reqbus,
  drseg_res_hb,

  hb_icompbus,
  hb_dit,
  hb_dib 

);

input                      clock;
input                      reset;
input                      debug_mode;
input  [`Ldrsegreqbus-1:0] hb_reqbus;
output [             31:0] drseg_res_hb;          
input  [      `Lexbus-1:0] exbus_i;

input  [   `Licompbus-1:0] hb_icompbus;
output                     hb_dit;
output                     hb_dib;

wire        ex_valid  = exbus_i[ 0: 0];
wire [ 5:0] ex_excode = exbus_i[13: 8];
wire [31:0] ex_pc     = exbus_i[45:14];




// Internal Logic
wire [31:0] drseg_res_ib;

ls132r_ejtag_ib ib_ins(
     .clock         (clock      ),
     .reset         (reset      ),
     .debug_mode    (debug_mode ),
     .hb_reqbus     (hb_reqbus  ),
     .hb_icompbus   (hb_icompbus),
     .ex_valid      (ex_valid   ),
     .ex_excode     (ex_excode  ),
     .ex_pc         (ex_pc      ),
     .drseg_res_ib  (drseg_res_ib),
     .hb_dit        (hb_dit     ),
     .hb_dib        (hb_dib     )
    );

assign drseg_res_hb = drseg_res_ib;

endmodule



//*******************************************************************
//function description                      instruction breakpoint 
//********************************************************************

module ls132r_ejtag_ib(clock,
  reset,
  debug_mode,
  hb_reqbus,
  hb_icompbus,
  ex_excode,
  ex_valid,
  ex_pc,
  drseg_res_ib,
  hb_dit,
  hb_dib
);
parameter IBPNUM=`LS132R_IBKP_NUM;

input                      clock;
input                      reset;
input                      debug_mode;
input  [`Ldrsegreqbus-1:0] hb_reqbus;
input  [   `Licompbus-1:0] hb_icompbus;
input                      ex_valid;
input  [              5:0] ex_excode;
input  [             31:0] ex_pc;

output [             31:0] drseg_res_ib;
output                     hb_dit;
output                     hb_dib;

wire        hb_req_valid = hb_reqbus[ 0: 0];
wire        hb_req_wr    = hb_reqbus[ 1: 1];
wire [31:0] hb_req_addr  = hb_reqbus[33: 2];
wire [31:0] hb_req_wdata = hb_reqbus[65:34];

wire        hb_ivalid = hb_icompbus[ 0: 0];
wire [31:0] pc        = hb_icompbus[32: 1];

//ib_reg
reg  [IBPNUM-1:0]    ibs; //only bit: [IBPNUM-1:0]
reg  [32*IBPNUM-1:0] iba;
reg  [32*IBPNUM-1:0] ibm;
reg  [ 2*IBPNUM-1:0] ibc; //only bit: 2, 0

integer i,j,k,l,m,n,t,p;

wire [31:0] cmp_pc    = {32{hb_ivalid}}&pc;
wire [31:0] cmp_ex_pc = {32{ex_valid}}&ex_pc;

wire [ 3:0] ibs_num = IBPNUM;

reg  [31:0]       ib_out;
reg  [IBPNUM*4:0] reg_rw_select;

reg               ihardbreak_skip;
wire [IBPNUM-1:0] ib_match;
reg  [IBPNUM-1:0] ib_match_be_0;
reg  [IBPNUM-1:0] ib_match_te_0;
reg  [IBPNUM-1:0] ib_match_0;
reg  [IBPNUM-1:0] ib_match_commit;

// reg addr decode
always @(*)
begin
  if (hb_req_valid && hb_req_addr[12])
  begin
    if ({hb_req_addr[11:8],hb_req_addr[4:3]}==6'h0)
      reg_rw_select[0]=1'b1;
    else 
      reg_rw_select[0]=1'b0;    

    for (k=1;k<(IBPNUM+1);k=k+1)
      for (j=0;j<4;j=j+1)
      begin
        if ((k==hb_req_addr[11:8]) && (j==hb_req_addr[4:3]))
            reg_rw_select[(k-1)*4+j+1]=1'b1;
        else 
            reg_rw_select[(k-1)*4+j+1]=1'b0;
      end
  end                    
  else
    reg_rw_select={(IBPNUM*4+1){1'b0}};
end


always @(posedge clock)
begin
  if(reset)
  begin
    ibc <= {(2*IBPNUM){1'b0}};
  end
  else if (debug_mode && hb_req_valid && hb_req_wr)
  begin
    for(t=0; t<IBPNUM; t=t+1)
    begin
      if(reg_rw_select[t*4+0+1])
        {iba[32*t+31],iba[32*t+30],iba[32*t+29],iba[32*t+28],
         iba[32*t+27],iba[32*t+26],iba[32*t+25],iba[32*t+24],
         iba[32*t+23],iba[32*t+22],iba[32*t+21],iba[32*t+20],
         iba[32*t+19],iba[32*t+18],iba[32*t+17],iba[32*t+16],
         iba[32*t+15],iba[32*t+14],iba[32*t+13],iba[32*t+12],
         iba[32*t+11],iba[32*t+10],iba[32*t+9 ],iba[32*t+8 ],
         iba[32*t+7 ],iba[32*t+6 ],iba[32*t+5 ],iba[32*t+4 ],
         iba[32*t+3 ],iba[32*t+2 ],iba[32*t+1 ],iba[32*t+0 ]} <= hb_req_wdata;

      if(reg_rw_select[t*4+1+1])
        {ibm[32*t+31],ibm[32*t+30],ibm[32*t+29],ibm[32*t+28],
         ibm[32*t+27],ibm[32*t+26],ibm[32*t+25],ibm[32*t+24],
         ibm[32*t+23],ibm[32*t+22],ibm[32*t+21],ibm[32*t+20],
         ibm[32*t+19],ibm[32*t+18],ibm[32*t+17],ibm[32*t+16],
         ibm[32*t+15],ibm[32*t+14],ibm[32*t+13],ibm[32*t+12],
         ibm[32*t+11],ibm[32*t+10],ibm[32*t+9 ],ibm[32*t+8 ],
         ibm[32*t+7 ],ibm[32*t+6 ],ibm[32*t+5 ],ibm[32*t+4 ],
         ibm[32*t+3 ],ibm[32*t+2 ],ibm[32*t+1 ],ibm[32*t+0 ]} <= hb_req_wdata;
    
      if(reg_rw_select[t*4+3+1])
      begin
        ibc[2*t+1] <= hb_req_wdata[2];
        ibc[2*t+0] <= hb_req_wdata[0];  
      end
    end
  end
end
        
//r
always @(*)
begin
  ib_out=32'b0;//default
  if (debug_mode && hb_req_valid && !hb_req_wr)
  begin
    if (reg_rw_select[0])
      ib_out = {4'h0, ibs_num, 8'h0, {(16-IBPNUM){1'b0}}, ibs};
    else
    begin
      for (i=0;i<IBPNUM;i=i+1)
        if (reg_rw_select[i*4+0+1])
          ib_out = 
            {iba[32*i+31],iba[32*i+30],iba[32*i+29],iba[32*i+28],
             iba[32*i+27],iba[32*i+26],iba[32*i+25],iba[32*i+24],
             iba[32*i+23],iba[32*i+22],iba[32*i+21],iba[32*i+20],
             iba[32*i+19],iba[32*i+18],iba[32*i+17],iba[32*i+16],
             iba[32*i+15],iba[32*i+14],iba[32*i+13],iba[32*i+12],
             iba[32*i+11],iba[32*i+10],iba[32*i+9 ],iba[32*i+8 ],
             iba[32*i+7 ],iba[32*i+6 ],iba[32*i+5 ],iba[32*i+4 ],
             iba[32*i+3 ],iba[32*i+2 ],iba[32*i+1 ],iba[32*i+0 ]};
        else if(reg_rw_select[i*4+1+1])        
          ib_out = 
            {ibm[32*i+31],ibm[32*i+30],ibm[32*i+29],ibm[32*i+28],
             ibm[32*i+27],ibm[32*i+26],ibm[32*i+25],ibm[32*i+24],
             ibm[32*i+23],ibm[32*i+22],ibm[32*i+21],ibm[32*i+20],
             ibm[32*i+19],ibm[32*i+18],ibm[32*i+17],ibm[32*i+16],
             ibm[32*i+15],ibm[32*i+14],ibm[32*i+13],ibm[32*i+12],
             ibm[32*i+11],ibm[32*i+10],ibm[32*i+9 ],ibm[32*i+8 ],
             ibm[32*i+7 ],ibm[32*i+6 ],ibm[32*i+5 ],ibm[32*i+4 ],
             ibm[32*i+3 ],ibm[32*i+2 ],ibm[32*i+1 ],ibm[32*i+0 ]};   
        else if(reg_rw_select[i*4+3+1])
          ib_out = {29'h0, ibc[2*i+1], 1'b0, ibc[2*i+0]};
     end
  end 
end

assign drseg_res_ib = ib_out;                                                 

//compare
always @(*)
begin
  for (t=0;t<IBPNUM;t=t+1)
    ib_match_0[t] = 
        (cmp_pc | 
         {ibm[32*t+31],ibm[32*t+30],ibm[32*t+29],ibm[32*t+28],
          ibm[32*t+27],ibm[32*t+26],ibm[32*t+25],ibm[32*t+24],
          ibm[32*t+23],ibm[32*t+22],ibm[32*t+21],ibm[32*t+20],
          ibm[32*t+19],ibm[32*t+18],ibm[32*t+17],ibm[32*t+16],
          ibm[32*t+15],ibm[32*t+14],ibm[32*t+13],ibm[32*t+12],
          ibm[32*t+11],ibm[32*t+10],ibm[32*t+9 ],ibm[32*t+8 ],
          ibm[32*t+7 ],ibm[32*t+6 ],ibm[32*t+5 ],ibm[32*t+4 ],
          ibm[32*t+3 ],ibm[32*t+2 ],ibm[32*t+1 ],ibm[32*t+0 ]}
        )
      ==({iba[32*t+31],iba[32*t+30],iba[32*t+29],iba[32*t+28],
          iba[32*t+27],iba[32*t+26],iba[32*t+25],iba[32*t+24],
          iba[32*t+23],iba[32*t+22],iba[32*t+21],iba[32*t+20],
          iba[32*t+19],iba[32*t+18],iba[32*t+17],iba[32*t+16],
          iba[32*t+15],iba[32*t+14],iba[32*t+13],iba[32*t+12],
          iba[32*t+11],iba[32*t+10],iba[32*t+9 ],iba[32*t+8 ],
          iba[32*t+7 ],iba[32*t+6 ],iba[32*t+5 ],iba[32*t+4 ],
          iba[32*t+3 ],iba[32*t+2 ],iba[32*t+1 ],iba[32*t+0 ]} | 
         {ibm[32*t+31],ibm[32*t+30],ibm[32*t+29],ibm[32*t+28],
          ibm[32*t+27],ibm[32*t+26],ibm[32*t+25],ibm[32*t+24],
          ibm[32*t+23],ibm[32*t+22],ibm[32*t+21],ibm[32*t+20],
          ibm[32*t+19],ibm[32*t+18],ibm[32*t+17],ibm[32*t+16],
          ibm[32*t+15],ibm[32*t+14],ibm[32*t+13],ibm[32*t+12],
          ibm[32*t+11],ibm[32*t+10],ibm[32*t+9 ],ibm[32*t+8 ],
          ibm[32*t+7 ],ibm[32*t+6 ],ibm[32*t+5 ],ibm[32*t+4 ],
          ibm[32*t+3 ],ibm[32*t+2 ],ibm[32*t+1 ],ibm[32*t+0 ]}
        );
end


always @(*)
  for (m=0;m<IBPNUM;m=m+1)
  begin
    ib_match_te_0[m]=ib_match_0[m]&ibc[2*m+1];
    ib_match_be_0[m]=ib_match_0[m]&ibc[2*m+0];
  end        

assign hb_dit = (|ib_match_te_0);
assign hb_dib = (|ib_match_be_0);

//compare ex_pc 
always @(*)
begin
  for (t=0;t<IBPNUM;t=t+1)
    ib_match_commit[t] = 
        (cmp_ex_pc | 
         {ibm[32*t+31],ibm[32*t+30],ibm[32*t+29],ibm[32*t+28],
          ibm[32*t+27],ibm[32*t+26],ibm[32*t+25],ibm[32*t+24],
          ibm[32*t+23],ibm[32*t+22],ibm[32*t+21],ibm[32*t+20],
          ibm[32*t+19],ibm[32*t+18],ibm[32*t+17],ibm[32*t+16],
          ibm[32*t+15],ibm[32*t+14],ibm[32*t+13],ibm[32*t+12],
          ibm[32*t+11],ibm[32*t+10],ibm[32*t+9 ],ibm[32*t+8 ],
          ibm[32*t+7 ],ibm[32*t+6 ],ibm[32*t+5 ],ibm[32*t+4 ],
          ibm[32*t+3 ],ibm[32*t+2 ],ibm[32*t+1 ],ibm[32*t+0 ]}
        )
      ==({iba[32*t+31],iba[32*t+30],iba[32*t+29],iba[32*t+28],
          iba[32*t+27],iba[32*t+26],iba[32*t+25],iba[32*t+24],
          iba[32*t+23],iba[32*t+22],iba[32*t+21],iba[32*t+20],
          iba[32*t+19],iba[32*t+18],iba[32*t+17],iba[32*t+16],
          iba[32*t+15],iba[32*t+14],iba[32*t+13],iba[32*t+12],
          iba[32*t+11],iba[32*t+10],iba[32*t+9 ],iba[32*t+8 ],
          iba[32*t+7 ],iba[32*t+6 ],iba[32*t+5 ],iba[32*t+4 ],
          iba[32*t+3 ],iba[32*t+2 ],iba[32*t+1 ],iba[32*t+0 ]} | 
         {ibm[32*t+31],ibm[32*t+30],ibm[32*t+29],ibm[32*t+28],
          ibm[32*t+27],ibm[32*t+26],ibm[32*t+25],ibm[32*t+24],
          ibm[32*t+23],ibm[32*t+22],ibm[32*t+21],ibm[32*t+20],
          ibm[32*t+19],ibm[32*t+18],ibm[32*t+17],ibm[32*t+16],
          ibm[32*t+15],ibm[32*t+14],ibm[32*t+13],ibm[32*t+12],
          ibm[32*t+11],ibm[32*t+10],ibm[32*t+9 ],ibm[32*t+8 ],
          ibm[32*t+7 ],ibm[32*t+6 ],ibm[32*t+5 ],ibm[32*t+4 ],
          ibm[32*t+3 ],ibm[32*t+2 ],ibm[32*t+1 ],ibm[32*t+0 ]}
        );end

assign ib_match = ib_match_commit; 

always@(posedge clock)
  if (reset)
    ibs <= {IBPNUM{1'b0}};
  else if (debug_mode && hb_req_valid && hb_req_wr && reg_rw_select[0])
    ibs <= ibs & hb_req_wdata[IBPNUM-1:0];   
  else if (!debug_mode && ex_valid && (ex_excode==`LS132R_EX_DIB) &&
           !(|ibs[IBPNUM-1:0]))
    for (p=0;p<IBPNUM;p=p+1)
      ibs[p] <= ib_match[p] && (ibc[2*p+1] || ibc[2*p+0]);
  else if (!debug_mode && ex_valid && !(ex_excode==`LS132R_EX_DIB))
    ibs[IBPNUM-1:0] <= {IBPNUM{1'b0}};

                     
endmodule
