// package cpu.pipeline

// import chisel3._
// import chisel3.util._
// import cpu.defines.Const._
// import chisel3.util.experimental.loadMemoryFromFile

// class InstMemory extends Module {
//   val io = IO(new Bundle {
//     val fromFetch = Flipped(new Fetch_InstMemory())
//     val decoder   = new InstMemory_Decoder()
//   })
//   // input-top
//   val ce = Wire(Bool())
//   val pc = Wire(UInt(32.W))
//   ce := io.fromFetch.ce
//   pc := io.fromFetch.pc

//   // output-top
//   val inst = Wire(UInt(32.W))
//   io.decoder.inst := inst

//   val inst_mem = Mem(INST_MEM_NUM, INST_BUS)
//   loadMemoryFromFile(inst_mem, "inst_rom.data")
//   val addr = pc

//   when(ce === CHIP_DISABLE) {
//     inst := ZERO_WORD
//   }.otherwise {
//     inst := inst_mem(addr(INST_MEM_NUM_LOG2 + 1, 2))
//   }
// }