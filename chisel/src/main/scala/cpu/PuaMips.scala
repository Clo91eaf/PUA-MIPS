import chisel3._
import chisel3.util._
import cpu.puamips._
import cpu.puamips.Const._
import chisel3.internal.DontCareBinding

class PuaMips extends Module {
  val io = IO(new Bundle {
    val ext_int = Input(UInt(6.W))
    val inst_sram = new INST_SRAM()
    val data_sram = new DATA_SRAM()
    val debug = new DEBUG()
  })
  val fetch = Module(new Fetch())
  val decoderStage = Module(new DecoderStage())
  val decoder = Module(new Decoder())
  val executeStage = Module(new ExecuteStage())
  val execute = Module(new Execute())
  val memoryStage = Module(new MemoryStage())
  val memory = Module(new Memory())
  val writeBackStage = Module(new WriteBackStage())
  val regfile = Module(new Regfile())
  val llbitReg = Module(new LLbitReg())
  val divider = Module(new Divider())
  val hilo = Module(new HILO())
  val control = Module(new Control())
  val cp0 = Module(new CP0Reg())

  // func_test interfacter
  io.inst_sram.en := fetch.io.instMemory.ce
  io.inst_sram.we := WEN_BUS_INIT
  io.inst_sram.addr := fetch.io.instMemory.pc
  io.inst_sram.wdata := REG_BUS_INIT
  decoderStage.io.fromInstMemory.inst := io.inst_sram.rdata

  io.data_sram.en := memory.io.dataMemory.ce
  io.data_sram.we := memory.io.dataMemory.sel & Fill(4, memory.io.dataMemory.we)
  io.data_sram.addr := memory.io.dataMemory.addr
  io.data_sram.wdata := memory.io.dataMemory.data
  memory.io.fromDataMemory.data := io.data_sram.rdata

  io.debug <> writeBackStage.io.debug

  // @formatter:off
  // fetch
  fetch.io.decoderStage <> decoderStage.io.fromFetch

  // decoderStage
  decoderStage.io.decoder <> decoder.io.fromDecoderStage

  // decoder
  decoder.io.executeStage <> executeStage.io.fromDecoder
  decoder.io.regfile      <> regfile.io.fromDecoder
  decoder.io.fetch        <> fetch.io.fromDecoder
  decoder.io.control      <> control.io.fromDecoder

  // executeStage
  executeStage.io.decoder <> decoder.io.fromExecuteStage
  executeStage.io.execute <> execute.io.fromExecuteStage

  // execute
  execute.io.control     <> control.io.fromExecute
  execute.io.decoder     <> decoder.io.fromExecute
  execute.io.memoryStage <> memoryStage.io.fromExecute
  execute.io.divider     <> divider.io.fromExecute
  execute.io.cp0         <> cp0.io.fromExecute

  // memoryStage
  memoryStage.io.execute <> execute.io.fromMemoryStage
  memoryStage.io.memory  <> memory.io.fromMemoryStage

  // memory
  memory.io.decoder        <> decoder.io.fromMemory
  memory.io.execute        <> execute.io.fromMemory
  memory.io.writeBackStage <> writeBackStage.io.fromMemory
//   memory.io.dataMemory  <> dataMemory.io.fromMemory

  // writeBackStage
  writeBackStage.io.execute  <> execute.io.fromWriteBackStage
  writeBackStage.io.regFile  <> regfile.io.fromWriteBackStage
  writeBackStage.io.hilo     <> hilo.io.fromWriteBackStage
  writeBackStage.io.llbitReg <> llbitReg.io.fromWriteBackStage
  writeBackStage.io.memory   <> memory.io.fromWriteBackStage
  writeBackStage.io.cp0      <> cp0.io.fromWriteBackStage

  // hilo
  hilo.io.execute <> execute.io.fromHILO

  // reg file
  regfile.io.decoder <> decoder.io.fromRegfile

  // divider
  divider.io.annul := DontCare
  divider.io.execute <> execute.io.fromDivider

  // llbitReg
  llbitReg.io.flush := DontCare
  llbitReg.io.memory <> memory.io.fromLLbitReg

  // control
  control.io.decoderStage   <> decoderStage.io.fromControl
  control.io.executeStage   <> executeStage.io.fromControl
  control.io.fetch          <> fetch.io.fromControl
  control.io.memoryStage    <> memoryStage.io.fromControl
  control.io.writeBackStage <> writeBackStage.io.fromControl

  //cp0
  cp0.io.execute  <> execute.io.fromCP0
  cp0.io.int_i    := Cat(0.U(5.W), cp0.io.timer_int_o)

  // inst memory
  // instMemory.io.fetch   <> fetch.io.fromInstMemory

  // data memory
  // dataMemory.io.memory  <> memory.io.fromDataMemory
  // @formatter:on
}
