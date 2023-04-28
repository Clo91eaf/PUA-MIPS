# vivado -mode batch -nojournal -nolog -source \\f\\NSCSCC\\PUA-MIPS\\chisel\\src\\utils\\tcl\\get_trace.tcl -tclargs func_test

set test_top [lindex $argv 0]

set cpu132_gettrace "$test_top\\cpu132_gettrace\\run_vivado\\cpu132_gettrace"
set tb_top "$cpu132_gettrace\\cpu132_gettrace.sim\\sim_1\\behav\\xsim\\tb_top.tcl"

# 打开 Vivado 项目
open_project $cpu132_gettrace\\cpu132_gettrace.xpr
update_compile_order -fileset sources_1
launch_simulation
source $tb_top
run all
close_sim