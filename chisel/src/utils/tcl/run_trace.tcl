# vivado -mode batch -nojournal -nolog -source /f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl/run_trace.tcl -tclargs func_test

set test_top [lindex $argv 0]
set soft_source "$test_top/soft_source"
set soft "$test_top/soft"
set project_top "$test_top/soc_sram_func/run_vivado/mycpu_prj1"
set tb_top "$project_top/mycpu.sim/sim_1/behav/xsim/tb_top.tcl"

# 设置要仿真的文件夹:func_test / func_lab3 / func_lab4

# 打开 Vivado 项目
open_project $project_top/mycpu.xpr

# 启动仿真
launch_simulation
source $tb_top 
run all
close_sim

puts "$filename run trace get!!!"