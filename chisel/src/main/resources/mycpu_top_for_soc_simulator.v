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
    output [31:0]      debug_wb_rf_wdata,
    // for soc-simulator
    output [31:0]      statistic_cpu_soc_cp0_count,
    output [31:0]      statistic_cpu_soc_cp0_random,
    output [31:0]      statistic_cpu_soc_cp0_cause,
    output             statistic_cpu_soc_int,
    output             statistic_cpu_soc_commit,

    // bpu statistic 
    output [31:0]      statistic_cpu_bpu_branch,
    output [31:0]      statistic_cpu_bpu_success,

    // cache statistic
    output [31:0]      statistic_cache_icache_request,
    output [31:0]      statistic_cache_icache_hit,
    output [31:0]      statistic_cache_dcache_request,
    output [31:0]      statistic_cache_dcache_hit
);

PuaMips puamips(
  .clock                                (aclk                           ),
  .reset                                (~aresetn                       ),
  .io_ext_int                           (ext_int                        ),
  .io_axi_ar_bits_id                    (arid                           ),
  .io_axi_ar_bits_addr                  (araddr                         ),
  .io_axi_ar_bits_len                   (arlen                          ),
  .io_axi_ar_bits_size                  (arsize                         ),
  .io_axi_ar_bits_burst                 (arburst                        ),
  .io_axi_ar_bits_lock                  (arlock                         ),
  .io_axi_ar_bits_cache                 (arcache                        ),
  .io_axi_ar_bits_prot                  (arprot                         ),
  .io_axi_ar_valid                      (arvalid                        ),
  .io_axi_ar_ready                      (arready                        ),
  .io_axi_r_bits_id                     (rid                            ),
  .io_axi_r_bits_data                   (rdata                          ),
  .io_axi_r_bits_resp                   (rresp                          ),
  .io_axi_r_bits_last                   (rlast                          ),
  .io_axi_r_valid                       (rvalid                         ),
  .io_axi_r_ready                       (rready                         ),
  .io_axi_aw_bits_id                    (awid                           ),
  .io_axi_aw_bits_addr                  (awaddr                         ),
  .io_axi_aw_bits_len                   (awlen                          ),
  .io_axi_aw_bits_size                  (awsize                         ),
  .io_axi_aw_bits_burst                 (awburst                        ),
  .io_axi_aw_bits_lock                  (awlock                         ),
  .io_axi_aw_bits_cache                 (awcache                        ),
  .io_axi_aw_bits_prot                  (awprot                         ),
  .io_axi_aw_valid                      (awvalid                        ),
  .io_axi_aw_ready                      (awready                        ),
  .io_axi_w_bits_id                     (wid                            ),
  .io_axi_w_bits_data                   (wdata                          ),
  .io_axi_w_bits_strb                   (wstrb                          ),
  .io_axi_w_bits_last                   (wlast                          ),
  .io_axi_w_valid                       (wvalid                         ),
  .io_axi_w_ready                       (wready                         ),
  .io_axi_b_bits_id                     (bid                            ),
  .io_axi_b_bits_resp                   (bresp                          ),
  .io_axi_b_valid                       (bvalid                         ),
  .io_axi_b_ready                       (bready                         ),
  .io_debug_wb_pc                       (debug_wb_pc                    ),
  .io_debug_wb_rf_wen                   (debug_wb_rf_wen                ),
  .io_debug_wb_rf_wnum                  (debug_wb_rf_wnum               ),
  .io_debug_wb_rf_wdata                 (debug_wb_rf_wdata              ),
  .io_statistic_cpu_soc_cp0_count       (statistic_cpu_soc_cp0_count    ),
  .io_statistic_cpu_soc_cp0_random      (statistic_cpu_soc_cp0_random   ),
  .io_statistic_cpu_soc_cp0_cause       (statistic_cpu_soc_cp0_cause    ),
  .io_statistic_cpu_soc_int             (statistic_cpu_soc_int          ),
  .io_statistic_cpu_soc_commit          (statistic_cpu_soc_commit       ),
  .io_statistic_cpu_bpu_success         (statistic_cpu_bpu_success      ),
  .io_statistic_cpu_bpu_branch          (statistic_cpu_bpu_branch       ),
  .io_statistic_cache_icache_request    (statistic_cache_icache_request ),
  .io_statistic_cache_icache_hit        (statistic_cache_icache_hit     ),
  .io_statistic_cache_dcache_request    (statistic_cache_dcache_request ),
  .io_statistic_cache_dcache_hit        (statistic_cache_dcache_hit     )
);
endmodule
