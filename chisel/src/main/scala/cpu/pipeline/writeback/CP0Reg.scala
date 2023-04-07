package cpu.pipeline.writeback

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._

class CP0Reg extends Module {
  val io = IO(new Bundle {
    val fromWriteBackStage = Flipped(new WriteBackStage_CP0())
    val fromExecute        = Flipped(new Execute_CP0())
    val fromMemory         = Flipped(new Memory_CP0())
    val int_i              = Input(UInt(6.W))

    val memory      = new CP0_Memory()
    val execute     = new CP0_Execute()
    val timer_int_o = Output(Bool())
    val out         = new CP0_Output
  })

  // output
  val data      = Wire(BUS)
  val count     = RegInit(BUS_INIT)
  val compare   = RegInit(BUS_INIT)
  val status    = RegInit("b00010000000000000000000000000000".U(32.W))
  val cause     = RegInit(BUS_INIT)
  val epc       = RegInit(BUS_INIT)
  val config    = RegInit("b00000000000000001000000000000000".U(32.W))
  val prid      = RegInit("b00000000010011000000000100000010".U(32.W))
  val timer_int = RegInit(INTERRUPT_NOT_ASSERT)

  // output-execute
  io.execute.cp0_data := data

  // output-out
  io.out.count   := count
  io.out.compare := compare

  // output-memory
  io.memory.status := status
  io.memory.cause  := cause
  io.memory.epc    := epc

  // output-out
  io.out.config := config
  io.out.prid   := prid

  // output-timer_int_o := timer_int
  io.timer_int_o := timer_int

  // io-finish

  count := count + 1.U
  cause := Cat(cause(31, 16), io.int_i, cause(9, 0))

  when(compare =/= ZERO_WORD && count === compare) {
    timer_int := INTERRUPT_ASSERT
  }

  when(io.fromWriteBackStage.cp0_wen === WRITE_ENABLE) {
    switch(io.fromWriteBackStage.cp0_waddr) {
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

  switch(io.fromMemory.except_type) {
    is("h00000001".U) {
      when(io.fromMemory.is_in_delayslot === IN_DELAY_SLOT) {
        epc   := io.fromMemory.current_inst_addr - 4.U
        cause := Cat(1.U(1.W), cause(30, 0))
      }.otherwise {
        epc   := io.fromMemory.current_inst_addr
        cause := Cat(0.U(1.W), cause(30, 0))
      }
      status := Cat(status(31, 2), 1.U(1.W), status(0))
      cause  := Cat(cause(31, 7), "b00000".U(5.W), cause(1, 0))
    }
    is("h00000008".U) {
      when(status(1) === 0.U) {
        when(io.fromMemory.is_in_delayslot === IN_DELAY_SLOT) {
          epc   := io.fromMemory.current_inst_addr - 4.U
          cause := Cat(1.U(1.W), cause(30, 0))
        }.otherwise {
          epc   := io.fromMemory.current_inst_addr
          cause := Cat(0.U(1.W), cause(30, 0))
        }
      }
      status := Cat(status(31, 2), 1.U(1.W), status(0))
      cause  := Cat(cause(31, 7), "b01000".U(5.W), cause(1, 0))
    }
    is("h0000000a".U) {
      when(status(1) === 0.U) {
        when(io.fromMemory.is_in_delayslot === IN_DELAY_SLOT) {
          epc   := io.fromMemory.current_inst_addr - 4.U
          cause := Cat(1.U(1.W), cause(30, 0))
        }.otherwise {
          epc   := io.fromMemory.current_inst_addr
          cause := Cat(0.U(1.W), cause(30, 0))
        }
      }
      status := Cat(status(31, 2), 1.U(1.W), status(0))
      cause  := Cat(cause(31, 7), "b01010".U(5.W), cause(1, 0))
    }
    is("h0000000d".U) {
      when(status(1) === 0.U) {
        when(io.fromMemory.is_in_delayslot === IN_DELAY_SLOT) {
          epc   := io.fromMemory.current_inst_addr - 4.U
          cause := Cat(1.U(1.W), cause(30, 0))
        }.otherwise {
          epc   := io.fromMemory.current_inst_addr
          cause := Cat(0.U(1.W), cause(30, 0))
        }
      }
      status := Cat(status(31, 2), 1.U(1.W), status(0))
      cause  := Cat(cause(31, 7), "b01101".U(5.W), cause(1, 0))
    }
    is("h0000000c".U) {
      when(status(1) === 0.U) {
        when(io.fromMemory.is_in_delayslot === IN_DELAY_SLOT) {
          epc   := io.fromMemory.current_inst_addr - 4.U
          cause := Cat(1.U(1.W), cause(30, 0))
        }.otherwise {
          epc   := io.fromMemory.current_inst_addr
          cause := Cat(0.U(1.W), cause(30, 0))
        }
      }
      status := Cat(status(31, 2), 1.U(1.W), status(0))
      cause  := Cat(cause(31, 7), "b01100".U(5.W), cause(1, 0))
    }
    is("h0000000e".U) {
      status := Cat(status(31, 2), 0.U(1.W), status(0))
    }
  }

  when(reset.asBool === RST_ENABLE) {
    data := ZERO_WORD
  }.otherwise {
    data := ZERO_WORD // defalut
    switch(io.fromExecute.cp0_raddr) {
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
}
