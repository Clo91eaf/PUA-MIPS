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
  val mtc0_wdata = io.executeUnit.in.mtc0_wdata
  val mtc0_wen   = io.executeUnit.in.inst_info.op === EXE_MTC0
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
  tlb_l2.in.write.en  := !exe_stall && (exe_op === EXE_TLBWI || exe_op === EXE_TLBWR)
  // tlb_l2.in.write.index := Mux(exe_op === EXE_TLBWI, index.index, random.random)
  // tlb_l2.in.write.entry.asid := entryhi.asid
  // tlb_l2.in.write.entry.vpn2 := entryhi.vpn2
  // tlb_l2.in.write.entry.g := entrylo0.g || entrylo1.g
  // tlb_l2.in.write.entry.pfn(0) := entrylo0.pfn
  // tlb_l2.in.write.entry.pfn(1) := entrylo1.pfn
  // tlb_l2.in.write.entry.c(0) := entrylo0.c
  // tlb_l2.in.write.entry.c(1) := entrylo1.c
  // tlb_l2.in.write.entry.d(0) := entrylo0.d
  // tlb_l2.in.write.entry.d(1) := entrylo1.d
  // tlb_l2.in.write.entry.v(0) := entrylo0.v
  // tlb_l2.in.write.entry.v(1) := entrylo1.v

  // index register
  val cp0_index = RegInit(new Bundle {
    val p     = false.B
    val blank = 0.U((31 - log2Ceil(TLB_NUM)).W)
    val index = 0.U(log2Ceil(TLB_NUM).W)
  })
  when(!exe_stall) {
    when(mtc0_wen && mtc0_wdata === CP0_INDEX_ADDR) {
      cp0_index.index := mtc0_wdata(log2Ceil(TLB_NUM) - 1, 0)
    }
    when(exe_op === EXE_TLBP) {
      cp0_index.index := Mux(tlb_l2.out.tlb_found, tlb_l2.out.tlb_match_index, cp0_index.index)
      cp0_index.p     := !tlb_l2.out.tlb_found
    }
  }
}

//   // INDEX
//   val index_p = RegInit(false.B)
//   when(tlbp) {
//     index_p := !s1_found
//   }

//   val index_index = RegInit(0.U(log2Ceil(TLB_NUM).W))
//   when(mtc0_we && cp0_addr === CP0_INDEX_ADDR) {
//     index_index := cp0_wdata(3, 0)
//   }.elsewhen(tlbp) {
//     index_index := s1_index
//   }

//   cp0_index := Cat(
//     index_p,
//     0.U((31 - log2Ceil(TLB_NUM)).W),
//     index_index,
//   )

//   // CP0_STATUS
//   val cp0_status_bev = RegInit(true.B)
//   when(mtc0_we && cp0_addr === CP0_STATUS_ADDR) {
//     cp0_status_bev := cp0_wdata(22)
//   }

//   val cp0_status_im = RegInit(0.U(8.W))
//   when(mtc0_we && cp0_addr === CP0_STATUS_ADDR) {
//     cp0_status_im := cp0_wdata(15, 8)
//   }

//   val cp0_status_exl = RegInit(false.B)
//   when(wb_ex) {
//     cp0_status_exl := true.B
//   }.elsewhen(eret_flush) {
//     cp0_status_exl := false.B
//   }.elsewhen(
//     mtc0_we && (cp0_addr === CP0_STATUS_ADDR),
//   ) {
//     cp0_status_exl := cp0_wdata(1)
//   }

//   val cp0_status_ie = RegInit(false.B)
//   when(mtc0_we && (cp0_addr === CP0_STATUS_ADDR)) {
//     cp0_status_ie := cp0_wdata(0)
//   }

//   cp0_status := Cat(
//     0.U(9.W),       // 31:23
//     cp0_status_bev, // 22:22
//     0.U(6.W),       // 21:16
//     cp0_status_im,  // 15:8
//     0.U(6.W),       // 7:2
//     cp0_status_exl, // 1:1
//     cp0_status_ie,  // 0:0
//   )

//   val cp0_cause_bd = RegInit(false.B)
//   when(wb_ex && !cp0_status_exl) {
//     cp0_cause_bd := wb_bd
//   }

//   val cp0_cause_ti     = RegInit(false.B)
//   val count_eq_compare = (cp0_count === cp0_compare)

//   when(mtc0_we && (cp0_addr === CP0_COMPARE_ADDR)) {
//     cp0_cause_ti := false.B
//   }.elsewhen(count_eq_compare) {
//     cp0_cause_ti := true.B
//   }

//   val cp0_cause_ip = RegInit(0.U(8.W))
//   cp0_cause_ip := Cat(
//     (ext_int_in(5) || cp0_cause_ti), // 7:7
//     ext_int_in(4, 0),                // 6:2
//     cp0_cause_ip(1, 0),              // 1:0
//   )

//   when(mtc0_we && cp0_addr === CP0_CAUSE_ADDR) {
//     cp0_cause_ip := Cat(
//       cp0_cause_ip(7, 2), // 7:2
//       cp0_wdata(9, 8),    // 1:0
//     )
//   }

//   val cp0_cause_excode = RegInit(0.U(5.W))
//   when(wb_ex) {
//     cp0_cause_excode := wb_excode
//   }

//   val cp0_cause_iv = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_CAUSE_ADDR) {
//     cp0_cause_iv := cp0_wdata(23)
//   }

//   cp0_cause := Cat(
//     cp0_cause_bd,     // 31:31
//     cp0_cause_ti,     // 30:30
//     0.U(6.W),         // 29:24
//     cp0_cause_iv,     // 23:23
//     0.U(7.W),         // 22:16
//     cp0_cause_ip,     // 15:8
//     false.B,          // 7:7
//     cp0_cause_excode, // 6:2
//     0.U(2.W),         // 1:0
//   )

//   // EPC
//   val c0_epc = RegInit(0.U(32.W))
//   when(wb_ex && !cp0_status_exl) {
//     c0_epc := Mux(wb_bd, wb_pc - 4.U, wb_pc)
//   }.elsewhen(mtc0_we && cp0_addr === CP0_EPC_ADDR) {
//     c0_epc := cp0_wdata
//   }

//   cp0_epc := c0_epc

//   // COUNT
//   val c0_count = RegInit(0.U(32.W))
//   when(mtc0_we && cp0_addr === CP0_COUNT_ADDR) {
//     c0_count := cp0_wdata
//   }.otherwise {
//     c0_count := c0_count + 1.U
//   }

//   cp0_count := c0_count

//   // COMPARE
//   val c0_compare = RegInit(0.U(32.W))
//   when(mtc0_we && cp0_addr === CP0_COMPARE_ADDR) {
//     c0_compare := cp0_wdata
//   }

//   cp0_compare := c0_compare

//   // EBase
//   val ebase = RegInit(0.U(18.W))
//   when(mtc0_we && cp0_addr === CP0_EBASE_ADDR) {
//     ebase := cp0_wdata(29, 12)
//   }
//   val cpu_num = 0.U(10.W)
//   cp0_ebase := Cat(
//     1.U(1.W), // 31:31
//     0.U(1.W), // 30:30
//     ebase,    // 29:12
//     0.U(2.W), // 11:10
//     cpu_num,  // 9:0
//   )

//   // ENTRYHI
//   val entry_hi_vpn2 = RegInit(0.U(19.W))
//   when(wb_ex && excode_tlb) {
//     entry_hi_vpn2 := wb_badvaddr(31, 13)
//   }.elsewhen(mtc0_we && cp0_addr === CP0_ENTRYHI_ADDR) {
//     entry_hi_vpn2 := cp0_wdata(31, 13)
//   }.elsewhen(tlbr) {
//     entry_hi_vpn2 := r_vpn2
//   }

//   val entry_hi_asid = RegInit(0.U(8.W))
//   when(mtc0_we && cp0_addr === CP0_ENTRYHI_ADDR) {
//     entry_hi_asid := cp0_wdata(7, 0)
//   }.elsewhen(tlbr) {
//     entry_hi_asid := r_asid
//   }

//   cp0_entryhi := Cat(
//     entry_hi_vpn2,
//     0.U(5.W),
//     entry_hi_asid,
//   )

//   // ENTRYLO0
//   val entrylo0_pfn = RegInit(0.U(20.W))
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO0_ADDR) {
//     entrylo0_pfn := cp0_wdata(25, 6)
//   }.elsewhen(tlbr) {
//     entrylo0_pfn := r_pfn0
//   }

//   val entrylo0_c = RegInit(0.U(3.W))
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO0_ADDR) {
//     entrylo0_c := cp0_wdata(5, 3)
//   }.elsewhen(tlbr) {
//     entrylo0_c := r_c0
//   }

//   val entrylo0_d = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO0_ADDR) {
//     entrylo0_d := cp0_wdata(2)
//   }.elsewhen(tlbr) {
//     entrylo0_d := r_d0
//   }

//   val entrylo0_v = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO0_ADDR) {
//     entrylo0_v := cp0_wdata(1)
//   }.elsewhen(tlbr) {
//     entrylo0_v := r_v0(0)
//   }

//   val entrylo0_g = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO0_ADDR) {
//     entrylo0_g := cp0_wdata(0)
//   }.elsewhen(tlbr) {
//     entrylo0_g := r_g
//   }

//   cp0_entrylo0 := Cat(
//     0.U(6.W),
//     entrylo0_pfn,
//     entrylo0_c,
//     entrylo0_d,
//     entrylo0_v,
//     entrylo0_g,
//   )

//   // ENTRYLO1
//   val entrylo1_pfn = RegInit(0.U(20.W))
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO1_ADDR) {
//     entrylo1_pfn := cp0_wdata(25, 6)
//   }.elsewhen(tlbr) {
//     entrylo1_pfn := r_pfn1
//   }

//   val entrylo1_c = RegInit(0.U(3.W))
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO1_ADDR) {
//     entrylo1_c := cp0_wdata(5, 3)
//   }.elsewhen(tlbr) {
//     entrylo1_c := r_c1
//   }

//   val entrylo1_d = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO1_ADDR) {
//     entrylo1_d := cp0_wdata(2)
//   }.elsewhen(tlbr) {
//     entrylo1_d := r_d1
//   }

//   val entrylo1_v = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO1_ADDR) {
//     entrylo1_v := cp0_wdata(1)
//   }.elsewhen(tlbr) {
//     entrylo1_v := r_v1
//   }

//   val entrylo1_g = RegInit(false.B)
//   when(mtc0_we && cp0_addr === CP0_ENTRYLO1_ADDR) {
//     entrylo1_g := cp0_wdata(0)
//   }.elsewhen(tlbr) {
//     entrylo1_g := r_g
//   }

//   cp0_entrylo1 := Cat(
//     0.U(6.W),
//     entrylo1_pfn,
//     entrylo1_c,
//     entrylo1_d,
//     entrylo1_v,
//     entrylo1_g,
//   )

//   // RANDOM
//   val random = RegInit((TLB_NUM - 1).U(log2Ceil(TLB_NUM).W))

//   random := Mux(cp0_random === cp0_wired, (TLB_NUM - 1).U, (random - 1.U))
//   when(mtc0_we && cp0_addr === CP0_WIRED_ADDR) {
//     random := (TLB_NUM - 1).U
//   }

//   cp0_random := Cat(
//     0.U((32 - log2Ceil(TLB_NUM)).W),
//     random,
//   )

//   // PageMask 4KB pagesize
//   val mask  = 0.U(16.W)
//   val maskx = 3.U(4.W)

//   cp0_page_mask := Cat(
//     0.U(4.W),
//     mask,
//     maskx,
//     0.U(11.W),
//   )

//   // Context
//   val ptebase = RegInit(0.U(9.W))
//   when(mtc0_we && cp0_addr === CP0_CONTEXT_ADDR) {
//     ptebase := cp0_wdata(31, 23)
//   }
//   val badvpn2 = RegInit(0.U(19.W))
//   when(wb_ex && excode_tlb) {
//     badvpn2 := wb_badvaddr(31, 13)
//   }

//   cp0_context := Cat(
//     ptebase,
//     badvpn2,
//     0.U(4.W),
//   )

//   // Config0
//   cp0_config := Cat(
//     1.U(1.W), // m
//     0.U(3.W), // k23
//     0.U(3.W), // ku
//     0.U(9.W), // impl
//     0.U(1.W), // be
//     0.U(2.W), // at
//     0.U(3.W), // ar
//     1.U(3.W), // mt
//     0.U(3.W), // 0
//     0.U(1.W), // vi
//     2.U(3.W), // k0
//   )

//   // Config1
//   cp0_config1 := Cat(
//     0.U(1.W),             // m
//     (TLB_NUM - 1).U(6.W), // ms
//     0.U(3.W),             // is
//     0.U(3.W),             // il
//     0.U(3.W),             // ia
//     0.U(3.W),             // ds
//     0.U(3.W),             // dl
//     0.U(3.W),             // da
//     0.U(1.W),             // c2
//     0.U(1.W),             // md
//     0.U(1.W),             // pc
//     0.U(1.W),             // wr
//     0.U(1.W),             // ca
//     0.U(1.W),             // ep
//     0.U(1.W),             // fp
//   )

//   // Wired
//   val wired = RegInit(0.U((log2Ceil(TLB_NUM)).W))
//   when(mtc0_we && cp0_addr === CP0_WIRED_ADDR) {
//     wired := cp0_wdata(log2Ceil(TLB_NUM) - 1, 0)
//   }

//   cp0_wired := Cat(
//     0.U((32 - log2Ceil(TLB_NUM)).W),
//     wired,
//   )

//   val trap_base = Wire(UInt(32.W))
//   trap_base := Mux(cp0_status(22), "hbfc00200".U, cp0_ebase)

//   flush_pc := MuxCase(
//     (trap_base + "h180".U),
//     Seq(
//       io.fromWriteBackStage.ws_after_tlb        -> wb_pc,
//       io.fromWriteBackStage.ws_inst_is_eret     -> cp0_epc,
//       io.fromWriteBackStage.ex_tlb_refill_entry -> (trap_base + 0.U),
//       (wb_ex & cp0_cause_iv & !cp0_status_bev)  -> (trap_base + "h200".U),
//     ),
//   )

//   cp0_rdata := MuxLookup(
//     cp0_addr,
//     ZERO_WORD,
//     Seq(
//       CP0_STATUS_ADDR    -> cp0_status,
//       CP0_CAUSE_ADDR     -> cp0_cause,
//       CP0_EPC_ADDR       -> cp0_epc,
//       CP0_BADV_ADDR      -> cp0_badvaddr,
//       CP0_COUNT_ADDR     -> cp0_count,
//       CP0_COMPARE_ADDR   -> cp0_compare,
//       CP0_ENTRYHI_ADDR   -> cp0_entryhi,
//       CP0_ENTRYLO0_ADDR  -> cp0_entrylo0,
//       CP0_ENTRYLO1_ADDR  -> cp0_entrylo1,
//       CP0_INDEX_ADDR     -> cp0_index,
//       CP0_EBASE_ADDR     -> cp0_ebase,
//       CP0_PAGE_MASK_ADDR -> cp0_page_mask,
//       CP0_CONTEXT_ADDR   -> cp0_context,
//       CP0_CONFIG_ADDR    -> cp0_config,
//       CP0_CONFIG1_ADDR   -> cp0_config1,
//       CP0_WIRED_ADDR     -> cp0_wired,
//     ),
//   )
// }
