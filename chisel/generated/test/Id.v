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
  reg [31:0] _RAND_3;
  reg [31:0] _RAND_4;
  reg [31:0] _RAND_5;
  reg [31:0] _RAND_6;
  reg [31:0] _RAND_7;
  reg [31:0] _RAND_8;
  reg [31:0] _RAND_9;
  reg [31:0] _RAND_10;
`endif // RANDOMIZE_REG_INIT
  reg  reg1_read_or; // @[Id.scala 33:25]
  reg  reg2_read_or; // @[Id.scala 34:25]
  reg [4:0] reg1_addr_or; // @[Id.scala 35:25]
  reg [4:0] reg2_addr_or; // @[Id.scala 36:25]
  reg [7:0] aluop_or; // @[Id.scala 39:21]
  reg [2:0] alusel_or; // @[Id.scala 40:22]
  reg [31:0] reg1_or; // @[Id.scala 41:20]
  reg [31:0] reg2_or; // @[Id.scala 42:20]
  reg [4:0] wd_or; // @[Id.scala 43:18]
  reg  wreg_or; // @[Id.scala 44:20]
  wire [5:0] op = io_inst_i[31:26]; // @[Id.scala 62:18]
  wire [5:0] op3 = io_inst_i[5:0]; // @[Id.scala 64:19]
  reg [31:0] imm; // @[Id.scala 68:16]
  wire [5:0] op2 = {{1'd0}, io_inst_i[10:6]}; // @[Id.scala 59:17 63:7]
  wire [7:0] _GEN_1 = 6'hf == op3 ? 8'h2 : 8'h0; // @[Id.scala 101:25 160:26 86:14]
  wire [7:0] _GEN_6 = 6'h7 == op3 ? 8'h3 : _GEN_1; // @[Id.scala 101:25 152:26]
  wire [2:0] _GEN_7 = 6'h7 == op3 ? 3'h2 : 3'h0; // @[Id.scala 101:25 153:27]
  wire  _GEN_8 = 6'h7 == op3 | 6'hf == op3; // @[Id.scala 101:25 155:30]
  wire  _GEN_10 = 6'h6 == op3 | 6'h7 == op3; // @[Id.scala 101:25 143:25]
  wire [7:0] _GEN_11 = 6'h6 == op3 ? 8'h2 : _GEN_6; // @[Id.scala 101:25 144:26]
  wire [2:0] _GEN_12 = 6'h6 == op3 ? 3'h2 : _GEN_7; // @[Id.scala 101:25 145:27]
  wire  _GEN_13 = 6'h6 == op3 | _GEN_8; // @[Id.scala 101:25 147:30]
  wire  _GEN_15 = 6'h4 == op3 | _GEN_10; // @[Id.scala 101:25 135:25]
  wire [7:0] _GEN_16 = 6'h4 == op3 ? 8'h7c : _GEN_11; // @[Id.scala 101:25 136:26]
  wire [2:0] _GEN_17 = 6'h4 == op3 ? 3'h2 : _GEN_12; // @[Id.scala 101:25 137:27]
  wire  _GEN_18 = 6'h4 == op3 | _GEN_13; // @[Id.scala 101:25 139:30]
  wire  _GEN_20 = 6'h27 == op3 | _GEN_15; // @[Id.scala 101:25 127:25]
  wire [7:0] _GEN_21 = 6'h27 == op3 ? 8'h27 : _GEN_16; // @[Id.scala 101:25 128:26]
  wire [2:0] _GEN_22 = 6'h27 == op3 ? 3'h1 : _GEN_17; // @[Id.scala 101:25 129:27]
  wire  _GEN_23 = 6'h27 == op3 | _GEN_18; // @[Id.scala 101:25 131:30]
  wire  _GEN_25 = 6'h26 == op3 | _GEN_20; // @[Id.scala 101:25 119:25]
  wire [7:0] _GEN_26 = 6'h26 == op3 ? 8'h26 : _GEN_21; // @[Id.scala 101:25 120:26]
  wire [2:0] _GEN_27 = 6'h26 == op3 ? 3'h1 : _GEN_22; // @[Id.scala 101:25 121:27]
  wire  _GEN_28 = 6'h26 == op3 | _GEN_23; // @[Id.scala 101:25 123:30]
  wire  _GEN_30 = 6'h24 == op3 | _GEN_25; // @[Id.scala 101:25 111:25]
  wire [7:0] _GEN_31 = 6'h24 == op3 ? 8'h24 : _GEN_26; // @[Id.scala 101:25 112:26]
  wire [2:0] _GEN_32 = 6'h24 == op3 ? 3'h1 : _GEN_27; // @[Id.scala 101:25 113:27]
  wire  _GEN_33 = 6'h24 == op3 | _GEN_28; // @[Id.scala 101:25 115:30]
  wire  _GEN_35 = 6'h25 == op3 | _GEN_30; // @[Id.scala 101:25 103:25]
  wire [7:0] _GEN_36 = 6'h25 == op3 ? 8'h25 : _GEN_31; // @[Id.scala 101:25 104:26]
  wire [2:0] _GEN_37 = 6'h25 == op3 ? 3'h1 : _GEN_32; // @[Id.scala 101:25 105:27]
  wire  _GEN_38 = 6'h25 == op3 | _GEN_33; // @[Id.scala 101:25 107:30]
  wire  _GEN_40 = 6'h0 == op2 & _GEN_35; // @[Id.scala 89:13 99:21]
  wire [7:0] _GEN_41 = 6'h0 == op2 ? _GEN_36 : 8'h0; // @[Id.scala 86:14 99:21]
  wire [2:0] _GEN_42 = 6'h0 == op2 ? _GEN_37 : 3'h0; // @[Id.scala 87:15 99:21]
  wire  _GEN_43 = 6'h0 == op2 & _GEN_38; // @[Id.scala 92:18 99:21]
  wire [31:0] _imm_T_1 = {16'h0,io_inst_i[15:0]}; // @[Cat.scala 33:92]
  wire [31:0] _imm_T_7 = {io_inst_i[15:0],16'h0}; // @[Cat.scala 33:92]
  wire  _GEN_50 = 6'hf == op | 6'h33 == op; // @[Id.scala 97:16 208:17]
  wire [7:0] _GEN_51 = 6'hf == op ? 8'h25 : 8'h0; // @[Id.scala 97:16 209:18]
  wire [2:0] _GEN_52 = 6'hf == op ? 3'h1 : 3'h0; // @[Id.scala 97:16 210:19]
  wire [31:0] _GEN_55 = 6'hf == op ? _imm_T_7 : 32'h0; // @[Id.scala 213:13 97:16 95:9]
  wire [4:0] _GEN_56 = 6'hf == op ? io_inst_i[20:16] : io_inst_i[15:11]; // @[Id.scala 214:15 88:11 97:16]
  wire  _GEN_58 = 6'he == op | _GEN_50; // @[Id.scala 97:16 198:17]
  wire [7:0] _GEN_59 = 6'he == op ? 8'h26 : _GEN_51; // @[Id.scala 97:16 199:18]
  wire [2:0] _GEN_60 = 6'he == op ? 3'h1 : _GEN_52; // @[Id.scala 97:16 200:19]
  wire  _GEN_61 = 6'he == op | 6'hf == op; // @[Id.scala 97:16 201:22]
  wire [31:0] _GEN_63 = 6'he == op ? _imm_T_1 : _GEN_55; // @[Id.scala 203:13 97:16]
  wire [4:0] _GEN_64 = 6'he == op ? io_inst_i[20:16] : _GEN_56; // @[Id.scala 204:15 97:16]
  wire  _GEN_66 = 6'hc == op | _GEN_58; // @[Id.scala 97:16 188:17]
  wire [7:0] _GEN_67 = 6'hc == op ? 8'h24 : _GEN_59; // @[Id.scala 97:16 189:18]
  wire [2:0] _GEN_68 = 6'hc == op ? 3'h1 : _GEN_60; // @[Id.scala 97:16 190:19]
  wire  _GEN_69 = 6'hc == op | _GEN_61; // @[Id.scala 97:16 191:22]
  wire [31:0] _GEN_71 = 6'hc == op ? _imm_T_1 : _GEN_63; // @[Id.scala 193:13 97:16]
  wire [4:0] _GEN_72 = 6'hc == op ? io_inst_i[20:16] : _GEN_64; // @[Id.scala 194:15 97:16]
  wire  _GEN_74 = 6'hd == op | _GEN_66; // @[Id.scala 97:16 171:17]
  wire [7:0] _GEN_75 = 6'hd == op ? 8'h25 : _GEN_67; // @[Id.scala 97:16 173:18]
  wire [2:0] _GEN_76 = 6'hd == op ? 3'h1 : _GEN_68; // @[Id.scala 97:16 175:19]
  wire  _GEN_77 = 6'hd == op | _GEN_69; // @[Id.scala 97:16 177:22]
  wire [31:0] _GEN_79 = 6'hd == op ? _imm_T_1 : _GEN_71; // @[Id.scala 181:13 97:16]
  wire [4:0] _GEN_80 = 6'hd == op ? io_inst_i[20:16] : _GEN_72; // @[Id.scala 183:15 97:16]
  wire  _GEN_82 = 6'h0 == op ? _GEN_40 : _GEN_74; // @[Id.scala 97:16]
  wire [7:0] _GEN_83 = 6'h0 == op ? _GEN_41 : _GEN_75; // @[Id.scala 97:16]
  wire [2:0] _GEN_84 = 6'h0 == op ? _GEN_42 : _GEN_76; // @[Id.scala 97:16]
  wire  _GEN_85 = 6'h0 == op ? _GEN_40 : _GEN_77; // @[Id.scala 97:16]
  wire  _GEN_86 = 6'h0 == op & _GEN_43; // @[Id.scala 97:16]
  wire [31:0] _GEN_88 = 6'h0 == op ? 32'h0 : _GEN_79; // @[Id.scala 97:16 95:9]
  wire [4:0] _GEN_89 = 6'h0 == op ? io_inst_i[15:11] : _GEN_80; // @[Id.scala 88:11 97:16]
  wire  _GEN_90 = op3 == 6'h3 | _GEN_82; // @[Id.scala 245:35 246:17]
  wire [7:0] _GEN_91 = op3 == 6'h3 ? 8'h3 : _GEN_83; // @[Id.scala 245:35 247:18]
  wire [2:0] _GEN_92 = op3 == 6'h3 ? 3'h2 : _GEN_84; // @[Id.scala 245:35 248:19]
  wire  _GEN_93 = op3 == 6'h3 ? 1'h0 : _GEN_85; // @[Id.scala 245:35 249:22]
  wire  _GEN_94 = op3 == 6'h3 | _GEN_86; // @[Id.scala 245:35 250:22]
  wire [31:0] _GEN_95 = op3 == 6'h3 ? {{27'd0}, io_inst_i[10:6]} : _GEN_88; // @[Id.scala 245:35 251:13]
  wire [4:0] _GEN_96 = op3 == 6'h3 ? io_inst_i[15:11] : _GEN_89; // @[Id.scala 245:35 252:15]
  wire  _GEN_98 = op3 == 6'h2 | _GEN_90; // @[Id.scala 236:35 237:17]
  wire  _GEN_102 = op3 == 6'h2 | _GEN_94; // @[Id.scala 236:35 241:22]
  wire  _GEN_106 = op3 == 6'h0 | _GEN_98; // @[Id.scala 227:29 228:17]
  wire  _GEN_110 = op3 == 6'h0 | _GEN_102; // @[Id.scala 227:29 232:22]
  assign io_reg1_read_o = reg1_read_or; // @[Id.scala 46:18]
  assign io_reg2_read_o = reg2_read_or; // @[Id.scala 47:18]
  assign io_reg1_addr_o = reg1_addr_or; // @[Id.scala 48:18]
  assign io_reg2_addr_o = reg2_addr_or; // @[Id.scala 49:18]
  assign io_aluop_o = aluop_or; // @[Id.scala 50:14]
  assign io_alusel_o = alusel_or; // @[Id.scala 51:15]
  assign io_reg1_o = reg1_or; // @[Id.scala 52:13]
  assign io_reg2_o = reg2_or; // @[Id.scala 53:13]
  assign io_wd_o = wd_or; // @[Id.scala 54:11]
  assign io_wreg_o = wreg_or; // @[Id.scala 55:13]
  always @(posedge clock) begin
    if (reset) begin // @[Id.scala 74:36]
      reg1_read_or <= 1'h0; // @[Id.scala 80:18]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      if (op3 == 6'h0) begin // @[Id.scala 227:29]
        reg1_read_or <= 1'h0; // @[Id.scala 231:22]
      end else if (op3 == 6'h2) begin // @[Id.scala 236:35]
        reg1_read_or <= 1'h0; // @[Id.scala 240:22]
      end else begin
        reg1_read_or <= _GEN_93;
      end
    end else if (6'h0 == op) begin // @[Id.scala 97:16]
      reg1_read_or <= _GEN_40;
    end else begin
      reg1_read_or <= _GEN_77;
    end
    if (reset) begin // @[Id.scala 74:36]
      reg2_read_or <= 1'h0; // @[Id.scala 81:18]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      reg2_read_or <= _GEN_110;
    end else begin
      reg2_read_or <= _GEN_86;
    end
    if (reset) begin // @[Id.scala 74:36]
      reg1_addr_or <= 5'h0; // @[Id.scala 82:18]
    end else begin
      reg1_addr_or <= io_inst_i[25:21]; // @[Id.scala 93:18]
    end
    if (reset) begin // @[Id.scala 74:36]
      reg2_addr_or <= 5'h0; // @[Id.scala 83:18]
    end else begin
      reg2_addr_or <= io_inst_i[20:16]; // @[Id.scala 94:18]
    end
    if (reset) begin // @[Id.scala 74:36]
      aluop_or <= 8'h0; // @[Id.scala 75:14]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      if (op3 == 6'h0) begin // @[Id.scala 227:29]
        aluop_or <= 8'h7c; // @[Id.scala 229:18]
      end else if (op3 == 6'h2) begin // @[Id.scala 236:35]
        aluop_or <= 8'h2; // @[Id.scala 238:18]
      end else begin
        aluop_or <= _GEN_91;
      end
    end else if (6'h0 == op) begin // @[Id.scala 97:16]
      if (6'h0 == op2) begin // @[Id.scala 99:21]
        aluop_or <= _GEN_36;
      end else begin
        aluop_or <= 8'h0; // @[Id.scala 86:14]
      end
    end else if (6'hd == op) begin // @[Id.scala 97:16]
      aluop_or <= 8'h25; // @[Id.scala 173:18]
    end else begin
      aluop_or <= _GEN_67;
    end
    if (reset) begin // @[Id.scala 74:36]
      alusel_or <= 3'h0; // @[Id.scala 76:15]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      if (op3 == 6'h0) begin // @[Id.scala 227:29]
        alusel_or <= 3'h2; // @[Id.scala 230:19]
      end else if (op3 == 6'h2) begin // @[Id.scala 236:35]
        alusel_or <= 3'h2; // @[Id.scala 239:19]
      end else begin
        alusel_or <= _GEN_92;
      end
    end else if (6'h0 == op) begin // @[Id.scala 97:16]
      if (6'h0 == op2) begin // @[Id.scala 99:21]
        alusel_or <= _GEN_37;
      end else begin
        alusel_or <= 3'h0; // @[Id.scala 87:15]
      end
    end else if (6'hd == op) begin // @[Id.scala 97:16]
      alusel_or <= 3'h1; // @[Id.scala 175:19]
    end else begin
      alusel_or <= _GEN_68;
    end
    if (reset) begin // @[Id.scala 259:36]
      reg1_or <= 32'h0; // @[Id.scala 260:13]
    end else if (reg1_read_or) begin // @[Id.scala 261:39]
      reg1_or <= io_reg1_data_i; // @[Id.scala 262:13]
    end else if (~reg1_read_or) begin // @[Id.scala 263:39]
      reg1_or <= imm; // @[Id.scala 264:13]
    end else begin
      reg1_or <= 32'h0; // @[Id.scala 266:13]
    end
    if (reset) begin // @[Id.scala 270:36]
      reg2_or <= 32'h0; // @[Id.scala 271:13]
    end else if (reg2_read_or) begin // @[Id.scala 272:39]
      reg2_or <= io_reg2_data_i; // @[Id.scala 273:13]
    end else if (~reg2_read_or) begin // @[Id.scala 274:39]
      reg2_or <= imm; // @[Id.scala 275:13]
    end else begin
      reg2_or <= 32'h0; // @[Id.scala 277:13]
    end
    if (reset) begin // @[Id.scala 74:36]
      wd_or <= 5'h0; // @[Id.scala 77:11]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      if (op3 == 6'h0) begin // @[Id.scala 227:29]
        wd_or <= io_inst_i[15:11]; // @[Id.scala 234:15]
      end else if (op3 == 6'h2) begin // @[Id.scala 236:35]
        wd_or <= io_inst_i[15:11]; // @[Id.scala 243:15]
      end else begin
        wd_or <= _GEN_96;
      end
    end else if (6'h0 == op) begin // @[Id.scala 97:16]
      wd_or <= io_inst_i[15:11]; // @[Id.scala 88:11]
    end else if (6'hd == op) begin // @[Id.scala 97:16]
      wd_or <= io_inst_i[20:16]; // @[Id.scala 183:15]
    end else begin
      wd_or <= _GEN_72;
    end
    if (reset) begin // @[Id.scala 74:36]
      wreg_or <= 1'h0; // @[Id.scala 78:13]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      wreg_or <= _GEN_106;
    end else if (6'h0 == op) begin // @[Id.scala 97:16]
      wreg_or <= _GEN_40;
    end else begin
      wreg_or <= _GEN_74;
    end
    if (reset) begin // @[Id.scala 74:36]
      imm <= 32'h0; // @[Id.scala 84:9]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 226:50]
      if (op3 == 6'h0) begin // @[Id.scala 227:29]
        imm <= {{27'd0}, io_inst_i[10:6]}; // @[Id.scala 233:13]
      end else if (op3 == 6'h2) begin // @[Id.scala 236:35]
        imm <= {{27'd0}, io_inst_i[10:6]}; // @[Id.scala 242:13]
      end else begin
        imm <= _GEN_95;
      end
    end else if (6'h0 == op) begin // @[Id.scala 97:16]
      imm <= 32'h0; // @[Id.scala 95:9]
    end else if (6'hd == op) begin // @[Id.scala 97:16]
      imm <= _imm_T_1; // @[Id.scala 181:13]
    end else begin
      imm <= _GEN_71;
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
  reg1_read_or = _RAND_0[0:0];
  _RAND_1 = {1{`RANDOM}};
  reg2_read_or = _RAND_1[0:0];
  _RAND_2 = {1{`RANDOM}};
  reg1_addr_or = _RAND_2[4:0];
  _RAND_3 = {1{`RANDOM}};
  reg2_addr_or = _RAND_3[4:0];
  _RAND_4 = {1{`RANDOM}};
  aluop_or = _RAND_4[7:0];
  _RAND_5 = {1{`RANDOM}};
  alusel_or = _RAND_5[2:0];
  _RAND_6 = {1{`RANDOM}};
  reg1_or = _RAND_6[31:0];
  _RAND_7 = {1{`RANDOM}};
  reg2_or = _RAND_7[31:0];
  _RAND_8 = {1{`RANDOM}};
  wd_or = _RAND_8[4:0];
  _RAND_9 = {1{`RANDOM}};
  wreg_or = _RAND_9[0:0];
  _RAND_10 = {1{`RANDOM}};
  imm = _RAND_10[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
