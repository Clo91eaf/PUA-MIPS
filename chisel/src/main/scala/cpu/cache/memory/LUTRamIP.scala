package cache.memory

import chisel3._
import chisel3.util.log2Ceil

/** XPM 2019.2 XPM_MEMORY_DPDISTRAM, at page 124 of UG953(2019.2) by default, this is initialized to
  * all 0
  *
  * @param wdataidth
  *   : the size of the data to store in each line, in bits
  * @param waddridth
  *   : the width of request
  * @param byteWriteWidth
  *   : addressable size of write
  * @param numberOfLines
  *   : how many **bits** there are in the memory
  */
class LUTRamIP(wdataidth: Int, waddridth: Int, byteWriteWidth: Int, numberOfLines: Int)
    extends BlackBox(
      Map(
        "ADDR_WIDTH_A"       -> waddridth,
        "ADDR_WIDTH_B"       -> waddridth,
        "MEMORY_SIZE"        -> numberOfLines * wdataidth,
        "WRITE_DATA_WIDTH_A" -> wdataidth,
        "READ_DATA_WIDTH_A"  -> wdataidth,
        "READ_DATA_WIDTH_B"  -> wdataidth,
        "BYTE_WRITE_WIDTH_A" -> byteWriteWidth,
        "READ_LATENCY_A"     -> 0,
        "READ_LATENCY_B"     -> 0,
        "READ_RESET_VALUE_A" -> 0,
        "READ_RESET_VALUE_B" -> 0,
        "CLOCKING_MODE"      -> "common_clock",
      ),
    ) {
  override def desiredName: String = "xpm_memory_dpdistram"
  require(
    waddridth == log2Ceil(numberOfLines),
    "request width should be log 2 of number of lines to request all",
  )
  require(
    wdataidth - (wdataidth / byteWriteWidth) * byteWriteWidth == 0,
    "data width should be a multiple of byte write width",
  )
  require(waddridth <= 20, "request width should be 1 to 20")
  val io = IO(new Bundle {
    val clka = Input(Clock())
    val clkb = Input(Clock())
    val rsta = Input(Reset())
    val rstb = Input(Reset())

    val ena    = Input(Bool())
    val enb    = Input(Bool())
    val regcea = Input(Bool())
    val regceb = Input(Bool())

    val dina  = Input(UInt(wdataidth.W))
    val addra = Input(UInt(waddridth.W))
    val addrb = Input(UInt(waddridth.W))

    val wea = Input(UInt((wdataidth / byteWriteWidth).W))

    val douta = Output(UInt(wdataidth.W))
    val doutb = Output(UInt(wdataidth.W))
  })
}
