module mycpu_top (
  input         clk              ,
  input         resetn           ,
  input  [5 :0] ext_int          ,
  
  // inst sram interface
  output        inst_sram_req    ,    
  output        inst_sram_wr     ,     
  output [ 1:0] inst_sram_size   ,   
  output [ 3:0] inst_sram_wstrb  ,  
  output [31:0] inst_sram_addr   ,
  output [31:0] inst_sram_wdata  ,
  input         inst_sram_addr_ok,
  input  [31:0] inst_sram_rdata  ,
  input         inst_sram_data_ok,

  output        data_sram_req    ,    
  output        data_sram_wr     ,     
  output [ 1:0] data_sram_size   ,   
  output [ 3:0] data_sram_wstrb  ,  
  output [31:0] data_sram_addr   ,
  output [31:0] data_sram_wdata  ,
  input         data_sram_addr_ok,
  input  [31:0] data_sram_rdata  ,
  input         data_sram_data_ok,
  
  output [31:0] debug_wb_pc      ,
  output [3 :0] debug_wb_rf_wen  ,
  output [4 :0] debug_wb_rf_wnum ,
  output [31:0] debug_wb_rf_wdata
);

PuaMips puamips(
  .clock              (clk),
  .reset              (~resetn),
  .io_ext_int         (ext_int),

  .io_inst_sram_req       (inst_sram_req),
  .io_inst_sram_wr        (inst_sram_wr),
  .io_inst_sram_size      (inst_sram_size),
  .io_inst_sram_addr      (inst_sram_addr),
  .io_inst_sram_wstrb     (inst_sram_wstrb),
  .io_inst_sram_wdata     (inst_sram_wdata),
  .io_inst_sram_addr_ok   (inst_sram_addr_ok),
  .io_inst_sram_data_ok   (inst_sram_data_ok),
  .io_inst_sram_rdata     (inst_sram_rdata),

  .io_data_sram_req       (data_sram_req),
  .io_data_sram_wr        (data_sram_wr),
  .io_data_sram_size      (data_sram_size),
  .io_data_sram_addr      (data_sram_addr),
  .io_data_sram_wstrb     (data_sram_wstrb),
  .io_data_sram_wdata     (data_sram_wdata),
  .io_data_sram_addr_ok   (data_sram_addr_ok),
  .io_data_sram_data_ok   (data_sram_data_ok),
  .io_data_sram_rdata     (data_sram_rdata),

  .io_debug_pc        (debug_wb_pc),
  .io_debug_wen       (debug_wb_rf_wen),
  .io_debug_waddr     (debug_wb_rf_wnum),
  .io_debug_wdata     (debug_wb_rf_wdata)
); 
endmodule