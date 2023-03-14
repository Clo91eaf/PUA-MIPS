circuit Ex :
  module Ex :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip aluop_i : UInt<8>, flip alusel_i : UInt<3>, flip reg1_i : UInt<32>, flip reg2_i : UInt<32>, flip wd_i : UInt<5>, flip wreg_i : UInt<1>, wd_o : UInt<5>, wreg_o : UInt<1>, wdata_o : UInt<32>}

    reg wd_or : UInt<5>, clock with :
      reset => (UInt<1>("h0"), wd_or) @[Ex.scala 24:18]
    reg wreg_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wreg_or) @[Ex.scala 25:20]
    reg wdata_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), wdata_or) @[Ex.scala 26:21]
    io.wd_o <= wd_or @[Ex.scala 28:11]
    io.wreg_o <= wreg_or @[Ex.scala 29:13]
    io.wdata_o <= wdata_or @[Ex.scala 30:14]
    reg logicout : UInt<32>, clock with :
      reset => (UInt<1>("h0"), logicout) @[Ex.scala 33:21]
    reg shiftres : UInt<32>, clock with :
      reset => (UInt<1>("h0"), shiftres) @[Ex.scala 34:21]
    node _T = bits(reset, 0, 0) @[Ex.scala 39:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Ex.scala 39:21]
    when _T_1 : @[Ex.scala 39:36]
      logicout <= UInt<32>("h0") @[Ex.scala 40:14]
    else :
      logicout <= UInt<32>("h0") @[Ex.scala 42:14]
      node _T_2 = eq(UInt<8>("h25"), io.aluop_i) @[Ex.scala 43:24]
      when _T_2 : @[Ex.scala 43:24]
        node _logicout_T = or(io.reg1_i, io.reg2_i) @[Ex.scala 45:31]
        logicout <= _logicout_T @[Ex.scala 45:18]
      else :
        node _T_3 = eq(UInt<8>("h24"), io.aluop_i) @[Ex.scala 43:24]
        when _T_3 : @[Ex.scala 43:24]
          node _logicout_T_1 = and(io.reg1_i, io.reg2_i) @[Ex.scala 48:31]
          logicout <= _logicout_T_1 @[Ex.scala 48:18]
        else :
          node _T_4 = eq(UInt<8>("h27"), io.aluop_i) @[Ex.scala 43:24]
          when _T_4 : @[Ex.scala 43:24]
            node _logicout_T_2 = or(io.reg1_i, io.reg2_i) @[Ex.scala 51:33]
            node _logicout_T_3 = not(_logicout_T_2) @[Ex.scala 51:21]
            logicout <= _logicout_T_3 @[Ex.scala 51:18]
          else :
            node _T_5 = eq(UInt<8>("h26"), io.aluop_i) @[Ex.scala 43:24]
            when _T_5 : @[Ex.scala 43:24]
              node _logicout_T_4 = xor(io.reg1_i, io.reg2_i) @[Ex.scala 54:31]
              logicout <= _logicout_T_4 @[Ex.scala 54:18]
    node _T_6 = bits(reset, 0, 0) @[Ex.scala 60:14]
    node _T_7 = eq(_T_6, UInt<1>("h1")) @[Ex.scala 60:21]
    when _T_7 : @[Ex.scala 60:36]
      shiftres <= UInt<32>("h0") @[Ex.scala 61:14]
    else :
      shiftres <= UInt<32>("h0") @[Ex.scala 63:14]
      node _T_8 = eq(UInt<8>("h7c"), io.aluop_i) @[Ex.scala 64:24]
      when _T_8 : @[Ex.scala 64:24]
        node _shiftres_T = bits(io.reg1_i, 4, 0) @[Ex.scala 66:43]
        node _shiftres_T_1 = dshl(io.reg2_i, _shiftres_T) @[Ex.scala 66:31]
        shiftres <= _shiftres_T_1 @[Ex.scala 66:18]
      else :
        node _T_9 = eq(UInt<8>("h2"), io.aluop_i) @[Ex.scala 64:24]
        when _T_9 : @[Ex.scala 64:24]
          node _shiftres_T_2 = bits(io.reg1_i, 4, 0) @[Ex.scala 69:43]
          node _shiftres_T_3 = dshr(io.reg2_i, _shiftres_T_2) @[Ex.scala 69:31]
          shiftres <= _shiftres_T_3 @[Ex.scala 69:18]
        else :
          node _T_10 = eq(UInt<8>("h3"), io.aluop_i) @[Ex.scala 64:24]
          when _T_10 : @[Ex.scala 64:24]
            node _shiftres_T_4 = asSInt(io.reg2_i) @[Ex.scala 72:32]
            node _shiftres_T_5 = bits(io.reg1_i, 4, 0) @[Ex.scala 72:51]
            node _shiftres_T_6 = dshr(_shiftres_T_4, _shiftres_T_5) @[Ex.scala 72:39]
            node _shiftres_T_7 = asUInt(_shiftres_T_6) @[Ex.scala 72:59]
            shiftres <= _shiftres_T_7 @[Ex.scala 72:18]
    wd_or <= io.wd_i @[Ex.scala 77:9]
    wreg_or <= io.wreg_i @[Ex.scala 78:11]
    wdata_or <= UInt<32>("h0") @[Ex.scala 80:12]
    node _T_11 = eq(UInt<3>("h1"), io.alusel_i) @[Ex.scala 81:23]
    when _T_11 : @[Ex.scala 81:23]
      wdata_or <= logicout @[Ex.scala 82:34]
    else :
      node _T_12 = eq(UInt<3>("h2"), io.alusel_i) @[Ex.scala 81:23]
      when _T_12 : @[Ex.scala 81:23]
        wdata_or <= shiftres @[Ex.scala 83:34]

