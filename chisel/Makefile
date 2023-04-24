PY=src/utils/python
PYSCRIPT=calculater.py
EXP="1 + 2"

test_top=../vivado/CPU_CDE_SRAM
tcl_top=./src/utils/tcl
cpu_dir=src/main/scala/cpu
puamips=F:\\NSCSCC\\PUA-MIPS\\chisel\\generated\\PuaMips.v
mycpu_top=F:\\NSCSCC\\PUA-MIPS\\chisel\\src\\main\\resources\\mycpu_top.v

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
	cp -r $(test_top)/soft_src/func_$(func) $(test_top)/soft/func_lab9
	vivado -mode batch -nojournal -nolog -source $(tcl_top)/get_trace.tcl -tclargs $(test_top) func_$(func)

# run trace target
run: verilog
	@echo "run trace"
	vivado -mode batch -nojournal -nolog -source $(tcl_top)/run_trace.tcl -tclargs $(test_top)

# wave target
wave:
	@echo "get wave"
	rm -rf *.str
	vivado -mode gui -nojournal -nolog -source $(tcl_top)/show_wave.tcl

count:
	@echo "count the lines"
	find . -name "*.scala" | xargs wc -l

count_commit:
	@echo "count the commits"
	git log --format=oneline | wc -l
