`timescale 1ns / 1ps

module tb_top();
reg reset;
reg clock;
wire [31:0] io_debug_pc;
wire [31:0] io_debug_wdata;
wire [4:0]  io_debug_waddr;
wire        io_debug_wen;

initial
begin
    clock = 1'b0;
    reset = 1'b0;
    #20;
    reset = 1'b1;
end
always #5 clock=~clock;

PuaMips puamips(
  .reset (reset),
  .clock (clock),
  .io_debug_pc (io_debug_pc),
  .io_debug_wdata (io_debug_wdata),
  .io_debug_waddr (io_debug_waddr),
  .io_debug_wen (io_debug_wen)
);


endmodule