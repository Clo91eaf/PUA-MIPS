module Wb(
  input         clock,
  input         reset,
  input  [4:0]  io_ex_wd,
  input         io_ex_wreg,
  input  [31:0] io_ex_wdata,
  input  [31:0] io_ex_hi,
  input  [31:0] io_ex_lo,
  input         io_ex_whilo,
  output [4:0]  io_wb_wd,
  output        io_wb_wreg,
  output [31:0] io_wb_wdata,
  output [31:0] io_wb_hi,
  output [31:0] io_wb_lo,
  output        io_wb_whilo
);
`ifdef RANDOMIZE_REG_INIT
  reg [31:0] _RAND_0;
  reg [31:0] _RAND_1;
  reg [31:0] _RAND_2;
  reg [31:0] _RAND_3;
`endif // RANDOMIZE_REG_INIT
  reg  wb_wregr; // @[Wb.scala 25:21]
  reg [31:0] wb_hir; // @[Wb.scala 26:19]
  reg [31:0] wb_lor; // @[Wb.scala 27:19]
  reg  wb_whilor; // @[Wb.scala 28:22]
  assign io_wb_wd = reset ? 5'h0 : io_ex_wd; // @[Wb.scala 34:36 35:14 42:14]
  assign io_wb_wreg = wb_wregr; // @[Wb.scala 29:14]
  assign io_wb_wdata = reset ? 32'h0 : io_ex_wdata; // @[Wb.scala 34:36 37:17 44:17]
  assign io_wb_hi = wb_hir; // @[Wb.scala 30:12]
  assign io_wb_lo = wb_lor; // @[Wb.scala 31:12]
  assign io_wb_whilo = wb_whilor; // @[Wb.scala 32:15]
  always @(posedge clock) begin
    if (reset) begin // @[Wb.scala 34:36]
      wb_wregr <= 1'h0; // @[Wb.scala 36:14]
    end else begin
      wb_wregr <= io_ex_wreg; // @[Wb.scala 43:14]
    end
    if (reset) begin // @[Wb.scala 34:36]
      wb_hir <= 32'h0; // @[Wb.scala 38:12]
    end else begin
      wb_hir <= io_ex_hi; // @[Wb.scala 45:12]
    end
    if (reset) begin // @[Wb.scala 34:36]
      wb_lor <= 32'h0; // @[Wb.scala 39:12]
    end else begin
      wb_lor <= io_ex_lo; // @[Wb.scala 46:12]
    end
    if (reset) begin // @[Wb.scala 34:36]
      wb_whilor <= 1'h0; // @[Wb.scala 40:15]
    end else begin
      wb_whilor <= io_ex_whilo; // @[Wb.scala 47:15]
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
  _RAND_1 = {1{`RANDOM}};
  wb_hir = _RAND_1[31:0];
  _RAND_2 = {1{`RANDOM}};
  wb_lor = _RAND_2[31:0];
  _RAND_3 = {1{`RANDOM}};
  wb_whilor = _RAND_3[0:0];
`endif // RANDOMIZE_REG_INIT
  `endif // RANDOMIZE
end // initial
`ifdef FIRRTL_AFTER_INITIAL
`FIRRTL_AFTER_INITIAL
`endif
`endif // SYNTHESIS
endmodule
