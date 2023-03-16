circuit Wb :
  module Wb :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip ex_wd : UInt<5>, flip ex_wreg : UInt<1>, flip ex_wdata : UInt<32>, flip ex_hi : UInt<32>, flip ex_lo : UInt<32>, flip ex_whilo : UInt<1>, wb_wd : UInt<5>, wb_wreg : UInt<1>, wb_wdata : UInt<32>, wb_hi : UInt<32>, wb_lo : UInt<32>, wb_whilo : UInt<1>}

    reg wb_wregr : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wb_wregr) @[Wb.scala 25:21]
    reg wb_hir : UInt<32>, clock with :
      reset => (UInt<1>("h0"), wb_hir) @[Wb.scala 26:19]
    reg wb_lor : UInt<32>, clock with :
      reset => (UInt<1>("h0"), wb_lor) @[Wb.scala 27:19]
    reg wb_whilor : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wb_whilor) @[Wb.scala 28:22]
    io.wb_wreg <= wb_wregr @[Wb.scala 29:14]
    io.wb_hi <= wb_hir @[Wb.scala 30:12]
    io.wb_lo <= wb_lor @[Wb.scala 31:12]
    io.wb_whilo <= wb_whilor @[Wb.scala 32:15]
    node _T = bits(reset, 0, 0) @[Wb.scala 34:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Wb.scala 34:21]
    when _T_1 : @[Wb.scala 34:36]
      io.wb_wd <= UInt<5>("h0") @[Wb.scala 35:14]
      wb_wregr <= UInt<1>("h0") @[Wb.scala 36:14]
      io.wb_wdata <= UInt<1>("h0") @[Wb.scala 37:17]
      wb_hir <= UInt<1>("h0") @[Wb.scala 38:12]
      wb_lor <= UInt<1>("h0") @[Wb.scala 39:12]
      wb_whilor <= UInt<1>("h0") @[Wb.scala 40:15]
    else :
      io.wb_wd <= io.ex_wd @[Wb.scala 42:14]
      wb_wregr <= io.ex_wreg @[Wb.scala 43:14]
      io.wb_wdata <= io.ex_wdata @[Wb.scala 44:17]
      wb_hir <= io.ex_hi @[Wb.scala 45:12]
      wb_lor <= io.ex_lo @[Wb.scala 46:12]
      wb_whilor <= io.ex_whilo @[Wb.scala 47:15]

