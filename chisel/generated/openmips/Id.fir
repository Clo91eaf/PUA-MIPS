circuit Id :
  module Id :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip pc_i : UInt<32>, flip inst_i : UInt<32>, flip reg1_data_i : UInt<32>, flip reg2_data_i : UInt<32>, reg1_read_o : UInt<1>, reg2_read_o : UInt<1>, reg1_addr_o : UInt<5>, reg2_addr_o : UInt<5>, aluop_o : UInt<8>, alusel_o : UInt<3>, reg1_o : UInt<32>, reg2_o : UInt<32>, wd_o : UInt<5>, wreg_o : UInt<1>}

    reg reg1_read_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), reg1_read_or) @[Id.scala 31:25]
    reg reg2_read_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), reg2_read_or) @[Id.scala 32:25]
    reg reg1_addr_or : UInt<5>, clock with :
      reset => (UInt<1>("h0"), reg1_addr_or) @[Id.scala 33:25]
    reg reg2_addr_or : UInt<5>, clock with :
      reset => (UInt<1>("h0"), reg2_addr_or) @[Id.scala 34:25]
    reg aluop_or : UInt<8>, clock with :
      reset => (UInt<1>("h0"), aluop_or) @[Id.scala 37:21]
    reg alusel_or : UInt<3>, clock with :
      reset => (UInt<1>("h0"), alusel_or) @[Id.scala 38:22]
    reg reg1_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), reg1_or) @[Id.scala 39:20]
    reg reg2_or : UInt<32>, clock with :
      reset => (UInt<1>("h0"), reg2_or) @[Id.scala 40:20]
    reg wd_or : UInt<5>, clock with :
      reset => (UInt<1>("h0"), wd_or) @[Id.scala 41:18]
    reg wreg_or : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wreg_or) @[Id.scala 42:20]
    io.reg1_read_o <= reg1_read_or @[Id.scala 44:18]
    io.reg2_read_o <= reg2_read_or @[Id.scala 45:18]
    io.reg1_addr_o <= reg1_addr_or @[Id.scala 46:18]
    io.reg2_addr_o <= reg2_addr_or @[Id.scala 47:18]
    io.aluop_o <= aluop_or @[Id.scala 48:14]
    io.alusel_o <= alusel_or @[Id.scala 49:15]
    io.reg1_o <= reg1_or @[Id.scala 50:13]
    io.reg2_o <= reg2_or @[Id.scala 51:13]
    io.wd_o <= wd_or @[Id.scala 52:11]
    io.wreg_o <= wreg_or @[Id.scala 53:13]
    wire op : UInt<6> @[Id.scala 55:16]
    wire op2 : UInt<6> @[Id.scala 56:17]
    wire op3 : UInt<6> @[Id.scala 57:17]
    wire op4 : UInt<6> @[Id.scala 58:17]
    node _op_T = bits(io.inst_i, 31, 26) @[Id.scala 60:18]
    op <= _op_T @[Id.scala 60:6]
    node _op2_T = bits(io.inst_i, 10, 6) @[Id.scala 61:19]
    op2 <= _op2_T @[Id.scala 61:7]
    node _op3_T = bits(io.inst_i, 5, 0) @[Id.scala 62:19]
    op3 <= _op3_T @[Id.scala 62:7]
    node _op4_T = bits(io.inst_i, 20, 16) @[Id.scala 63:19]
    op4 <= _op4_T @[Id.scala 63:7]
    reg imm : UInt<32>, clock with :
      reset => (UInt<1>("h0"), imm) @[Id.scala 65:16]
    reg instvalid : UInt<1>, clock with :
      reset => (UInt<1>("h0"), instvalid) @[Id.scala 67:22]
    node _T = bits(reset, 0, 0) @[Id.scala 69:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Id.scala 69:21]
    when _T_1 : @[Id.scala 69:36]
      aluop_or <= UInt<8>("h0") @[Id.scala 70:14]
      alusel_or <= UInt<3>("h0") @[Id.scala 71:15]
      wd_or <= UInt<5>("h0") @[Id.scala 72:11]
      wreg_or <= UInt<1>("h0") @[Id.scala 73:13]
      instvalid <= UInt<1>("h1") @[Id.scala 74:15]
      reg1_read_or <= UInt<1>("h0") @[Id.scala 75:18]
      reg2_read_or <= UInt<1>("h0") @[Id.scala 76:18]
      reg1_addr_or <= UInt<5>("h0") @[Id.scala 77:18]
      reg2_addr_or <= UInt<5>("h0") @[Id.scala 78:18]
      imm <= UInt<1>("h0") @[Id.scala 79:9]
    else :
      aluop_or <= UInt<8>("h0") @[Id.scala 81:14]
      alusel_or <= UInt<3>("h0") @[Id.scala 82:15]
      node _wd_or_T = bits(io.inst_i, 15, 11) @[Id.scala 83:23]
      wd_or <= _wd_or_T @[Id.scala 83:11]
      wreg_or <= UInt<1>("h0") @[Id.scala 84:13]
      instvalid <= UInt<1>("h1") @[Id.scala 85:15]
      reg1_read_or <= UInt<1>("h0") @[Id.scala 86:18]
      reg2_read_or <= UInt<1>("h0") @[Id.scala 87:18]
      node _reg1_addr_or_T = bits(io.inst_i, 25, 21) @[Id.scala 88:30]
      reg1_addr_or <= _reg1_addr_or_T @[Id.scala 88:18]
      node _reg2_addr_or_T = bits(io.inst_i, 20, 16) @[Id.scala 89:30]
      reg2_addr_or <= _reg2_addr_or_T @[Id.scala 89:18]
      imm <= UInt<32>("h0") @[Id.scala 90:9]
    node _T_2 = bits(reset, 0, 0) @[Id.scala 113:14]
    node _T_3 = eq(_T_2, UInt<1>("h1")) @[Id.scala 113:21]
    when _T_3 : @[Id.scala 113:36]
      reg1_or <= UInt<32>("h0") @[Id.scala 114:13]
    else :
      node _T_4 = eq(reg2_read_or, UInt<1>("h1")) @[Id.scala 115:27]
      when _T_4 : @[Id.scala 115:39]
        reg2_or <= io.reg2_data_i @[Id.scala 116:13]
      else :
        node _T_5 = eq(reg2_read_or, UInt<1>("h0")) @[Id.scala 117:27]
        when _T_5 : @[Id.scala 117:39]
          reg2_or <= imm @[Id.scala 118:13]
        else :
          reg2_or <= UInt<32>("h0") @[Id.scala 120:13]

