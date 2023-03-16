circuit HILO_reg :
  module HILO_reg :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip we : UInt<1>, flip hi_i : UInt<32>, flip lo_i : UInt<32>, hi_o : UInt<32>, lo_o : UInt<32>}

    reg hi_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), hi_or) @[HILO_reg.scala 20:18]
    reg lo_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), lo_or) @[HILO_reg.scala 21:18]
    io.hi_o <= hi_or @[HILO_reg.scala 23:11]
    io.lo_o <= lo_or @[HILO_reg.scala 24:11]
    node _T = bits(reset, 0, 0) @[HILO_reg.scala 26:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[HILO_reg.scala 26:21]
    when _T_1 : @[HILO_reg.scala 26:36]
      hi_or <= UInt<32>("h0") @[HILO_reg.scala 27:11]
      lo_or <= UInt<32>("h0") @[HILO_reg.scala 28:11]
    else :
      node _T_2 = eq(io.we, UInt<1>("h1")) @[HILO_reg.scala 29:20]
      when _T_2 : @[HILO_reg.scala 29:37]
        hi_or <= io.hi_i @[HILO_reg.scala 30:11]
        lo_or <= io.lo_i @[HILO_reg.scala 31:11]

