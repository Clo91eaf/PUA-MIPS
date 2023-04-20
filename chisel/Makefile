PY=src/utils/python
PYSCRIPT=calculater.py
EXP="1 + 2"

test_top=F:\NSCSCC\func_test_v0.01
test_source=func_test
tcl_top=src/utils/tcl
cpu_dir=src/main/scala/cpu
func=lab7

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
	vivado -mode batch -nojournal -nolog -source $(tcl_top)/get_trace.tcl -tclargs $(test_top) func_$(func)

# run trace target
run: verilog
	@echo "run trace"
	vivado -mode batch -nojournal -nolog -source $(tcl_top)/run_trace.tcl -tclargs $(test_top)

# wave target
wave:
	@echo "get wave"
	vivado -mode gui -nojournal -nolog -source $(tcl_top)/show_wave.tcl -tclargs $(test_top)

count:
	@echo "count the lines"
	find . -name "*.scala" | xargs wc -l

count_commit:
	@echo "count the commits"
	git log --format=oneline | wc -l
