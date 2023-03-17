package cpu.puamips

import chisel3._
import chisel3.util._
import cpu.puamips.Const._
import scala.annotation.switch

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

  // 对指令进行译码
  when(reset.asBool === RstEnable) {
    aluop := EXE_NOP_OP
    alusel := EXE_RES_NOP
    wd := NOPRegAddr
    wreg := WriteDisable
    instvalid := InstInvalid
    reg1_read := 0.U
    reg2_read := 0.U
    reg1_addr := NOPRegAddr
    reg2_addr := NOPRegAddr
    imm := 0.U
  }.otherwise {
    aluop := EXE_NOP_OP
    alusel := EXE_RES_NOP
    wd := io.inst(15, 11)
    wreg := WriteDisable
    instvalid := InstInvalid
    reg1_read := 0.U
    reg2_read := 0.U
    reg1_addr := io.inst(25, 21) // 默认第一个操作数寄存器为端口1读取的寄存器
    reg2_addr := io.inst(20, 16) // 默认第二个操作寄存器为端口2读取的寄存器
    imm := ZeroWord

    switch(op) {
      is(EXE_SPECIALNST) {
        switch(op2) {
          is(0.U(5.W)) {
            switch(op3) {
              is(EXE) {
                wreg := WriteEnable
                aluop := EXE_OP
                alusel := EXE_RES_LOGIC
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_AND) {
                wreg := WriteEnable
                aluop := EXE_AND_OP
                alusel := EXE_RES_LOGIC
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_XOR) {
                wreg := WriteEnable
                aluop := EXE_XOR_OP
                alusel := EXE_RES_LOGIC
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_NOR) {
                wreg := WriteEnable
                aluop := EXE_NOR_OP
                alusel := EXE_RES_LOGIC
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_SLLV) {
                wreg := WriteEnable
                aluop := EXE_SLL_OP
                alusel := EXE_RES_SHIFT
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_SRLV) {
                wreg := WriteEnable
                aluop := EXE_SRL_OP
                alusel := EXE_RES_SHIFT
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_SRAV) {
                wreg := WriteEnable
                aluop := EXE_SRA_OP
                alusel := EXE_RES_SHIFT
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_SYNC) {
                wreg := WriteDisable
                aluop := EXE_SRL_OP
                alusel := EXE_RES_NOP
                reg1_read := 0.U
                reg2_read := 1.U
                instvalid := InstValid
              }
              is(EXE_MFHI) {
                wreg := WriteEnable
                aluop := EXE_MFHI_OP
                alusel := EXE_RES_MOVE
                reg1_read := 0.U
                reg2_read := 0.U
                instvalid := InstValid
              }
              is(EXE_MFLO) {
                wreg := WriteEnable
                aluop := EXE_MFLO_OP
                alusel := EXE_RES_MOVE
                reg1_read := 0.U
                reg2_read := 0.U
                instvalid := InstValid
              }
              is(EXE_MTHI) {
                wreg := WriteDisable
                aluop := EXE_MTHI_OP
                reg1_read := 1.U
                reg2_read := 0.U
                instvalid := InstValid
              }
              is(EXE_MTLO) {
                wreg := WriteDisable
                aluop := EXE_MTLO_OP
                reg1_read := 1.U
                reg2_read := 0.U
                instvalid := InstValid
              }
              is(EXE_MOVN) {
                aluop := EXE_MOVN_OP
                alusel := EXE_RES_MOVE
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
                when(reg2 === ZeroWord) {
                  // reg2_o的值为rt通用寄存器的值
                  wreg := WriteEnable
                }.otherwise {
                  wreg := WriteDisable
                }
              }
              is(EXE_MOVZ) {
                aluop := EXE_MOVZ_OP
                alusel := EXE_RES_MOVE
                reg1_read := 1.U
                reg2_read := 1.U
                instvalid := InstValid
                when(reg2 === ZeroWord) {
                  // reg2_o的值为rt通用寄存器的值
                  wreg := WriteEnable
                }.otherwise {
                  wreg := WriteDisable
                }
              }
            } // op3
          } // 0.U(5.W)
        }
      } // EXE_SPECIALNST
      is(EXEI) {
        wreg := WriteEnable
        // 运算的子类型是逻辑“或”运算
        aluop := EXE_OP
        // 运算类型是逻辑运算
        alusel := EXE_RES_LOGIC
        // 需要通过Regfile的读端口1读寄存器
        reg1_read := 1.U
        // 需要通过Regfile的读端口2读寄存器
        reg2_read := 0.U
        // 指令执行需要的立即数
        imm := Cat(0.U(16.W), io.inst(15, 0))
        // 指令执行要写的目的寄存器
        wd := io.inst(20, 16)
        // ori指令有效
        instvalid := InstValid
      } // EXEI
      is(EXE_ANDI) {
        wreg := WriteEnable
        aluop := EXE_AND_OP
        alusel := EXE_RES_LOGIC
        reg1_read := 1.U
        reg2_read := 0.U
        imm := Cat(0.U(16.W), io.inst(15, 0))
        wd := io.inst(20, 16)
        instvalid := InstValid
      }
      is(EXE_XORI) {
        wreg := WriteEnable
        aluop := EXE_XOR_OP
        alusel := EXE_RES_LOGIC
        reg1_read := 1.U
        reg2_read := 0.U
        imm := Cat(0.U(16.W), io.inst(15, 0))
        wd := io.inst(20, 16)
        instvalid := InstValid
      }
      is(EXE_LUI) {
        wreg := WriteEnable
        aluop := EXE_OP
        alusel := EXE_RES_LOGIC
        reg1_read := 1.U
        reg2_read := 0.U
        imm := Cat(io.inst(15, 0), 0.U(16.W))
        wd := io.inst(20, 16)
        instvalid := InstValid
      }
      is(EXE_PREF) {
        wreg := WriteEnable
        aluop := EXE_NOP_OP
        alusel := EXE_RES_NOP
        reg1_read := 0.U
        reg2_read := 0.U
        instvalid := InstValid
      }
    }
    when(io.inst(31, 21) === 0.U) {
      when(op3 === EXE_SLL) {
        wreg := WriteEnable;
        aluop := EXE_SLL_OP;
        alusel := EXE_RES_SHIFT;
        reg1_read := 0.U;
        reg2_read := 1.U;
        imm := io.inst(10, 6);
        wd := io.inst(15, 11);
        instvalid := InstValid;
      }.elsewhen(op3 === EXE_SRL) {
        wreg := WriteEnable;
        aluop := EXE_SRL_OP;
        alusel := EXE_RES_SHIFT;
        reg1_read := 0.U;
        reg2_read := 1.U;
        imm := io.inst(10, 6);
        wd := io.inst(15, 11);
        instvalid := InstValid;
      }.elsewhen(op3 === EXE_SRA) {
        wreg := WriteEnable;
        aluop := EXE_SRA_OP;
        alusel := EXE_RES_SHIFT;
        reg1_read := 0.U;
        reg2_read := 1.U;
        imm := io.inst(10, 6);
        wd := io.inst(15, 11);
        instvalid := InstValid;
      }
    }
  }

//确定运算源操作数1
  when(reset.asBool === RstEnable) {
    reg1 := ZeroWord
  }.elsewhen(reg1_read === 1.U) {
    reg1 := io.reg1_data
  }.elsewhen(reg1_read === 0.U) {
    reg1 := imm
  }.otherwise {
    reg1 := ZeroWord
  }

//确定运算源操作数2
  when(reset.asBool === RstEnable) {
    reg2 := ZeroWord
  }.elsewhen(reg2_read === 1.U) {
    reg2 := io.reg2_data
  }.elsewhen(reg2_read === 0.U) {
    reg2 := imm
  }.otherwise {
    reg2 := ZeroWord
  }
}
