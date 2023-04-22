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

module ls132r_ejtag_dcr(
  clock,
  reset,
  nmi_in, 
  proben_in,
  dcr_reqbus, 
  drseg_res_dcr,
  inte,
  nmi_out,
  proben_out
);

input                       clock     ;
input                       reset     ;
input  [`Ldrsegreqbus-1:0]  dcr_reqbus;
input                       nmi_in    ;
input                       proben_in ;

output [31:0]   drseg_res_dcr;
output          inte         ;
output          nmi_out      ;
output          proben_out   ;

wire        dcr_req_valid = dcr_reqbus[ 0: 0];
wire        dcr_req_wr    = dcr_reqbus[ 1: 1];
wire [31:0] dcr_req_addr  = dcr_reqbus[33: 2];
wire [31:0] dcr_req_wdata = dcr_reqbus[65:34];

wire        dcr_valid;
wire [31:0] dcr_value;

assign drseg_res_dcr   = dcr_value;

reg  [ 3:0] dcr_reg;

wire        dcr_ENM     = 1'b0;
wire        dcr_DataBrk = 1'b0;
wire        dcr_InstBrk = 1'b1;
wire        dcr_SRstE   = 1'b1;

always @(posedge clock)
begin
  if (reset)
    dcr_reg[3:1] <= 3'b110;
  else
  begin
    dcr_reg[1] <= nmi_in;

    if (dcr_req_valid && dcr_req_wr)
      dcr_reg[3:2] <= dcr_req_wdata[4:3]; 
  end

  dcr_reg[0]<=proben_in;
end

assign dcr_valid  = 1'b1;
assign dcr_value = {2'b00, dcr_ENM, 11'b0, dcr_DataBrk,
                    dcr_InstBrk, 11'b0, dcr_reg[3:1],
                    dcr_SRstE, dcr_reg[0]};


assign inte       = dcr_reg[3]            ;
assign nmi_out    = dcr_reg[2]&&dcr_reg[1]; //NMIE && NMIpend
assign proben_out = dcr_reg[0]            ;

endmodule
