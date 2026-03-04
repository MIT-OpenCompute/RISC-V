package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage

class Memory() extends Module {
    val io = IO(new Bundle {
        val program_pointer = Input(UInt(32.W))
        val instruction = Output(UInt(32.W))

        val address = Input(UInt(32.W))

        val write = Input(Bool())
        val write_value = Input(UInt(32.W))

        val read = Input(Bool())
        val read_value = Output(UInt(32.W))
    })

    val memory = SyncReadMem(1024, UInt(32.W))

    io.instruction := memory.read(io.program_pointer, true.B)

    io.read_value := memory.readWrite(
      io.address,
      io.write_value,
      io.read || io.write,
      io.write
    )
}
