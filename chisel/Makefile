TCL = src/utils/tcl/tcl.sh
PY = src/utils/python
PYSCRIPT = calculater.py
EXP = "1 + 2"

verilog:
	sbt 'runMain Elaborate'

run:
	sh $(TCL) run

wave:
	sh $(TCL) wave

# python script
python:
	@python $(PY)/$(PYSCRIPT) $(EXP)
