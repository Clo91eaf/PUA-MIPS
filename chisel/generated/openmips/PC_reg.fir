circuit PC_reg :
  module PC_reg :
    input clock : Clock
    input reset : UInt<1>
    output io : { pc : UInt<32>, ce : UInt<1>}

    reg pcr : UInt<32>, clock with :
      reset => (UInt<1>("h0"), pcr) @[PC_reg.scala 13:16]
    reg cer : UInt<1>, clock with :
      reset => (UInt<1>("h0"), cer) @[PC_reg.scala 14:16]
    io.pc <= pcr @[PC_reg.scala 16:9]
    io.ce <= cer @[PC_reg.scala 17:9]
    node _T = bits(reset, 0, 0) @[PC_reg.scala 19:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[PC_reg.scala 19:21]
    when _T_1 : @[PC_reg.scala 19:36]
      cer <= UInt<1>("h0") @[PC_reg.scala 20:9]
    else :
      cer <= UInt<1>("h1") @[PC_reg.scala 22:9]
    node _T_2 = eq(cer, UInt<1>("h0")) @[PC_reg.scala 24:12]
    when _T_2 : @[PC_reg.scala 24:29]
      pcr <= UInt<1>("h0") @[PC_reg.scala 25:9]
    else :
      node _pcr_T = add(pcr, UInt<3>("h4")) @[PC_reg.scala 27:16]
      node _pcr_T_1 = tail(_pcr_T, 1) @[PC_reg.scala 27:16]
      pcr <= _pcr_T_1 @[PC_reg.scala 27:9]

