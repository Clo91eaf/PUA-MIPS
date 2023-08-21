TEST_TOP=../../PUA-MIPS-TEST
CPU_DIR=./src/main/scala/cpu
PUA_MIPS=./generated/PuaMips.v
MYCPU_TOP=./src/main/resources/mycpu_top.v
SCORES_FILE=../SCORES.md
OUTPUT=$(TEST_TOP)/verilator/output.txt

verilog:
	@echo "make verilog"
	find ./generated ! -name 'mycpu_top.v' -type f -delete
	sbt 'runMain Elaborate'


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

test:
	@echo "make test"
	find ./generated ! -name 'mycpu_top.v' -type f -delete
	sbt "Test / runMain cpu.testMain"

define TASK
	sbt "runMain Elaborate $(1)"
	mv ./generated/PuaMips.v ./tmp/PuaMips_$(1).v
endef

tmp/BranchPredictorUnit_$(BHT_DEPTH)_$(PHT_DEPTH).v: src/main/scala/cpu/pipeline/fetch/BranchPredictorConfig.scala
	@echo 'package cpu.pipeline.fetch' > $<
	@echo 'case class BranchPredictorConfig(' >> $<
	@echo 'val bhtDepth: Int = $(BHT_DEPTH),' >> $<
	@echo 'val phtDepth: Int = $(PHT_DEPTH),' >> $<
	@echo ')' >> $<
	sbt 'runMain Elaborate'
	cp ./generated/BranchPredictorUnit.v $@

loop:tmp/BranchPredictorUnit_$(BHT_DEPTH)_$(PHT_DEPTH).v

# Makefile targets
.PHONY: get run wave