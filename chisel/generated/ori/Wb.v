module Wb(
  input         clock,
  input         reset,
  input  [4:0]  io_ex_wd,
  input         io_ex_wreg,
  input  [31:0] io_ex_wdata,
  output [4:0]  io_wb_wd,
  output        io_wb_wreg,
  output [31:0] io_wb_wdata
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
`endif // RANDOMIZE_REG_INIT
  reg  wb_wregr; // @[Wb.scala 20:21]
  assign io_wb_wd = reset ? 5'h0 : io_ex_wd; // @[Wb.scala 23:36 24:14 28:14]
  assign io_wb_wreg = wb_wregr; // @[Wb.scala 21:14]
  assign io_wb_wdata = reset ? 32'h0 : io_ex_wdata; // @[Wb.scala 23:36 26:17 30:17]
  always @(posedge clock) begin
    if (reset) begin // @[Wb.scala 23:36]
      wb_wregr <= 1'h0; // @[Wb.scala 25:14]
    end else begin
      wb_wregr <= io_ex_wreg; // @[Wb.scala 29:14]
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
  wb_wregr = _RAND_0[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
