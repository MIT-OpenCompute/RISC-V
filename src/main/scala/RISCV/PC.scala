// See README.md for license details.

package RISCV

import chisel3._
// _root_ disambiguates from packages like chisel3.util.circt if imported
import _root_.circt.stage.ChiselStage
import scala.math._

class PC() extends Module {
   val io = IO(new Bundle {
       val pc_in = Input(UInt(32.W))
       val pc_out = Output(UInt(32.W))
   })
    val pc_reg = RegInit(0.U(32.W))
    pc_reg := io.pc_in
    io.pc_out := pc_reg
}


object PC extends App {
  ChiselStage.emitSystemVerilogFile(
    new PC(),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    ),
    args = Array("--target-dir", "generated")
  )
}
