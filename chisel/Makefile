TEST_TOP=../../PUA-MIPS-TEST
CPU_DIR=./src/main/scala/cpu
PUA_MIPS=./generated/PuaMips.v
MYCPU_TOP=./src/main/resources/mycpu_top.v
SCORES_FILE=../SCORES.md
OUTPUT=$(TEST_TOP)/verilator/output.txt

verilog:
	@echo "make verilog"
	sbt 'runMain Elaborate'
	cp $(PUA_MIPS) $(TEST_TOP)/mycpu/
	cp $(MYCPU_TOP) $(TEST_TOP)/mycpu/
	$(MAKE) -C $(TEST_TOP)/verilator/ 

# run trace target
func:
	$(MAKE) -C $(TEST_TOP)/verilator/ func

perf:
	$(MAKE) -C $(TEST_TOP)/verilator/ perf

score:
ifeq ($(strip $(MESSAGE)),)
	@echo "message is empty"
else
	@echo "\
	|$(shell date +"%Y-%m-%d")\
	|$(shell grep IPC $(OUTPUT) | grep -oP '\d+\.\d+')\
	|$(shell grep scores $(OUTPUT)	| grep -oP '\d+\.\d+')\
	|$(MESSAGE)\
	|$(shell grep -oE '^[0-9]+\.[0-9]{3}' $(OUTPUT) | tr '\n' '|')\
	" | tee -a $(SCORES_FILE)
endif

perfdiff:
	$(MAKE) -C $(TEST_TOP)/verilator/ perfdiff

count:
	@echo "count the lines"
	find . -name "*.scala" | xargs wc -l

count_commit:
	@echo "count the commits"
	git log --format=oneline | wc -l

# Makefile targets
.PHONY: get run wave