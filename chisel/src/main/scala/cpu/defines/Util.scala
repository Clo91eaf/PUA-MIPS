package cpu.defines

import chisel3._
import chisel3.util._

object Util {
  def subwordModify(source: UInt, start: Int, md: UInt): UInt = {
    val ms = md.getWidth
    subwordModify(source, (start, start - ms + 1), md)
  }

  def subwordModify(source: UInt, tuple: (Int, Int), md: UInt): UInt = {
    val ws = source.getWidth
    val ms = md.getWidth
    val start = tuple._1
    val end = tuple._2
    require(
      ws > start && start >= end && end >= 0,
      s"ws: $ws, start: $start, end: $end"
    )
    require(start - end == ms - 1)
    if (end == 0) Cat(source(ws - 1, start + 1), md)
    else if (start == ws - 1) Cat(md, source(end - 1, 0))
    else Cat(source(ws - 1, start + 1), md, source(end - 1, 0))
  }

  def listHasElement(list: Seq[UInt], element: UInt): Bool = {
    list.foldLeft(false.B)((r, e) => r || (e === element))
  }

  def MAXnBIT(m: Int): BigInt = BigInt(1) << m

  def unsignedToSigned(s: BigInt, width: Int = 32): BigInt = {
    val m = MAXnBIT(width - 1)
    if (s >= m) s - 2 * m
    else s
  }

  def signedExtend(raw: UInt, to: Int = 32): UInt = {
    signedExtend(raw, raw.getWidth, to)
  }

  def signedExtend(raw: UInt, from: Int, to: Int): UInt = {
    require(to > from && from >= 1)
    Cat(Fill(to - from, raw(from - 1)), raw)
  }

  def zeroExtend(raw: UInt, to: Int = 32): UInt = {
    zeroExtend(raw, raw.getWidth, to)
  }

  def zeroExtend(raw: UInt, from: Int, to: Int): UInt = {
    require(to > from && from >= 1)
    Cat(Fill(to - from, 0.U), raw)
  }
}
