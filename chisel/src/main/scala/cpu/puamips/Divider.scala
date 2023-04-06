package cpu.puamips

import Const._
import chisel3._
import chisel3.util._

class Divider extends Module {
  val io = IO(new Bundle {
    val fromExecute = Flipped(new Execute_Divider())
    val annul = Input(Bool())

    val execute = new Divider_Execute()
  })
  // input
  val signed_div_i = Wire(Bool())
  val opdata1_i = Wire(UInt(32.W))
  val opdata2_i = Wire(UInt(32.W))
  val start_i = Wire(Bool())
  val annul_i = Wire(Bool())

  // input-execute
  signed_div_i := io.fromExecute.signed_div
  opdata1_i := io.fromExecute.opdata1
  opdata2_i := io.fromExecute.opdata2
  start_i := io.fromExecute.start

  // input-annul
  annul_i := io.annul


  // output
  val result = RegInit(DOUBLE_BUS_INIT)
  val ready = RegInit(DIV_RESULT_NOT_READY)


  // output-execute
  io.execute.result := result
  io.execute.ready := ready

  val div_temp = Wire(UInt(33.W))
  val cnt = RegInit(0.U(6.W))
  val dividend = RegInit(0.U(65.W))
  val state = RegInit(DIV_FREE)
  val divisor = RegInit(0.U(32.W))
  val temp_op1 = RegInit(0.U(32.W))
  val temp_op2 = RegInit(0.U(32.W))

  div_temp := Cat(0.U(1.W), dividend(63, 32)) - Cat(0.U(1.W), divisor)

  switch(state) {
    is(DIV_FREE) {
      when(start_i === DIV_START && annul_i === false.B) {
        when(opdata2_i === ZERO_WORD) {
          state := DIV_BY_ZERO
        }.otherwise {
          state := DIV_ON
          cnt := "b000000".U
          when(signed_div_i === true.B && opdata1_i(31) === true.B) {
            temp_op1 := ~opdata1_i + 1.U
          }.otherwise {
            temp_op1 := opdata1_i
          }
          when(signed_div_i === true.B && opdata2_i(31) === true.B) {
            temp_op2 := ~opdata2_i + 1.U
          }.otherwise {
            temp_op2 := opdata2_i
          }
          dividend := ZERO_WORD
          dividend := Cat(0.U(32.W), temp_op1, 0.U(1.W))
          divisor := temp_op2
        }
      }.otherwise {
        ready := DIV_RESULT_NOT_READY
        result := ZERO_WORD
      }
    }
    is(DIV_BY_ZERO) {
      dividend := ZERO_WORD
      state := DIV_END
    }
    is(DIV_ON) {
      when(annul_i === false.B) {
        when(cnt =/= "b100000".U) {
          when(div_temp(32) === false.B) {
            dividend := Cat(dividend(63, 0), 0.U(1.W))
          }.otherwise {
            dividend := Cat(div_temp(31, 0), dividend(31, 0), 1.U(1.W))
          }
          cnt := cnt + 1.U
        }.otherwise {
          when(
            (signed_div_i === false.B) &&
              ((opdata1_i(31) ^ opdata2_i(31)) === false.B)
          ) {
            dividend := Cat(dividend(64, 32), (~dividend(31, 0) + 1.U))
          }
          when(
            (signed_div_i === false.B) &&
              ((opdata1_i(31) ^ dividend(64)) === false.B)
          ) {
            dividend := Cat((~dividend(64, 33) + 1.U), dividend(32, 0))
          }
          state := DIV_END
          cnt := "b000000".U
        }
      }.otherwise {
        state := DIV_FREE
      }
    }
    is(DIV_END) {
      result := Cat(dividend(64, 33), dividend(31, 0))
      ready := DIV_RESULT_READY
      when(start_i === DIV_STOP) {
        state := DIV_FREE
        ready := DIV_RESULT_NOT_READY
        result := ZERO_WORD
      }
    }
  }

  // debug
  // printf(
  //   p"divider :result 0x${Hexadecimal(result)}, ready 0x${Hexadecimal(ready)}\n"
  // )
}
