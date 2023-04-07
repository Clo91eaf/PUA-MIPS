# vivado -mode batch -source /f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl/get_trace.tcl -tclargs func_test

set soft_source_file "F:/NSCSCC/func_test_v0.01/soft_source"
set filename [lindex $argv 0]
set soft_file "F:/NSCSCC/func_test_v0.01/soft"
set project_path "F:/NSCSCC/func_test_v0.01/cpu132_gettrace/run_vivado/cpu132_gettrace"
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