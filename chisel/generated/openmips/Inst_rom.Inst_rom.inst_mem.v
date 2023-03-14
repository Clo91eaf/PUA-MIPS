module BindsTo_0_Inst_rom(
  input         clock,
  input         reset,
  input         io_ce,
  input  [31:0] io_addr,
  output [31:0] io_inst
);

initial begin
  $readmemh("inst_rom.data", Inst_rom.inst_mem);
end
                      endmodule

bind Inst_rom BindsTo_0_Inst_rom BindsTo_0_Inst_rom_Inst(.*);