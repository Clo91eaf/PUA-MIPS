module mycpu_top (
    input  [ 5:0]      ext_int,
    input              aclk,
    input              aresetn,
    //axi interface

    //read request
    output [ 3:0]      arid,
    output [31:0]      araddr,
    output [ 7:0]      arlen,
    output [ 2:0]      arsize,
    output [ 1:0]      arburst,
    output [ 1:0]      arlock,
    output [ 3:0]      arcache,
    output [ 2:0]      arprot,
    output             arvalid,
    input              arready,

    //read response
    input  [ 3:0]      rid,
    input  [31:0]      rdata,
    input  [ 1:0]      rresp,
    input              rlast,
    input              rvalid,
    output             rready,

    //write request
    output [ 3:0]      awid,
    output [31:0]      awaddr,
    output [ 7:0]      awlen,
    output [ 2:0]      awsize,
    output [ 1:0]      awburst,
    output [ 1:0]      awlock,
    output [ 3:0]      awcache,
    output [ 2:0]      awprot,
    output             awvalid,
    input              awready,

    //write data
    output [ 3:0]      wid,
    output [31:0]      wdata,
    output [ 3:0]      wstrb,
    output             wlast,
    output             wvalid,
    input              wready,

    //write response
    input  [ 3:0]      bid,
    input  [ 1:0]      bresp,
    input              bvalid,
    output             bready,
    
    // trace debug interface
    output [31:0]      debug_wb_pc,
    output [ 3:0]      debug_wb_rf_wen,
    output [ 4:0]      debug_wb_rf_wnum,
    output [31:0]      debug_wb_rf_wdata
);

PuaMips puamips(
  .clock                (aclk             ),
  .reset                (~aresetn         ),
  .io_ext_int           (ext_int          ),
  .io_axi_arid          (arid             ),
  .io_axi_araddr        (araddr           ),
  .io_axi_arlen         (arlen            ),
  .io_axi_arsize        (arsize           ),
  .io_axi_arburst       (arburst          ),
  .io_axi_arlock        (arlock           ),
  .io_axi_arcache       (arcache          ),
  .io_axi_arprot        (arprot           ),
  .io_axi_arvalid       (arvalid          ),
  .io_axi_arready       (arready          ),
  .io_axi_rid           (rid              ),
  .io_axi_rdata         (rdata            ),
  .io_axi_rresp         (rresp            ),
  .io_axi_rlast         (rlast            ),
  .io_axi_rvalid        (rvalid           ),
  .io_axi_rready        (rready           ),
  .io_axi_awid          (awid             ),
  .io_axi_awaddr        (awaddr           ),
  .io_axi_awlen         (awlen            ),
  .io_axi_awsize        (awsize           ),
  .io_axi_awburst       (awburst          ),
  .io_axi_awlock        (awlock           ),
  .io_axi_awcache       (awcache          ),
  .io_axi_awprot        (awprot           ),
  .io_axi_awvalid       (awvalid          ),
  .io_axi_awready       (awready          ),
  .io_axi_wid           (wid              ),
  .io_axi_wdata         (wdata            ),
  .io_axi_wstrb         (wstrb            ),
  .io_axi_wlast         (wlast            ),
  .io_axi_wvalid        (wvalid           ),
  .io_axi_wready        (wready           ),
  .io_axi_bid           (bid              ),
  .io_axi_bresp         (bresp            ),
  .io_axi_bvalid        (bvalid           ),
  .io_axi_bready        (bready           ),
  .io_debug_pc          (debug_wb_pc      ),
  .io_debug_wen         (debug_wb_rf_wen  ),
  .io_debug_waddr       (debug_wb_rf_wnum ),
  .io_debug_wdata       (debug_wb_rf_wdata)
); 

endmodule