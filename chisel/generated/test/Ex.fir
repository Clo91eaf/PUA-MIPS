circuit Ex :
  module Ex :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip aluop_i : UInt<8>, flip alusel_i : UInt<3>, flip reg1_i : UInt<32>, flip reg2_i : UInt<32>, flip wd_i : UInt<5>, flip wreg_i : UInt<1>, flip hi_i : UInt<32>, flip lo_i : UInt<32>, wd_o : UInt<5>, wreg_o : UInt<1>, wdata_o : UInt<32>, hi_o : UInt<32>, lo_o : UInt<32>, whilo_o : UInt<1>}

    reg wd_or : UInt<5>, clock with :
      reset => (UInt<1>("h0"), wd_or) @[Ex.scala 32:18]
    reg wreg_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wreg_or) @[Ex.scala 33:20]
    reg wdata_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), wdata_or) @[Ex.scala 34:21]
    reg hi_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), hi_or) @[Ex.scala 35:18]
    reg lo_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), lo_or) @[Ex.scala 36:18]
    reg whilo_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), whilo_or) @[Ex.scala 37:21]
    io.wd_o <= wd_or @[Ex.scala 39:11]
    io.wreg_o <= wreg_or @[Ex.scala 40:13]
    io.wdata_o <= wdata_or @[Ex.scala 41:14]
    io.hi_o <= hi_or @[Ex.scala 42:11]
    io.lo_o <= lo_or @[Ex.scala 43:11]
    io.whilo_o <= whilo_or @[Ex.scala 44:14]
    reg logicout : UInt<32>, clock with :
      reset => (UInt<1>("h0"), logicout) @[Ex.scala 47:21]
    reg shiftres : UInt<32>, clock with :
      reset => (UInt<1>("h0"), shiftres) @[Ex.scala 48:21]
    reg moveres : UInt<32>, clock with :
      reset => (UInt<1>("h0"), moveres) @[Ex.scala 49:20]
    reg HI : UInt<32>, clock with :
      reset => (UInt<1>("h0"), HI) @[Ex.scala 50:15]
    reg LO : UInt<32>, clock with :
      reset => (UInt<1>("h0"), LO) @[Ex.scala 51:15]
    node _T = bits(reset, 0, 0) @[Ex.scala 56:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Ex.scala 56:21]
    when _T_1 : @[Ex.scala 56:36]
      logicout <= UInt<32>("h0") @[Ex.scala 57:14]
    else :
      logicout <= UInt<32>("h0") @[Ex.scala 59:14]
      node _T_2 = eq(UInt<8>("h25"), io.aluop_i) @[Ex.scala 60:24]
      when _T_2 : @[Ex.scala 60:24]
        node _logicout_T = or(io.reg1_i, io.reg2_i) @[Ex.scala 62:31]
        logicout <= _logicout_T @[Ex.scala 62:18]
      else :
        node _T_3 = eq(UInt<8>("h24"), io.aluop_i) @[Ex.scala 60:24]
        when _T_3 : @[Ex.scala 60:24]
          node _logicout_T_1 = and(io.reg1_i, io.reg2_i) @[Ex.scala 65:31]
          logicout <= _logicout_T_1 @[Ex.scala 65:18]
        else :
          node _T_4 = eq(UInt<8>("h27"), io.aluop_i) @[Ex.scala 60:24]
          when _T_4 : @[Ex.scala 60:24]
            node _logicout_T_2 = or(io.reg1_i, io.reg2_i) @[Ex.scala 68:33]
            node _logicout_T_3 = not(_logicout_T_2) @[Ex.scala 68:21]
            logicout <= _logicout_T_3 @[Ex.scala 68:18]
          else :
            node _T_5 = eq(UInt<8>("h26"), io.aluop_i) @[Ex.scala 60:24]
            when _T_5 : @[Ex.scala 60:24]
              node _logicout_T_4 = xor(io.reg1_i, io.reg2_i) @[Ex.scala 71:31]
              logicout <= _logicout_T_4 @[Ex.scala 71:18]
    node _T_6 = bits(reset, 0, 0) @[Ex.scala 77:14]
    node _T_7 = eq(_T_6, UInt<1>("h1")) @[Ex.scala 77:21]
    when _T_7 : @[Ex.scala 77:36]
      shiftres <= UInt<32>("h0") @[Ex.scala 78:14]
    else :
      shiftres <= UInt<32>("h0") @[Ex.scala 80:14]
      node _T_8 = eq(UInt<8>("h7c"), io.aluop_i) @[Ex.scala 81:24]
      when _T_8 : @[Ex.scala 81:24]
        node _shiftres_T = bits(io.reg1_i, 4, 0) @[Ex.scala 83:43]
        node _shiftres_T_1 = dshl(io.reg2_i, _shiftres_T) @[Ex.scala 83:31]
        shiftres <= _shiftres_T_1 @[Ex.scala 83:18]
      else :
        node _T_9 = eq(UInt<8>("h2"), io.aluop_i) @[Ex.scala 81:24]
        when _T_9 : @[Ex.scala 81:24]
          node _shiftres_T_2 = bits(io.reg1_i, 4, 0) @[Ex.scala 86:43]
          node _shiftres_T_3 = dshr(io.reg2_i, _shiftres_T_2) @[Ex.scala 86:31]
          shiftres <= _shiftres_T_3 @[Ex.scala 86:18]
        else :
          node _T_10 = eq(UInt<8>("h3"), io.aluop_i) @[Ex.scala 81:24]
          when _T_10 : @[Ex.scala 81:24]
            node _shiftres_T_4 = asSInt(io.reg2_i) @[Ex.scala 89:32]
            node _shiftres_T_5 = bits(io.reg1_i, 4, 0) @[Ex.scala 89:51]
            node _shiftres_T_6 = dshr(_shiftres_T_4, _shiftres_T_5) @[Ex.scala 89:39]
            node _shiftres_T_7 = asUInt(_shiftres_T_6) @[Ex.scala 89:59]
            shiftres <= _shiftres_T_7 @[Ex.scala 89:18]
    node _T_11 = bits(reset, 0, 0) @[Ex.scala 95:14]
    node _T_12 = eq(_T_11, UInt<1>("h1")) @[Ex.scala 95:21]
    when _T_12 : @[Ex.scala 95:36]
      HI <= UInt<32>("h0") @[Ex.scala 96:8]
      LO <= UInt<32>("h0") @[Ex.scala 97:8]
    else :
      HI <= io.hi_i @[Ex.scala 99:8]
      LO <= io.lo_i @[Ex.scala 100:8]
    node _T_13 = bits(reset, 0, 0) @[Ex.scala 104:14]
    node _T_14 = eq(_T_13, UInt<1>("h1")) @[Ex.scala 104:21]
    when _T_14 : @[Ex.scala 104:36]
      moveres <= UInt<32>("h0") @[Ex.scala 105:13]
    else :
      moveres <= UInt<32>("h0") @[Ex.scala 107:13]
      node _T_15 = eq(UInt<8>("h10"), io.aluop_i) @[Ex.scala 108:24]
      when _T_15 : @[Ex.scala 108:24]
        moveres <= HI @[Ex.scala 110:17]
      else :
        node _T_16 = eq(UInt<8>("h12"), io.aluop_i) @[Ex.scala 108:24]
        when _T_16 : @[Ex.scala 108:24]
          moveres <= LO @[Ex.scala 113:17]
        else :
          node _T_17 = eq(UInt<8>("ha"), io.aluop_i) @[Ex.scala 108:24]
          when _T_17 : @[Ex.scala 108:24]
            moveres <= io.reg1_i @[Ex.scala 116:17]
          else :
            node _T_18 = eq(UInt<8>("hb"), io.aluop_i) @[Ex.scala 108:24]
            when _T_18 : @[Ex.scala 108:24]
              moveres <= io.reg1_i @[Ex.scala 119:17]
    wd_or <= io.wd_i @[Ex.scala 125:9]
    wreg_or <= io.wreg_i @[Ex.scala 126:11]
    wdata_or <= UInt<32>("h0") @[Ex.scala 127:12]
    node _T_19 = eq(UInt<3>("h1"), io.alusel_i) @[Ex.scala 128:23]
    when _T_19 : @[Ex.scala 128:23]
      wdata_or <= logicout @[Ex.scala 129:34]
    else :
      node _T_20 = eq(UInt<3>("h2"), io.alusel_i) @[Ex.scala 128:23]
      when _T_20 : @[Ex.scala 128:23]
        wdata_or <= shiftres @[Ex.scala 130:34]
      else :
        node _T_21 = eq(UInt<3>("h3"), io.alusel_i) @[Ex.scala 128:23]
        when _T_21 : @[Ex.scala 128:23]
          wdata_or <= moveres @[Ex.scala 131:33]
    node _T_22 = bits(reset, 0, 0) @[Ex.scala 135:14]
    node _T_23 = eq(_T_22, UInt<1>("h1")) @[Ex.scala 135:21]
    when _T_23 : @[Ex.scala 135:36]
      whilo_or <= UInt<1>("h0") @[Ex.scala 136:14]
      hi_or <= UInt<32>("h0") @[Ex.scala 137:11]
      lo_or <= UInt<32>("h0") @[Ex.scala 138:11]
    else :
      node _T_24 = eq(io.aluop_i, UInt<8>("h11")) @[Ex.scala 139:25]
      when _T_24 : @[Ex.scala 139:42]
        whilo_or <= UInt<1>("h1") @[Ex.scala 140:14]
        hi_or <= io.reg1_i @[Ex.scala 141:11]
        lo_or <= LO @[Ex.scala 142:11]
      else :
        node _T_25 = eq(io.aluop_i, UInt<8>("h13")) @[Ex.scala 143:25]
        when _T_25 : @[Ex.scala 143:42]
          whilo_or <= UInt<1>("h1") @[Ex.scala 144:14]
          hi_or <= HI @[Ex.scala 145:11]
          lo_or <= io.reg1_i @[Ex.scala 146:11]
        else :
          whilo_or <= UInt<1>("h0") @[Ex.scala 148:14]
          hi_or <= UInt<32>("h0") @[Ex.scala 149:11]
          lo_or <= UInt<32>("h0") @[Ex.scala 150:11]

