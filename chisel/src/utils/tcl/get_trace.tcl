# vivado -mode batch -nojournal -nolog -source /f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl/get_trace.tcl -tclargs func_test

set test_top [lindex $argv 0]
set filename [lindex $argv 1]
set soft_source_file "$test_top/soft_source"
set soft_file "$test_top/soft"
set project_path "$test_top/cpu132_gettrace/run_vivado/cpu132_gettrace"
set tb_top "$project_path/cpu132_gettrace.sim/sim_1/behav/xsim/tb_top.tcl"

# 设置要仿真的文件夹:func_test / func_lab3 / func_lab4

# 拷贝到soft底下
if {[file exists $soft_file/func]} {
    file delete -force $soft_file/func
}
file copy "$soft_source_file/$filename" "$soft_file/func" 

# 打开 Vivado 项目
open_project $project_path/cpu132_gettrace.xpr

# 启动仿真
launch_simulation
source $tb_top 
run all
close_sim

puts "$filename golden trace get!!!"