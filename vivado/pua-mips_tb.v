`timescale 1ns / 1ps

module tb_top();
reg reset;
reg clock;

initial
begin
    clock = 1'b0;
    reset = 1'b0;
    #2000;
    reset = 1'b1;
end
always #5 clock=~clock;

PuaMips puamips(
  .reset (reset),
  .clock (clock)
);


endmodule