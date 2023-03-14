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
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
`endif // RANDOMIZE_REG_INIT
  reg [4:0] wd_or; // @[Ex.scala 24:18]
  reg  wreg_or; // @[Ex.scala 25:20]
  reg [31:0] wdata_or; // @[Ex.scala 26:21]
  reg [31:0] logicout; // @[Ex.scala 33:21]
  reg [31:0] shiftres; // @[Ex.scala 34:21]
  wire [31:0] _logicout_T = io_reg1_i | io_reg2_i; // @[Ex.scala 45:31]
  wire [31:0] _logicout_T_1 = io_reg1_i & io_reg2_i; // @[Ex.scala 48:31]
  wire [31:0] _logicout_T_3 = ~_logicout_T; // @[Ex.scala 51:21]
  wire [31:0] _logicout_T_4 = io_reg1_i ^ io_reg2_i; // @[Ex.scala 54:31]
  wire [31:0] _GEN_0 = 8'h26 == io_aluop_i ? _logicout_T_4 : 32'h0; // @[Ex.scala 42:14 43:24 54:18]
  wire [62:0] _GEN_4 = {{31'd0}, io_reg2_i}; // @[Ex.scala 66:31]
  wire [62:0] _shiftres_T_1 = _GEN_4 << io_reg1_i[4:0]; // @[Ex.scala 66:31]
  wire [31:0] _shiftres_T_3 = io_reg2_i >> io_reg1_i[4:0]; // @[Ex.scala 69:31]
  wire [31:0] _shiftres_T_7 = $signed(io_reg2_i) >>> io_reg1_i[4:0]; // @[Ex.scala 72:59]
  wire [31:0] _GEN_5 = 8'h3 == io_aluop_i ? _shiftres_T_7 : 32'h0; // @[Ex.scala 63:14 64:24 72:18]
  wire [31:0] _GEN_6 = 8'h2 == io_aluop_i ? _shiftres_T_3 : _GEN_5; // @[Ex.scala 64:24 69:18]
  wire [62:0] _GEN_7 = 8'h7c == io_aluop_i ? _shiftres_T_1 : {{31'd0}, _GEN_6}; // @[Ex.scala 64:24 66:18]
  wire [62:0] _GEN_8 = reset ? 63'h0 : _GEN_7; // @[Ex.scala 60:36 61:14]
  assign io_wd_o = wd_or; // @[Ex.scala 28:11]
  assign io_wreg_o = wreg_or; // @[Ex.scala 29:13]
  assign io_wdata_o = wdata_or; // @[Ex.scala 30:14]
  always @(posedge clock) begin
    wd_or <= io_wd_i; // @[Ex.scala 77:9]
    wreg_or <= io_wreg_i; // @[Ex.scala 78:11]
    if (3'h1 == io_alusel_i) begin // @[Ex.scala 81:23]
      wdata_or <= logicout; // @[Ex.scala 82:34]
    end else if (3'h2 == io_alusel_i) begin // @[Ex.scala 81:23]
      wdata_or <= shiftres; // @[Ex.scala 83:34]
    end else begin
      wdata_or <= 32'h0; // @[Ex.scala 80:12]
    end
    if (reset) begin // @[Ex.scala 39:36]
      logicout <= 32'h0; // @[Ex.scala 40:14]
    end else if (8'h25 == io_aluop_i) begin // @[Ex.scala 43:24]
      logicout <= _logicout_T; // @[Ex.scala 45:18]
    end else if (8'h24 == io_aluop_i) begin // @[Ex.scala 43:24]
      logicout <= _logicout_T_1; // @[Ex.scala 48:18]
    end else if (8'h27 == io_aluop_i) begin // @[Ex.scala 43:24]
      logicout <= _logicout_T_3; // @[Ex.scala 51:18]
    end else begin
      logicout <= _GEN_0;
    end
    shiftres <= _GEN_8[31:0];
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
  _RAND_2 = {1{`RANDOM}};
  wdata_or = _RAND_2[31:0];
  _RAND_3 = {1{`RANDOM}};
  logicout = _RAND_3[31:0];
  _RAND_4 = {1{`RANDOM}};
  shiftres = _RAND_4[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
