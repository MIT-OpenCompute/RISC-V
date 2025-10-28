// See README.md for license details.

package RISCV

import chisel3._
// _root_ disambiguates from packages like chisel3.util.circt if imported
import _root_.circt.stage.ChiselStage
import scala.math._

/**
  * A simple register file module with parameterizable width and number of registers.
  *
  * @param width Width of each register (default: 32 bits)
  * @param nreg Number of registers (default: 32 registers)
  */

class Registers(val width: Int = 32, val nreg: Int = 5) extends Module {
    // Define input/output interface
    val io = IO(new Bundle {
        val in  = Input(UInt(width.W))
        val select = Input(UInt(nreg.W))
        val write_enable = Input(Bool())
        val enable = Input(Bool())
        val out = Output(UInt(width.W))
    })

    // Internal register array
    val regs = RegInit(VecInit(Seq.fill(pow(2,nreg).toInt)(0.U(width.W))))

    // Default output
    io.out := regs(io.select)

    when(io.enable && io.write_enable && (io.select =/= 0.U)) { // Write operation; Register 0 is hardwired to 0
        regs(io.select) := io.in
    }
}

/**
  * Object to generate Verilog/SystemVerilog for the module.
  * Customize firtoolOpts if needed.
  */
object Registers extends App {
  ChiselStage.emitSystemVerilogFile(
    new Registers(),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    )
  )
}
