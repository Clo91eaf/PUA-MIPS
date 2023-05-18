PY=src\\utils\\python
PYSCRIPT=calculater.py
EXP="1 + 2"

TEST_TOP=../../PUA-MIPS-TEST/
CPU_DIR=./src/main/scala/cpu
PUA_MIPS=./generated/PuaMips.v
MYCPU_TOP=./src/main/resources/mycpu_top.v

verilog:
	@echo "make verilog"
	sbt 'runMain Elaborate'
	cp $(PUA_MIPS) $(TEST_TOP)/mycpu/

# python script
python:
	@python $(PY)/$(PYSCRIPT) $(EXP)

# run trace target
run: verilog
	@echo "run trace"
	vivado -mode batch -nojournal -nolog -source $(tcl_top)\\run_trace.tcl -tclargs $(test_top)

count:
	@echo "count the lines"
	find . -name "*.scala" | xargs wc -l

count_commit:
	@echo "count the commits"
	git log --format=oneline | wc -l

# Makefile targets
.PHONY: get run wave