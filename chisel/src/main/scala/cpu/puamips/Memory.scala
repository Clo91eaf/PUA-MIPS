package cpu.puamips

import chisel3._
import cpu.puamips.Const._
import chisel3.util._

class Memory extends Module {
  val io = IO(new Bundle {
    val fromExecute = Flipped(new Execute_Memory())
    val fromDataMemory = Flipped(new DataMemory_Memory())
    val decoder = new Memory_Decoder()
    val execute = new Memory_Execute()
    val writeBack = new Memory_WriteBack()
    val dataMemory = new Memory_DataMemory()
  })
  // input-execute
  val pc = RegInit(REG_BUS_INIT)
  val wdata = RegInit(REG_BUS_INIT)
  val wd = RegInit(REG_ADDR_BUS_INIT)
  val wreg = RegInit(false.B)
  val aluop = Wire(ALU_OP_BUS)
  val reg2 = Wire(REG_BUS)
  pc := io.fromExecute.pc
  wdata := io.fromExecute.wdata
  wd := io.fromExecute.wd
  wreg := io.fromExecute.wreg
  aluop := io.fromExecute.aluop
  reg2 := io.fromExecute.reg2

  // input-dataMemory

  // output-dataMemory
  val mem_addr = RegInit(REG_BUS_INIT)
  val mem_we = RegInit(WRITE_DISABLE)
  val mem_sel = RegInit(DATA_MEMORY_SEL_BUS_INIT)
  val mem_ce = RegInit(CHIP_DISABLE)
  val mem_data = RegInit(REG_BUS_INIT)
  io.dataMemory.addr := mem_addr
  io.dataMemory.we := mem_we
  io.dataMemory.sel := mem_sel
  io.dataMemory.data := mem_data
  io.dataMemory.ce := mem_ce

  // output-decoder
  io.decoder.wdata := wdata
  io.decoder.wd := wd
  io.decoder.wreg := wreg

  // output-execute
  val whilo = RegInit(WRITE_DISABLE)
  val hi = RegInit(REG_BUS_INIT)
  val lo = RegInit(REG_BUS_INIT)
  io.execute.whilo := whilo
  io.execute.hi := hi
  io.execute.lo := lo

  // output-write back
  io.writeBack.pc := pc
  io.writeBack.wdata := wdata
  io.writeBack.wd := wd
  io.writeBack.wreg := wreg
  io.writeBack.whilo := whilo
  io.writeBack.hi := hi
  io.writeBack.lo := lo

  hi := io.fromExecute.hi
  lo := io.fromExecute.lo
  whilo := io.fromExecute.whilo
  mem_sel := "b1111".U

  val addrLowBit2 = io.fromExecute.addr(1, 0)

  mem_ce := MuxLookup(
    aluop,
    CHIP_DISABLE,
    Seq(
      EXE_LB_OP -> CHIP_ENABLE,
      EXE_LBU_OP -> CHIP_ENABLE,
      EXE_LH_OP -> CHIP_ENABLE,
      EXE_LHU_OP -> CHIP_ENABLE,
      EXE_LW_OP -> CHIP_ENABLE,
      EXE_SB_OP -> CHIP_ENABLE,
      EXE_SH_OP -> CHIP_ENABLE,
      EXE_SW_OP -> CHIP_ENABLE
    )
  )

  mem_addr := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_LB_OP -> io.fromExecute.addr,
      EXE_LBU_OP -> io.fromExecute.addr,
      EXE_LH_OP -> io.fromExecute.addr,
      EXE_LHU_OP -> io.fromExecute.addr,
      EXE_LW_OP -> io.fromExecute.addr,
      EXE_SB_OP -> io.fromExecute.addr,
      EXE_SH_OP -> io.fromExecute.addr,
      EXE_SW_OP -> io.fromExecute.addr
    )
  )

  mem_we := MuxLookup(
    aluop,
    WRITE_DISABLE,
    Seq(
      EXE_LB_OP -> WRITE_DISABLE,
      EXE_LBU_OP -> WRITE_DISABLE,
      EXE_LH_OP -> WRITE_DISABLE,
      EXE_LHU_OP -> WRITE_DISABLE,
      EXE_LW_OP -> WRITE_DISABLE,
      EXE_SB_OP -> WRITE_ENABLE,
      EXE_SH_OP -> WRITE_ENABLE,
      EXE_SW_OP -> WRITE_ENABLE
    )
  )

  mem_sel := MuxLookup(
    aluop,
    "b1111".U,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U
        )
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        "b1111".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U
        )
      ),
      EXE_LW_OP -> "b1111".U,
      EXE_SB_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1000".U,
          "b01".U -> "b0100".U,
          "b10".U -> "b0010".U,
          "b11".U -> "b0001".U
        )
      ),
      EXE_SH_OP -> MuxLookup(
        addrLowBit2,
        "b0000".U,
        Seq(
          "b00".U -> "b1100".U,
          "b10".U -> "b0011".U
        )
      ),
      EXE_SW_OP -> "b1111".U
    )
  )
  wdata := MuxLookup(
    aluop,
    io.fromExecute.wdata,
    Seq(
      EXE_LB_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.signedExtend(io.fromExecute.addr(31, 24)),
          "b01".U -> Util.signedExtend(io.fromExecute.addr(23, 16)),
          "b10".U -> Util.signedExtend(io.fromExecute.addr(15, 8)),
          "b11".U -> Util.signedExtend(io.fromExecute.addr(7, 0))
        )
      ),
      EXE_LBU_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.zeroExtend(io.fromExecute.addr(31, 24)),
          "b01".U -> Util.zeroExtend(io.fromExecute.addr(23, 16)),
          "b10".U -> Util.zeroExtend(io.fromExecute.addr(15, 8)),
          "b11".U -> Util.zeroExtend(io.fromExecute.addr(7, 0))
        )
      ),
      EXE_LH_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.signedExtend(io.fromExecute.addr(31, 16)),
          "b10".U -> Util.signedExtend(io.fromExecute.addr(15, 0))
        )
      ),
      EXE_LHU_OP -> MuxLookup(
        addrLowBit2,
        ZERO_WORD,
        Seq(
          "b00".U -> Util.zeroExtend(io.fromExecute.addr(31, 16)),
          "b10".U -> Util.zeroExtend(io.fromExecute.addr(15, 0))
        )
      ),
      EXE_LW_OP -> io.fromDataMemory.data
    )
  )

  mem_data := MuxLookup(
    aluop,
    ZERO_WORD,
    Seq(
      EXE_SB_OP -> Fill(4, reg2(7, 0)),
      EXE_SH_OP -> Fill(2, reg2(15, 0)),
      EXE_SW_OP -> reg2
    )
  )

  printf(p"memory :pc 0x${Hexadecimal(pc)}\n")
}
