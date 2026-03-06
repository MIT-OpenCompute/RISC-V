package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
// import chisel3.util.experimental.loadMemoryFromFileInline

class Memory() extends Module {
    val io = IO(new Bundle {
        val address_1 = Input(UInt(32.W))
        val write_1 = Input(Bool())
        val write_value_1 = Input(UInt(32.W))
        val read_1 = Input(Bool())
        val read_value_1 = Output(UInt(32.W))

        val address_2 = Input(UInt(32.W))
        val write_2 = Input(Bool())
        val write_value_2 = Input(UInt(32.W))
        val read_2 = Input(Bool())
        val read_value_2 = Output(UInt(32.W))

        val address_vga = Output(UInt(32.W))
        val write_vga = Output(Bool())
        val write_value_vga = Output(UInt(32.W))
    })

    val memory = SyncReadMem(1024, UInt(32.W))
    // loadMemoryFromFileInline(memory, "program.hex") 
    io.address_vga := 0.U
    io.write_vga := true.B
    io.write_value_vga := 0.U
    io.read_value_1    := 0.U
    io.read_value_2    := 0.U

    when(io.address_1 > 0b1000000000000.U) {
        io.address_vga := io.address_1 - 0b1000000000000.U
        io.write_vga := true.B
        io.write_value_vga := io.write_value_1
    }.otherwise {
        io.read_value_1 := memory.readWrite(
            io.address_1,
            io.write_value_1,
            io.read_1 || io.write_1,
            io.write_1
        )

        io.read_value_2 := memory.readWrite(
            io.address_2,
            io.write_value_2,
            io.read_2 || io.write_2,
            io.write_2
        )
    }
}
