module Inst_rom(
  input         clock,
  input         reset,
  input         io_ce,
  input  [31:0] io_addr,
  output [31:0] io_inst
);
`ifdef RANDOMIZE_GARBAGE_ASSIGN
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_GARBAGE_ASSIGN
`ifdef RANDOMIZE_MEM_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_2;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] inst_mem [0:131070]; // @[Inst_rom.scala 18:21]
  wire  inst_mem_instr_MPORT_en; // @[Inst_rom.scala 18:21]
  wire [16:0] inst_mem_instr_MPORT_addr; // @[Inst_rom.scala 18:21]
  wire [31:0] inst_mem_instr_MPORT_data; // @[Inst_rom.scala 18:21]
  reg [31:0] instr; // @[Inst_rom.scala 15:18]
  wire  _T = ~io_ce; // @[Inst_rom.scala 22:14]
  assign inst_mem_instr_MPORT_en = _T ? 1'h0 : 1'h1;
  assign inst_mem_instr_MPORT_addr = io_addr[18:2];
  `ifndef RANDOMIZE_GARBAGE_ASSIGN
  assign inst_mem_instr_MPORT_data = inst_mem[inst_mem_instr_MPORT_addr]; // @[Inst_rom.scala 18:21]
  `else
  assign inst_mem_instr_MPORT_data = inst_mem_instr_MPORT_addr >= 17'h1ffff ? _RAND_1[31:0] :
    inst_mem[inst_mem_instr_MPORT_addr]; // @[Inst_rom.scala 18:21]
  `endif // RANDOMIZE_GARBAGE_ASSIGN
  assign io_inst = instr; // @[Inst_rom.scala 16:11]
  always @(posedge clock) begin
    if (~io_ce) begin // @[Inst_rom.scala 22:31]
      instr <= 32'h0; // @[Inst_rom.scala 23:11]
    end else begin
      instr <= inst_mem_instr_MPORT_data; // @[Inst_rom.scala 25:11]
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
`ifdef RANDOMIZE_GARBAGE_ASSIGN
  _RAND_1 = {1{`RANDOM}};
`endif // RANDOMIZE_GARBAGE_ASSIGN
`ifdef RANDOMIZE_MEM_INIT
  _RAND_0 = {1{`RANDOM}};
  for (initvar = 0; initvar < 131071; initvar = initvar+1)
    inst_mem[initvar] = _RAND_0[31:0];
`endif // RANDOMIZE_MEM_INIT
`ifdef RANDOMIZE_REG_INIT
  _RAND_2 = {1{`RANDOM}};
  instr = _RAND_2[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
