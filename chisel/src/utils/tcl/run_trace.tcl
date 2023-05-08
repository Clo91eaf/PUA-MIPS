set test_top [lindex $argv 0]

set ip_top "$test_top\\mycpu_axi_verify\\rtl\\xilinx_ip"
set tb_top "$test_top\\mycpu_axi_verify\\run_vivado\\PUA-MIPS\\mycpu.sim\\sim_1\\behav\\xsim\\tb_top.tcl"
set verify_top "$test_top\\mycpu_axi_verify\\run_vivado\\PUA-MIPS"

# 打开 Vivado 项目
open_project $verify_top\\mycpu.xpr
update_compile_order -fileset sources_1

# 启动仿真
launch_simulation
source $tb_top
run 100000ns
# run all
close_sim