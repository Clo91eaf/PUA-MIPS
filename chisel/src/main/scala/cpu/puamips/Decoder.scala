package cpu.puamips

import Const._
import chisel3._
import chisel3.util._
import UInt._

class Decoder extends Module {
  val io = IO(new Bundle {
    // 从各个流水线阶段传来的信号
    val fetch = Flipped(new Fetch_Decoder())
    val instMemory = Flipped(new InstMemory_Decoder())
    val fromRegfile = Flipped(new RegFile_Decoder())
    val fromExecute = Flipped(new Execute_Decoder())
    val fromMemory = Flipped(new Memory_Decoder())

    val regfile = new Decoder_RegFile()
    val execute = new Decoder_Execute()
  })
  // input-fetch
  val pc = RegInit(RegBusInit)
  pc := io.fetch.pc

  // input-inst memory
  val inst = RegInit(RegBusInit)
  inst := io.instMemory.inst

  // input-regfile
  val reg1_data = RegInit(RegBusInit)
  val reg2_data = RegInit(RegBusInit)
  reg1_data := io.fromRegfile.rdata1
  reg2_data := io.fromRegfile.rdata2

  // input-execute
  val exWdata = RegInit(RegBusInit)
  val exWd = RegInit(RegAddrBusInit)
  val exWreg = RegInit(false.B)

  // input-memory
  val memWdata = RegInit(RegBusInit)
  val memWd = RegInit(RegAddrBusInit)
  val memWreg = RegInit(false.B)

  // Output-regfile
  val reg1_read = RegInit(false.B)
  val reg2_read = RegInit(false.B)
  val reg1_addr = RegInit(RegAddrBusInit)
  val reg2_addr = RegInit(RegAddrBusInit)
  io.regfile.reg1_read := reg1_read
  io.regfile.reg2_read := reg2_read
  io.regfile.reg1_addr := reg1_addr
  io.regfile.reg2_addr := reg2_addr

  // Output-execute
  val aluop = RegInit(ALU_OP_BUS_INIT)
  val alusel = RegInit(ALU_SEL_BUS_INIT)
  val reg1 = RegInit(RegBusInit)
  val reg2 = RegInit(RegBusInit)
  val wd = RegInit(RegAddrBusInit)
  val wreg = RegInit(false.B)
  io.execute.aluop := aluop
  io.execute.alusel := alusel
  io.execute.reg1 := reg1
  io.execute.reg2 := reg2
  io.execute.wd := wd
  io.execute.wreg := wreg

  // 取得的指令码功能码
  val op = Wire(UInt(6.W))
  val op2 = Wire(UInt(5.W))
  val op3 = Wire(UInt(6.W))
  val op4 = Wire(UInt(5.W))
  op := inst(31, 26)
  op2 := inst(10, 6)
  op3 := inst(5, 0)
  op4 := inst(20, 16)

  // 保存指令执行需要的立即数
  val imm = Reg(RegBus)

  // 指示指令是否有效
  val instvalid = RegInit(false.B)

  val rt = UInt(5.W)
  val rd = UInt(5.W)
  val sa = UInt(5.W)
  val rs = UInt(5.W)
  val imm16 = UInt(16.W)

  rt := inst(20, 16)
  rd := inst(15, 11)
  sa := inst(10, 6)
  rs := inst(25, 21)
  imm16 := inst(15, 0)

  // 对指令进行译码
  instvalid := InstInvalid
  aluop := EXE_NOP_OP
  alusel := EXE_RES_NOP
  wd := rd // inst(15, 11)
  wreg := WriteDisable
  reg1_read := ReadDisable
  reg2_read := ReadDisable
  reg1_addr := rs // inst(25, 21)
  reg2_addr := rt //inst(20, 16)
  imm := ZeroWord

  val signals: List[UInt] = ListLookup(
    inst,
  // @formatter:off
    List(InstInvalid, ReadDisable, ReadDisable, EXE_RES_NOP, EXE_NOP_OP, WriteDisable, WRA_X, IMM_N),
    Array(         /* val  | Op1    | Op2    | inst    |operation| Write | WReg   | Imm */
                   /* inst | sel    | sel    | type    | type    | reg   | Target | type */
      // 位操作
      OR        -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_OR_OP  , WriteEnable   , WRA_T1 , IMM_N  ),
      AND       -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_AND_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      XOR       -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_XOR_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      NOR       -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_NOR_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      // 移位
      SLLV      -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_SHIFT, EXE_SLL_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      SRLV      -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_SHIFT, EXE_SRL_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      SRAV      -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_SHIFT, EXE_SRA_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      SLL       -> List(InstValid , ReadDisable, ReadEnable   , EXE_RES_SHIFT, EXE_SLL_OP , WriteEnable   , WRA_T1 , IMM_SHT),
      SRL       -> List(InstValid , ReadDisable, ReadEnable   , EXE_RES_SHIFT, EXE_SRL_OP , WriteEnable   , WRA_T1 , IMM_SHT),
      SRA       -> List(InstValid , ReadDisable, ReadEnable   , EXE_RES_SHIFT, EXE_SRA_OP , WriteEnable   , WRA_T1 , IMM_SHT),
      // 立即数
      ORI       -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_OR_OP  , WriteEnable   , WRA_T2 , IMM_LZE),
      ANDI      -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_AND_OP , WriteEnable   , WRA_T2 , IMM_LZE),
      XORI      -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_XOR_OP , WriteEnable   , WRA_T2 , IMM_LZE),
      LUI       -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_OR_OP  , WriteEnable   , WRA_T2 , IMM_HZE),

      // Move
      MOVN      -> List(InstValid , ReadEnable   , ReadEnable   , INST_MV , EXE_MOVN_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      MOVZ      -> List(InstValid , ReadEnable   , ReadEnable   , INST_MV , EXE_MOVZ_OP , WriteEnable   , WRA_T1 , IMM_N  ),

      // HI，LO的Move指令
      MFHI      -> List(InstValid , ReadDisable  , ReadDisable  , INST_MV , EXE_MFHI_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      MFLO      -> List(InstValid , ReadDisable  , ReadDisable  , INST_MV , EXE_MFLO_OP , WriteEnable   , WRA_T1 , IMM_N  ),
      MTHI      -> List(InstValid , ReadEnable   , ReadDisable  , INST_WO , EXE_MTHI_OP , WriteDisable  , WRA_X  , IMM_N  ),
      MTLO      -> List(InstValid , ReadEnable   , ReadDisable  , INST_WO , EXE_MTLO_OP , WriteDisable  , WRA_X  , IMM_N  ),
      // // C0的Move指令
      // MFC0      -> List(InstValid , ReadDisable  , ReadDisable  , INST_MV , EXE_MFC0 , WriteEnable   , WRA_T2 , IMM_N  ),
      // MTC0      -> List(InstValid , ReadDisable  , ReadEnable   , INST_WO , EXE_MTC0 , WriteDisable  , WRA_X  , IMM_N  ),

      // // 比较指令
      // SLT       -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_SLT , WriteEnable   , WRA_T1 , IMM_N  ),
      // SLTU      -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_SLTU, WriteEnable   , WRA_T1 , IMM_N  ),
      // // 立即数
      // SLTI      -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_SLT , WriteEnable   , WRA_T2 , IMM_LSE),
      // SLTIU     -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_SLTU, WriteEnable   , WRA_T2 , IMM_LSE),

      // // Trap
      // TEQ       -> List(InstValid , ReadEnable   , ReadEnable   , INST_TRAP, TRAP_EQ, WriteDisable  , WRA_X  , IMM_N  ),
      // TEQI      -> List(InstValid , ReadEnable   , ReadDisable, INST_TRAP, TRAP_EQ, WriteDisable  , WRA_X  , IMM_LSE),
      // TGE       -> List(InstValid , ReadEnable   , ReadEnable   , INST_TRAP, TRAP_GE, WriteDisable  , WRA_X  , IMM_N  ),
      // TGEI      -> List(InstValid , ReadEnable   , ReadDisable, INST_TRAP, TRAP_GE, WriteDisable  , WRA_X  , IMM_LSE),
      // TGEIU     -> List(InstValid , ReadEnable   , ReadDisable, INST_TRAP, TRAP_GEU, WriteDisable , WRA_X  , IMM_LSE),
      // TGEU      -> List(InstValid , ReadEnable   , ReadEnable   , INST_TRAP, TRAP_GEU, WriteDisable , WRA_X  , IMM_N  ),
      // TLT       -> List(InstValid , ReadEnable   , ReadEnable   , INST_TRAP, TRAP_LT, WriteDisable  , WRA_X  , IMM_N  ),
      // TLTI      -> List(InstValid , ReadEnable   , ReadDisable, INST_TRAP, TRAP_LT, WriteDisable  , WRA_X  , IMM_LSE),
      // TLTIU     -> List(InstValid , ReadEnable   , ReadDisable, INST_TRAP, TRAP_LTU, WriteDisable , WRA_X  , IMM_LSE),
      // TLTU      -> List(InstValid , ReadEnable   , ReadEnable   , INST_TRAP, TRAP_LTU, WriteDisable , WRA_X  , IMM_N  ),
      // TNE       -> List(InstValid , ReadEnable   , ReadEnable   , INST_TRAP, TRAP_NE, WriteDisable  , WRA_X  , IMM_N  ),
      // TNEI      -> List(InstValid , ReadEnable   , ReadDisable, INST_TRAP, TRAP_NE, WriteDisable  , WRA_X  , IMM_LSE),

      // // 算术指令
      // ADD       -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_ADD , WriteEnable   , WRA_T1 , IMM_N  ),
      // ADDU      -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_ADDU, WriteEnable   , WRA_T1 , IMM_N  ),
      // SUB       -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_SUB , WriteEnable   , WRA_T1 , IMM_N  ),
      // SUBU      -> List(InstValid , ReadEnable   , ReadEnable   , EXE_RES_LOGIC, EXE_SUBU, WriteEnable   , WRA_T1 , IMM_N  ),
      // MUL       -> List(InstValid , ReadEnable   , ReadEnable  ,  EXE_RES_LOGIC, EXE_MUL , WriteEnable   , WRA_T1 , IMM_N  ),
      // MULT      -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_MULT , WriteDisable  , WRA_X  , IMM_N  ),
      // MULTU     -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_MULTU, WriteDisable  , WRA_X  , IMM_N  ),
      // MADD      -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_MADD , WriteDisable  , WRA_X  , IMM_N  ),
      // MADDU     -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_MADDU, WriteDisable  , WRA_X  , IMM_N  ),
      // MSUB      -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_MSUB , WriteDisable  , WRA_X  , IMM_N  ),
      // MSUBU     -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_MSUBU, WriteDisable  , WRA_X  , IMM_N  ),
      // DIV       -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_DIV  , WriteDisable  , WRA_X  , IMM_N  ),
      // DIVU      -> List(InstValid , ReadEnable   , ReadEnable   , INST_WO , EXE_DIVU , WriteDisable  , WRA_X  , IMM_N  ),
      // CLO       -> List(InstValid , ReadEnable   , ReadDisable  , EXE_RES_LOGIC, EXE_CLO , WriteEnable   , WRA_T1 , IMM_N  ),
      // CLZ       -> List(InstValid , ReadEnable   , ReadDisable  , EXE_RES_LOGIC, EXE_CLZ , WriteEnable   , WRA_T1 , IMM_N  ),
      // // 立即数
      // ADDI      -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_ADD , WriteEnable   , WRA_T2 , IMM_LSE),
      // ADDIU     -> List(InstValid , ReadEnable   , ReadDisable, EXE_RES_LOGIC, EXE_ADDU, WriteEnable   , WRA_T2 , IMM_LSE),


      // // 跳转指令
      // J         -> List(InstValid , ReadDisable  , ReadDisable  , INST_BR , BR_J    , WriteDisable  , WRA_X  , IMM_N  ),
      // JAL       -> List(InstValid , ReadDisable  , ReadDisable  , INST_BR , BR_JAL  , WriteEnable   , WRA_T3 , IMM_N  ),
      // JR        -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_JR   , WriteDisable  , WRA_X  , IMM_N  ),
      // JALR      -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_JALR , WriteEnable   , WRA_T1 , IMM_N  ),
      // BEQ       -> List(InstValid , ReadEnable   , ReadEnable   , INST_BR , BR_EQ   , WriteDisable  , WRA_X  , IMM_N  ),
      // BNE       -> List(InstValid , ReadEnable   , ReadEnable   , INST_BR , BR_NE   , WriteDisable  , WRA_X  , IMM_N  ),
      // BGTZ      -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_GTZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BLEZ      -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_LEZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BGEZ      -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_GEZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BGEZAL    -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_GEZAL, WriteEnable   , WRA_T3 , IMM_N  ),
      // BLTZ      -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_LTZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BLTZAL    -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_LTZAL, WriteEnable   , WRA_T3 , IMM_N  ),
      // BEQL      -> List(InstValid , ReadEnable   , ReadEnable   , INST_BR , BR_EQ   , WriteDisable  , WRA_X  , IMM_N  ),
      // BNEL      -> List(InstValid , ReadEnable   , ReadEnable   , INST_BR , BR_NE   , WriteDisable  , WRA_X  , IMM_N  ),
      // BGTZL     -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_GTZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BLEZL     -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_LEZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BGEZL     -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_GEZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BGEZALL   -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_GEZAL, WriteEnable   , WRA_T3 , IMM_N  ),
      // BLTZL     -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_LTZ  , WriteDisable  , WRA_X  , IMM_N  ),
      // BLTZALL   -> List(InstValid , ReadEnable   , ReadDisable  , INST_BR , BR_LTZAL, WriteEnable   , WRA_T3 , IMM_N  ),

      // // TLB
      // TLBP      -> List(InstValid , ReadDisable  , ReadDisable  , INST_TLB, TLB_P   , WriteDisable  , WRA_X  , IMM_N  ),
      // TLBR      -> List(InstValid , ReadDisable  , ReadDisable  , INST_TLB, TLB_R   , WriteDisable  , WRA_X  , IMM_N  ),
      // TLBWI     -> List(InstValid , ReadDisable  , ReadDisable  , INST_TLB, TLB_WI  , WriteDisable  , WRA_X  , IMM_N  ),
      // TLBWR     -> List(InstValid , ReadDisable  , ReadDisable  , INST_TLB, TLB_WR  , WriteDisable  , WRA_X  , IMM_N  ),

      // // 例外指令
      // SYSCALL   -> List(InstValid , ReadDisable  , ReadDisable  , INST_EXC, EXC_SC  , WriteDisable  , WRA_X  , IMM_N  ),
      // BREAK     -> List(InstValid , ReadDisable  , ReadDisable  , INST_EXC, EXC_BR  , WriteDisable  , WRA_X  , IMM_N  ),
      // ERET      -> List(InstValid , ReadDisable  , ReadDisable  , INST_EXC, EXC_ER  , WriteDisable  , WRA_X  , IMM_N  ),
      // WAIT      -> List(InstValid , ReadDisable  , ReadDisable  , INST_EXC, EXC_WAIT, WriteDisable  , WRA_X  , IMM_N  ),

      // // 访存指令
      // LB        -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_LB  , WriteEnable   , WRA_T2 , IMM_N  ),
      // LBU       -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_LBU , WriteEnable   , WRA_T2 , IMM_N  ),
      // LH        -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_LH  , WriteEnable   , WRA_T2 , IMM_N  ),
      // LHU       -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_LHU , WriteEnable   , WRA_T2 , IMM_N  ),
      // LW        -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_LW  , WriteEnable   , WRA_T2 , IMM_N  ),
      // SB        -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_SB  , WriteDisable  , WRA_X  , IMM_N  ),
      // SH        -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_SH  , WriteDisable  , WRA_X  , IMM_N  ),
      // SW        -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_SW  , WriteDisable  , WRA_X  , IMM_N  ),
      // LWL       -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_LWL , WriteEnable   , WRA_T2 , IMM_N  ),
      // LWR       -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_LWR , WriteEnable   , WRA_T2 , IMM_N  ),
      // SWL       -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_SWL , WriteDisable  , WRA_X  , IMM_N  ),
      // SWR       -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_SWR , WriteDisable  , WRA_X  , IMM_N  ),
      // LL        -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_LL  , WriteEnable   , WRA_T2 , IMM_N  ),
      // SC        -> List(InstValid , ReadEnable   , ReadEnable   , INST_MEM, MEM_SC  , WriteEnable   , WRA_T2 , IMM_N  ),


      SYNC      -> List(InstValid , ReadDisable  , ReadEnable     , EXE_RES_NOP  , EXE_SRL_OP    , WriteDisable  , WRA_X  , IMM_N  ),
      PREF      -> List(InstValid , ReadDisable  , ReadDisable  , EXE_RES_NOP  , EXE_NOP_OP    , WriteEnable    , WRA_X  , IMM_N  ),
      PREFX     -> List(InstValid , ReadDisable  , ReadDisable  , EXE_RES_NOP  , EXE_NOP_OP    , WriteDisable  , WRA_X  , IMM_N  ),

      // // Cache
      // CACHE     -> List(InstValid , ReadEnable   , ReadDisable  , INST_MEM, MEM_CAC , WriteDisable  , WRA_X  , IMM_N  ),
    )
  )
  // @formatter:on


  val (csInstValid: Bool) :: (op1_type: Bool) :: (op2_type: Bool) :: csInstType :: cs0 =
    signals
  val csOpType :: (csWReg: Bool) :: csWRType :: csIMMType :: Nil = cs0

  val instValid = Bool()
  // val wreg        = Bool() // write to register-file
  val wraType = UInt(2.W) // write register request type
  val immType = UInt(3.W)

  instValid := csInstValid
  wraType := csWRType
  immType := csIMMType

  reg1_read := op1_type
  reg2_read := op2_type

  imm := MuxLookup(
    immType,
    Util.zeroExtend(sa), // default IMM_SHT
    Array(
      IMM_LSE -> Util.signedExtend(imm16),
      IMM_LZE -> Util.zeroExtend(imm16),
      IMM_HZE -> Cat(imm16, Fill(16, 0.U))
    )
  )

  wd := MuxLookup(
    wraType,
    "b11111".U(5.W), // 取"b11111", 即31号寄存器
    Array(
      WRA_T1 -> rd, // 取inst(15,11)
      WRA_T2 -> rt // 取inst(20,16)
    )
  )

  aluop := csOpType
  alusel := csInstType
  wreg:=csWReg

//确定运算源操作数1
  when(reg1_read === ReadEnable  ) {
    reg1 := reg1_data
  }.elsewhen(reg1_read === ReadDisable) {
    reg1 := imm
  }.otherwise {
    reg1 := ZeroWord
  }

//确定运算源操作数2
  when(reg2_read === ReadEnable  ) {
    reg2 := reg2_data
  }.elsewhen(reg2_read === ReadDisable) {
    reg2 := imm
  }.otherwise {
    reg2 := ZeroWord
  }
}
