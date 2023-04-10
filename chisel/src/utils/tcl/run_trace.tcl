# vivado -mode batch -nojournal -nolog -source /f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl/run_trace.tcl -tclargs func_test

set test_top [lindex $argv 0]
set soft_source "$test_top/soft_source"
set soft "$test_top/soft"
set inst_ram "$test_top/soc_sram_func/rtl/xilinx_ip/inst_ram/inst_ram.xci"
set project_top "$test_top/soc_sram_func/run_vivado/mycpu_prj1"
set tb_top "$project_top/mycpu.sim/sim_1/behav/xsim/tb_top.tcl"

# 设置要仿真的文件夹:func_test / func_lab3 / func_lab4

# 打开 Vivado 项目
open_project $project_top/mycpu.xpr
update_compile_order -fileset sources_1
generate_target Simulation [get_files $inst_ram]
export_ip_user_files -of_objects [get_files $inst_ram] -no_script -sync -force -quiet
export_simulation -of_objects [get_files $inst_ram] -directory $project_top/mycpu.ip_user_files/sim_scripts -ip_user_files_dir $project_top/mycpu.ip_user_files -ipstatic_source_dir $project_top/mycpu.ip_user_files/ipstatic -lib_map_path [list {modelsim=F:/../../Modeltech_pe_edu_10.4a/xilinx_lib} {questa=$project_top/mycpu.cache/compile_simlib/questa} {riviera=$project_top/mycpu.cache/compile_simlib/riviera} {activehdl=$project_top/mycpu.cache/compile_simlib/activehdl}] -use_ip_compiled_libs -force -quiet

# 启动仿真
launch_simulation
source $tb_top 
run all
close_sim