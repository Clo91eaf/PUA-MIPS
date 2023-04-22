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

module ls132r_decoder_5_32(in,out);
    
input [4:0] in;
output [31:0] out; 

wire [4:0]in;
wire [31:0]out;

wire [3:0] high_d;
wire [7:0] low_d;

assign high_d[3]=( in[4])&( in[3]);
assign high_d[2]=( in[4])&(~in[3]);
assign high_d[1]=(~in[4])&( in[3]);
assign high_d[0]=(~in[4])&(~in[3]);

assign low_d[7]=( in[2])&( in[1])&( in[0]);
assign low_d[6]=( in[2])&( in[1])&(~in[0]);
assign low_d[5]=( in[2])&(~in[1])&( in[0]);
assign low_d[4]=( in[2])&(~in[1])&(~in[0]);
assign low_d[3]=(~in[2])&( in[1])&( in[0]);
assign low_d[2]=(~in[2])&( in[1])&(~in[0]);
assign low_d[1]=(~in[2])&(~in[1])&( in[0]);
assign low_d[0]=(~in[2])&(~in[1])&(~in[0]);

assign out[31]=high_d[3]&low_d[7];
assign out[30]=high_d[3]&low_d[6];
assign out[29]=high_d[3]&low_d[5];
assign out[28]=high_d[3]&low_d[4];
assign out[27]=high_d[3]&low_d[3];
assign out[26]=high_d[3]&low_d[2];
assign out[25]=high_d[3]&low_d[1];
assign out[24]=high_d[3]&low_d[0];    
assign out[23]=high_d[2]&low_d[7];
assign out[22]=high_d[2]&low_d[6];
assign out[21]=high_d[2]&low_d[5];
assign out[20]=high_d[2]&low_d[4];
assign out[19]=high_d[2]&low_d[3];
assign out[18]=high_d[2]&low_d[2];
assign out[17]=high_d[2]&low_d[1];
assign out[16]=high_d[2]&low_d[0];    
assign out[15]=high_d[1]&low_d[7];
assign out[14]=high_d[1]&low_d[6];
assign out[13]=high_d[1]&low_d[5];
assign out[12]=high_d[1]&low_d[4];
assign out[11]=high_d[1]&low_d[3];
assign out[10]=high_d[1]&low_d[2];
assign out[ 9]=high_d[1]&low_d[1];
assign out[ 8]=high_d[1]&low_d[0];    
assign out[ 7]=high_d[0]&low_d[7];
assign out[ 6]=high_d[0]&low_d[6];
assign out[ 5]=high_d[0]&low_d[5];
assign out[ 4]=high_d[0]&low_d[4];
assign out[ 3]=high_d[0]&low_d[3];
assign out[ 2]=high_d[0]&low_d[2];
assign out[ 1]=high_d[0]&low_d[1];
assign out[ 0]=high_d[0]&low_d[0];    

endmodule //ls132r_decoder_5_32





module ls132r_decoder_6_64(in,out);

input  [ 5:0] in;
output [63:0] out; 

wire [5:0]in;
wire [63:0] out;
wire [7:0] high_d;
wire [7:0] low_d;

assign high_d[7]=( in[5])&( in[4])&( in[3]);
assign high_d[6]=( in[5])&( in[4])&(~in[3]);
assign high_d[5]=( in[5])&(~in[4])&( in[3]);
assign high_d[4]=( in[5])&(~in[4])&(~in[3]);
assign high_d[3]=(~in[5])&( in[4])&( in[3]);
assign high_d[2]=(~in[5])&( in[4])&(~in[3]);
assign high_d[1]=(~in[5])&(~in[4])&( in[3]);
assign high_d[0]=(~in[5])&(~in[4])&(~in[3]);

assign low_d[7]=( in[2])&( in[1])&( in[0]);
assign low_d[6]=( in[2])&( in[1])&(~in[0]);
assign low_d[5]=( in[2])&(~in[1])&( in[0]);
assign low_d[4]=( in[2])&(~in[1])&(~in[0]);
assign low_d[3]=(~in[2])&( in[1])&( in[0]);
assign low_d[2]=(~in[2])&( in[1])&(~in[0]);
assign low_d[1]=(~in[2])&(~in[1])&( in[0]);
assign low_d[0]=(~in[2])&(~in[1])&(~in[0]);

assign out[63]=high_d[7]&low_d[7];
assign out[62]=high_d[7]&low_d[6];
assign out[61]=high_d[7]&low_d[5];
assign out[60]=high_d[7]&low_d[4];
assign out[59]=high_d[7]&low_d[3];
assign out[58]=high_d[7]&low_d[2];
assign out[57]=high_d[7]&low_d[1];
assign out[56]=high_d[7]&low_d[0];    
assign out[55]=high_d[6]&low_d[7];
assign out[54]=high_d[6]&low_d[6];
assign out[53]=high_d[6]&low_d[5];
assign out[52]=high_d[6]&low_d[4];
assign out[51]=high_d[6]&low_d[3];
assign out[50]=high_d[6]&low_d[2];
assign out[49]=high_d[6]&low_d[1];
assign out[48]=high_d[6]&low_d[0];    
assign out[47]=high_d[5]&low_d[7];
assign out[46]=high_d[5]&low_d[6];
assign out[45]=high_d[5]&low_d[5];
assign out[44]=high_d[5]&low_d[4];
assign out[43]=high_d[5]&low_d[3];
assign out[42]=high_d[5]&low_d[2];
assign out[41]=high_d[5]&low_d[1];
assign out[40]=high_d[5]&low_d[0];    
assign out[39]=high_d[4]&low_d[7];
assign out[38]=high_d[4]&low_d[6];
assign out[37]=high_d[4]&low_d[5];
assign out[36]=high_d[4]&low_d[4];
assign out[35]=high_d[4]&low_d[3];
assign out[34]=high_d[4]&low_d[2];
assign out[33]=high_d[4]&low_d[1];
assign out[32]=high_d[4]&low_d[0];    
assign out[31]=high_d[3]&low_d[7];
assign out[30]=high_d[3]&low_d[6];
assign out[29]=high_d[3]&low_d[5];
assign out[28]=high_d[3]&low_d[4];
assign out[27]=high_d[3]&low_d[3];
assign out[26]=high_d[3]&low_d[2];
assign out[25]=high_d[3]&low_d[1];
assign out[24]=high_d[3]&low_d[0];    
assign out[23]=high_d[2]&low_d[7];
assign out[22]=high_d[2]&low_d[6];
assign out[21]=high_d[2]&low_d[5];
assign out[20]=high_d[2]&low_d[4];
assign out[19]=high_d[2]&low_d[3];
assign out[18]=high_d[2]&low_d[2];
assign out[17]=high_d[2]&low_d[1];
assign out[16]=high_d[2]&low_d[0];    
assign out[15]=high_d[1]&low_d[7];
assign out[14]=high_d[1]&low_d[6];
assign out[13]=high_d[1]&low_d[5];
assign out[12]=high_d[1]&low_d[4];
assign out[11]=high_d[1]&low_d[3];
assign out[10]=high_d[1]&low_d[2];
assign out[ 9]=high_d[1]&low_d[1];
assign out[ 8]=high_d[1]&low_d[0];    
assign out[ 7]=high_d[0]&low_d[7];
assign out[ 6]=high_d[0]&low_d[6];
assign out[ 5]=high_d[0]&low_d[5];
assign out[ 4]=high_d[0]&low_d[4];
assign out[ 3]=high_d[0]&low_d[3];
assign out[ 2]=high_d[0]&low_d[2];
assign out[ 1]=high_d[0]&low_d[1];
assign out[ 0]=high_d[0]&low_d[0];    

endmodule //ls132r_decoder_6_64

module ls132r_first_one_32_5(in,out,nonzero);
input  [31:0] in;
output [4:0] out;
output nonzero;

wire [2:0] a3,a2,a1,a0;
wire [3:0] nz;
wire zero;

ls132r_first_one_8_3 first_one3(.in(in[31:24]),.out(a3),.nonzero(nz[3]));
ls132r_first_one_8_3 first_one2(.in(in[23:16]),.out(a2),.nonzero(nz[2]));
ls132r_first_one_8_3 first_one1(.in(in[15: 8]),.out(a1),.nonzero(nz[1]));
ls132r_first_one_8_3 first_one0(.in(in[ 7: 0]),.out(a0),.nonzero(nz[0]));

ls132r_first_one_4_2 first_one4(.in(nz),.out(out[4:3]),.nonzero(nonzero));

assign out[2:0]= a0 |
                (a1 & {3{~nz[0]}})    |
                (a2 & {3{~|nz[1:0]}}) |
                (a3 & {3{~|nz[2:0]}});

endmodule

module ls132r_first_one_8_3(in,out,nonzero);
input  [7:0] in;
output [2:0] out;
output nonzero;
 
assign out[2]=(~(|in[3:0]))&(|in[7:4]);
assign out[1]=((~in[0]) & (~in[1]) & (~in[4]) & (~in[5]) & (in[6] | in[7])) |
              ((~in[0]) & (~in[1]) & (in[2] | in[3]));
assign out[0]=((~in[0]) & (~in[2]) & (~in[4]) & (~in[6]) & in[7])|
              ((~in[0]) & (~in[2]) & (~in[4]) & in[5])|
              ((~in[0]) & (~in[2]) & in[3])|
              ((~in[0]) & in[1]);
assign nonzero=|in[7:0];
endmodule

module ls132r_first_one_4_2(in,out,nonzero);
input  [3:0] in;
output [1:0] out;
output nonzero;
 
assign out[1] = (~in[0])&(~in[1]) & (in[2] | in[3]);
assign out[0] = ((~in[0])&( in[1])) | ((~in[0])&(~in[2])&( in[3]));
assign nonzero   = |in[3:0];
endmodule

module ls132r_ejtag_rstgen(tck, trst_in, trst_out, testmode);
input       tck; 
input       trst_in;
input       testmode;
output      trst_out;

reg         rff, rff1; 

always @ (posedge tck or negedge trst_in)
  begin
    if(!trst_in) {rff1, rff} <= 2'b0;
    else         {rff1, rff} <= {rff, 1'b1}; 
  end

assign trst_out = testmode ? trst_in : rff1; 

endmodule
