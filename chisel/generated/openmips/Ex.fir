circuit Ex :
  module Ex :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip aluop_i : UInt<8>, flip alusel_i : UInt<3>, flip reg1_i : UInt<32>, flip reg2_i : UInt<32>, flip wd_i : UInt<5>, flip wreg_i : UInt<1>, wd_o : UInt<5>, wreg_o : UInt<1>, wdata_o : UInt<32>}

    reg wd_or : UInt<5>, clock with :
      reset => (UInt<1>("h0"), wd_or) @[Ex.scala 23:18]
    reg wreg_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wreg_or) @[Ex.scala 24:20]
    reg wdata_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), wdata_or) @[Ex.scala 25:21]
    io.wd_o <= wd_or @[Ex.scala 27:11]
    io.wreg_o <= wreg_or @[Ex.scala 28:13]
    io.wdata_o <= wdata_or @[Ex.scala 29:14]
    reg logicout : UInt<32>, clock with :
      reset => (UInt<1>("h0"), logicout) @[Ex.scala 32:21]
    reg shiftres : UInt<32>, clock with :
      reset => (UInt<1>("h0"), shiftres) @[Ex.scala 33:21]
    node _T = bits(reset, 0, 0) @[Ex.scala 38:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Ex.scala 38:21]
    when _T_1 : @[Ex.scala 38:36]
      logicout <= UInt<32>("h0") @[Ex.scala 39:14]
    else :
      logicout <= UInt<32>("h0") @[Ex.scala 55:18]
    node _T_2 = bits(reset, 0, 0) @[Ex.scala 61:14]
    node _T_3 = eq(_T_2, UInt<1>("h1")) @[Ex.scala 61:21]
    when _T_3 : @[Ex.scala 61:36]
      shiftres <= UInt<32>("h0") @[Ex.scala 62:14]
    else :
      shiftres <= UInt<32>("h0") @[Ex.scala 75:18]
    wd_or <= io.wd_i @[Ex.scala 80:9]
    wreg_or <= io.wreg_i @[Ex.scala 81:11]
    wdata_or <= UInt<32>("h0") @[Ex.scala 86:36]

