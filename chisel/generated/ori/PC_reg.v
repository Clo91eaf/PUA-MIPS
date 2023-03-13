module PC_reg(
  input         clock,
  input         reset,
  output [31:0] io_pc,
  output        io_ce
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] pcr; // @[PC_reg.scala 13:16]
  reg  cer; // @[PC_reg.scala 14:16]
  wire [31:0] _pcr_T_1 = pcr + 32'h4; // @[PC_reg.scala 27:16]
  assign io_pc = pcr; // @[PC_reg.scala 16:9]
  assign io_ce = cer; // @[PC_reg.scala 17:9]
  always @(posedge clock) begin
    if (~cer) begin // @[PC_reg.scala 24:29]
      pcr <= 32'h0; // @[PC_reg.scala 25:9]
    end else begin
      pcr <= _pcr_T_1; // @[PC_reg.scala 27:9]
    end
    if (reset) begin // @[PC_reg.scala 19:36]
      cer <= 1'h0; // @[PC_reg.scala 20:9]
    end else begin
      cer <= 1'h1; // @[PC_reg.scala 22:9]
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
  pcr = _RAND_0[31:0];
  _RAND_1 = {1{`RANDOM}};
  cer = _RAND_1[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
