module Regfile(
  input         clock,
  input         reset,
  input         io_we,
  input  [4:0]  io_waddr,
  input  [31:0] io_wdata,
  input         io_re1,
  input  [4:0]  io_raddr1,
  input         io_re2,
  input  [4:0]  io_raddr2,
  output [31:0] io_rdata1,
  output [31:0] io_rdata2
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
  reg [31:0] _RAND_11;
  reg [31:0] _RAND_12;
  reg [31:0] _RAND_13;
  reg [31:0] _RAND_14;
  reg [31:0] _RAND_15;
  reg [31:0] _RAND_16;
  reg [31:0] _RAND_17;
  reg [31:0] _RAND_18;
  reg [31:0] _RAND_19;
  reg [31:0] _RAND_20;
  reg [31:0] _RAND_21;
  reg [31:0] _RAND_22;
  reg [31:0] _RAND_23;
  reg [31:0] _RAND_24;
  reg [31:0] _RAND_25;
  reg [31:0] _RAND_26;
  reg [31:0] _RAND_27;
  reg [31:0] _RAND_28;
  reg [31:0] _RAND_29;
  reg [31:0] _RAND_30;
  reg [31:0] _RAND_31;
  reg [31:0] _RAND_32;
  reg [31:0] _RAND_33;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] rdata1r; // @[Regfile.scala 22:20]
  reg [31:0] rdata2r; // @[Regfile.scala 23:20]
  reg [31:0] regs_0; // @[Regfile.scala 29:21]
  reg [31:0] regs_1; // @[Regfile.scala 29:21]
  reg [31:0] regs_2; // @[Regfile.scala 29:21]
  reg [31:0] regs_3; // @[Regfile.scala 29:21]
  reg [31:0] regs_4; // @[Regfile.scala 29:21]
  reg [31:0] regs_5; // @[Regfile.scala 29:21]
  reg [31:0] regs_6; // @[Regfile.scala 29:21]
  reg [31:0] regs_7; // @[Regfile.scala 29:21]
  reg [31:0] regs_8; // @[Regfile.scala 29:21]
  reg [31:0] regs_9; // @[Regfile.scala 29:21]
  reg [31:0] regs_10; // @[Regfile.scala 29:21]
  reg [31:0] regs_11; // @[Regfile.scala 29:21]
  reg [31:0] regs_12; // @[Regfile.scala 29:21]
  reg [31:0] regs_13; // @[Regfile.scala 29:21]
  reg [31:0] regs_14; // @[Regfile.scala 29:21]
  reg [31:0] regs_15; // @[Regfile.scala 29:21]
  reg [31:0] regs_16; // @[Regfile.scala 29:21]
  reg [31:0] regs_17; // @[Regfile.scala 29:21]
  reg [31:0] regs_18; // @[Regfile.scala 29:21]
  reg [31:0] regs_19; // @[Regfile.scala 29:21]
  reg [31:0] regs_20; // @[Regfile.scala 29:21]
  reg [31:0] regs_21; // @[Regfile.scala 29:21]
  reg [31:0] regs_22; // @[Regfile.scala 29:21]
  reg [31:0] regs_23; // @[Regfile.scala 29:21]
  reg [31:0] regs_24; // @[Regfile.scala 29:21]
  reg [31:0] regs_25; // @[Regfile.scala 29:21]
  reg [31:0] regs_26; // @[Regfile.scala 29:21]
  reg [31:0] regs_27; // @[Regfile.scala 29:21]
  reg [31:0] regs_28; // @[Regfile.scala 29:21]
  reg [31:0] regs_29; // @[Regfile.scala 29:21]
  reg [31:0] regs_30; // @[Regfile.scala 29:21]
  reg [31:0] regs_31; // @[Regfile.scala 29:21]
  wire  _T_1 = ~reset; // @[Regfile.scala 31:21]
  wire [31:0] _GEN_97 = 5'h1 == io_raddr1 ? regs_1 : regs_0; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_98 = 5'h2 == io_raddr1 ? regs_2 : _GEN_97; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_99 = 5'h3 == io_raddr1 ? regs_3 : _GEN_98; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_100 = 5'h4 == io_raddr1 ? regs_4 : _GEN_99; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_101 = 5'h5 == io_raddr1 ? regs_5 : _GEN_100; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_102 = 5'h6 == io_raddr1 ? regs_6 : _GEN_101; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_103 = 5'h7 == io_raddr1 ? regs_7 : _GEN_102; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_104 = 5'h8 == io_raddr1 ? regs_8 : _GEN_103; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_105 = 5'h9 == io_raddr1 ? regs_9 : _GEN_104; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_106 = 5'ha == io_raddr1 ? regs_10 : _GEN_105; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_107 = 5'hb == io_raddr1 ? regs_11 : _GEN_106; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_108 = 5'hc == io_raddr1 ? regs_12 : _GEN_107; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_109 = 5'hd == io_raddr1 ? regs_13 : _GEN_108; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_110 = 5'he == io_raddr1 ? regs_14 : _GEN_109; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_111 = 5'hf == io_raddr1 ? regs_15 : _GEN_110; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_112 = 5'h10 == io_raddr1 ? regs_16 : _GEN_111; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_113 = 5'h11 == io_raddr1 ? regs_17 : _GEN_112; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_114 = 5'h12 == io_raddr1 ? regs_18 : _GEN_113; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_115 = 5'h13 == io_raddr1 ? regs_19 : _GEN_114; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_116 = 5'h14 == io_raddr1 ? regs_20 : _GEN_115; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_117 = 5'h15 == io_raddr1 ? regs_21 : _GEN_116; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_118 = 5'h16 == io_raddr1 ? regs_22 : _GEN_117; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_119 = 5'h17 == io_raddr1 ? regs_23 : _GEN_118; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_120 = 5'h18 == io_raddr1 ? regs_24 : _GEN_119; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_121 = 5'h19 == io_raddr1 ? regs_25 : _GEN_120; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_122 = 5'h1a == io_raddr1 ? regs_26 : _GEN_121; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_123 = 5'h1b == io_raddr1 ? regs_27 : _GEN_122; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_124 = 5'h1c == io_raddr1 ? regs_28 : _GEN_123; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_125 = 5'h1d == io_raddr1 ? regs_29 : _GEN_124; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_126 = 5'h1e == io_raddr1 ? regs_30 : _GEN_125; // @[Regfile.scala 41:{13,13}]
  wire [31:0] _GEN_132 = 5'h1 == io_raddr2 ? regs_1 : regs_0; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_133 = 5'h2 == io_raddr2 ? regs_2 : _GEN_132; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_134 = 5'h3 == io_raddr2 ? regs_3 : _GEN_133; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_135 = 5'h4 == io_raddr2 ? regs_4 : _GEN_134; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_136 = 5'h5 == io_raddr2 ? regs_5 : _GEN_135; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_137 = 5'h6 == io_raddr2 ? regs_6 : _GEN_136; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_138 = 5'h7 == io_raddr2 ? regs_7 : _GEN_137; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_139 = 5'h8 == io_raddr2 ? regs_8 : _GEN_138; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_140 = 5'h9 == io_raddr2 ? regs_9 : _GEN_139; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_141 = 5'ha == io_raddr2 ? regs_10 : _GEN_140; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_142 = 5'hb == io_raddr2 ? regs_11 : _GEN_141; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_143 = 5'hc == io_raddr2 ? regs_12 : _GEN_142; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_144 = 5'hd == io_raddr2 ? regs_13 : _GEN_143; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_145 = 5'he == io_raddr2 ? regs_14 : _GEN_144; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_146 = 5'hf == io_raddr2 ? regs_15 : _GEN_145; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_147 = 5'h10 == io_raddr2 ? regs_16 : _GEN_146; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_148 = 5'h11 == io_raddr2 ? regs_17 : _GEN_147; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_149 = 5'h12 == io_raddr2 ? regs_18 : _GEN_148; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_150 = 5'h13 == io_raddr2 ? regs_19 : _GEN_149; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_151 = 5'h14 == io_raddr2 ? regs_20 : _GEN_150; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_152 = 5'h15 == io_raddr2 ? regs_21 : _GEN_151; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_153 = 5'h16 == io_raddr2 ? regs_22 : _GEN_152; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_154 = 5'h17 == io_raddr2 ? regs_23 : _GEN_153; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_155 = 5'h18 == io_raddr2 ? regs_24 : _GEN_154; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_156 = 5'h19 == io_raddr2 ? regs_25 : _GEN_155; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_157 = 5'h1a == io_raddr2 ? regs_26 : _GEN_156; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_158 = 5'h1b == io_raddr2 ? regs_27 : _GEN_157; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_159 = 5'h1c == io_raddr2 ? regs_28 : _GEN_158; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_160 = 5'h1d == io_raddr2 ? regs_29 : _GEN_159; // @[Regfile.scala 51:{13,13}]
  wire [31:0] _GEN_161 = 5'h1e == io_raddr2 ? regs_30 : _GEN_160; // @[Regfile.scala 51:{13,13}]
  assign io_rdata1 = rdata1r; // @[Regfile.scala 25:13]
  assign io_rdata2 = rdata2r; // @[Regfile.scala 26:13]
  always @(posedge clock) begin
    if (_T_1) begin // @[Regfile.scala 36:37]
      rdata1r <= 32'h0; // @[Regfile.scala 37:13]
    end else if (io_raddr1 == 5'h0) begin // @[Regfile.scala 38:33]
      rdata1r <= 32'h0; // @[Regfile.scala 39:13]
    end else if (io_re1) begin // @[Regfile.scala 40:37]
      if (5'h1f == io_raddr1) begin // @[Regfile.scala 41:13]
        rdata1r <= regs_31; // @[Regfile.scala 41:13]
      end else begin
        rdata1r <= _GEN_126;
      end
    end else begin
      rdata1r <= 32'h0; // @[Regfile.scala 43:13]
    end
    if (reset) begin // @[Regfile.scala 46:36]
      rdata2r <= 32'h0; // @[Regfile.scala 47:13]
    end else if (io_raddr2 == 5'h0) begin // @[Regfile.scala 48:33]
      rdata2r <= 32'h0; // @[Regfile.scala 49:13]
    end else if (io_re2) begin // @[Regfile.scala 50:37]
      if (5'h1f == io_raddr2) begin // @[Regfile.scala 51:13]
        rdata2r <= regs_31; // @[Regfile.scala 51:13]
      end else begin
        rdata2r <= _GEN_161;
      end
    end else begin
      rdata2r <= 32'h0; // @[Regfile.scala 53:13]
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_0 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h0 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_0 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_1 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_1 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_2 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h2 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_2 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_3 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h3 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_3 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_4 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h4 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_4 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_5 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h5 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_5 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_6 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h6 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_6 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_7 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h7 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_7 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_8 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h8 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_8 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_9 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h9 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_9 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_10 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'ha == io_waddr) begin // @[Regfile.scala 33:22]
          regs_10 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_11 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'hb == io_waddr) begin // @[Regfile.scala 33:22]
          regs_11 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_12 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'hc == io_waddr) begin // @[Regfile.scala 33:22]
          regs_12 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_13 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'hd == io_waddr) begin // @[Regfile.scala 33:22]
          regs_13 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_14 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'he == io_waddr) begin // @[Regfile.scala 33:22]
          regs_14 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_15 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'hf == io_waddr) begin // @[Regfile.scala 33:22]
          regs_15 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_16 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h10 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_16 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_17 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h11 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_17 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_18 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h12 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_18 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_19 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h13 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_19 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_20 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h14 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_20 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_21 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h15 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_21 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_22 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h16 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_22 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_23 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h17 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_23 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_24 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h18 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_24 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_25 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h19 == io_waddr) begin // @[Regfile.scala 33:22]
          regs_25 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_26 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1a == io_waddr) begin // @[Regfile.scala 33:22]
          regs_26 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_27 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1b == io_waddr) begin // @[Regfile.scala 33:22]
          regs_27 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_28 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1c == io_waddr) begin // @[Regfile.scala 33:22]
          regs_28 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_29 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1d == io_waddr) begin // @[Regfile.scala 33:22]
          regs_29 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_30 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1e == io_waddr) begin // @[Regfile.scala 33:22]
          regs_30 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
    end
    if (reset) begin // @[Regfile.scala 29:21]
      regs_31 <= 32'h0; // @[Regfile.scala 29:21]
    end else if (~reset) begin // @[Regfile.scala 31:37]
      if (io_we & io_waddr != 5'h0) begin // @[Regfile.scala 32:53]
        if (5'h1f == io_waddr) begin // @[Regfile.scala 33:22]
          regs_31 <= io_wdata; // @[Regfile.scala 33:22]
        end
      end
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
  rdata1r = _RAND_0[31:0];
  _RAND_1 = {1{`RANDOM}};
  rdata2r = _RAND_1[31:0];
  _RAND_2 = {1{`RANDOM}};
  regs_0 = _RAND_2[31:0];
  _RAND_3 = {1{`RANDOM}};
  regs_1 = _RAND_3[31:0];
  _RAND_4 = {1{`RANDOM}};
  regs_2 = _RAND_4[31:0];
  _RAND_5 = {1{`RANDOM}};
  regs_3 = _RAND_5[31:0];
  _RAND_6 = {1{`RANDOM}};
  regs_4 = _RAND_6[31:0];
  _RAND_7 = {1{`RANDOM}};
  regs_5 = _RAND_7[31:0];
  _RAND_8 = {1{`RANDOM}};
  regs_6 = _RAND_8[31:0];
  _RAND_9 = {1{`RANDOM}};
  regs_7 = _RAND_9[31:0];
  _RAND_10 = {1{`RANDOM}};
  regs_8 = _RAND_10[31:0];
  _RAND_11 = {1{`RANDOM}};
  regs_9 = _RAND_11[31:0];
  _RAND_12 = {1{`RANDOM}};
  regs_10 = _RAND_12[31:0];
  _RAND_13 = {1{`RANDOM}};
  regs_11 = _RAND_13[31:0];
  _RAND_14 = {1{`RANDOM}};
  regs_12 = _RAND_14[31:0];
  _RAND_15 = {1{`RANDOM}};
  regs_13 = _RAND_15[31:0];
  _RAND_16 = {1{`RANDOM}};
  regs_14 = _RAND_16[31:0];
  _RAND_17 = {1{`RANDOM}};
  regs_15 = _RAND_17[31:0];
  _RAND_18 = {1{`RANDOM}};
  regs_16 = _RAND_18[31:0];
  _RAND_19 = {1{`RANDOM}};
  regs_17 = _RAND_19[31:0];
  _RAND_20 = {1{`RANDOM}};
  regs_18 = _RAND_20[31:0];
  _RAND_21 = {1{`RANDOM}};
  regs_19 = _RAND_21[31:0];
  _RAND_22 = {1{`RANDOM}};
  regs_20 = _RAND_22[31:0];
  _RAND_23 = {1{`RANDOM}};
  regs_21 = _RAND_23[31:0];
  _RAND_24 = {1{`RANDOM}};
  regs_22 = _RAND_24[31:0];
  _RAND_25 = {1{`RANDOM}};
  regs_23 = _RAND_25[31:0];
  _RAND_26 = {1{`RANDOM}};
  regs_24 = _RAND_26[31:0];
  _RAND_27 = {1{`RANDOM}};
  regs_25 = _RAND_27[31:0];
  _RAND_28 = {1{`RANDOM}};
  regs_26 = _RAND_28[31:0];
  _RAND_29 = {1{`RANDOM}};
  regs_27 = _RAND_29[31:0];
  _RAND_30 = {1{`RANDOM}};
  regs_28 = _RAND_30[31:0];
  _RAND_31 = {1{`RANDOM}};
  regs_29 = _RAND_31[31:0];
  _RAND_32 = {1{`RANDOM}};
  regs_30 = _RAND_32[31:0];
  _RAND_33 = {1{`RANDOM}};
  regs_31 = _RAND_33[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
