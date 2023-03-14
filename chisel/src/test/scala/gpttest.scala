// fullAdderGen.scala
package cpu.openmips

import chisel3._

object test extends App {
  val s = getVerilogString(new Sopc())
  println(s)
}
