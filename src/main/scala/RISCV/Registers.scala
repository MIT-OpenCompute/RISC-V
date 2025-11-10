package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage

/* 
 * The register file module for RISC-V
 * 32 registers, each 32 bits wide
 * Dual read ports, single write port
 * Register x0 is hardwired to 0
 * 
 * This register file supports a simultaneous combinational read from two registers via the two read addresses and output ports
 * as well as a synchronous write to one register on the rising edge of the clock when write_enable is high.
 * 
 * I/O:
    * in: 32-bit input data to write to register
    * write_addr: 5-bit address of register to write to
    * read_addr_A: 5-bit address of register to read from (port A)
    * read_addr_B: 5-bit address of register to read from (port B)
    * write_enable: boolean signal to enable writing to register
    * out_A: 32-bit output data from read port A
    * out_B: 32-bit output data from read port B
 *
 */

class Registers() extends Module {
    val io = IO(new Bundle {
        val in  = Input(UInt(32.W))
        val write_addr = Input(UInt(5.W))
        val read_addr_A = Input(UInt(5.W))
        val read_addr_B = Input(UInt(5.W))
        val read_addr_C = Input(UInt(5.W)) // Testing address port
        val write_enable = Input(Bool())
        val out_A = Output(UInt(32.W))
        val out_B = Output(UInt(32.W))
        val out_C = Output(UInt(32.W)) // Testing output port
    })

    val regs = RegInit(VecInit(Seq.fill(32.toInt)(0.U(32.W))))

    // Dual read ports
    io.out_A := regs(io.read_addr_A)
    io.out_B := regs(io.read_addr_B)
    io.out_C := regs(io.read_addr_C)

    // Uncomment to print the register contents every time they are accessed
    //printf("Regs: [%d]=%d, [%d]=%d, WE=%b, WA=%d, IN=%d\n", io.read_addr_A, io.out_A, io.read_addr_B, io.out_B, io.write_enable, io.write_addr, io.in)

    // Single write port
    when (io.write_enable && (io.write_addr =/= 0.U)) {
        regs(io.write_addr) := io.in
    }
}
