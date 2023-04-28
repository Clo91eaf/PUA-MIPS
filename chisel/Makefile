PY=src\\utils\\python
PYSCRIPT=calculater.py
EXP="1 + 2"

test_top=../../PUA-MIPS-TEST/CPU_CDE_SRAM
tcl_top=.\\src\\utils\\tcl
cpu_dir=.\\src\\main\\scala\\cpu
puamips=.\\generated\\PuaMips.v
mycpu_top=.\\src\\main\\resources\\mycpu_top.v

func=lab9

verilog: $(shell find $(cpu_dir) -name '*.scala')
	@echo "make verilog"
	sbt 'runMain Elaborate'

# python script
python:
	@python $(PY)/$(PYSCRIPT) $(EXP)

# Makefile targets
.PHONY: get run wave

# get trace target
get:
	@echo "get trace"
	rm -rf $(test_top)/soft/func_lab9
	cp -r $(test_top)/soft_source/func_$(func) $(test_top)/soft/func_lab9
	vivado -mode batch -nojournal -nolog -source $(tcl_top)\\get_trace.tcl -tclargs $(test_top)
	@echo "$(func) trace get!!!"

# run trace target
run: verilog
	@echo "run trace"
	vivado -mode batch -nojournal -nolog -source $(tcl_top)\\run_trace.tcl -tclargs $(test_top)

# wave target
wave:
	@echo "get wave"
	vivado -mode gui -nojournal -nolog -source $(tcl_top)\\show_wave.tcl -tclargs $(test_top)

example:
	@echo "get example wave"
	vivado -mode gui -nojournal -nolog -source $(tcl_top)\\show_example.tcl -tclargs $(test_top)

count:
	@echo "count the lines"
	find . -name "*.scala" | xargs wc -l

count_commit:
	@echo "count the commits"
	git log --format=oneline | wc -l
