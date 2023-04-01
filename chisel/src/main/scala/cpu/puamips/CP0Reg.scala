package cpu.puamips

import Const._
import chisel3._
import chisel3.util._

class CP0Reg extends Module {
  val io = IO(new Bundle {
    val fromWriteBackStage = Flipped(new WriteBackStage_CP0())
    val fromExecute = Flipped(new Execute_CP0())
    val int_i = Input(UInt(6.W))

    val execute = new CP0_Execute()
    val timer_int_o = Output(Bool())
    val out = new CP0_Output
  })

  // output
  val data = RegInit(REG_BUS_INIT)
  io.execute.cp0_data := data
  val count = RegInit(REG_BUS_INIT)
  io.out.count := count
  val compare = RegInit(REG_BUS_INIT)
  io.out.compare := compare
  val status = RegInit("b00010000000000000000000000000000".U(32.W))
  io.out.status := status
  val cause = RegInit(REG_BUS_INIT)
  io.out.cause := cause
  val epc = RegInit(REG_BUS_INIT)
  io.out.epc := epc
  val config = RegInit("b00000000000000001000000000000000".U(32.W))
  io.out.config := config
  val prid = RegInit("b00000000010011000000000100000010".U(32.W))
  io.out.prid := prid
  val timer_int = RegInit(INTERRUPT_NOT_ASSERT)
  io.timer_int_o := timer_int

  count := count + 1.U
  cause := Cat(cause(31, 16), io.int_i, cause(9, 0))

  when(compare =/= ZERO_WORD && count === compare) {
    timer_int := INTERRUPT_ASSERT
  }

  when(io.fromWriteBackStage.cp0_we === WRITE_ENABLE) {
    switch(io.fromWriteBackStage.cp0_write_addr) {
      is(CP0_REG_COUNT) {
        count := io.fromWriteBackStage.cp0_data
      }
      is(CP0_REG_COMPARE) {
        compare := io.fromWriteBackStage.cp0_data
        // count := `ZeroWord
        timer_int := INTERRUPT_NOT_ASSERT
      }
      is(CP0_REG_STATUS) {
        status := io.fromWriteBackStage.cp0_data
      }
      is(CP0_REG_EPC) {
        epc := io.fromWriteBackStage.cp0_data
      }
      is(CP0_REG_CAUSE) {
        // cause寄存器只有IP(1,0)、IV、WP字段是可写的
        cause := Cat(
          cause(31, 24),
          io.fromWriteBackStage.cp0_data(23),
          io.fromWriteBackStage.cp0_data(22),
          cause(21, 10),
          io.fromWriteBackStage.cp0_data(9, 8),
          cause(7, 0)
        )
      }
    }
  }

  switch(io.fromExecute.cp0_read_addr) {
    is(CP0_REG_COUNT) {
      data := count
    }
    is(CP0_REG_COMPARE) {
      data := compare
    }
    is(CP0_REG_STATUS) {
      data := status
    }
    is(CP0_REG_CAUSE) {
      data := cause
    }
    is(CP0_REG_EPC) {
      data := epc
    }
    is(CP0_REG_PRID) {
      data := prid
    }
    is(CP0_REG_CONFIG) {
      data := config
    }
  }
}
