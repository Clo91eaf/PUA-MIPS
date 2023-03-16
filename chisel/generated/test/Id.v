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
  reg [7:0] aluop_or; // @[Id.scala 37:21]
  reg [2:0] alusel_or; // @[Id.scala 38:22]
  reg [31:0] reg1_or; // @[Id.scala 39:20]
  reg [31:0] reg2_or; // @[Id.scala 40:20]
  reg [4:0] wd_or; // @[Id.scala 41:18]
  reg  wreg_or; // @[Id.scala 42:20]
  wire [5:0] op = io_inst_i[31:26]; // @[Id.scala 60:18]
  wire [4:0] op2 = io_inst_i[10:6]; // @[Id.scala 61:19]
  wire [5:0] op3 = io_inst_i[5:0]; // @[Id.scala 62:19]
  wire [4:0] op4 = io_inst_i[20:16]; // @[Id.scala 63:19]
  reg [31:0] imm; // @[Id.scala 66:16]
  wire  _T_17 = reg2_or == 32'h0; // @[Id.scala 200:30]
  wire [7:0] _GEN_2 = 6'ha == op3 ? 8'ha : 8'h0; // @[Id.scala 99:25 208:26 84:14]
  wire [2:0] _GEN_3 = 6'ha == op3 ? 3'h3 : 3'h0; // @[Id.scala 99:25 209:27 85:15]
  wire  _GEN_6 = 6'ha == op3 & _T_17; // @[Id.scala 87:13 99:25]
  wire [7:0] _GEN_7 = 6'hb == op3 ? 8'hb : _GEN_2; // @[Id.scala 99:25 195:26]
  wire [2:0] _GEN_8 = 6'hb == op3 ? 3'h3 : _GEN_3; // @[Id.scala 99:25 196:27]
  wire  _GEN_9 = 6'hb == op3 | 6'ha == op3; // @[Id.scala 99:25 197:30]
  wire  _GEN_11 = 6'hb == op3 ? _T_17 : _GEN_6; // @[Id.scala 99:25]
  wire  _GEN_12 = 6'h13 == op3 ? 1'h0 : _GEN_11; // @[Id.scala 188:25 99:25]
  wire [7:0] _GEN_13 = 6'h13 == op3 ? 8'h13 : _GEN_7; // @[Id.scala 99:25 189:26]
  wire  _GEN_14 = 6'h13 == op3 | _GEN_9; // @[Id.scala 99:25 190:30]
  wire  _GEN_15 = 6'h13 == op3 ? 1'h0 : _GEN_9; // @[Id.scala 99:25 191:30]
  wire [2:0] _GEN_17 = 6'h13 == op3 ? 3'h0 : _GEN_8; // @[Id.scala 85:15 99:25]
  wire  _GEN_18 = 6'h11 == op3 ? 1'h0 : _GEN_12; // @[Id.scala 181:25 99:25]
  wire [7:0] _GEN_19 = 6'h11 == op3 ? 8'h11 : _GEN_13; // @[Id.scala 99:25 182:26]
  wire  _GEN_20 = 6'h11 == op3 | _GEN_14; // @[Id.scala 99:25 183:30]
  wire  _GEN_21 = 6'h11 == op3 ? 1'h0 : _GEN_15; // @[Id.scala 99:25 184:30]
  wire [2:0] _GEN_23 = 6'h11 == op3 ? 3'h0 : _GEN_17; // @[Id.scala 85:15 99:25]
  wire  _GEN_24 = 6'h12 == op3 | _GEN_18; // @[Id.scala 173:25 99:25]
  wire [7:0] _GEN_25 = 6'h12 == op3 ? 8'h12 : _GEN_19; // @[Id.scala 99:25 174:26]
  wire [2:0] _GEN_26 = 6'h12 == op3 ? 3'h3 : _GEN_23; // @[Id.scala 99:25 175:27]
  wire  _GEN_27 = 6'h12 == op3 ? 1'h0 : _GEN_20; // @[Id.scala 99:25 176:30]
  wire  _GEN_28 = 6'h12 == op3 ? 1'h0 : _GEN_21; // @[Id.scala 99:25 177:30]
  wire  _GEN_30 = 6'h10 == op3 | _GEN_24; // @[Id.scala 165:25 99:25]
  wire [7:0] _GEN_31 = 6'h10 == op3 ? 8'h10 : _GEN_25; // @[Id.scala 99:25 166:26]
  wire [2:0] _GEN_32 = 6'h10 == op3 ? 3'h3 : _GEN_26; // @[Id.scala 99:25 167:27]
  wire  _GEN_33 = 6'h10 == op3 ? 1'h0 : _GEN_27; // @[Id.scala 99:25 168:30]
  wire  _GEN_34 = 6'h10 == op3 ? 1'h0 : _GEN_28; // @[Id.scala 99:25 169:30]
  wire  _GEN_36 = 6'hf == op3 ? 1'h0 : _GEN_30; // @[Id.scala 157:25 99:25]
  wire [7:0] _GEN_37 = 6'hf == op3 ? 8'h2 : _GEN_31; // @[Id.scala 99:25 158:26]
  wire [2:0] _GEN_38 = 6'hf == op3 ? 3'h0 : _GEN_32; // @[Id.scala 99:25 159:27]
  wire  _GEN_39 = 6'hf == op3 ? 1'h0 : _GEN_33; // @[Id.scala 99:25 160:30]
  wire  _GEN_40 = 6'hf == op3 | _GEN_34; // @[Id.scala 99:25 161:30]
  wire  _GEN_42 = 6'h7 == op3 | _GEN_36; // @[Id.scala 149:25 99:25]
  wire [7:0] _GEN_43 = 6'h7 == op3 ? 8'h3 : _GEN_37; // @[Id.scala 99:25 150:26]
  wire [2:0] _GEN_44 = 6'h7 == op3 ? 3'h2 : _GEN_38; // @[Id.scala 99:25 151:27]
  wire  _GEN_45 = 6'h7 == op3 | _GEN_39; // @[Id.scala 99:25 152:30]
  wire  _GEN_46 = 6'h7 == op3 | _GEN_40; // @[Id.scala 99:25 153:30]
  wire  _GEN_48 = 6'h6 == op3 | _GEN_42; // @[Id.scala 141:25 99:25]
  wire [7:0] _GEN_49 = 6'h6 == op3 ? 8'h2 : _GEN_43; // @[Id.scala 99:25 142:26]
  wire [2:0] _GEN_50 = 6'h6 == op3 ? 3'h2 : _GEN_44; // @[Id.scala 99:25 143:27]
  wire  _GEN_51 = 6'h6 == op3 | _GEN_45; // @[Id.scala 99:25 144:30]
  wire  _GEN_52 = 6'h6 == op3 | _GEN_46; // @[Id.scala 99:25 145:30]
  wire  _GEN_54 = 6'h4 == op3 | _GEN_48; // @[Id.scala 133:25 99:25]
  wire [7:0] _GEN_55 = 6'h4 == op3 ? 8'h7c : _GEN_49; // @[Id.scala 99:25 134:26]
  wire [2:0] _GEN_56 = 6'h4 == op3 ? 3'h2 : _GEN_50; // @[Id.scala 99:25 135:27]
  wire  _GEN_57 = 6'h4 == op3 | _GEN_51; // @[Id.scala 99:25 136:30]
  wire  _GEN_58 = 6'h4 == op3 | _GEN_52; // @[Id.scala 99:25 137:30]
  wire  _GEN_60 = 6'h27 == op3 | _GEN_54; // @[Id.scala 125:25 99:25]
  wire [7:0] _GEN_61 = 6'h27 == op3 ? 8'h27 : _GEN_55; // @[Id.scala 99:25 126:26]
  wire [2:0] _GEN_62 = 6'h27 == op3 ? 3'h1 : _GEN_56; // @[Id.scala 99:25 127:27]
  wire  _GEN_63 = 6'h27 == op3 | _GEN_57; // @[Id.scala 99:25 128:30]
  wire  _GEN_64 = 6'h27 == op3 | _GEN_58; // @[Id.scala 99:25 129:30]
  wire  _GEN_66 = 6'h26 == op3 | _GEN_60; // @[Id.scala 117:25 99:25]
  wire [7:0] _GEN_67 = 6'h26 == op3 ? 8'h26 : _GEN_61; // @[Id.scala 99:25 118:26]
  wire [2:0] _GEN_68 = 6'h26 == op3 ? 3'h1 : _GEN_62; // @[Id.scala 99:25 119:27]
  wire  _GEN_69 = 6'h26 == op3 | _GEN_63; // @[Id.scala 99:25 120:30]
  wire  _GEN_70 = 6'h26 == op3 | _GEN_64; // @[Id.scala 99:25 121:30]
  wire  _GEN_72 = 6'h24 == op3 | _GEN_66; // @[Id.scala 109:25 99:25]
  wire [7:0] _GEN_73 = 6'h24 == op3 ? 8'h24 : _GEN_67; // @[Id.scala 99:25 110:26]
  wire [2:0] _GEN_74 = 6'h24 == op3 ? 3'h1 : _GEN_68; // @[Id.scala 99:25 111:27]
  wire  _GEN_75 = 6'h24 == op3 | _GEN_69; // @[Id.scala 99:25 112:30]
  wire  _GEN_76 = 6'h24 == op3 | _GEN_70; // @[Id.scala 99:25 113:30]
  wire  _GEN_78 = 6'h25 == op3 | _GEN_72; // @[Id.scala 101:25 99:25]
  wire [7:0] _GEN_79 = 6'h25 == op3 ? 8'h25 : _GEN_73; // @[Id.scala 99:25 102:26]
  wire [2:0] _GEN_80 = 6'h25 == op3 ? 3'h1 : _GEN_74; // @[Id.scala 99:25 103:27]
  wire  _GEN_81 = 6'h25 == op3 | _GEN_75; // @[Id.scala 99:25 104:30]
  wire  _GEN_82 = 6'h25 == op3 | _GEN_76; // @[Id.scala 99:25 105:30]
  wire  _GEN_84 = 5'h0 == op2 & _GEN_78; // @[Id.scala 87:13 97:21]
  wire [7:0] _GEN_85 = 5'h0 == op2 ? _GEN_79 : 8'h0; // @[Id.scala 84:14 97:21]
  wire [2:0] _GEN_86 = 5'h0 == op2 ? _GEN_80 : 3'h0; // @[Id.scala 85:15 97:21]
  wire  _GEN_87 = 5'h0 == op2 & _GEN_81; // @[Id.scala 89:18 97:21]
  wire  _GEN_88 = 5'h0 == op2 & _GEN_82; // @[Id.scala 90:18 97:21]
  wire [31:0] _imm_T_1 = {16'h0,io_inst_i[15:0]}; // @[Cat.scala 33:92]
  wire [31:0] _imm_T_7 = {io_inst_i[15:0],16'h0}; // @[Cat.scala 33:92]
  wire  _GEN_95 = 6'hf == op | 6'h33 == op; // @[Id.scala 95:16 262:17]
  wire [7:0] _GEN_96 = 6'hf == op ? 8'h25 : 8'h0; // @[Id.scala 95:16 263:18]
  wire [2:0] _GEN_97 = 6'hf == op ? 3'h1 : 3'h0; // @[Id.scala 95:16 264:19]
  wire [31:0] _GEN_100 = 6'hf == op ? _imm_T_7 : 32'h0; // @[Id.scala 267:13 95:16 93:9]
  wire [4:0] _GEN_101 = 6'hf == op ? op4 : io_inst_i[15:11]; // @[Id.scala 268:15 86:11 95:16]
  wire  _GEN_103 = 6'he == op | _GEN_95; // @[Id.scala 95:16 252:17]
  wire [7:0] _GEN_104 = 6'he == op ? 8'h26 : _GEN_96; // @[Id.scala 95:16 253:18]
  wire [2:0] _GEN_105 = 6'he == op ? 3'h1 : _GEN_97; // @[Id.scala 95:16 254:19]
  wire  _GEN_106 = 6'he == op | 6'hf == op; // @[Id.scala 95:16 255:22]
  wire [31:0] _GEN_108 = 6'he == op ? _imm_T_1 : _GEN_100; // @[Id.scala 257:13 95:16]
  wire [4:0] _GEN_109 = 6'he == op ? op4 : _GEN_101; // @[Id.scala 258:15 95:16]
  wire  _GEN_111 = 6'hc == op | _GEN_103; // @[Id.scala 95:16 242:17]
  wire [7:0] _GEN_112 = 6'hc == op ? 8'h24 : _GEN_104; // @[Id.scala 95:16 243:18]
  wire [2:0] _GEN_113 = 6'hc == op ? 3'h1 : _GEN_105; // @[Id.scala 95:16 244:19]
  wire  _GEN_114 = 6'hc == op | _GEN_106; // @[Id.scala 95:16 245:22]
  wire [31:0] _GEN_116 = 6'hc == op ? _imm_T_1 : _GEN_108; // @[Id.scala 247:13 95:16]
  wire [4:0] _GEN_117 = 6'hc == op ? op4 : _GEN_109; // @[Id.scala 248:15 95:16]
  wire  _GEN_119 = 6'hd == op | _GEN_111; // @[Id.scala 95:16 225:17]
  wire [7:0] _GEN_120 = 6'hd == op ? 8'h25 : _GEN_112; // @[Id.scala 95:16 227:18]
  wire [2:0] _GEN_121 = 6'hd == op ? 3'h1 : _GEN_113; // @[Id.scala 95:16 229:19]
  wire  _GEN_122 = 6'hd == op | _GEN_114; // @[Id.scala 95:16 231:22]
  wire [31:0] _GEN_124 = 6'hd == op ? _imm_T_1 : _GEN_116; // @[Id.scala 235:13 95:16]
  wire [4:0] _GEN_125 = 6'hd == op ? op4 : _GEN_117; // @[Id.scala 237:15 95:16]
  wire  _GEN_127 = 6'h0 == op ? _GEN_84 : _GEN_119; // @[Id.scala 95:16]
  wire [7:0] _GEN_128 = 6'h0 == op ? _GEN_85 : _GEN_120; // @[Id.scala 95:16]
  wire [2:0] _GEN_129 = 6'h0 == op ? _GEN_86 : _GEN_121; // @[Id.scala 95:16]
  wire  _GEN_130 = 6'h0 == op ? _GEN_87 : _GEN_122; // @[Id.scala 95:16]
  wire  _GEN_131 = 6'h0 == op & _GEN_88; // @[Id.scala 95:16]
  wire [31:0] _GEN_133 = 6'h0 == op ? 32'h0 : _GEN_124; // @[Id.scala 95:16 93:9]
  wire [4:0] _GEN_134 = 6'h0 == op ? io_inst_i[15:11] : _GEN_125; // @[Id.scala 86:11 95:16]
  wire  _GEN_135 = op3 == 6'h3 | _GEN_127; // @[Id.scala 299:35 300:17]
  wire [7:0] _GEN_136 = op3 == 6'h3 ? 8'h3 : _GEN_128; // @[Id.scala 299:35 301:18]
  wire [2:0] _GEN_137 = op3 == 6'h3 ? 3'h2 : _GEN_129; // @[Id.scala 299:35 302:19]
  wire  _GEN_138 = op3 == 6'h3 ? 1'h0 : _GEN_130; // @[Id.scala 299:35 303:22]
  wire  _GEN_139 = op3 == 6'h3 | _GEN_131; // @[Id.scala 299:35 304:22]
  wire [31:0] _GEN_140 = op3 == 6'h3 ? {{27'd0}, op2} : _GEN_133; // @[Id.scala 299:35 305:13]
  wire [4:0] _GEN_141 = op3 == 6'h3 ? io_inst_i[15:11] : _GEN_134; // @[Id.scala 299:35 306:15]
  wire  _GEN_143 = op3 == 6'h2 | _GEN_135; // @[Id.scala 290:35 291:17]
  wire  _GEN_147 = op3 == 6'h2 | _GEN_139; // @[Id.scala 290:35 295:22]
  wire  _GEN_151 = op3 == 6'h0 | _GEN_143; // @[Id.scala 281:29 282:17]
  wire  _GEN_155 = op3 == 6'h0 | _GEN_147; // @[Id.scala 281:29 286:22]
  assign io_reg1_read_o = reg1_read_or; // @[Id.scala 44:18]
  assign io_reg2_read_o = reg2_read_or; // @[Id.scala 45:18]
  assign io_reg1_addr_o = reg1_addr_or; // @[Id.scala 46:18]
  assign io_reg2_addr_o = reg2_addr_or; // @[Id.scala 47:18]
  assign io_aluop_o = aluop_or; // @[Id.scala 48:14]
  assign io_alusel_o = alusel_or; // @[Id.scala 49:15]
  assign io_reg1_o = reg1_or; // @[Id.scala 50:13]
  assign io_reg2_o = reg2_or; // @[Id.scala 51:13]
  assign io_wd_o = wd_or; // @[Id.scala 52:11]
  assign io_wreg_o = wreg_or; // @[Id.scala 53:13]
  always @(posedge clock) begin
    if (reset) begin // @[Id.scala 72:36]
      reg1_read_or <= 1'h0; // @[Id.scala 78:18]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      if (op3 == 6'h0) begin // @[Id.scala 281:29]
        reg1_read_or <= 1'h0; // @[Id.scala 285:22]
      end else if (op3 == 6'h2) begin // @[Id.scala 290:35]
        reg1_read_or <= 1'h0; // @[Id.scala 294:22]
      end else begin
        reg1_read_or <= _GEN_138;
      end
    end else if (6'h0 == op) begin // @[Id.scala 95:16]
      reg1_read_or <= _GEN_87;
    end else begin
      reg1_read_or <= _GEN_122;
    end
    if (reset) begin // @[Id.scala 72:36]
      reg2_read_or <= 1'h0; // @[Id.scala 79:18]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      reg2_read_or <= _GEN_155;
    end else begin
      reg2_read_or <= _GEN_131;
    end
    if (reset) begin // @[Id.scala 72:36]
      reg1_addr_or <= 5'h0; // @[Id.scala 80:18]
    end else begin
      reg1_addr_or <= io_inst_i[25:21]; // @[Id.scala 91:18]
    end
    if (reset) begin // @[Id.scala 72:36]
      reg2_addr_or <= 5'h0; // @[Id.scala 81:18]
    end else begin
      reg2_addr_or <= op4; // @[Id.scala 92:18]
    end
    if (reset) begin // @[Id.scala 72:36]
      aluop_or <= 8'h0; // @[Id.scala 73:14]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      if (op3 == 6'h0) begin // @[Id.scala 281:29]
        aluop_or <= 8'h7c; // @[Id.scala 283:18]
      end else if (op3 == 6'h2) begin // @[Id.scala 290:35]
        aluop_or <= 8'h2; // @[Id.scala 292:18]
      end else begin
        aluop_or <= _GEN_136;
      end
    end else if (6'h0 == op) begin // @[Id.scala 95:16]
      if (5'h0 == op2) begin // @[Id.scala 97:21]
        aluop_or <= _GEN_79;
      end else begin
        aluop_or <= 8'h0; // @[Id.scala 84:14]
      end
    end else if (6'hd == op) begin // @[Id.scala 95:16]
      aluop_or <= 8'h25; // @[Id.scala 227:18]
    end else begin
      aluop_or <= _GEN_112;
    end
    if (reset) begin // @[Id.scala 72:36]
      alusel_or <= 3'h0; // @[Id.scala 74:15]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      if (op3 == 6'h0) begin // @[Id.scala 281:29]
        alusel_or <= 3'h2; // @[Id.scala 284:19]
      end else if (op3 == 6'h2) begin // @[Id.scala 290:35]
        alusel_or <= 3'h2; // @[Id.scala 293:19]
      end else begin
        alusel_or <= _GEN_137;
      end
    end else if (6'h0 == op) begin // @[Id.scala 95:16]
      if (5'h0 == op2) begin // @[Id.scala 97:21]
        alusel_or <= _GEN_80;
      end else begin
        alusel_or <= 3'h0; // @[Id.scala 85:15]
      end
    end else if (6'hd == op) begin // @[Id.scala 95:16]
      alusel_or <= 3'h1; // @[Id.scala 229:19]
    end else begin
      alusel_or <= _GEN_113;
    end
    if (reset) begin // @[Id.scala 313:36]
      reg1_or <= 32'h0; // @[Id.scala 314:13]
    end else if (reg1_read_or) begin // @[Id.scala 315:36]
      reg1_or <= io_reg1_data_i; // @[Id.scala 316:13]
    end else if (~reg1_read_or) begin // @[Id.scala 317:36]
      reg1_or <= imm; // @[Id.scala 318:13]
    end else begin
      reg1_or <= 32'h0; // @[Id.scala 320:13]
    end
    if (reset) begin // @[Id.scala 324:36]
      reg2_or <= 32'h0; // @[Id.scala 325:13]
    end else if (reg2_read_or) begin // @[Id.scala 326:36]
      reg2_or <= io_reg2_data_i; // @[Id.scala 327:13]
    end else if (~reg2_read_or) begin // @[Id.scala 328:36]
      reg2_or <= imm; // @[Id.scala 329:13]
    end else begin
      reg2_or <= 32'h0; // @[Id.scala 331:13]
    end
    if (reset) begin // @[Id.scala 72:36]
      wd_or <= 5'h0; // @[Id.scala 75:11]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      if (op3 == 6'h0) begin // @[Id.scala 281:29]
        wd_or <= io_inst_i[15:11]; // @[Id.scala 288:15]
      end else if (op3 == 6'h2) begin // @[Id.scala 290:35]
        wd_or <= io_inst_i[15:11]; // @[Id.scala 297:15]
      end else begin
        wd_or <= _GEN_141;
      end
    end else if (6'h0 == op) begin // @[Id.scala 95:16]
      wd_or <= io_inst_i[15:11]; // @[Id.scala 86:11]
    end else if (6'hd == op) begin // @[Id.scala 95:16]
      wd_or <= op4; // @[Id.scala 237:15]
    end else begin
      wd_or <= _GEN_117;
    end
    if (reset) begin // @[Id.scala 72:36]
      wreg_or <= 1'h0; // @[Id.scala 76:13]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      wreg_or <= _GEN_151;
    end else if (6'h0 == op) begin // @[Id.scala 95:16]
      wreg_or <= _GEN_84;
    end else begin
      wreg_or <= _GEN_119;
    end
    if (reset) begin // @[Id.scala 72:36]
      imm <= 32'h0; // @[Id.scala 82:9]
    end else if (io_inst_i[31:21] == 11'h0) begin // @[Id.scala 280:37]
      if (op3 == 6'h0) begin // @[Id.scala 281:29]
        imm <= {{27'd0}, op2}; // @[Id.scala 287:13]
      end else if (op3 == 6'h2) begin // @[Id.scala 290:35]
        imm <= {{27'd0}, op2}; // @[Id.scala 296:13]
      end else begin
        imm <= _GEN_140;
      end
    end else if (6'h0 == op) begin // @[Id.scala 95:16]
      imm <= 32'h0; // @[Id.scala 93:9]
    end else if (6'hd == op) begin // @[Id.scala 95:16]
      imm <= _imm_T_1; // @[Id.scala 235:13]
    end else begin
      imm <= _GEN_116;
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
