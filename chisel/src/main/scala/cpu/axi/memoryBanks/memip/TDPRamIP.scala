package cache.memip

import chisel3._
import chisel3.util.log2Ceil

/** true dual port ram, could be LUT or bram or UltraRam(non applicable)
  *
  * @param wdataidth
  *   : the width of data in bits
  * @param byteWriteWidth
  *   : how many bits to write each bit in write mask (wea)
  * @param waddridth
  *   : the request of the width to request all locations
  * @param numberOfLines
  *   : how wide is the request (to cover all lines)
  * @param memoryPrimitive
  *   : should I use auto, block ram or distributed ram
  */
class TDPRamIP(
    wdataidth: Int = 32,
    byteWriteWidth: Int = 8,
    waddridth: Int,
    numberOfLines: Int,
    memoryPrimitive: String = "block",
) extends BlackBox(
      Map(
        "ADDR_WIDTH_A"       -> waddridth,
        "ADDR_WIDTH_B"       -> waddridth,
        "WRITE_DATA_WIDTH_A" -> wdataidth,
        "WRITE_DATA_WIDTH_B" -> wdataidth,
        "READ_DATA_WIDTH_A"  -> wdataidth,
        "READ_DATA_WIDTH_B"  -> wdataidth,
        "BYTE_WRITE_WIDTH_A" -> byteWriteWidth,
        "BYTE_WRITE_WIDTH_B" -> byteWriteWidth,
        "CLOCKING_MODE"      -> "common_clock",
        "READ_LATENCY_A"     -> 1,
        "READ_LATENCY_B"     -> 1,
        "MEMORY_SIZE"        -> numberOfLines * wdataidth,
        "MEMORY_PRIMITIVE"   -> memoryPrimitive,
        "WRITE_MODE_A"       -> "write_first",
        "WRITE_MODE_B"       -> "write_first",
      ),
    ) {
  override def desiredName: String = "xpm_memory_tdpram"
  require(
    wdataidth - (wdataidth / byteWriteWidth) * byteWriteWidth == 0,
    "data width should be a multiple of byte write width",
  )
  require(
    List("auto", "block", "distributed", "ultra").contains(memoryPrimitive),
    "memory primitive should be auto, block ram, dist ram or ultra ram",
  )
  require(waddridth <= 20, "request width should be 1 to 20")
  require(
    waddridth == log2Ceil(numberOfLines),
    "request width should be log 2 of number of lines to request all",
  )
  val io = IO(new Bundle {
    // clock and reset
    val clka = Input(Clock())
    val clkb = Input(Clock())
    val rsta = Input(Reset())
    val rstb = Input(Reset())

    val addra  = Input(UInt(waddridth.W))
    val dina   = Input(UInt(wdataidth.W))
    val ena    = Input(Bool())
    val regcea = Input(Bool())
    val wea    = Input(UInt((wdataidth / byteWriteWidth).W))
    val douta  = Output(UInt(wdataidth.W))

    val addrb  = Input(UInt(waddridth.W))
    val dinb   = Input(UInt(wdataidth.W))
    val enb    = Input(Bool())
    val regceb = Input(Bool())
    val web    = Input(UInt((wdataidth / byteWriteWidth).W))
    val doutb  = Output(UInt(wdataidth.W))

  })

}
