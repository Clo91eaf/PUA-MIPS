package cpu.openmips

import chisel3._
import chisel3.util._
import cpu.openmips.Constants._
import scala.annotation.switch

class Id extends Module {
  val io = IO(new Bundle {
    val pc_i = Input(InstAddrBus)
    val inst_i = Input(InstBus)

// 读取得Regfile的值
    val reg1_data_i = Input(RegBus)
    val reg2_data_i = Input(RegBus)

// 输出到Regfile的信息
    val reg1_read_o = Output(Bool())
    val reg2_read_o = Output(Bool())
    val reg1_addr_o = Output(RegAddrBus)
    val reg2_addr_o = Output(RegAddrBus)

// 输出到执行阶段
    val aluop_o = Output(AluOpBus)
    val alusel_o = Output(AluSelBus)
    val reg1_o = Output(RegBus)
    val reg2_o = Output(RegBus)
    val wd_o = Output(RegAddrBus)
    val wreg_o = Output(Bool())
  })

  // 输出到Regfile的信息
  val reg1_read_or = Reg(Bool())
  val reg2_read_or = Reg(Bool())
  val reg1_addr_or = Reg(RegAddrBus)
  val reg2_addr_or = Reg(RegAddrBus)

  // 输出到执行阶段
  val aluop_or = Reg(AluOpBus)
  val alusel_or = Reg(AluSelBus)
  val reg1_or = Reg(RegBus)
  val reg2_or = Reg(RegBus)
  val wd_or = Reg(RegAddrBus)
  val wreg_or = Reg(Bool())

  io.reg1_read_o := reg1_read_or
  io.reg2_read_o := reg2_read_or
  io.reg1_addr_o := reg1_addr_or
  io.reg2_addr_o := reg2_addr_or
  io.aluop_o := aluop_or
  io.alusel_o := alusel_or
  io.reg1_o := reg1_or
  io.reg2_o := reg2_or
  io.wd_o := wd_or
  io.wreg_o := wreg_or

  // 取得的指令码功能码
  val op = Wire(UInt(6.W))
  val op2 = Wire(UInt(6.W))
  val op3 = Wire(UInt(6.W))
  val op4 = Wire(UInt(6.W))
  op := io.inst_i(31, 26)
  op2 := io.inst_i(10, 6)
  op3 := io.inst_i(5, 0)
  op4 := io.inst_i(20, 16)

  // 保存指令执行需要的立即数
  val imm = Reg(RegBus)

  // 指示指令是否有效
  val instvalid = Reg(Bool())

  // 对指令进行译码
  when(reset.asBool === RstEnable) {
    aluop_or := EXE_NOP_OP
    alusel_or := EXE_RES_NOP
    wd_or := NOPRegAddr
    wreg_or := WriteDisable
    instvalid := InstInvalid
    reg1_read_or := "b0".U
    reg2_read_or := "b0".U
    reg1_addr_or := NOPRegAddr
    reg2_addr_or := NOPRegAddr
    imm := "h0".U
  }.otherwise {
    aluop_or := EXE_NOP_OP
    alusel_or := EXE_RES_NOP
    wd_or := io.inst_i(15, 11)
    wreg_or := WriteDisable
    instvalid := InstInvalid
    reg1_read_or := "b0".U
    reg2_read_or := "b0".U
    reg1_addr_or := io.inst_i(25, 21) // 默认第一个操作数寄存器为端口1读取的寄存器
    reg2_addr_or := io.inst_i(20, 16) // 默认第二个操作寄存器为端口2读取的寄存器
    imm := ZeroWord

    switch(op) {
      is(EXE_SPECIAL_INST) {
        switch(op2) {
          is(0.U(5.W)) {
            switch(op3) {
              is(EXE_OR) {
                wreg_or := WriteEnable
                aluop_or := EXE_OR_OP
                alusel_or := EXE_RES_LOGIC
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_AND) {
                wreg_or := WriteEnable
                aluop_or := EXE_AND_OP
                alusel_or := EXE_RES_LOGIC
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_XOR) {
                wreg_or := WriteEnable
                aluop_or := EXE_XOR_OP
                alusel_or := EXE_RES_LOGIC
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_NOR) {
                wreg_or := WriteEnable
                aluop_or := EXE_NOR_OP
                alusel_or := EXE_RES_LOGIC
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_SLLV) {
                wreg_or := WriteEnable
                aluop_or := EXE_SLL_OP
                alusel_or := EXE_RES_SHIFT
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_SRLV) {
                wreg_or := WriteEnable
                aluop_or := EXE_SRL_OP
                alusel_or := EXE_RES_SHIFT
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_SRAV) {
                wreg_or := WriteEnable
                aluop_or := EXE_SRA_OP
                alusel_or := EXE_RES_SHIFT
                reg1_read_or := 1.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
              is(EXE_SYNC) {
                wreg_or := WriteDisable
                aluop_or := EXE_SRL_OP
                alusel_or := EXE_RES_NOP
                reg1_read_or := 0.U
                reg2_read_or := 1.U
                instvalid := InstValid
              }
            } // op3
          } // 0.U(5.W)
        }
      } // EXE_SPECIAL_INST
      is(EXE_ORI) {
        wreg_or := WriteEnable
        // 运算的子类型是逻辑“或”运算
        aluop_or := EXE_OR_OP
        // 运算类型是逻辑运算
        alusel_or := EXE_RES_LOGIC
        // 需要通过Regfile的读端口1读寄存器
        reg1_read_or := "b1".U
        // 需要通过Regfile的读端口2读寄存器
        reg2_read_or := "b0".U
        // 指令执行需要的立即数
        imm := Cat("h0".U(16.W), io.inst_i(15, 0))
        // 指令执行要写的目的寄存器
        wd_or := io.inst_i(20, 16)
        // ori指令有效
        instvalid := InstValid
      } // EXE_ORI
      is(EXE_ANDI) {
        wreg_or := WriteEnable
        aluop_or := EXE_AND_OP
        alusel_or := EXE_RES_LOGIC
        reg1_read_or := 1.U
        reg2_read_or := 0.U
        imm := Cat("h0".U(16.W), io.inst_i(15, 0))
        wd_or := io.inst_i(20, 16)
        instvalid := InstValid
      }
      is(EXE_XORI) {
        wreg_or := WriteEnable
        aluop_or := EXE_XOR_OP
        alusel_or := EXE_RES_LOGIC
        reg1_read_or := 1.U
        reg2_read_or := 0.U
        imm := Cat("h0".U(16.W), io.inst_i(15, 0))
        wd_or := io.inst_i(20, 16)
        instvalid := InstValid
      }
      is(EXE_LUI) {
        wreg_or := WriteEnable
        aluop_or := EXE_OR_OP
        alusel_or := EXE_RES_LOGIC
        reg1_read_or := 1.U
        reg2_read_or := 0.U
        imm := Cat(io.inst_i(15, 0), "h0".U(16.W))
        wd_or := io.inst_i(20, 16)
        instvalid := InstValid
      }
      is(EXE_PREF) {
        wreg_or := WriteEnable
        aluop_or := EXE_NOP_OP
        alusel_or := EXE_RES_NOP
        reg1_read_or := 0.U
        reg2_read_or := 0.U
        instvalid := InstValid
      }
    }
    when(io.inst_i(31, 21) === "b00000000000".U) {
      when(op3 === EXE_SLL) {
        wreg_or := WriteEnable;
        aluop_or := EXE_SLL_OP;
        alusel_or := EXE_RES_SHIFT;
        reg1_read_or := "b0".U;
        reg2_read_or := "b1".U;
        imm := io.inst_i(10, 6);
        wd_or := io.inst_i(15, 11);
        instvalid := InstValid;
      }.elsewhen(op3 === EXE_SRL) {
        wreg_or := WriteEnable;
        aluop_or := EXE_SRL_OP;
        alusel_or := EXE_RES_SHIFT;
        reg1_read_or := "b0".U;
        reg2_read_or := "b1".U;
        imm := io.inst_i(10, 6);
        wd_or := io.inst_i(15, 11);
        instvalid := InstValid;
      }.elsewhen(op3 === EXE_SRA) {
        wreg_or := WriteEnable;
        aluop_or := EXE_SRA_OP;
        alusel_or := EXE_RES_SHIFT;
        reg1_read_or := "b0".U;
        reg2_read_or := "b1".U;
        imm := io.inst_i(10, 6);
        wd_or := io.inst_i(15, 11);
        instvalid := InstValid;
      }
    }
  }

//确定运算源操作数1
  when(reset.asBool === RstEnable) {
    reg1_or := ZeroWord
  }.elsewhen(reg1_read_or === "b1".U) {
    reg1_or := io.reg1_data_i
  }.elsewhen(reg1_read_or === "b0".U) {
    reg1_or := imm
  }.otherwise {
    reg1_or := ZeroWord
  }

//确定运算源操作数2
  when(reset.asBool === RstEnable) {
    reg2_or := ZeroWord
  }.elsewhen(reg2_read_or === "b1".U) {
    reg2_or := io.reg2_data_i
  }.elsewhen(reg2_read_or === "b0".U) {
    reg2_or := imm
  }.otherwise {
    reg2_or := ZeroWord
  }
}
