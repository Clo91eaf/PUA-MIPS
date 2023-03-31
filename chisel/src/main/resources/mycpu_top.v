module mycpu_top (
  input         clk              ,
  input         resetn           ,
  input  [5 :0] ext_int          ,
  
  output        inst_sram_en     ,
  output [3 :0] inst_sram_wen    ,
  output [31:0] inst_sram_addr   ,
  output [31:0] inst_sram_wdata  ,
  input  [31:0] inst_sram_rdata  ,

  output        data_sram_en     ,
  output [3 :0] data_sram_wen    ,
  output [31:0] data_sram_addr   ,
  output [31:0] data_sram_wdata  ,
  input  [31:0] data_sram_rdata  ,
  
  output [31:0] debug_wb_pc      ,
  output [3 :0] debug_wb_rf_wen  ,
  output [4 :0] debug_wb_rf_wnum ,
  output [31:0] debug_wb_rf_wdata
);

PuaMips puamips(
  .clock              (clk),
  .reset              (~resetn),
  .io_ext_int         (ext_int),

  .io_inst_sram_en    (inst_sram_en),
  .io_inst_sram_wen   (inst_sram_wen),
  .io_inst_sram_addr  (inst_sram_addr),
  .io_inst_sram_wdata (inst_sram_wdata),
  .io_inst_sram_rdata (inst_sram_rdata),

  .io_data_sram_en    (data_sram_en),
  .io_data_sram_wen   (data_sram_wen),
  .io_data_sram_addr  (data_sram_addr),
  .io_data_sram_wdata (data_sram_wdata),
  .io_data_sram_rdata (data_sram_rdata),

  .io_debug_pc        (debug_wb_pc),
  .io_debug_wen       (debug_wb_rf_wen),
  .io_debug_waddr     (debug_wb_rf_wnum),
  .io_debug_wdata     (debug_wb_rf_wdata)
); 
endmodule