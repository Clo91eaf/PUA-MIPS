# config your tcl script here.
test_top="/f/NSCSCC/func_test_v0.01"
test_source="func_test"
tcl_top="/f/NSCSCC/PUA-MIPS/chisel/src/utils/tcl"

while [ $# -gt 0 ]; do
  case "$1" in
    # get trace
    get)
      echo "get trace"
      vivado -mode batch -nojournal -nolog -source $tcl_top/get_trace.tcl -tclargs $test_top $test_source
      ;;
    # run trace
    run)
      echo "run trace"
      make verilog && vivado -mode batch -nojournal -nolog -source $tcl_top/run_trace.tcl -tclargs $test_top
      ;;
    wave)
      echo "get wave"
      vivado -mode gui -nojournal -nolog -source $tcl_top/show_wave.tcl -tclargs $test_top
      ;;
    *)
      echo "Invalid argument: $1"
      exit 1
      ;;
  esac
  shift
done
