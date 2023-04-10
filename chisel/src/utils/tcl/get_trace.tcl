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
update_compile_order -fileset sources_1
generate_target Simulation [get_files $test_top/cpu132_gettrace/rtl/xilinx_ip/inst_ram/inst_ram.xci]
export_ip_user_files -of_objects [get_files $test_top/cpu132_gettrace/rtl/xilinx_ip/inst_ram/inst_ram.xci] -no_script -sync -force -quiet
export_simulation -of_objects [get_files $test_top/cpu132_gettrace/rtl/xilinx_ip/inst_ram/inst_ram.xci] -directory $test_top/cpu132_gettrace/run_vivado/cpu132_gettrace/cpu132_gettrace.ip_user_files/sim_scripts -ip_user_files_dir $test_top/cpu132_gettrace/run_vivado/cpu132_gettrace/cpu132_gettrace.ip_user_files -ipstatic_source_dir $test_top/cpu132_gettrace/run_vivado/cpu132_gettrace/cpu132_gettrace.ip_user_files/ipstatic -lib_map_path [list {modelsim=F:/../../Modeltech_pe_edu_10.4a/xilinx_lib} {questa=$test_top/cpu132_gettrace/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/questa} {riviera=$test_top/cpu132_gettrace/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/riviera} {activehdl=$test_top/cpu132_gettrace/run_vivado/cpu132_gettrace/cpu132_gettrace.cache/compile_simlib/activehdl}] -use_ip_compiled_libs -force -quiet

# 启动仿真
launch_simulation
source $tb_top 
run all
close_sim

puts "$filename golden trace get!!!"