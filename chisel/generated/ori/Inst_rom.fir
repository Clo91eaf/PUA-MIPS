circuit Inst_rom :
  module Inst_rom :
    input clock : Clock
    input reset : UInt<1>
    output io : { flip ce : UInt<1>, flip addr : UInt<32>, inst : UInt<32>}

    reg instr : UInt<32>, clock with :
      reset => (UInt<1>("h0"), instr) @[Inst_rom.scala 15:18]
    io.inst <= instr @[Inst_rom.scala 16:11]
    cmem inst_mem : UInt<32> [131071] @[Inst_rom.scala 18:21]
    node _T = eq(io.ce, UInt<1>("h0")) @[Inst_rom.scala 22:14]
    when _T : @[Inst_rom.scala 22:31]
      instr <= UInt<32>("h0") @[Inst_rom.scala 23:11]
    else :
      node _instr_T = bits(io.addr, 18, 2) @[Inst_rom.scala 25:30]
      infer mport instr_MPORT = inst_mem[_instr_T], clock @[Inst_rom.scala 25:22]
      instr <= instr_MPORT @[Inst_rom.scala 25:11]

