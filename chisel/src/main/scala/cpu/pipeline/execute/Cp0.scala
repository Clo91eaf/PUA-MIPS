package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.pipeline.memory.Cp0Info
import cpu.CpuConfig
import cpu.pipeline.decoder.Cp0DecoderUnit

class TlbEntry extends Bundle {
  val vpn2 = UInt(VPN2_WID.W)
  val asid = UInt(ASID_WID.W)
  val g    = Bool()
  val pfn  = Vec(2, UInt(PFN_WID.W))
  val c    = Vec(2, Bool())
  val d    = Vec(2, Bool())
  val v    = Vec(2, Bool())
}

class Cp0(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val ctrl = Input(new Bundle {
      val exe_stall = Bool()
      val mem_stall = Bool()
    })
    val decoderUnit = Output(new Cp0DecoderUnit())
    val executeUnit = new Bundle {
      val in = Input(new Bundle {
        val inst_info  = new InstInfo()
        val mtc0_wdata = UInt(DATA_WID.W)
        val ext_int    = UInt(EXT_INT_WID.W)
      })
      val out = Output(new Bundle {
        val cp0_rdata = UInt(DATA_WID.W)
        val debug     = Output(new Cp0Info())
      })
    }
    val memoryUnit = new Bundle {
      val in = Input(new Bundle {
        val inst = Vec(
          config.fuNum,
          new Bundle {
            val pc = UInt(PC_WID.W)
            val ex = new ExceptionInfo()
          },
        )
      })
      val out = Output(new Bundle {
        val flush    = Bool()
        val flush_pc = UInt(PC_WID.W)
      })
    }
    val tlb = Vec(
      2,
      new Bundle {
        val vpn2 = Input(UInt(VPN2_WID.W))

        val found = Output(Bool())
        val info  = Output(new TlbEntry())
      },
    )
  })
  // 优先使用inst0的信息
  val pc = Mux(io.memoryUnit.in.inst(0).ex.excode =/= EX_NO, io.memoryUnit.in.inst(0).pc, io.memoryUnit.in.inst(1).pc)
  val ex = Mux(io.memoryUnit.in.inst(0).ex.excode =/= EX_NO, io.memoryUnit.in.inst(0).ex, io.memoryUnit.in.inst(1).ex)
  val mtc0_wen   = io.executeUnit.in.inst_info.op === EXE_MTC0
  val mtc0_wdata = io.executeUnit.in.mtc0_wdata
  val mtc0_addr  = io.executeUnit.in.inst_info.cp0_addr
  val exe_op     = io.executeUnit.in.inst_info.op
  val exe_stall  = io.ctrl.exe_stall
  val mem_stall  = io.ctrl.mem_stall

  val tlb_l2 = Module(new TlbL2()).io
  tlb_l2.in.tlb1_vpn2 := io.tlb(0).vpn2
  tlb_l2.in.tlb2_vpn2 := io.tlb(1).vpn2
  io.tlb(0).found     := tlb_l2.out.tlb1_found
  io.tlb(1).found     := tlb_l2.out.tlb2_found
  io.tlb(0).info      := tlb_l2.out.tlb1_entry
  io.tlb(1).info      := tlb_l2.out.tlb2_entry

  // ---------------cp0-defines-----------------

  // index register (0,0)
  val cp0_index = RegInit(0.U.asTypeOf(new Cp0Index()))

  // random register (1,0)
  val random_init = Wire(new Cp0Random())
  random_init        := 0.U.asTypeOf(new Cp0Random())
  random_init.random := (TLB_NUM - 1).U
  val cp0_random = RegInit(random_init)

  // entrylo0 register (2,0)
  val cp0_entrylo0 = RegInit(0.U.asTypeOf(new Cp0EntryLo()))

  // entrylo1 register (3,0)
  val cp0_entrylo1 = RegInit(0.U.asTypeOf(new Cp0EntryLo()))

  // context register (4,0)
  val cp0_context = RegInit(0.U.asTypeOf(new Cp0Context()))

  // page mask register (5,0)
  val cp0_pagemask = 0.U

  // wired register (6,0)
  val cp0_wired = RegInit(0.U.asTypeOf(new Cp0Wired()))

  // badvaddr register (8,0)
  val cp0_badvaddr = RegInit(0.U.asTypeOf(new Cp0BadVAddr()))

  // count register (9,0)
  val count_init = Wire(new Cp0Count())
  count_init       := 0.U.asTypeOf(new Cp0Count())
  count_init.count := 1.U
  val cp0_count = RegInit(count_init)

  // entryhi register (10,0)
  val cp0_entryhi = RegInit(0.U.asTypeOf(new Cp0EntryHi()))

  // compare register (11,0)
  val cp0_compare = RegInit(0.U.asTypeOf(new Cp0Compare()))

  // status register (12,0)
  val status_init = Wire(new Cp0Status())
  status_init     := 0.U
  status_init.bev := true.B
  val cp0_status = RegInit(status_init)

  // cause register (13,0)
  val cp0_cause = RegInit(0.U.asTypeOf(new Cp0Cause()))

  // epc register (14,0)
  val cp0_epc = RegInit(0.U.asTypeOf(new Cp0Epc()))

  // prid register (15,0)
  val prid = "h_0001_8003".U

  // ebase register (15,1)
  val ebase_init = Wire(new Cp0Ebase())
  ebase_init      := 0.U.asTypeOf(new Cp0Ebase())
  ebase_init.fill := true.B
  val cp0_ebase = RegInit(ebase_init)

  // config register (16,0)
  val cp0_config = Wire(new Cp0Config())
  cp0_config    := 0.U.asTypeOf(new Cp0Config())
  cp0_config.k0 := 3.U
  cp0_config.mt := 1.U
  cp0_config.m  := true.B

  // config1 register (16,1)
  val cp0_config1 = Wire(new Cp0Config1())
  cp0_config1    := 0.U.asTypeOf(new Cp0Config1())
  cp0_config1.da := 1.U
  cp0_config1.dl := 5.U
  cp0_config1.ia := 1.U
  cp0_config1.il := 5.U
  cp0_config1.ms := (TLB_NUM - 1).U

  tlb_l2.in.write.en    := !exe_stall && (exe_op === EXE_TLBWI || exe_op === EXE_TLBWR)
  tlb_l2.in.write.index := Mux(exe_op === EXE_TLBWI, cp0_index.index, cp0_random.random)
  // tlb_l2.in.write.entry.asid := entryhi.asid
  // tlb_l2.in.write.entry.vpn2 := entryhi.vpn2
  tlb_l2.in.write.entry.g      := cp0_entrylo0.g || cp0_entrylo1.g
  tlb_l2.in.write.entry.pfn(0) := cp0_entrylo0.pfn
  tlb_l2.in.write.entry.pfn(1) := cp0_entrylo1.pfn
  tlb_l2.in.write.entry.c(0)   := cp0_entrylo0.c
  tlb_l2.in.write.entry.c(1)   := cp0_entrylo1.c
  tlb_l2.in.write.entry.d(0)   := cp0_entrylo0.d
  tlb_l2.in.write.entry.d(1)   := cp0_entrylo1.d
  tlb_l2.in.write.entry.v(0)   := cp0_entrylo0.v
  tlb_l2.in.write.entry.v(1)   := cp0_entrylo1.v

  // index register (0,0)
  when(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_INDEX_ADDR) {
      cp0_index.index := mtc0_wdata(log2Ceil(TLB_NUM) - 1, 0)
    }.elsewhen(exe_op === EXE_TLBP) {
      cp0_index.index := Mux(tlb_l2.out.tlb_found, tlb_l2.out.tlb_match_index, cp0_index.index)
      cp0_index.p     := !tlb_l2.out.tlb_found
    }
  }

  // random register (1,0)
  cp0_random := Mux(cp0_random.random === cp0_wired.wired, (TLB_NUM - 1).U, (cp0_random.random - 1.U))

  // entrylo0 register (2,0)
  when(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_ENTRYLO0_ADDR) {
      val wdata = mtc0_wdata.asTypeOf(new Cp0EntryLo())
      cp0_entrylo0.pfn := wdata.pfn
      cp0_entrylo0.c   := wdata.c
      cp0_entrylo0.d   := wdata.d
      cp0_entrylo0.v   := wdata.v
      cp0_entrylo0.g   := wdata.g
    }.elsewhen(exe_op === EXE_TLBR) {
      cp0_entrylo0.pfn := tlb_l2.out.read.entry.pfn(0)
      cp0_entrylo0.g   := tlb_l2.out.read.entry.g
      cp0_entrylo0.c   := Cat(1.U((C_WID - 1).W), tlb_l2.out.read.entry.c(0))
      cp0_entrylo0.d   := tlb_l2.out.read.entry.d(0)
      cp0_entrylo0.v   := tlb_l2.out.read.entry.v(0)
    }
  }

  // entrylo1 register (3,0)
  when(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_ENTRYLO1_ADDR) {
      val wdata = mtc0_wdata.asTypeOf(new Cp0EntryLo())
      cp0_entrylo1.pfn := wdata.pfn
      cp0_entrylo1.c   := wdata.c
      cp0_entrylo1.d   := wdata.d
      cp0_entrylo1.v   := wdata.v
      cp0_entrylo1.g   := wdata.g
    }.elsewhen(exe_op === EXE_TLBR) {
      cp0_entrylo1.pfn := tlb_l2.out.read.entry.pfn(1)
      cp0_entrylo1.g   := tlb_l2.out.read.entry.g
      cp0_entrylo1.c   := Cat(1.U((C_WID - 1).W), tlb_l2.out.read.entry.c(1))
      cp0_entrylo1.d   := tlb_l2.out.read.entry.d(1)
      cp0_entrylo1.v   := tlb_l2.out.read.entry.v(1)
    }
  }

  // context register (4,0)
  when(!mem_stall && ex.flush_req) {
    when(VecInit(EX_TLBL, EX_TLBS, EX_MOD).contains(ex.excode)) {
      cp0_context.badvpn2 := ex.badvaddr(31, 13)
    }
  }.elsewhen(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_CONTEXT_ADDR) {
      cp0_context.ptebase := mtc0_wdata.asTypeOf(new Cp0Context()).ptebase
    }
  }

  // wired register (6,0)
  when(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_WIRED_ADDR) {
      cp0_wired.wired   := mtc0_wdata.asTypeOf(new Cp0Wired()).wired
      cp0_random.random := (TLB_NUM - 1).U
    }
  }

  // badvaddr register (8,0)
  when(!mem_stall && ex.flush_req) {
    when(VecInit(EX_ADEL, EX_TLBL, EX_ADES, EX_TLBS, EX_MOD).contains(ex.excode)) {
      cp0_badvaddr.badvaddr := ex.badvaddr
    }
  }

  // count register (9,0)
  cp0_count.count := cp0_count.count + 1.U
  when(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_COUNT_ADDR) {
      cp0_count.count := mtc0_wdata.asTypeOf(new Cp0Count()).count
    }
  }

  // entryhi register (10,0)
  when(!mem_stall && ex.flush_req) {
    when(VecInit(EX_TLBL, EX_TLBS, EX_MOD).contains(ex.excode)) {
      cp0_entryhi.vpn2 := ex.badvaddr(31, 13)
    }
  }.elsewhen(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_ENTRYHI_ADDR) {
      val wdata = mtc0_wdata.asTypeOf(new Cp0EntryHi())
      cp0_entryhi.asid := wdata.asid
      cp0_entryhi.vpn2 := wdata.vpn2
    }
  }

  // compare register (11,0)
  when(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_COMPARE_ADDR) {
      cp0_compare.compare := mtc0_wdata.asTypeOf(new Cp0Compare()).compare
    }
  }

  // status register (12,0)
  when(!mem_stall) {
    when(ex.eret) {
      when(cp0_status.erl) {
        cp0_status.erl := false.B
      }.otherwise {
        cp0_status.exl := false.B
      }
    }.elsewhen(ex.flush_req) {
      cp0_status.exl := true.B
    }
  }.elsewhen(!exe_stall) {
    when(mtc0_wen && mtc0_addr === CP0_STATUS_ADDR) {
      val wdata = mtc0_wdata.asTypeOf(new Cp0Status())
      cp0_status.cu0 := wdata.cu0
      cp0_status.ie  := wdata.ie
      cp0_status.exl := wdata.exl
      cp0_status.erl := wdata.erl
      cp0_status.um  := wdata.um
      cp0_status.im  := wdata.im
      cp0_status.bev := wdata.bev
    }
  }

  // cause register (13,0)
  cp0_cause.ip := Cat(
    cp0_cause.ip(7) || cp0_compare.compare === cp0_count.count,
    io.executeUnit.in.ext_int,
    cp0_cause.ip(1, 0),
  )
  when(!mem_stall) {
    when(ex.flush_req) {
      when(!cp0_status.exl) {
        cp0_cause.bd := ex.bd
      }
      cp0_cause.excode := ex.excode
    }
  }.elsewhen(!exe_stall) {
    when(mtc0_wen) {
      when(mtc0_addr === CP0_COMPARE_ADDR) {
        cp0_cause.ip(7) := false.B
      }.elsewhen(mtc0_addr === CP0_CAUSE_ADDR) {
        val wdata = mtc0_wdata.asTypeOf(new Cp0Cause())
        cp0_cause.ip := Cat(
          cp0_cause.ip(7, 2),
          wdata.ip(1, 0),
        )
        cp0_cause.iv := wdata.iv
      }
    }

    // epc register (14,0)
    when(!mem_stall) {
      when(ex.flush_req) {
        when(!cp0_status.exl) {
          cp0_epc.epc := Mux(ex.bd, pc - 4.U, pc)
        }
      }
    }.elsewhen(!exe_stall) {
      when(mtc0_wen && mtc0_addr === CP0_EPC_ADDR) {
        cp0_epc.epc := mtc0_wdata.asTypeOf(new Cp0Epc()).epc
      }
    }

    // ebase register (15,1)
    when(!exe_stall) {
      when(mtc0_wen && mtc0_addr === CP0_EBASE_ADDR) {
        cp0_ebase.ebase := mtc0_wdata.asTypeOf(new Cp0Ebase()).ebase
      }
    }

  }

  // io.executeUnit.out.cp0_rdata := MuxLookup(
  //   io.executeUnit.in.inst_info.cp0_addr,
  //   0.U,
  //   Seq(
  //   ),
  // )
}
