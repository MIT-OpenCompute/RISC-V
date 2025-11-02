package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

/**
  * The Hardware thread "Hart" used in the 
  *
  * @param width Bit width (default: 32 bits)
  */
  
class Hart(val width: Int = 32) extends Module {
    val io = IO(new Bundle {})

    val program_pointer = RegInit(0.U(width.W))

    val registers = Module(new Registers(width, 32));
}