package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class ALU(val width: Int = 32) extends Module {
    val io = IO(new Bundle {
        val add = Input(Bool());
        val compare = Input(Bool());

        val a = Input(UInt(width.W));
        val b = Input(UInt(width.W));

        val output = Output(UInt(width.W));
    })

    when(io.compare) {
      val gt = io.a > io.b;
      val eq = io.a === io.b;
      val lt = io.a < io.b;

      io.output := Cat(0.U((width - 3).W), gt, eq, lt);
    }.otherwise {
        when(io.add) {
            io.output := io.a + io.b;
        }.otherwise {
            io.output := io.a * io.b;
        }
    }
}

object ALU extends App {
  ChiselStage.emitSystemVerilogFile(
    new ALU(32),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    ),
    args = Array("--target-dir", "generated")
  )
}