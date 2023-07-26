TEST_TOP=../../PUA-MIPS-TEST
CPU_DIR=./src/main/scala/cpu
PUA_MIPS=./generated/PuaMips.v

verilog:
	@echo "make verilog"
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
	rm -rf ./generated/*
	sbt "Test / runMain cpu.testMain"
	cp ./generated/Core.v ../../CDIM/mycpu/0core

# Makefile targets
.PHONY: get run wave