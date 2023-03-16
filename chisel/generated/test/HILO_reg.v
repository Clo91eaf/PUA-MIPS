module HILO_reg(
  input         clock,
  input         reset,
  input         io_we,
  input  [31:0] io_hi_i,
  input  [31:0] io_lo_i,
  output [31:0] io_hi_o,
  output [31:0] io_lo_o
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
`endif // RANDOMIZE_REG_INIT
  reg [31:0] hi_or; // @[HILO_reg.scala 20:18]
  reg [31:0] lo_or; // @[HILO_reg.scala 21:18]
  assign io_hi_o = hi_or; // @[HILO_reg.scala 23:11]
  assign io_lo_o = lo_or; // @[HILO_reg.scala 24:11]
  always @(posedge clock) begin
    if (reset) begin // @[HILO_reg.scala 26:36]
      hi_or <= 32'h0; // @[HILO_reg.scala 27:11]
    end else if (io_we) begin // @[HILO_reg.scala 29:37]
      hi_or <= io_hi_i; // @[HILO_reg.scala 30:11]
    end
    if (reset) begin // @[HILO_reg.scala 26:36]
      lo_or <= 32'h0; // @[HILO_reg.scala 28:11]
    end else if (io_we) begin // @[HILO_reg.scala 29:37]
      lo_or <= io_lo_i; // @[HILO_reg.scala 31:11]
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
  hi_or = _RAND_0[31:0];
  _RAND_1 = {1{`RANDOM}};
  lo_or = _RAND_1[31:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
