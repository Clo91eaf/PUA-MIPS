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

module ls132r_interface(
  int_n_i,
  nmi_n_i,

  aclk,
  areset_n,

  arid,
  araddr,
  arlen,
  arsize,
  arburst,
  arlock,
  arcache,
  arprot,
  arvalid,
  arready,

  rid,
  rdata,
  rresp,
  rlast,
  rvalid,
  rready,

  awid,
  awaddr,
  awlen,
  awsize,
  awburst,
  awlock,
  awcache,
  awprot,
  awvalid,
  awready,

  wid,
  wdata,
  wstrb,
  wlast,
  wvalid,
  wready,

  bid,
  bresp,
  bvalid,
  bready,

  inst_sram_cen,
  inst_sram_wr,
  inst_sram_addr,
  inst_sram_wdata,
  inst_sram_ack,
  inst_sram_rrdy,
  inst_sram_rdata,

  data_sram_cen,
  data_sram_wr,
  data_sram_addr,
  data_sram_wdata,
  data_sram_ack,
  data_sram_rrdy,
  data_sram_rdata,

  prrst_i,
  clock_o,
  reset_o,
  hw_int_o,
  nmi_internal_o,

  idle_in_ifc_o,

  inst_ifc_i,
  ifc_inst_o,

  data_ifc_i,
  ifc_data_o 

);

input   [ 5:0]  int_n_i;
input           nmi_n_i;

input           aclk;
input           areset_n;

//read address channel
input           arready;
output  [3:0]   arid;
output  [31:0]  araddr;
output  [3:0]   arlen;
output  [2:0]   arsize;
output  [1:0]   arburst;
output  [1:0]   arlock;
output  [3:0]   arcache;
output  [2:0]   arprot;
output          arvalid;

//read data channel
input   [3:0]   rid;
input   [31:0]  rdata;
input   [1:0]   rresp;
input           rlast;
input           rvalid;
output          rready;

//write address channel
input           awready;
output  [3:0]   awid;
output  [31:0]  awaddr;
output  [3:0]   awlen;
output  [2:0]   awsize;
output  [1:0]   awburst;
output  [1:0]   awlock;
output  [3:0]   awcache;
output  [2:0]   awprot;
output          awvalid;

//write data channel
input           wready;
output  [3:0]   wid;
output  [31:0]  wdata;
output  [3:0]   wstrb;
output          wlast;
output          wvalid;

//write response channel
input   [3:0]   bid;
input   [1:0]   bresp;
input           bvalid;
output          bready;

output  [ 3:0]  inst_sram_cen;
output  [31:0]  inst_sram_wdata;
input   [31:0]  inst_sram_rdata;
output          inst_sram_wr;
output  [31:0]  inst_sram_addr;
input           inst_sram_ack;
input           inst_sram_rrdy;

output  [ 3:0]  data_sram_cen;
output  [31:0]  data_sram_wdata;
input   [31:0]  data_sram_rdata;
output          data_sram_wr;
output  [31:0]  data_sram_addr;
input           data_sram_ack;
input           data_sram_rrdy;

input           prrst_i;
output          clock_o;
output          reset_o;
output  [ 5:0]  hw_int_o;
output          nmi_internal_o;

output          idle_in_ifc_o;

input  [`Ltoifcbus-1:0]     inst_ifc_i;
output [`Lfromifcbus-1:0]   ifc_inst_o;

input  [`Ltoifcbus-1:0]     data_ifc_i;
output [`Lfromifcbus-1:0]   ifc_data_o;

wire        inst_ifc_valid = inst_ifc_i[ 0: 0];
wire [ 3:0] inst_ifc_width = inst_ifc_i[ 4: 1];
wire [ 3:0] inst_ifc_ben   = inst_ifc_i[ 8: 5];
wire        inst_ifc_wr    = inst_ifc_i[ 9: 9];
wire [31:0] inst_ifc_addr  = inst_ifc_i[41:10];
wire [31:0] inst_ifc_wdata = inst_ifc_i[73:42];

wire        inst_ifc_ack  ;
wire        inst_ifc_rrdy ;
wire [31:0] inst_ifc_rdata;

assign ifc_inst_o[ 0: 0] = inst_ifc_ack  ;
assign ifc_inst_o[ 1: 1] = inst_ifc_rrdy ;
assign ifc_inst_o[33: 2] = inst_ifc_rdata;

wire        data_ifc_valid = data_ifc_i[ 0: 0];
wire [ 3:0] data_ifc_width = data_ifc_i[ 4: 1];
wire [ 3:0] data_ifc_ben   = data_ifc_i[ 8: 5];
wire        data_ifc_wr    = data_ifc_i[ 9: 9];
wire [31:0] data_ifc_addr  = data_ifc_i[41:10];
wire [31:0] data_ifc_wdata = data_ifc_i[73:42];

wire        data_ifc_rack ;
wire        data_ifc_rrdy ;
wire [31:0] data_ifc_rdata;
wire        data_ifc_wack ;

assign ifc_data_o[ 0: 0] = data_ifc_rack ;
assign ifc_data_o[ 1: 1] = data_ifc_rrdy ;
assign ifc_data_o[33: 2] = data_ifc_rdata;
assign ifc_data_o[34:34] = data_ifc_wack ;


/*
 *  Internal Logic
 */
wire        core_reset;

wire        inst_sel_isram;
wire        inst_sel_bus;
wire        data_sel_isram;
wire        data_sel_dsram;
wire        data_sel_bus;
wire        inst_isram_valid;
wire        data_isram_valid;
wire        data_dsram_valid;
wire        inst_bus_rvalid;
wire        data_bus_valid;
wire        data_bus_rvalid;
wire        data_bus_wvalid;
wire [31:0] isram_rdata;
wire [31:0] dsram_rdata;
reg         inst_sel_isram_r;
wire        data_bus_RAW_ok;

wire        bus_rack;
wire        bus_rrdy;
wire        bus_wack;
wire [ 3:0] bus_rd_width;
wire [ 3:0] bus_wr_width;
wire [31:0] bus_rdata;

reg  [ 1:0] ifc_rd_flag_r;
wire        ifc_rd_idle;
wire        ifc_wr_idle;

wire        cpu_arvalid;
wire [ 3:0] cpu_arid;
wire [31:0] cpu_araddr;
wire [ 3:0] cpu_arlen;
wire [ 2:0] cpu_arsize;
wire [ 1:0] cpu_arburst;
wire [ 1:0] cpu_arlock;
wire [ 3:0] cpu_arcache;
wire [ 2:0] cpu_arprot;

reg         arvalid_reg;
reg         pesudo_axi_raddr_r;
reg         pesudo_axi_rdata_r;


assign inst_sel_isram   = 
                (inst_ifc_addr&`LS132R_INST_SRAM_MASK)==`LS132R_INST_SRAM_WINDOW;

assign inst_isram_valid = inst_ifc_valid && inst_sel_isram;

assign inst_sel_bus     = !inst_sel_isram;

assign inst_bus_rvalid  = inst_ifc_valid && inst_sel_bus;

assign data_sel_dsram   = 
                (data_ifc_addr&`LS132R_DATA_SRAM_MASK)==`LS132R_DATA_SRAM_WINDOW;

assign data_dsram_valid = data_ifc_valid && data_sel_dsram;

assign data_sel_isram   = 1'b0;
//                (data_ifc_addr&`LS132R_INST_SRAM_MASK)==`LS132R_INST_SRAM_WINDOW;

assign data_isram_valid = data_ifc_valid && data_sel_isram;

assign data_sel_bus     = !data_sel_dsram && !data_sel_isram;

assign data_bus_valid   = data_ifc_valid && data_sel_bus;
assign data_bus_rvalid  = data_bus_valid && !data_ifc_wr & data_bus_RAW_ok;
assign data_bus_wvalid  = data_bus_valid &&  data_ifc_wr;


// inst sram interface

assign inst_sram_cen    = data_isram_valid ? data_ifc_ben :
                          inst_isram_valid ? inst_ifc_ben : 4'h0;

assign inst_sram_wdata  = data_ifc_wdata;

assign isram_rdata      = inst_sram_rdata[31:0];

assign inst_sram_wr     = data_isram_valid && data_ifc_wr;

assign inst_sram_addr   = data_isram_valid ? data_ifc_addr : 
                                             inst_ifc_addr;

// data sram interface
assign data_sram_cen    = data_dsram_valid ? data_ifc_ben : 4'h0;

assign data_sram_wdata  = data_ifc_wdata;

assign dsram_rdata      = data_sram_rdata[31:0];

assign data_sram_wr     = data_dsram_valid && data_ifc_wr;

assign data_sram_addr   = data_ifc_addr;


// bus interface

// axi bus interface
reg  [ 2:0] axi_wtrans_r;  
wire        axi_wr_idle           ;
wire        axi_wr_addr_ok_data_no;
wire        axi_wr_addr_no_data_ok;
wire        axi_wr_addr_ok_data_ok;
wire        axi_wr_wait_resp      ;

parameter WR_IDLE             = 3'b000;
parameter WR_ADDR_OK_DATA_NO  = 3'b001;
parameter WR_ADDR_NO_DATA_OK  = 3'b010;
parameter WR_ADDR_OK_DATA_OK  = 3'b011;
parameter WR_WAIT_RESP        = 3'b100;

assign axi_wr_idle            = axi_wtrans_r==WR_IDLE           ;
assign axi_wr_addr_ok_data_no = axi_wtrans_r==WR_ADDR_OK_DATA_NO;
assign axi_wr_addr_no_data_ok = axi_wtrans_r==WR_ADDR_NO_DATA_OK;
assign axi_wr_addr_ok_data_ok = axi_wtrans_r==WR_ADDR_OK_DATA_OK;
assign axi_wr_wait_resp       = axi_wtrans_r==WR_WAIT_RESP      ;

assign bus_rack = arready && ~pesudo_axi_raddr_r;

assign bus_wack = axi_wr_addr_ok_data_ok;

assign bus_rrdy = rvalid && ~pesudo_axi_rdata_r;

assign data_bus_RAW_ok = axi_wr_idle;
       
//read address channel
assign cpu_arvalid  = (inst_bus_rvalid || data_bus_rvalid) && ~pesudo_axi_raddr_r;
assign cpu_arid     = (data_bus_rvalid & ~pesudo_axi_raddr_r) ? 4'h1 : 4'h0;
assign cpu_araddr   = data_bus_rvalid  ? data_ifc_addr : 
                      (arvalid_reg && !cpu_arvalid) ? 32'h1fc00000 : inst_ifc_addr; 

// Now we just consider width <= 32bit
assign cpu_arlen    = `AXI_LEN_1;

assign bus_rd_width = data_bus_rvalid ? data_ifc_width : inst_ifc_width; 

assign cpu_arsize   = bus_rd_width==`LS132R_WIDTH_8BIT  ? `AXI_SIZE_1B :
                      bus_rd_width==`LS132R_WIDTH_16BIT ? `AXI_SIZE_2B :
                                                          `AXI_SIZE_4B ;

assign cpu_arburst  = `AXI_BURST_INCR;

assign cpu_arlock   = `AXI_LOCK_NORMAL;

assign cpu_arcache  = `AXI_NO_CACHE_NO_BUF;

assign cpu_arprot[0]= `AXI_PROT0_NORMAL;
assign cpu_arprot[1]= `AXI_PROT1_NONSECURE;
assign cpu_arprot[2]= (data_bus_rvalid & ~pesudo_axi_raddr_r) ? `AXI_PROT2_DATA : `AXI_PROT2_INST;

always @(posedge aclk)
begin
  if (~areset_n)
    arvalid_reg <= 1'b0;
  else if (~arvalid_reg & cpu_arvalid & ~arready)
    arvalid_reg <= 1'b1;
  else if (arvalid_reg & arready)
    arvalid_reg <= 1'b0;

  if (~areset_n)
    pesudo_axi_raddr_r <= 1'b0;
  else if (~pesudo_axi_rdata_r & ~cpu_arvalid & arvalid_reg & ~arready)
    pesudo_axi_raddr_r <= 1'b1;
  else if (arvalid_reg & arready & pesudo_axi_raddr_r) 
    pesudo_axi_raddr_r <= 1'b0;

  if (~areset_n)
    pesudo_axi_rdata_r <= 1'b0;
  else if (~pesudo_axi_rdata_r & ~cpu_arvalid & arvalid_reg)
    pesudo_axi_rdata_r <= 1'b1;
  else if (pesudo_axi_rdata_r & rvalid & rready & rlast & (rid==4'h0))
    pesudo_axi_rdata_r <= 1'b0;

end

assign arvalid  = cpu_arvalid | arvalid_reg;
assign arid     = cpu_arid;
assign araddr   = cpu_araddr;
assign arsize   = cpu_arsize;
assign arlen    = cpu_arlen;
assign arburst  = cpu_arburst;
assign arlock   = cpu_arlock;
assign arcache  = cpu_arcache;
assign arprot   = cpu_arprot;

//read data channel
assign rready   = 1'b1;

assign bus_rdata= rdata[31:0];

//write address channel
assign awid     = 4'h1;

assign awaddr   = data_ifc_addr;

assign awlen    = `AXI_LEN_1;

assign bus_wr_width = data_ifc_width; 

assign awsize   = bus_wr_width==`LS132R_WIDTH_8BIT  ? `AXI_SIZE_1B :
                  bus_wr_width==`LS132R_WIDTH_16BIT ? `AXI_SIZE_2B :
                                                      `AXI_SIZE_4B ;

assign awburst  = `AXI_BURST_INCR;

assign awlock   = `AXI_LOCK_NORMAL;

assign awcache  = `AXI_NO_CACHE_NO_BUF;

assign awprot[0]= `AXI_PROT0_NORMAL;
assign awprot[1]= `AXI_PROT1_NONSECURE;
assign awprot[2]= `AXI_PROT2_DATA;

assign awvalid  = data_bus_wvalid && (axi_wr_idle || axi_wr_addr_no_data_ok); 

//write data channel
assign wid      = 4'h1;
assign wdata    = data_ifc_wdata[31:0];
assign wstrb    = data_ifc_ben[3:0];

assign wvalid   = data_bus_wvalid && (axi_wr_idle || axi_wr_addr_ok_data_no);
assign wlast    = 1'b1;

//write response channel
assign bready   = 1'b1;

always @(posedge aclk)
begin
  if (reset_o)
    axi_wtrans_r <= WR_IDLE;
  else if (axi_wr_idle)
  begin
    if (data_bus_wvalid)
    begin
      if      ( awready &&  wready)
        axi_wtrans_r <= WR_ADDR_OK_DATA_OK;
      else if ( awready && !wready)
        axi_wtrans_r <= WR_ADDR_OK_DATA_NO;
      else if (!awready &&  wready)
        axi_wtrans_r <= WR_ADDR_NO_DATA_OK;
    end
  end
  else if (axi_wr_addr_ok_data_no)
  begin
    if (data_bus_wvalid && wready)
      axi_wtrans_r <= WR_ADDR_OK_DATA_OK;
  end
  else if (axi_wr_addr_no_data_ok)
  begin
    if (data_bus_wvalid && awready)
      axi_wtrans_r <= WR_ADDR_OK_DATA_OK;
  end
  else if (axi_wr_addr_ok_data_ok)
  begin
    if (bvalid && bid==4'h1)
      axi_wtrans_r <= WR_IDLE;
    else 
      axi_wtrans_r <= WR_WAIT_RESP;
  end
  else if (axi_wr_wait_resp)
  begin
    if (bvalid && bid==4'h1)
      axi_wtrans_r <= WR_IDLE;
  end
end

assign ifc_wr_idle = axi_wr_idle;

// ifc_inst_o

always @(posedge clock_o)
begin
  if (reset_o)
    inst_sel_isram_r <= 1'b0;
  else if (inst_isram_valid && !data_isram_valid && inst_ifc_ack)
    inst_sel_isram_r <= 1'b1;
  else if (inst_sel_isram_r && inst_sram_rrdy)
    inst_sel_isram_r <= 1'b0;
end

assign inst_ifc_ack     = inst_sel_isram ? inst_sram_ack && !data_isram_valid 
                                         : bus_rack && !data_bus_rvalid;

assign inst_ifc_rrdy    = inst_sel_isram_r ? inst_sram_rrdy  
                                           : bus_rrdy && rid[0]==1'b0;

assign inst_ifc_rdata   = inst_sel_isram_r ? isram_rdata : bus_rdata;
                          

// ifc_data_o
assign data_ifc_rack    = data_sel_bus   ? bus_rack & data_bus_RAW_ok :
                          data_sel_isram ? inst_sram_ack :
                                           data_sram_ack; 

assign data_ifc_wack    = data_sel_bus   ? bus_wack      :
                          data_sel_isram ? inst_sram_ack :
                                           data_sram_ack; 

assign data_ifc_rrdy    = data_sel_bus   ? bus_rrdy && rid[0]==1'b1 :
                          data_sel_isram ? inst_sram_rrdy : 
                                           data_sram_rrdy;
                                           

assign data_ifc_rdata   = data_sel_bus   ? bus_rdata   :
                          data_sel_isram ? isram_rdata : 
                                           dsram_rdata;
// idle_in_ifc_o
wire axi_rd_req = arvalid && arready;
wire axi_rd_ret = rvalid && rlast && rready && (rid[3:1]==3'b000); 
reg  axi_rd_flag;
always @(posedge clock_o)
begin
  if (reset_o) begin
    axi_rd_flag   <= 1'b0;
  end
  else begin
    axi_rd_flag   <= axi_rd_flag   + axi_rd_req   + (~axi_rd_ret  ) + 1'b1;
  end
end


wire isram_rd_req = inst_sram_cen && !inst_sram_wr && inst_sram_ack;
wire isram_rd_ret = inst_sram_rrdy;
reg  isram_rd_flag;
always @(posedge clock_o)
begin
  if (reset_o) begin
    isram_rd_flag <= 1'b0;
  end
  else begin
    isram_rd_flag <= isram_rd_flag + isram_rd_req + (~isram_rd_ret) + 1'b1;
  end
end

wire dsram_rd_req = data_sram_cen && !data_sram_wr && data_sram_ack;
wire dsram_rd_ret = data_sram_rrdy;
reg  dsram_rd_flag;
always @(posedge clock_o)
begin
  if (reset_o) begin
    dsram_rd_flag <= 1'b0;
  end
  else begin
    dsram_rd_flag <= dsram_rd_flag + dsram_rd_req + (~dsram_rd_ret) + 1'b1;
  end
end

assign ifc_rd_idle      = !axi_rd_flag && !isram_rd_flag && !dsram_rd_flag;
assign idle_in_ifc_o    = ifc_rd_idle && ifc_wr_idle;


// interface to core

assign clock_o  = aclk;

assign core_reset = ~areset_n;

assign hw_int_o = ~int_n_i;
assign nmi_internal_o = ~nmi_n_i;

assign reset_o  = core_reset | prrst_i; 


endmodule //ls132r_interface


