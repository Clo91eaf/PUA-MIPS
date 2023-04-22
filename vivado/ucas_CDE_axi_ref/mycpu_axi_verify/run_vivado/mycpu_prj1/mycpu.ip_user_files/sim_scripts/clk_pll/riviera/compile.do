vlib work
vlib riviera

vlib riviera/xpm
vlib riviera/xil_defaultlib

vmap xpm riviera/xpm
vmap xil_defaultlib riviera/xil_defaultlib

vlog -work xpm  -sv2k12 "+incdir+../../../ipstatic" \
"F:/vivado/Vivado/2019.2/data/ip/xpm/xpm_cdc/hdl/xpm_cdc.sv" \
"F:/vivado/Vivado/2019.2/data/ip/xpm/xpm_memory/hdl/xpm_memory.sv" \

vcom -work xpm -93 \
"F:/vivado/Vivado/2019.2/data/ip/xpm/xpm_VCOMP.vhd" \

vlog -work xil_defaultlib  -v2k5 "+incdir+../../../ipstatic" \
"../../../../../../../../CPU_CDE_SRAM/mycpu_sram_verify/rtl/xilinx_ip/clk_pll/clk_pll_clk_wiz.v" \
"../../../../../../../../CPU_CDE_SRAM/mycpu_sram_verify/rtl/xilinx_ip/clk_pll/clk_pll.v" \

vlog -work xil_defaultlib \
"glbl.v"

