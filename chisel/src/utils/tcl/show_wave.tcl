set test_top [lindex $argv 0]
start_gui
open_wave_database ${test_top}\\mycpu_sram_verify\\run_vivado\\PUA-MIPS\\mycpu.sim\\sim_1\\behav\\xsim\\tb_top_behav.wdb
open_wave_config ${test_top}\\mycpu_sram_verify\\run_vivado\\PUA-MIPS\\tb_top_behav.wcfg