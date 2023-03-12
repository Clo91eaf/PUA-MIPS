package cpu.ori
import cpu.ori.Constants._
import chisel3.util._
import chisel3._

class OpenMips extends Module {
  val io = IO(new Bundle {
    val rom_data_i = Input(RegBus)
    val rom_addr_o = Output(RegBus)
    val rom_ce_o = Output(Bool())
  })

  // 连接 ID 模块和 EX 模块
  val id_aluop = Wire(AluOpBus)
  val id_alusel = Wire(AluSelBus)
  val id_reg1 = Wire(RegBus)
  val id_reg2 = Wire(RegBus)
  val id_wreg = Wire(Bool())
  val id_wd = Wire(RegAddrBus)
  // 连接 ID 模块和 Regfile 模块
  val reg1_read = Wire(Bool())
  val reg2_read = Wire(Bool())
  val reg1_data = Wire(RegBus)
  val reg2_data = Wire(RegBus)
  val reg1_addr = Wire(RegAddrBus)
  val reg2_addr = Wire(RegAddrBus)
  // 连接 EX 模块和 WB 模块
  val ex_wreg = Wire(Bool())
  val ex_wd = Wire(RegAddrBus)
  val ex_wdata = Wire(RegBus)
  // 连接 WB 模块和 Regfile 模块
  val wb_wd = Wire(RegAddrBus)
  val wb_wreg = Wire(Bool())
  val wb_wdata = Wire(RegBus)

  // pc_reg 实例化
  val pc_reg0 = Module(new PC_reg())
  io.rom_addr_o := pc_reg0.io.pc
  io.rom_ce_o := pc_reg0.io.ce

  // ID 实例化
  val id0 = Module(new Id())
  id0.io.pc_i := io.rom_addr_o
  id0.io.inst_i := io.rom_data_i
  // 来自 Regfile 模块的输入
  id0.io.reg1_data_i := reg1_data
  id0.io.reg2_data_i := reg2_data
  // 送到 Regfile 模块的信息
  reg1_read := id0.io.reg1_read_o
  reg2_read := id0.io.reg2_read_o
  reg1_addr := id0.io.reg1_addr_o
  reg2_addr := id0.io.reg2_addr_o
  // 送到 EX 模块的信息
  id_aluop := id0.io.aluop_o
  id_alusel := id0.io.alusel_o
  id_reg1 := id0.io.reg1_o
  id_reg2 := id0.io.reg2_o
  id_wd := id0.io.wd_o
  id_wreg := id0.io.wreg_o

  // Regfile 实例化
  val regfile1 = Module(new Regfile())
  // 从 WB 模块传来信息
  regfile1.io.we := wb_wreg
  regfile1.io.waddr := wb_wd
  regfile1.io.wdata := wb_wdata
  regfile1.io.re1 := reg1_read
  regfile1.io.raddr1 := reg1_addr
  reg1_data := regfile1.io.rdata1
  regfile1.io.re2 := reg2_read
  regfile1.io.raddr2 := reg2_addr
  reg2_data := regfile1.io.rdata2

  val ex0 = Module(new Ex())
  // input
  ex0.io.aluop_i := id_aluop
  ex0.io.alusel_i := id_alusel
  ex0.io.reg1_i := id_reg1
  ex0.io.reg2_i := id_reg2
  ex0.io.wd_i := id_wd
  ex0.io.wreg_i := id_wreg
  // output
  ex_wd := ex0.io.wd_o
  ex_wreg := ex0.io.wreg_o
  ex_wdata := ex0.io.wdata_o

  val wb0 = Module(new Wb())
  // input
  wb0.io.ex_wd := ex_wd
  wb0.io.ex_wreg := ex_wreg
  wb0.io.ex_wdata := ex_wdata
  // ouput
  wb_wd := wb0.io.wb_wd
  wb_wreg := wb0.io.wb_wreg
  wb_wdata := wb0.io.wb_wdata
}
