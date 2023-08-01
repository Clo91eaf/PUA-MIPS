package cache.memory

import chisel3._
import chisel3.util.log2Ceil

/** simple dual port ram
  *
  * @param wdataidth
  *   : width of every data line
  * @param byteWriteWidth
  *   : how many bits to write per mask
  * @param numberOfLines
  *   : how many lines of data are in the ram
  * @param waddridth
  *   : how wide is the request (to cover all lines)
  * @param memoryPrimitive
  *   : should I use auto, block ram or distributed ram
  */
class SimpleDualPortRamIP(
    wdataidth: Int = 32,
    byteWriteWidth: Int = 8,
    numberOfLines: Int,
    waddridth: Int,
    memoryPrimitive: String = "block",
) extends BlackBox(
      Map(
        "ADDR_WIDTH_A"       -> waddridth,
        "ADDR_WIDTH_B"       -> waddridth,
        "WRITE_DATA_WIDTH_A" -> wdataidth,
        "READ_DATA_WIDTH_B"  -> wdataidth,
        "BYTE_WRITE_WIDTH_A" -> byteWriteWidth,
        "CLOCKING_MODE"      -> "common_clock",
        "READ_LATENCY_B"     -> 1,
        "MEMORY_SIZE"        -> numberOfLines * wdataidth,
        "MEMORY_PRIMITIVE"   -> memoryPrimitive,
      ),
    ) {
  override def desiredName: String = "xpm_memory_sdpram"
  require(waddridth <= 20, "request width should be 1 to 20")
  require(
    wdataidth - (wdataidth / byteWriteWidth) * byteWriteWidth == 0,
    "data width should be a multiple of byte write width",
  )
  require(
    List("auto", "block", "distributed", "ultra").contains(memoryPrimitive),
    "memory primitive should be auto, block ram, dist ram or ultra ram",
  )
  require(
    waddridth == log2Ceil(numberOfLines),
    "request width should be log 2 of number of lines to request all",
  )
  val io = IO(new Bundle {
    // clock and reset
    val clka = Input(Clock())
    val clkb = Input(Clock())
    val rstb = Input(Reset())

    val addra = Input(UInt(waddridth.W))
    val dina  = Input(UInt(wdataidth.W))
    val ena   = Input(Bool())
    val wea   = Input(UInt((wdataidth / byteWriteWidth).W))

    val addrb  = Input(UInt(waddridth.W))
    val enb    = Input(Bool())
    val regceb = Input(Bool())
    val doutb  = Output(UInt(wdataidth.W))
  })
}
