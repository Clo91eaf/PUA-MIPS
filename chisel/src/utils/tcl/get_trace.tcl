# vivado -mode batch -nojournal -nolog -source /f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl/get_trace.tcl -tclargs func_test

set test_top [lindex $argv 0]
set filename [lindex $argv 1]
set soft_file "$test_top/soft"
set project_path "$test_top/cpu132_gettrace/run_vivado/cpu132_gettrace"
set tb_top "$project_path/cpu132_gettrace.sim/sim_1/behav/xsim/tb_top.tcl"

# 打开 Vivado 项目
open_project $project_path/cpu132_gettrace.xpr
update_compile_order -fileset sources_1

# 启动仿真
launch_simulation
source $tb_top 
run all
close_sim

puts "$filename golden trace get!!!"