package cpu.puamips

import Const._
import chisel3._
import chisel3.util._
import UInt._

class Decoder extends Module {
  val io = IO(new Bundle {
    // 从各个流水线阶段传来的信号
    val fromFetch = Flipped(new Fetch_Decoder())
    val fromTop = Flipped(new Top_Decoder())
    val fromRegfile = Flipped(new RegFile_Decoder())
    val fromExecute = Flipped(new Execute_Decoder())
    val fromMemory = Flipped(new Memory_Decoder())

    val regfile = new Decoder_RegFile()
    val execute = new Decoder_Execute()
  })
  // input-fetch
  val pc = RegInit(REG_BUS_INIT)
  pc := io.fromFetch.pc

  // input-inst memory
  val inst = RegInit(REG_BUS_INIT)
  inst := io.fromTop.inst

  // input-regfile
  val reg1_data = RegInit(REG_BUS_INIT)
  val reg2_data = RegInit(REG_BUS_INIT)
  reg1_data := io.fromRegfile.rdata1
  reg2_data := io.fromRegfile.rdata2

  // input-execute
  val exWdata = RegInit(REG_BUS_INIT)
  val exWd = RegInit(REG_ADDR_BUS_INIT)
  val exWreg = RegInit(false.B)
  exWdata := io.fromExecute.wdata
  exWd := io.fromExecute.wd
  exWreg := io.fromExecute.wreg

  // input-memory
  val memWdata = RegInit(REG_BUS_INIT)
  val memWd = RegInit(REG_ADDR_BUS_INIT)
  val memWreg = RegInit(false.B)
  memWdata := io.fromMemory.wdata
  memWd := io.fromMemory.wd
  memWreg := io.fromMemory.wreg

  // Output-regfile
  val reg1_read = RegInit(false.B)
  val reg2_read = RegInit(false.B)
  val reg1_addr = RegInit(REG_ADDR_BUS_INIT)
  val reg2_addr = RegInit(REG_ADDR_BUS_INIT)
  io.regfile.reg1_read := reg1_read
  io.regfile.reg2_read := reg2_read
  io.regfile.reg1_addr := reg1_addr
  io.regfile.reg2_addr := reg2_addr

  // Output-execute
  val aluop = RegInit(ALU_OP_BUS_INIT)
  val alusel = RegInit(ALU_SEL_BUS_INIT)
  val reg1 = RegInit(REG_BUS_INIT)
  val reg2 = RegInit(REG_BUS_INIT)
  val wd = RegInit(REG_ADDR_BUS_INIT)
  val wreg = RegInit(false.B)
  io.execute.pc := pc
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
  val imm = Reg(REG_BUS)

  // 指示指令是否有效
  val instvalid = RegInit(false.B)

  val rt = Wire(UInt(5.W))
  val rd = Wire(UInt(5.W))
  val sa = Wire(UInt(5.W))
  val rs = Wire(UInt(5.W))
  val imm16 = Wire(UInt(16.W))

  rt := inst(20, 16)
  rd := inst(15, 11)
  sa := inst(10, 6)
  rs := inst(25, 21)
  imm16 := inst(15, 0)

  // 对指令进行译码
  instvalid := INST_INVALID
  aluop := EXE_NOP_OP
  alusel := EXE_RES_NOP
  wd := rd // inst(15, 11)
  wreg := WRITE_DISABLE
  reg1_read := READ_DISABLE  
  reg2_read := READ_DISABLE  
  reg1_addr := rs // inst(25, 21)
  reg2_addr := rt // inst(20, 16)
  imm := ZERO_WORD

  val signals: List[UInt] = ListLookup(
    inst,
  // @formatter:off
    List(INST_INVALID, READ_DISABLE  , READ_DISABLE  , EXE_RES_NOP, EXE_NOP_OP, WRITE_DISABLE, WRA_X, IMM_N),
    Array(         /* val  | Op1    | Op2    | inst    |operation| Write | WReg   | Imm */
                   /* inst | sel    | sel    | type    | type    | reg   | Target | type */
      // 位操作
      OR        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_OR_OP  , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      AND       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_AND_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      XOR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_XOR_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      NOR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_NOR_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // 移位
      SLLV      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_SHIFT, EXE_SLL_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SRLV      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRL_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SRAV      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRA_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      SLL       -> List(INST_VALID , READ_DISABLE  , READ_ENABLE   , EXE_RES_SHIFT, EXE_SLL_OP , WRITE_ENABLE   , WRA_T1 , IMM_SHT),
      SRL       -> List(INST_VALID , READ_DISABLE  , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRL_OP , WRITE_ENABLE   , WRA_T1 , IMM_SHT),
      SRA       -> List(INST_VALID , READ_DISABLE  , READ_ENABLE   , EXE_RES_SHIFT, EXE_SRA_OP , WRITE_ENABLE   , WRA_T1 , IMM_SHT),
      // 立即数
      ORI       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_OR_OP  , WRITE_ENABLE   , WRA_T2 , IMM_LZE),
      ANDI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_AND_OP , WRITE_ENABLE   , WRA_T2 , IMM_LZE),
      XORI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_XOR_OP , WRITE_ENABLE   , WRA_T2 , IMM_LZE),
      LUI       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_OR_OP  , WRITE_ENABLE   , WRA_T2 , IMM_HZE),

      // Move
      MOVN      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MV , EXE_MOVN_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MOVZ      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MV , EXE_MOVZ_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),

      // HI，LO的Move指令
      MFHI      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_MV , EXE_MFHI_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MFLO      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_MV , EXE_MFLO_OP , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      MTHI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_WO , EXE_MTHI_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      MTLO      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_WO , EXE_MTLO_OP , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // // C0的Move指令
      // MFC0      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_MV , EXE_MFC0 , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // MTC0      -> List(INST_VALID , READ_DISABLE    , READ_ENABLE   , INST_WO , EXE_MTC0 , WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // // 比较指令
      // SLT       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_SLT , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // SLTU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_SLTU, WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // // 立即数
      // SLTI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_SLT , WRITE_ENABLE   , WRA_T2 , IMM_LSE),
      // SLTIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_SLTU, WRITE_ENABLE   , WRA_T2 , IMM_LSE),

      // // Trap
      // TEQ       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_TRAP, TRAP_EQ, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TEQI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , INST_TRAP, TRAP_EQ, WRITE_DISABLE  , WRA_X  , IMM_LSE),
      // TGE       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_TRAP, TRAP_GE, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TGEI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , INST_TRAP, TRAP_GE, WRITE_DISABLE  , WRA_X  , IMM_LSE),
      // TGEIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , INST_TRAP, TRAP_GEU, WRITE_DISABLE , WRA_X  , IMM_LSE),
      // TGEU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_TRAP, TRAP_GEU, WRITE_DISABLE , WRA_X  , IMM_N  ),
      // TLT       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_TRAP, TRAP_LT, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLTI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , INST_TRAP, TRAP_LT, WRITE_DISABLE  , WRA_X  , IMM_LSE),
      // TLTIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , INST_TRAP, TRAP_LTU, WRITE_DISABLE , WRA_X  , IMM_LSE),
      // TLTU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_TRAP, TRAP_LTU, WRITE_DISABLE , WRA_X  , IMM_N  ),
      // TNE       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_TRAP, TRAP_NE, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TNEI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , INST_TRAP, TRAP_NE, WRITE_DISABLE  , WRA_X  , IMM_LSE),

      // // 算术指令
      // ADD       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_ADD , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // ADDU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_ADDU, WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // SUB       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_SUB , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // SUBU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , EXE_RES_LOGIC, EXE_SUBU, WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // MUL       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE  ,  EXE_RES_LOGIC, EXE_MUL , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // MULT      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_MULT , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // MULTU     -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_MULTU, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // MADD      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_MADD , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // MADDU     -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_MADDU, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // MSUB      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_MSUB , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // MSUBU     -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_MSUBU, WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // DIV       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_DIV  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // DIVU      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_WO , EXE_DIVU , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // CLO       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOGIC, EXE_CLO , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // CLZ       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , EXE_RES_LOGIC, EXE_CLZ , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // // 立即数
      // ADDI      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_ADD , WRITE_ENABLE   , WRA_T2 , IMM_LSE),
      // ADDIU     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE  , EXE_RES_LOGIC, EXE_ADDU, WRITE_ENABLE   , WRA_T2 , IMM_LSE),


      // // 跳转指令
      // J         -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_BR , BR_J    , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // JAL       -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_BR , BR_JAL  , WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // JR        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_JR   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // JALR      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_JALR , WRITE_ENABLE   , WRA_T1 , IMM_N  ),
      // BEQ       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_BR , BR_EQ   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BNE       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_BR , BR_NE   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGTZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_GTZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLEZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_LEZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_GEZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZAL    -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_GEZAL, WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // BLTZ      -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_LTZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLTZAL    -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_LTZAL, WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // BEQL      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_BR , BR_EQ   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BNEL      -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_BR , BR_NE   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGTZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_GTZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLEZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_LEZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_GEZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BGEZALL   -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_GEZAL, WRITE_ENABLE   , WRA_T3 , IMM_N  ),
      // BLTZL     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_LTZ  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BLTZALL   -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_BR , BR_LTZAL, WRITE_ENABLE   , WRA_T3 , IMM_N  ),

      // // TLB
      // TLBP      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_P   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBR      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_R   , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBWI     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_WI  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // TLBWR     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_TLB, TLB_WR  , WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // // 例外指令
      // SYSCALL   -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_EXC, EXC_SC  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // BREAK     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_EXC, EXC_BR  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // ERET      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_EXC, EXC_ER  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // WAIT      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , INST_EXC, EXC_WAIT, WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // // 访存指令
      // LB        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_LB  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // LBU       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_LBU , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // LH        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_LH  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // LHU       -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_LHU , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // LW        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_LW  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // SB        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_SB  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // SH        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_SH  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // SW        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_SW  , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // LWL       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_LWL , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // LWR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_LWR , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // SWL       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_SWL , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // SWR       -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_SWR , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      // LL        -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_LL  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),
      // SC        -> List(INST_VALID , READ_ENABLE   , READ_ENABLE   , INST_MEM, MEM_SC  , WRITE_ENABLE   , WRA_T2 , IMM_N  ),


      SYNC      -> List(INST_VALID , READ_DISABLE    , READ_ENABLE     , EXE_RES_NOP  , EXE_SRL_OP    , WRITE_DISABLE  , WRA_X  , IMM_N  ),
      PREF      -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP  , EXE_NOP_OP    , WRITE_ENABLE    , WRA_X  , IMM_N  ),
      PREFX     -> List(INST_VALID , READ_DISABLE    , READ_DISABLE    , EXE_RES_NOP  , EXE_NOP_OP    , WRITE_DISABLE  , WRA_X  , IMM_N  ),

      // // Cache
      // CACHE     -> List(INST_VALID , READ_ENABLE   , READ_DISABLE    , INST_MEM, MEM_CAC , WRITE_DISABLE  , WRA_X  , IMM_N  ),
    )
  )
  // @formatter:on

  val (csInstValid: Bool) :: (op1_type: Bool) :: (op2_type: Bool) :: csInstType :: cs0 =
    signals
  val csOpType :: (csWReg: Bool) :: csWRType :: csIMMType :: Nil = cs0

  val instValid = Wire(Bool())
  val wraType = Wire(UInt(2.W)) 
  val immType = Wire(UInt(3.W))

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
  wreg := csWReg

//确定运算源操作数1
  when(reg1_read === READ_ENABLE) {
    reg1 := reg1_data
  }.elsewhen(reg1_read === READ_DISABLE  ) {
    reg1 := imm
  }.otherwise {
    reg1 := ZERO_WORD
  }

//确定运算源操作数2
  when(reg2_read === READ_ENABLE) {
    reg2 := reg2_data
  }.elsewhen(reg2_read === READ_DISABLE  ) {
    reg2 := imm
  }.otherwise {
    reg2 := ZERO_WORD
  }

  // debug
  printf(p"decoder :pc 0x${Hexadecimal(pc)}, inst 0x${Hexadecimal(inst)}\n")
}
