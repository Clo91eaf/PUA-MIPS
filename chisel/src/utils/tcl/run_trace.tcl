set test_top [lindex $argv 0]

set ip_top "$test_top\\mycpu_sram_verify\\rtl\\xilinx_ip"
set tb_top "$test_top\\mycpu_sram_verify\\run_vivado\\PUA-MIPS\\mycpu.sim\\sim_1\\behav\\xsim\\tb_top.tcl"
set verify_top "$test_top\\mycpu_sram_verify\\run_vivado\\PUA-MIPS"

# 打开 Vivado 项目
open_project $verify_top\\mycpu.xpr
update_compile_order -fileset sources_1

generate_target all [get_files $ip_top\\inst_ram\\inst_ram.xci]

export_ip_user_files -of_objects [get_files $ip_top\\inst_ram\\inst_ram.xci] -no_script -sync -force -quiet
export_simulation -of_objects [get_files $ip_top\\inst_ram\\inst_ram.xci] -directory $verify_top\\mycpu.ip_user_files\\sim_scripts -ip_user_files_dir $verify_top\\mycpu.ip_user_files -ipstatic_source_dir $verify_top\\mycpu.ip_user_files\\ipstatic -lib_map_path [list {modelsim=$verify_top\\mycpu.cache\\compile_simlib\\modelsim} {questa=$verify_top\\mycpu.cache\\compile_simlib\\questa} {riviera=$verify_top\\mycpu.cache\\compile_simlib\\riviera} {activehdl=$verify_top\\mycpu.cache\\compile_simlib\\activehdl}] -force -quiet

reset_run inst_ram_synth_1

launch_runs -jobs 1 inst_ram_synth_1

wait_on_run inst_ram_synth_1

# 启动仿真
launch_simulation
source $tb_top
run all
close_sim