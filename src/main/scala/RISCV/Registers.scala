package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class Registers() extends Module {
    val io = IO(new Bundle {
        val in  = Input(UInt(32.W))
        val write_addr = Input(UInt(5.W))
        val read_addr = Input(UInt(5.W))
        val write_enable = Input(Bool())
        val out = Output(UInt(32.W))
    })

    val regs = RegInit(VecInit(Seq.fill(32.toInt)(0.U(32.W))))

    io.out := regs(io.read_addr)

    when(io.write_enable && (io.write_addr =/= 0.U)) {
        regs(io.write_addr) := io.in
    }
}
