// package cpu.pipeline.execute

// import chisel3._
// import cpu.defines._
// import cpu.defines.Const._

// class Mul extends Module {
//   val io = IO(new Bundle {
//     val fromExecute = Flipped(new Execute_Mul())
//     val execute     = new Mul_Execute()
//   })
//   // input
//   val op  = io.fromExecute.op
//   val in1 = io.fromExecute.in1
//   val in2 = io.fromExecute.in2

//   // output
//   io.execute.out := Mux(
//     (op === EXE_MULT_OP || op === EXE_MUL_OP || op === EXE_MADD_OP || op === EXE_MSUB_OP),
//     (in1.asSInt() * in2.asSInt()).asUInt(),
//     in1 * in2,
//   )
// }
