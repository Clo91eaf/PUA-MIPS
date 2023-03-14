module Ex(
  input         clock,
  input         reset,
  input  [7:0]  io_aluop_i,
  input  [2:0]  io_alusel_i,
  input  [31:0] io_reg1_i,
  input  [31:0] io_reg2_i,
  input  [4:0]  io_wd_i,
  input         io_wreg_i,
  output [4:0]  io_wd_o,
  output        io_wreg_o,
  output [31:0] io_wdata_o
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_REG_INIT
  reg [4:0] wd_or; // @[Ex.scala 23:18]
  reg  wreg_or; // @[Ex.scala 24:20]
  assign io_wd_o = wd_or; // @[Ex.scala 27:11]
  assign io_wreg_o = wreg_or; // @[Ex.scala 28:13]
  assign io_wdata_o = 32'h0; // @[Ex.scala 29:14]
  always @(posedge clock) begin
    wd_or <= io_wd_i; // @[Ex.scala 80:9]
    wreg_or <= io_wreg_i; // @[Ex.scala 81:11]
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
  wd_or = _RAND_0[4:0];
  _RAND_1 = {1{`RANDOM}};
  wreg_or = _RAND_1[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
