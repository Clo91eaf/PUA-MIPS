// package cpu.pipeline

// import chisel3._
// import chisel3.util._
// import cpu.defines._
// import cpu.defines.Const._

// class Control extends Module {
//   val io = IO(new Bundle {
//     val fromExecute = Flipped(new Execute_Control())
//     val fromMemory  = Flipped(new Memory_Control())

//     val fetchStage     = new Control_FetchStage()
//     val decoderStage   = new Control_DecoderStage()
//     val decoder        = new Control_Decoder()
//     val executeStage   = new Control_ExecuteStage()
//     val memoryStage    = new Control_MemoryStage()
//     val writeBackStage = new Control_WriteBackStage()
//     val llbitReg       = new Control_LLbitReg()
//     val divider        = new Control_Divider()
//   })
//   val stall = RegInit(STALL_BUS_INIT)
//   io.fetchStage.stall     := stall
//   io.decoderStage.stall   := stall
//   io.executeStage.stall   := stall
//   io.memoryStage.stall    := stall
//   io.writeBackStage.stall := stall
//   val new_pc = RegInit(BUS_INIT)
//   io.fetchStage.new_pc := new_pc
//   val flush = RegInit(false.B)
//   io.decoderStage.flush   := flush
//   io.decoder.flush        := flush
//   io.divider.flush        := flush
//   io.executeStage.flush   := flush
//   io.fetchStage.flush     := flush
//   io.llbitReg.flush       := flush
//   io.memoryStage.flush    := flush
//   io.writeBackStage.flush := flush

//   // INIT
//   new_pc := PC_INIT // liphen

//   when(reset.asBool === RST_ENABLE) {
//     stall  := "b000000".U
//     flush  := false.B
//     new_pc := PC_INIT
//   }.elsewhen(io.fromMemory.except_type =/= ZERO_WORD) {
//     flush := true.B
//     stall := "b000000".U
//     switch(io.fromMemory.except_type) {
//       is("h00000001".U) { // interrupt
//         new_pc := "h00000020".U
//       }
//       is("h00000008".U) { // syscall
//         new_pc := "h00000040".U
//       }
//       is("h0000000a".U) { // inst_invalid
//         new_pc := "h00000040".U
//       }
//       is("h0000000d".U) { // trap
//         new_pc := "h00000040".U
//       }
//       is("h0000000c".U) { // ov
//         new_pc := "h00000040".U
//       }
//       is("h0000000e".U) { // eret
//         new_pc := io.fromMemory.cp0_epc
//       }
//     }
//   }.elsewhen(io.fromExecute.stallreq === STOP) {
//     stall := "b001111".U
//     flush := false.B
//   }.otherwise {
//     stall  := "b000000".U
//     flush  := false.B
//     new_pc := PC_INIT
//   }

//   // debug
//   // printf(p"control :stall 0x${Hexadecimal(stall)}\n")
// }
