package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/**
  *
  *
  */
class RAM() extends Module {
  val width: Int = 32
  val io = IO(new Bundle {
    val enable = Input(Bool())
    val write = Input(Bool())
    val addr = Input(UInt(10.W))
    val data_in = Input(UInt(width.W))
    val data_out = Output(UInt(width.W))
  })

  val mem = SyncReadMem(1024, UInt(width.W))
  // Create one write port and one read port
  mem.write(io.addr, io.dataIn)
  io.dataOut := mem.read(io.addr, io.enable)
}

object RAM extends App {
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