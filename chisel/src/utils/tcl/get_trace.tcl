# vivado -mode batch -nojournal -nolog -source /f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl/get_trace.tcl -tclargs func_test

set test_top [lindex $argv 0]
set project_path "$test_top/cpu132_gettrace/run_vivado/cpu132_gettrace"
set tb_top "$project_path/cpu132_gettrace.sim/sim_1/behav/xsim/tb_top.tcl"

# 打开 Vivado 项目
open_project $project_path/cpu132_gettrace.xpr
update_compile_order -fileset sources_1
generate_target Simulation [get_files $project_path/rtl/xilinx_ip/inst_ram/inst_ram.xci]
export_ip_user_files -of_objects [get_files $project_path/rtl/xilinx_ip/inst_ram/inst_ram.xci] -no_script -sync -force -quiet
export_simulation -of_objects [get_files $project_path/rtl/xilinx_ip/inst_ram/inst_ram.xci] -directory $project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.ip_user_files/sim_scripts -ip_user_files_dir $project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.ip_user_files -ipstatic_source_dir $project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.ip_user_files/ipstatic -lib_map_path [list {modelsim=$project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/modelsim} {questa=$project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/questa} {riviera=$project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/riviera} {activehdl=$project_path/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/activehdl}] -force -quiet

# 启动仿真
launch_simulation
source $tb_top 
run all
close_sim