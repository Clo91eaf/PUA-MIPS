package cpu.pipeline.execute

import chisel3._
import chisel3.util._
import cpu.defines._
import cpu.defines.Const._
import cpu.CpuConfig

class SignedMul extends BlackBox with HasBlackBoxResource {
  val io = IO(new Bundle {
    val CLK = Input(Clock())
    val CE  = Input(Bool())
    val A   = Input(UInt((DATA_WID + 1).W))
    val B   = Input(UInt((DATA_WID + 1).W))

    val P = Output(UInt((HILO_WID + 2).W))
  })
}

class Mul(implicit val config: CpuConfig) extends Module {
  val io = IO(new Bundle {
    val src1        = Input(UInt(DATA_WID.W))
    val src2        = Input(UInt(DATA_WID.W))
    val signed      = Input(Bool())
    val start       = Input(Bool())
    val allow_to_go = Input(Bool())

    val ready  = Output(Bool())
    val result = Output(UInt(HILO_WID.W))
  })

  if (config.build) {
    val signedMul = Module(new SignedMul()).io
    val cnt       = RegInit(0.U(log2Ceil(config.mulClockNum).W))

    cnt := MuxCase(
      cnt,
      Seq(
        (io.start && !io.ready) -> (cnt + 1.U),
        io.allow_to_go          -> 0.U,
      ),
    )

    signedMul.CLK := clock
    signedMul.CE  := io.start
    when(io.signed) {
      signedMul.A := Cat(io.src1(DATA_WID - 1), io.src1)
      signedMul.B := Cat(io.src2(DATA_WID - 1), io.src2)
    }.otherwise {
      signedMul.A := Cat(0.U(1.W), io.src1)
      signedMul.B := Cat(0.U(1.W), io.src2)
    }
    io.ready  := cnt >= config.mulClockNum.U
    io.result := signedMul.P(HILO_WID - 1, 0)
  } else {
    val ready = RegInit(false.B)
    when(io.start) {
      ready := true.B
    }.elsewhen(io.allow_to_go) {
      ready := false.B
    }
    val signed   = RegInit(0.U(HILO_WID.W))
    val unsigned = RegInit(0.U(HILO_WID.W))
    when(io.start) {
      signed   := (io.src1.asSInt * io.src2.asSInt).asUInt
      unsigned := io.src1 * io.src2
    }
    io.result := Mux(io.signed, signed, unsigned)
    io.ready  := ready
  }
}

// class ArrayMulDataModule(len: Int) extends Module {
//   val io = IO(new Bundle() {
//     val a, b       = Input(UInt(len.W))
//     val regEnables = Input(Vec(2, Bool()))
//     val result     = Output(UInt((2 * len).W))
//   })
//   val (a, b) = (io.a, io.b)

//   val b_sext, bx2, neg_b, neg_bx2 = Wire(UInt((len + 1).W))
//   b_sext  := SignExt(b, len + 1)
//   bx2     := b_sext << 1
//   neg_b   := (~b_sext).asUInt()
//   neg_bx2 := neg_b << 1

//   val columns: Array[Seq[Bool]] = Array.fill(2 * len)(Seq())

//   var last_x = WireInit(0.U(3.W))
//   for (i <- Range(0, len, 2)) {
//     val x = if (i == 0) Cat(a(1, 0), 0.U(1.W)) else if (i + 1 == len) SignExt(a(i, i - 1), 3) else a(i + 1, i - 1)
//     val pp_temp = MuxLookup(
//       x,
//       0.U,
//       Seq(
//         1.U -> b_sext,
//         2.U -> b_sext,
//         3.U -> bx2,
//         4.U -> neg_bx2,
//         5.U -> neg_b,
//         6.U -> neg_b,
//       ),
//     )
//     val s = pp_temp(len)
//     val t = MuxLookup(
//       last_x,
//       0.U(2.W),
//       Seq(
//         4.U -> 2.U(2.W),
//         5.U -> 1.U(2.W),
//         6.U -> 1.U(2.W),
//       ),
//     )
//     last_x = x
//     val (pp, weight) = i match {
//       case 0 =>
//         (Cat(~s, s, s, pp_temp), 0)
//       case n if (n == len - 1) || (n == len - 2) =>
//         (Cat(~s, pp_temp, t), i - 2)
//       case _ =>
//         (Cat(1.U(1.W), ~s, pp_temp, t), i - 2)
//     }
//     for (j <- columns.indices) {
//       if (j >= weight && j < (weight + pp.getWidth)) {
//         columns(j) = columns(j) :+ pp(j - weight)
//       }
//     }
//   }

//   def addOneColumn(col: Seq[Bool], cin: Seq[Bool]): (Seq[Bool], Seq[Bool], Seq[Bool]) = {
//     var sum   = Seq[Bool]()
//     var cout1 = Seq[Bool]()
//     var cout2 = Seq[Bool]()
//     col.size match {
//       case 1 => // do nothing
//         sum = col ++ cin
//       case 2 =>
//         val c22 = Module(new C22)
//         c22.io.in := col
//         sum = c22.io.out(0).asBool() +: cin
//         cout2 = Seq(c22.io.out(1).asBool())
//       case 3 =>
//         val c32 = Module(new C32)
//         c32.io.in := col
//         sum = c32.io.out(0).asBool() +: cin
//         cout2 = Seq(c32.io.out(1).asBool())
//       case 4 =>
//         val c53 = Module(new C53)
//         for ((x, y) <- c53.io.in.take(4) zip col) {
//           x := y
//         }
//         c53.io.in.last := (if (cin.nonEmpty) cin.head else 0.U)
//         sum = Seq(c53.io.out(0).asBool()) ++ (if (cin.nonEmpty) cin.drop(1) else Nil)
//         cout1 = Seq(c53.io.out(1).asBool())
//         cout2 = Seq(c53.io.out(2).asBool())
//       case n =>
//         val cin_1               = if (cin.nonEmpty) Seq(cin.head) else Nil
//         val cin_2               = if (cin.nonEmpty) cin.drop(1) else Nil
//         val (s_1, c_1_1, c_1_2) = addOneColumn(col take 4, cin_1)
//         val (s_2, c_2_1, c_2_2) = addOneColumn(col drop 4, cin_2)
//         sum = s_1 ++ s_2
//         cout1 = c_1_1 ++ c_2_1
//         cout2 = c_1_2 ++ c_2_2
//     }
//     (sum, cout1, cout2)
//   }

//   def max(in: Iterable[Int]): Int = in.reduce((a, b) => if (a > b) a else b)
//   def addAll(cols: Array[Seq[Bool]], depth: Int): (UInt, UInt) = {
//     if (max(cols.map(_.size)) <= 2) {
//       val sum = Cat(cols.map(_(0)).reverse)
//       var k   = 0
//       while (cols(k).size == 1) k = k + 1
//       val carry = Cat(cols.drop(k).map(_(1)).reverse)
//       (sum, Cat(carry, 0.U(k.W)))
//     } else {
//       val columns_next = Array.fill(2 * len)(Seq[Bool]())
//       var cout1, cout2 = Seq[Bool]()
//       for (i <- cols.indices) {
//         val (s, c1, c2) = addOneColumn(cols(i), cout1)
//         columns_next(i) = s ++ cout2
//         cout1 = c1
//         cout2 = c2
//       }

//       val needReg = depth == 4
//       val toNextLayer =
//         if (needReg)
//           columns_next.map(_.map(x => RegEnable(x, io.regEnables(1))))
//         else
//           columns_next

//       addAll(toNextLayer, depth + 1)
//     }
//   }

//   val columns_reg  = columns.map(col => col.map(b => RegEnable(b, io.regEnables(0))))
//   val (sum, carry) = addAll(cols = columns_reg, depth = 0)

//   io.result := sum + carry
// }

// class ArrayMultiplier(len: Int) extends Module {
//   override def latency = 2

//   val mulDataModule = Module(new ArrayMulDataModule(len))
//   mulDataModule.io.a          := io.in.bits.src(0)
//   mulDataModule.io.b          := io.in.bits.src(1)
//   mulDataModule.io.regEnables := VecInit((1 to latency) map (i => regEnable(i)))
//   val result = mulDataModule.io.result

//   var ctrlVec = Seq(ctrl)
//   for (i <- 1 to latency) {
//     ctrlVec = ctrlVec :+ PipelineReg(i)(ctrlVec(i - 1))
//   }
//   val 32  = len - 1
//   val res = Mux(ctrlVec.last.isHi, result(2 * 32 - 1, 32), result(32 - 1, 0))

//   io.out.bits.data := Mux(ctrlVec.last.isW, SignExt(res(31, 0), 32), res)

//   XSDebug(p"validVec:${Binary(Cat(validVec))} flushVec:${Binary(Cat(flushVec))}\n")
// }
