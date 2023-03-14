circuit Wb :
  module Wb :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip ex_wd : UInt<5>, flip ex_wreg : UInt<1>, flip ex_wdata : UInt<32>, wb_wd : UInt<5>, wb_wreg : UInt<1>, wb_wdata : UInt<32>}

    reg wb_wregr : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wb_wregr) @[Wb.scala 20:21]
    io.wb_wreg <= wb_wregr @[Wb.scala 21:14]
    node _T = bits(reset, 0, 0) @[Wb.scala 23:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Wb.scala 23:21]
    when _T_1 : @[Wb.scala 23:36]
      io.wb_wd <= UInt<5>("h0") @[Wb.scala 24:14]
      wb_wregr <= UInt<1>("h0") @[Wb.scala 25:14]
      io.wb_wdata <= UInt<32>("h0") @[Wb.scala 26:17]
    else :
      io.wb_wd <= io.ex_wd @[Wb.scala 28:14]
      wb_wregr <= io.ex_wreg @[Wb.scala 29:14]
      io.wb_wdata <= io.ex_wdata @[Wb.scala 30:17]

