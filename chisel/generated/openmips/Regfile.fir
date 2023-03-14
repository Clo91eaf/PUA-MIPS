circuit Regfile :
  module Regfile :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip we : UInt<1>, flip waddr : UInt<5>, flip wdata : UInt<32>, flip re1 : UInt<1>, flip raddr1 : UInt<5>, flip re2 : UInt<1>, flip raddr2 : UInt<5>, rdata1 : UInt<32>, rdata2 : UInt<32>}

    reg rdata1r : UInt<32>, clock with :
      reset => (UInt<1>("h0"), rdata1r) @[Regfile.scala 22:20]
    reg rdata2r : UInt<32>, clock with :
      reset => (UInt<1>("h0"), rdata2r) @[Regfile.scala 23:20]
    io.rdata1 <= rdata1r @[Regfile.scala 25:13]
    io.rdata2 <= rdata2r @[Regfile.scala 26:13]
    wire _regs_WIRE : UInt<32>[32] @[Regfile.scala 29:29]
    _regs_WIRE[0] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[1] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[2] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[3] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[4] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[5] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[6] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[7] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[8] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[9] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[10] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[11] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[12] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[13] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[14] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[15] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[16] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[17] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[18] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[19] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[20] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[21] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[22] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[23] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[24] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[25] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[26] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[27] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[28] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[29] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[30] <= UInt<32>("h0") @[Regfile.scala 29:29]
    _regs_WIRE[31] <= UInt<32>("h0") @[Regfile.scala 29:29]
    reg regs : UInt<32>[32], clock with :
      reset => (reset, _regs_WIRE) @[Regfile.scala 29:21]
    node _T = bits(reset, 0, 0) @[Regfile.scala 31:14]
    node _T_1 = eq(_T, UInt<1>("h0")) @[Regfile.scala 31:21]
    when _T_1 : @[Regfile.scala 31:37]
      node _T_2 = eq(io.we, UInt<1>("h1")) @[Regfile.scala 32:16]
      node _T_3 = neq(io.waddr, UInt<1>("h0")) @[Regfile.scala 32:44]
      node _T_4 = and(_T_2, _T_3) @[Regfile.scala 32:32]
      when _T_4 : @[Regfile.scala 32:53]
        regs[io.waddr] <= io.wdata @[Regfile.scala 33:22]
    node _T_5 = bits(reset, 0, 0) @[Regfile.scala 36:14]
    node _T_6 = eq(_T_5, UInt<1>("h0")) @[Regfile.scala 36:21]
    when _T_6 : @[Regfile.scala 36:37]
      rdata1r <= UInt<32>("h0") @[Regfile.scala 37:13]
    else :
      node _T_7 = eq(io.raddr1, UInt<1>("h0")) @[Regfile.scala 38:24]
      when _T_7 : @[Regfile.scala 38:33]
        rdata1r <= UInt<32>("h0") @[Regfile.scala 39:13]
      else :
        node _T_8 = eq(io.re1, UInt<1>("h1")) @[Regfile.scala 40:21]
        when _T_8 : @[Regfile.scala 40:37]
          rdata1r <= regs[io.raddr1] @[Regfile.scala 41:13]
        else :
          rdata1r <= UInt<32>("h0") @[Regfile.scala 43:13]
    node _T_9 = bits(reset, 0, 0) @[Regfile.scala 46:14]
    node _T_10 = eq(_T_9, UInt<1>("h1")) @[Regfile.scala 46:21]
    when _T_10 : @[Regfile.scala 46:36]
      rdata2r <= UInt<32>("h0") @[Regfile.scala 47:13]
    else :
      node _T_11 = eq(io.raddr2, UInt<1>("h0")) @[Regfile.scala 48:24]
      when _T_11 : @[Regfile.scala 48:33]
        rdata2r <= UInt<32>("h0") @[Regfile.scala 49:13]
      else :
        node _T_12 = eq(io.re2, UInt<1>("h1")) @[Regfile.scala 50:21]
        when _T_12 : @[Regfile.scala 50:37]
          rdata2r <= regs[io.raddr2] @[Regfile.scala 51:13]
        else :
          rdata2r <= UInt<32>("h0") @[Regfile.scala 53:13]

