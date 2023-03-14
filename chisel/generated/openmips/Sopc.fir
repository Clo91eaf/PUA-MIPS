circuit Sopc :
  module PC_reg :
    input clock : Clock
    input reset : Reset
    output io : { pc : UInt<32>, ce : UInt<1>}

    reg pcr : UInt<32>, clock with :
      reset => (UInt<1>("h0"), pcr) @[PC_reg.scala 13:16]
    reg cer : UInt<1>, clock with :
      reset => (UInt<1>("h0"), cer) @[PC_reg.scala 14:16]
    io.pc <= pcr @[PC_reg.scala 16:9]
    io.ce <= cer @[PC_reg.scala 17:9]
    node _T = asUInt(reset) @[PC_reg.scala 19:14]
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

  module Id :
    input clock : Clock
    input reset : Reset
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
    node _T = asUInt(reset) @[Id.scala 69:14]
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
    node _T_2 = asUInt(reset) @[Id.scala 113:14]
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

  module Regfile :
    input clock : Clock
    input reset : Reset
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
    node _T = asUInt(reset) @[Regfile.scala 31:14]
    node _T_1 = eq(_T, UInt<1>("h0")) @[Regfile.scala 31:21]
    when _T_1 : @[Regfile.scala 31:37]
      node _T_2 = eq(io.we, UInt<1>("h1")) @[Regfile.scala 32:16]
      node _T_3 = neq(io.waddr, UInt<1>("h0")) @[Regfile.scala 32:44]
      node _T_4 = and(_T_2, _T_3) @[Regfile.scala 32:32]
      when _T_4 : @[Regfile.scala 32:53]
        regs[io.waddr] <= io.wdata @[Regfile.scala 33:22]
    node _T_5 = asUInt(reset) @[Regfile.scala 36:14]
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
    node _T_9 = asUInt(reset) @[Regfile.scala 46:14]
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

  module Ex :
    input clock : Clock
    input reset : Reset
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
    node _T = asUInt(reset) @[Ex.scala 38:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Ex.scala 38:21]
    when _T_1 : @[Ex.scala 38:36]
      logicout <= UInt<32>("h0") @[Ex.scala 39:14]
    else :
      logicout <= UInt<32>("h0") @[Ex.scala 55:18]
    node _T_2 = asUInt(reset) @[Ex.scala 61:14]
    node _T_3 = eq(_T_2, UInt<1>("h1")) @[Ex.scala 61:21]
    when _T_3 : @[Ex.scala 61:36]
      shiftres <= UInt<32>("h0") @[Ex.scala 62:14]
    else :
      shiftres <= UInt<32>("h0") @[Ex.scala 75:18]
    wd_or <= io.wd_i @[Ex.scala 80:9]
    wreg_or <= io.wreg_i @[Ex.scala 81:11]
    wdata_or <= UInt<32>("h0") @[Ex.scala 86:36]

  module Wb :
    input clock : Clock
    input reset : Reset
    output io : { flip ex_wd : UInt<5>, flip ex_wreg : UInt<1>, flip ex_wdata : UInt<32>, wb_wd : UInt<5>, wb_wreg : UInt<1>, wb_wdata : UInt<32>}

    reg wb_wregr : UInt<1>, clock with :
      reset => (UInt<1>("h0"), wb_wregr) @[Wb.scala 20:21]
    io.wb_wreg <= wb_wregr @[Wb.scala 21:14]
    node _T = asUInt(reset) @[Wb.scala 23:14]
    node _T_1 = eq(_T, UInt<1>("h1")) @[Wb.scala 23:21]
    when _T_1 : @[Wb.scala 23:36]
      io.wb_wd <= UInt<5>("h0") @[Wb.scala 24:14]
      wb_wregr <= UInt<1>("h0") @[Wb.scala 25:14]
      io.wb_wdata <= UInt<32>("h0") @[Wb.scala 26:17]
    else :
      io.wb_wd <= io.ex_wd @[Wb.scala 28:14]
      wb_wregr <= io.ex_wreg @[Wb.scala 29:14]
      io.wb_wdata <= io.ex_wdata @[Wb.scala 30:17]

  module OpenMips :
    input clock : Clock
    input reset : Reset
    output io : { flip rom_data_i : UInt<32>, rom_addr_o : UInt<32>, rom_ce_o : UInt<1>}

    wire id_aluop : UInt<8> @[OpenMips.scala 15:22]
    wire id_alusel : UInt<3> @[OpenMips.scala 16:23]
    wire id_reg1 : UInt<32> @[OpenMips.scala 17:21]
    wire id_reg2 : UInt<32> @[OpenMips.scala 18:21]
    wire id_wreg : UInt<1> @[OpenMips.scala 19:21]
    wire id_wd : UInt<5> @[OpenMips.scala 20:19]
    wire reg1_read : UInt<1> @[OpenMips.scala 22:23]
    wire reg2_read : UInt<1> @[OpenMips.scala 23:23]
    wire reg1_data : UInt<32> @[OpenMips.scala 24:23]
    wire reg2_data : UInt<32> @[OpenMips.scala 25:23]
    wire reg1_addr : UInt<5> @[OpenMips.scala 26:23]
    wire reg2_addr : UInt<5> @[OpenMips.scala 27:23]
    wire ex_wreg : UInt<1> @[OpenMips.scala 29:21]
    wire ex_wd : UInt<5> @[OpenMips.scala 30:19]
    wire ex_wdata : UInt<32> @[OpenMips.scala 31:22]
    wire wb_wd : UInt<5> @[OpenMips.scala 33:19]
    wire wb_wreg : UInt<1> @[OpenMips.scala 34:21]
    wire wb_wdata : UInt<32> @[OpenMips.scala 35:22]
    inst pc_reg0 of PC_reg @[OpenMips.scala 38:23]
    pc_reg0.clock <= clock
    pc_reg0.reset <= reset
    io.rom_addr_o <= pc_reg0.io.pc @[OpenMips.scala 39:17]
    io.rom_ce_o <= pc_reg0.io.ce @[OpenMips.scala 40:15]
    inst id0 of Id @[OpenMips.scala 43:19]
    id0.clock <= clock
    id0.reset <= reset
    id0.io.pc_i <= io.rom_addr_o @[OpenMips.scala 44:15]
    id0.io.inst_i <= io.rom_data_i @[OpenMips.scala 45:17]
    id0.io.reg1_data_i <= reg1_data @[OpenMips.scala 47:22]
    id0.io.reg2_data_i <= reg2_data @[OpenMips.scala 48:22]
    reg1_read <= id0.io.reg1_read_o @[OpenMips.scala 50:13]
    reg2_read <= id0.io.reg2_read_o @[OpenMips.scala 51:13]
    reg1_addr <= id0.io.reg1_addr_o @[OpenMips.scala 52:13]
    reg2_addr <= id0.io.reg2_addr_o @[OpenMips.scala 53:13]
    id_aluop <= id0.io.aluop_o @[OpenMips.scala 55:12]
    id_alusel <= id0.io.alusel_o @[OpenMips.scala 56:13]
    id_reg1 <= id0.io.reg1_o @[OpenMips.scala 57:11]
    id_reg2 <= id0.io.reg2_o @[OpenMips.scala 58:11]
    id_wd <= id0.io.wd_o @[OpenMips.scala 59:9]
    id_wreg <= id0.io.wreg_o @[OpenMips.scala 60:11]
    inst regfile1 of Regfile @[OpenMips.scala 63:24]
    regfile1.clock <= clock
    regfile1.reset <= reset
    regfile1.io.we <= wb_wreg @[OpenMips.scala 65:18]
    regfile1.io.waddr <= wb_wd @[OpenMips.scala 66:21]
    regfile1.io.wdata <= wb_wdata @[OpenMips.scala 67:21]
    regfile1.io.re1 <= reg1_read @[OpenMips.scala 68:19]
    regfile1.io.raddr1 <= reg1_addr @[OpenMips.scala 69:22]
    reg1_data <= regfile1.io.rdata1 @[OpenMips.scala 70:13]
    regfile1.io.re2 <= reg2_read @[OpenMips.scala 71:19]
    regfile1.io.raddr2 <= reg2_addr @[OpenMips.scala 72:22]
    reg2_data <= regfile1.io.rdata2 @[OpenMips.scala 73:13]
    inst ex0 of Ex @[OpenMips.scala 75:19]
    ex0.clock <= clock
    ex0.reset <= reset
    ex0.io.aluop_i <= id_aluop @[OpenMips.scala 77:18]
    ex0.io.alusel_i <= id_alusel @[OpenMips.scala 78:19]
    ex0.io.reg1_i <= id_reg1 @[OpenMips.scala 79:17]
    ex0.io.reg2_i <= id_reg2 @[OpenMips.scala 80:17]
    ex0.io.wd_i <= id_wd @[OpenMips.scala 81:15]
    ex0.io.wreg_i <= id_wreg @[OpenMips.scala 82:17]
    ex_wd <= ex0.io.wd_o @[OpenMips.scala 84:9]
    ex_wreg <= ex0.io.wreg_o @[OpenMips.scala 85:11]
    ex_wdata <= ex0.io.wdata_o @[OpenMips.scala 86:12]
    inst wb0 of Wb @[OpenMips.scala 88:19]
    wb0.clock <= clock
    wb0.reset <= reset
    wb0.io.ex_wd <= ex_wd @[OpenMips.scala 90:16]
    wb0.io.ex_wreg <= ex_wreg @[OpenMips.scala 91:18]
    wb0.io.ex_wdata <= ex_wdata @[OpenMips.scala 92:19]
    wb_wd <= wb0.io.wb_wd @[OpenMips.scala 94:9]
    wb_wreg <= wb0.io.wb_wreg @[OpenMips.scala 95:11]
    wb_wdata <= wb0.io.wb_wdata @[OpenMips.scala 96:12]

  module Inst_rom :
    input clock : Clock
    input reset : Reset
    output io : { flip ce : UInt<1>, flip addr : UInt<32>, inst : UInt<32>}

    reg instr : UInt<32>, clock with :
      reset => (UInt<1>("h0"), instr) @[Inst_rom.scala 14:18]
    io.inst <= instr @[Inst_rom.scala 15:11]
    cmem inst_mem : UInt<32> [131071] @[Inst_rom.scala 17:21]
    node _T = eq(io.ce, UInt<1>("h0")) @[Inst_rom.scala 21:14]
    when _T : @[Inst_rom.scala 21:31]
      instr <= UInt<32>("h0") @[Inst_rom.scala 22:11]
    else :
      node _instr_T = bits(io.addr, 18, 2) @[Inst_rom.scala 24:30]
      infer mport instr_MPORT = inst_mem[_instr_T], clock @[Inst_rom.scala 24:22]
      instr <= instr_MPORT @[Inst_rom.scala 24:11]

  module Sopc :
    input clock : Clock
    input reset : UInt<1>
    output io : { success : UInt<1>}

    io.success is invalid @[Sopc.scala 11:14]
    wire inst_addr : UInt<32> @[Sopc.scala 13:23]
    wire inst : UInt<32> @[Sopc.scala 14:18]
    wire rom_ce : UInt<1> @[Sopc.scala 15:20]
    inst openmips0 of OpenMips @[Sopc.scala 17:25]
    openmips0.clock <= clock
    openmips0.reset <= reset
    inst_addr <= openmips0.io.rom_addr_o @[Sopc.scala 18:13]
    openmips0.io.rom_data_i <= inst @[Sopc.scala 19:27]
    rom_ce <= openmips0.io.rom_ce_o @[Sopc.scala 20:10]
    inst inst_rom0 of Inst_rom @[Sopc.scala 22:25]
    inst_rom0.clock <= clock
    inst_rom0.reset <= reset
    inst_rom0.io.ce <= rom_ce @[Sopc.scala 23:19]
    inst_rom0.io.addr <= inst_addr @[Sopc.scala 24:21]
    inst <= inst_rom0.io.inst @[Sopc.scala 25:8]

