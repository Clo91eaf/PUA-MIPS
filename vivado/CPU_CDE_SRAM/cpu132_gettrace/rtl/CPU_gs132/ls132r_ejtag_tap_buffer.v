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

module ls132r_ejtag_tap_buffer(
  clock , reset , softreset , commit_ex , test_mode,
  dmseg_dreqbus ,
  dmseg_ireqbus , ejtagbrk_from_core , debugmode_from_core ,
  data_from_tap , pracc_from_tap , prrst_from_tap ,
  proben_from_tap , trap_from_tap , ejtagbrk_from_tap ,
  iresbus_from_dmseg , dresbus_from_dmseg , 
  ejtagbrk_to_core , prrst_to_core ,
  proben_to_core , trap_to_core , 
  addr_to_tap , data_to_tap ,
  width_to_tap , write_to_tap , pracc_to_tap , ejtagbrk_to_tap,
  reset_to_tap , debugmode_to_tap
  );

input           clock              ;
input           reset              ;
input           softreset          ;
input           commit_ex          ;
input           test_mode          ;

input           ejtagbrk_from_core ;
input           debugmode_from_core;

output          ejtagbrk_to_core  ;
output          prrst_to_core     ;
output          proben_to_core    ;
output          trap_to_core      ;

input  [`Ldmsegdreqbus-1:0] dmseg_dreqbus      ;
input  [`Ldmsegireqbus-1:0] dmseg_ireqbus      ;
output [`Ldmsegdresbus-1:0] dresbus_from_dmseg; 
output [`Ldmsegiresbus-1:0] iresbus_from_dmseg;

input  [31:0]   data_from_tap    ;
input           pracc_from_tap   ;
input           prrst_from_tap   ;
input           proben_from_tap  ;
input           trap_from_tap    ;
input           ejtagbrk_from_tap;

output [31:0]   addr_to_tap     ;
output [31:0]   data_to_tap     ;
output [ 1:0]   width_to_tap    ;
output          write_to_tap    ;
output          pracc_to_tap    ;
output          ejtagbrk_to_tap ;
output          reset_to_tap    ;
output          debugmode_to_tap;

// dmseg_dreqbus
wire        dmseg_dreq_valid = dmseg_dreqbus[ 0: 0];
wire        dmseg_dreq_wr    = dmseg_dreqbus[ 1: 1];
wire [ 1:0] dmseg_dreq_width = dmseg_dreqbus[ 3: 2];
wire [31:0] dmseg_dreq_addr  = dmseg_dreqbus[35: 4];
wire [31:0] dmseg_dreq_wdata = dmseg_dreqbus[67:36];

// dmseg_ireqbus
wire        dmseg_ireq_valid = dmseg_ireqbus[ 0: 0];
wire [ 1:0] dmseg_ireq_width = dmseg_ireqbus[ 2: 1];
wire [31:0] dmseg_ireq_addr  = dmseg_ireqbus[34: 3];

// dresbus_from_dmseg
wire        dmseg_dres_valid;
wire [31:0] dmseg_dres_value;
wire        dmseg_dreq_ack  ;

assign dresbus_from_dmseg[ 0: 0] = dmseg_dres_valid;
assign dresbus_from_dmseg[32: 1] = dmseg_dres_value;
assign dresbus_from_dmseg[33:33] = dmseg_dreq_ack  ;

// iresbus_from_dmseg
wire        dmseg_ires_valid;
wire [31:0] dmseg_ires_value;
wire        dmseg_ireq_ack  ;

assign iresbus_from_dmseg[ 0: 0] = dmseg_ires_valid;
assign iresbus_from_dmseg[32: 1] = dmseg_ires_value;
assign iresbus_from_dmseg[33:33] = dmseg_ireq_ack  ;


reg         pracc_from_tap_0_reg   , pracc_from_tap_reg   ;
reg         prrst_from_tap_0_reg   , prrst_from_tap_reg   ;
reg         proben_from_tap_0_reg  , proben_from_tap_reg  ;
reg         trap_from_tap_0_reg    , trap_from_tap_reg    ;
reg         ejtagbrk_from_tap_0_reg, ejtagbrk_from_tap_reg;

reg         pracc_to_tap_reg;
reg         keep_pracc_reg  ;
reg         keep_dint_reg   ;
reg         dreq_reg        ;
reg         busy_reg        ;

always @(posedge clock)
begin
  pracc_from_tap_0_reg  <= pracc_from_tap;
  pracc_from_tap_reg    <= pracc_from_tap_0_reg;
  keep_pracc_reg        <= pracc_from_tap_reg;

  prrst_from_tap_0_reg  <= prrst_from_tap;
  prrst_from_tap_reg    <= prrst_from_tap_0_reg;

  proben_from_tap_0_reg <= proben_from_tap;
  proben_from_tap_reg   <= proben_from_tap_0_reg;

  trap_from_tap_0_reg   <= trap_from_tap;
  trap_from_tap_reg     <= trap_from_tap_0_reg;

  ejtagbrk_from_tap_0_reg <= ejtagbrk_from_tap;
  ejtagbrk_from_tap_reg   <= ejtagbrk_from_tap_0_reg;
  
  keep_dint_reg         <= ejtagbrk_from_core;
end

always @(posedge clock)
begin
  if (reset || commit_ex)
  begin
    pracc_to_tap_reg    <=1'b0;
    busy_reg            <=1'b0;
    dreq_reg            <=1'b0;
  end
  else
  begin
    if ((dmseg_ireq_valid || dmseg_dreq_valid) && !pracc_to_tap_reg &&
        !pracc_from_tap_reg && !busy_reg)
    begin
      pracc_to_tap_reg <=1'b1;
      busy_reg         <=1'b1;
      dreq_reg         <= dmseg_dreq_valid;
    end
    else 
    begin
      if (!keep_pracc_reg && pracc_from_tap_reg)
        pracc_to_tap_reg <= 1'b0;

      if (keep_pracc_reg && !pracc_from_tap_reg && !pracc_to_tap_reg)
        busy_reg <=1'b0;
    end
  end
end

// OUTPUT
// --> TAP
assign addr_to_tap      = dreq_reg ? dmseg_dreq_addr : dmseg_ireq_addr;
assign data_to_tap      = dmseg_dreq_wdata;
assign width_to_tap     = dreq_reg ? dmseg_dreq_width : dmseg_ireq_width;
assign write_to_tap     = dreq_reg & dmseg_dreq_wr;
assign pracc_to_tap     = pracc_to_tap_reg;
assign ejtagbrk_to_tap  = ejtagbrk_from_core;
assign reset_to_tap     = reset || softreset;
assign debugmode_to_tap = debugmode_from_core;


// --> CORE
assign prrst_to_core    = ~test_mode & prrst_from_tap_reg;
assign proben_to_core   = proben_from_tap_reg;
assign trap_to_core     = trap_from_tap_reg;
assign ejtagbrk_to_core = ejtagbrk_from_tap_reg;

assign dmseg_ires_valid = !dreq_reg && keep_pracc_reg && !pracc_from_tap_reg;
assign dmseg_ires_value = data_from_tap;
assign dmseg_ireq_ack   = !dreq_reg && pracc_to_tap_reg && 
                          !keep_pracc_reg && pracc_from_tap_reg;

assign dmseg_dres_valid =  dreq_reg && keep_pracc_reg && !pracc_from_tap_reg;
assign dmseg_dres_value = data_from_tap;
assign dmseg_dreq_ack   =  dreq_reg && pracc_to_tap_reg &&
                          !keep_pracc_reg && pracc_from_tap_reg;

endmodule


