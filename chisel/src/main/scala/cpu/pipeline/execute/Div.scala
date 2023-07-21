// package cpu.pipeline.execute

// import cpu.defines.Const._
// import chisel3._
// import chisel3.util._
// import cpu.defines._

// class Div extends Module {
//   val io = IO(new Bundle {
//     val fromExecute = Flipped(new Execute_Div())
//     val execute     = new Div_Execute()
//   })
//   // input-execute
//   val op       = io.fromExecute.op
//   val divisor  = io.fromExecute.divisor
//   val dividend = io.fromExecute.dividend

//   // output-execute
//   val quotient  = Wire(BUS)
//   val remainder = Wire(BUS)
//   io.execute.quotient  := quotient
//   io.execute.remainder := remainder

//   quotient := MuxLookup(
//     op,
//     ZERO_WORD,
//     Seq(
//       EXE_DIV_OP  -> (divisor.asSInt() / dividend.asSInt()).asUInt(),
//       EXE_DIVU_OP -> (divisor.asUInt() / dividend.asUInt()),
//     ),
//   )
//   remainder := MuxLookup(
//     op,
//     ZERO_WORD,
//     Seq(
//       EXE_DIV_OP  -> (divisor.asSInt() % dividend.asSInt()).asUInt(),
//       EXE_DIVU_OP -> (divisor.asUInt() % dividend.asUInt()),
//     ),
//   )
// }
