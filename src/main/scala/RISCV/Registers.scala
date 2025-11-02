// See README.md for license details.

package RISCV

import chisel3._
// _root_ disambiguates from packages like chisel3.util.circt if imported
import _root_.circt.stage.ChiselStage
import scala.math._

/**
  * A simple register file module with parameterizable width and number of registers.
  * 
  * TODO: Set up two simultaneous read ports
  */

class Registers() extends Module {
    // Define input/output interface
    val io = IO(new Bundle {
        val in  = Input(UInt(32.W))
        val write_addr = Input(UInt(5.W))
        val read_addr = Input(UInt(5.W))
        val write_enable = Input(Bool())
        val out = Output(UInt(32.W))
    })

    // Internal register array
    val regs = RegInit(VecInit(Seq.fill(32.toInt)(0.U(32.W))))

    // Default output
    io.out := regs(io.read_addr)

    when(io.write_enable && (io.write_addr =/= 0.U)) { // Write operation; Register 0 is hardwired to 0
        regs(io.write_addr) := io.in
        //printf(p"Writing value ${io.in} to register ${io.write_addr}\n")
    }
}
