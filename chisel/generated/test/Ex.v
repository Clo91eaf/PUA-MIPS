module Ex(
  input         clock,
  input         reset,
  input  [7:0]  io_aluop_i,
  input  [2:0]  io_alusel_i,
  input  [31:0] io_reg1_i,
  input  [31:0] io_reg2_i,
  input  [4:0]  io_wd_i,
  input         io_wreg_i,
  input  [31:0] io_hi_i,
  input  [31:0] io_lo_i,
  output [4:0]  io_wd_o,
  output        io_wreg_o,
  output [31:0] io_wdata_o,
  output [31:0] io_hi_o,
  output [31:0] io_lo_o,
  output        io_whilo_o
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_9;
  reg [31:0] _RAND_10;
`endif // RANDOMIZE_REG_INIT
  reg [4:0] wd_or; // @[Ex.scala 32:18]
  reg  wreg_or; // @[Ex.scala 33:20]
  reg [31:0] wdata_or; // @[Ex.scala 34:21]
  reg [31:0] hi_or; // @[Ex.scala 35:18]
  reg [31:0] lo_or; // @[Ex.scala 36:18]
  reg  whilo_or; // @[Ex.scala 37:21]
  reg [31:0] logicout; // @[Ex.scala 47:21]
  reg [31:0] shiftres; // @[Ex.scala 48:21]
  reg [31:0] moveres; // @[Ex.scala 49:20]
  reg [31:0] HI; // @[Ex.scala 50:15]
  reg [31:0] LO; // @[Ex.scala 51:15]
  wire [31:0] _logicout_T = io_reg1_i | io_reg2_i; // @[Ex.scala 62:31]
  wire [31:0] _logicout_T_1 = io_reg1_i & io_reg2_i; // @[Ex.scala 65:31]
  wire [31:0] _logicout_T_3 = ~_logicout_T; // @[Ex.scala 68:21]
  wire [31:0] _logicout_T_4 = io_reg1_i ^ io_reg2_i; // @[Ex.scala 71:31]
  wire [31:0] _GEN_0 = 8'h26 == io_aluop_i ? _logicout_T_4 : 32'h0; // @[Ex.scala 59:14 60:24 71:18]
  wire [62:0] _GEN_4 = {{31'd0}, io_reg2_i}; // @[Ex.scala 83:31]
  wire [62:0] _shiftres_T_1 = _GEN_4 << io_reg1_i[4:0]; // @[Ex.scala 83:31]
  wire [31:0] _shiftres_T_3 = io_reg2_i >> io_reg1_i[4:0]; // @[Ex.scala 86:31]
  wire [31:0] _shiftres_T_7 = $signed(io_reg2_i) >>> io_reg1_i[4:0]; // @[Ex.scala 89:59]
  wire [31:0] _GEN_5 = 8'h3 == io_aluop_i ? _shiftres_T_7 : 32'h0; // @[Ex.scala 80:14 81:24 89:18]
  wire [31:0] _GEN_6 = 8'h2 == io_aluop_i ? _shiftres_T_3 : _GEN_5; // @[Ex.scala 81:24 86:18]
  wire [62:0] _GEN_7 = 8'h7c == io_aluop_i ? _shiftres_T_1 : {{31'd0}, _GEN_6}; // @[Ex.scala 81:24 83:18]
  wire [62:0] _GEN_8 = reset ? 63'h0 : _GEN_7; // @[Ex.scala 77:36 78:14]
  wire [31:0] _GEN_11 = 8'hb == io_aluop_i ? io_reg1_i : 32'h0; // @[Ex.scala 107:13 108:24 119:17]
  wire  _T_25 = io_aluop_i == 8'h13; // @[Ex.scala 143:25]
  wire  _GEN_22 = io_aluop_i == 8'h11 | _T_25; // @[Ex.scala 139:42 140:14]
  assign io_wd_o = wd_or; // @[Ex.scala 39:11]
  assign io_wreg_o = wreg_or; // @[Ex.scala 40:13]
  assign io_wdata_o = wdata_or; // @[Ex.scala 41:14]
  assign io_hi_o = hi_or; // @[Ex.scala 42:11]
  assign io_lo_o = lo_or; // @[Ex.scala 43:11]
  assign io_whilo_o = whilo_or; // @[Ex.scala 44:14]
  always @(posedge clock) begin
    wd_or <= io_wd_i; // @[Ex.scala 125:9]
    wreg_or <= io_wreg_i; // @[Ex.scala 126:11]
    if (3'h1 == io_alusel_i) begin // @[Ex.scala 128:23]
      wdata_or <= logicout; // @[Ex.scala 129:34]
    end else if (3'h2 == io_alusel_i) begin // @[Ex.scala 128:23]
      wdata_or <= shiftres; // @[Ex.scala 130:34]
    end else if (3'h3 == io_alusel_i) begin // @[Ex.scala 128:23]
      wdata_or <= moveres; // @[Ex.scala 131:33]
    end else begin
      wdata_or <= 32'h0; // @[Ex.scala 127:12]
    end
    if (reset) begin // @[Ex.scala 135:36]
      hi_or <= 32'h0; // @[Ex.scala 137:11]
    end else if (io_aluop_i == 8'h11) begin // @[Ex.scala 139:42]
      hi_or <= io_reg1_i; // @[Ex.scala 141:11]
    end else if (io_aluop_i == 8'h13) begin // @[Ex.scala 143:42]
      hi_or <= HI; // @[Ex.scala 145:11]
    end else begin
      hi_or <= 32'h0; // @[Ex.scala 149:11]
    end
    if (reset) begin // @[Ex.scala 135:36]
      lo_or <= 32'h0; // @[Ex.scala 138:11]
    end else if (io_aluop_i == 8'h11) begin // @[Ex.scala 139:42]
      lo_or <= LO; // @[Ex.scala 142:11]
    end else if (io_aluop_i == 8'h13) begin // @[Ex.scala 143:42]
      lo_or <= io_reg1_i; // @[Ex.scala 146:11]
    end else begin
      lo_or <= 32'h0; // @[Ex.scala 150:11]
    end
    if (reset) begin // @[Ex.scala 135:36]
      whilo_or <= 1'h0; // @[Ex.scala 136:14]
    end else begin
      whilo_or <= _GEN_22;
    end
    if (reset) begin // @[Ex.scala 56:36]
      logicout <= 32'h0; // @[Ex.scala 57:14]
    end else if (8'h25 == io_aluop_i) begin // @[Ex.scala 60:24]
      logicout <= _logicout_T; // @[Ex.scala 62:18]
    end else if (8'h24 == io_aluop_i) begin // @[Ex.scala 60:24]
      logicout <= _logicout_T_1; // @[Ex.scala 65:18]
    end else if (8'h27 == io_aluop_i) begin // @[Ex.scala 60:24]
      logicout <= _logicout_T_3; // @[Ex.scala 68:18]
    end else begin
      logicout <= _GEN_0;
    end
    shiftres <= _GEN_8[31:0];
    if (reset) begin // @[Ex.scala 104:36]
      moveres <= 32'h0; // @[Ex.scala 105:13]
    end else if (8'h10 == io_aluop_i) begin // @[Ex.scala 108:24]
      moveres <= HI; // @[Ex.scala 110:17]
    end else if (8'h12 == io_aluop_i) begin // @[Ex.scala 108:24]
      moveres <= LO; // @[Ex.scala 113:17]
    end else if (8'ha == io_aluop_i) begin // @[Ex.scala 108:24]
      moveres <= io_reg1_i; // @[Ex.scala 116:17]
    end else begin
      moveres <= _GEN_11;
    end
    if (reset) begin // @[Ex.scala 95:36]
      HI <= 32'h0; // @[Ex.scala 96:8]
    end else begin
      HI <= io_hi_i; // @[Ex.scala 99:8]
    end
    if (reset) begin // @[Ex.scala 95:36]
      LO <= 32'h0; // @[Ex.scala 97:8]
    end else begin
      LO <= io_lo_i; // @[Ex.scala 100:8]
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
  wd_or = _RAND_0[4:0];
  _RAND_1 = {1{`RANDOM}};
  wreg_or = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  wdata_or = _RAND_2[31:0];
  _RAND_3 = {1{`RANDOM}};
  hi_or = _RAND_3[31:0];
  _RAND_4 = {1{`RANDOM}};
  lo_or = _RAND_4[31:0];
  _RAND_5 = {1{`RANDOM}};
  whilo_or = _RAND_5[0:0];
  _RAND_6 = {1{`RANDOM}};
  logicout = _RAND_6[31:0];
  _RAND_7 = {1{`RANDOM}};
  shiftres = _RAND_7[31:0];
  _RAND_8 = {1{`RANDOM}};
  moveres = _RAND_8[31:0];
  _RAND_9 = {1{`RANDOM}};
  HI = _RAND_9[31:0];
  _RAND_10 = {1{`RANDOM}};
  LO = _RAND_10[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
