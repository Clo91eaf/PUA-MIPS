module Id(
  input         clock,
  input         reset,
  input  [31:0] io_pc_i,
  input  [31:0] io_inst_i,
  input  [31:0] io_reg1_data_i,
  input  [31:0] io_reg2_data_i,
  output        io_reg1_read_o,
  output        io_reg2_read_o,
  output [4:0]  io_reg1_addr_o,
  output [4:0]  io_reg2_addr_o,
  output [7:0]  io_aluop_o,
  output [2:0]  io_alusel_o,
  output [31:0] io_reg1_o,
  output [31:0] io_reg2_o,
  output [4:0]  io_wd_o,
  output        io_wreg_o
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
`endif // RANDOMIZE_REG_INIT
  reg [4:0] reg1_addr_or; // @[Id.scala 33:25]
  reg [4:0] reg2_addr_or; // @[Id.scala 34:25]
  reg [4:0] wd_or; // @[Id.scala 41:18]
  assign io_reg1_read_o = 1'h0; // @[Id.scala 44:18]
  assign io_reg2_read_o = 1'h0; // @[Id.scala 45:18]
  assign io_reg1_addr_o = reg1_addr_or; // @[Id.scala 46:18]
  assign io_reg2_addr_o = reg2_addr_or; // @[Id.scala 47:18]
  assign io_aluop_o = 8'h0; // @[Id.scala 48:14]
  assign io_alusel_o = 3'h0; // @[Id.scala 49:15]
  assign io_reg1_o = 32'h0; // @[Id.scala 50:13]
  assign io_reg2_o = 32'h0; // @[Id.scala 51:13]
  assign io_wd_o = wd_or; // @[Id.scala 52:11]
  assign io_wreg_o = 1'h0; // @[Id.scala 53:13]
  always @(posedge clock) begin
    if (reset) begin // @[Id.scala 69:36]
      reg1_addr_or <= 5'h0; // @[Id.scala 77:18]
    end else begin
      reg1_addr_or <= io_inst_i[25:21]; // @[Id.scala 88:18]
    end
    if (reset) begin // @[Id.scala 69:36]
      reg2_addr_or <= 5'h0; // @[Id.scala 78:18]
    end else begin
      reg2_addr_or <= io_inst_i[20:16]; // @[Id.scala 89:18]
    end
    if (reset) begin // @[Id.scala 69:36]
      wd_or <= 5'h0; // @[Id.scala 72:11]
    end else begin
      wd_or <= io_inst_i[15:11]; // @[Id.scala 83:11]
    end
  end
// Register and memory initialization
`ifdef RANDOMIZE_GARBAGE_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_INVALID_ASSIGN
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_REG_INIT
`define RANDOMIZE
`endif
`ifdef RANDOMIZE_MEM_INIT
`define RANDOMIZE
`endif
`ifndef RANDOM
`define RANDOM $random
`endif
`ifdef RANDOMIZE_MEM_INIT
  integer initvar;
`endif
`ifndef SYNTHESIS
`ifdef FIRRTL_BEFORE_INITIAL
`FIRRTL_BEFORE_INITIAL
`endif
initial begin
  `ifdef RANDOMIZE
    `ifdef INIT_RANDOM
      `INIT_RANDOM
    `endif
    `ifndef VERILATOR
      `ifdef RANDOMIZE_DELAY
        #`RANDOMIZE_DELAY begin end
      `else
        #0.002 begin end
      `endif
    `endif
`ifdef RANDOMIZE_REG_INIT
  _RAND_0 = {1{`RANDOM}};
  reg1_addr_or = _RAND_0[4:0];
  _RAND_1 = {1{`RANDOM}};
  reg2_addr_or = _RAND_1[4:0];
  _RAND_2 = {1{`RANDOM}};
  wd_or = _RAND_2[4:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
