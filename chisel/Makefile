TEST_TOP=../../PUA-MIPS-TEST
CPU_DIR=./src/main/scala/cpu
PUA_MIPS=./generated/PuaMips.v

verilog:
	@echo "make verilog"
	find ./generated ! -name 'mycpu_top.v' -type f -delete
	sbt 'runMain Elaborate'

# run trace target
func:
	$(MAKE) -C $(TEST_TOP)/verilator/

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