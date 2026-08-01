package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Alu(val width: Int = 32) extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val out = Output(new InstructionBundle())
        val out_valid = Output(Bool())

        val ready = Output(Bool())

        val lsu_broadcast_valid = Input(Bool())
        val broadcast_free_valid = Output(Bool())
        val broadcast_free_register = Output(UInt(5.W))
        val broadcast_free_value = Output(UInt(32.W))
    })

    val out = RegInit(0.U.asTypeOf(new InstructionBundle))
    io.out := out
    val out_valid = RegInit(false.B)
    io.out_valid := out_valid

    val ready = io.next_ready && !io.lsu_broadcast_valid

    io.ready := ready

    io.broadcast_free_valid := false.B
    io.broadcast_free_register := 0.U
    io.broadcast_free_value := 0.U

    when(ready && io.valid) {
        out := io.instruction
        out_valid := true.B

        switch(io.instruction.opcode) {
            is("b0010011".U) {
                switch(io.instruction.func3) {
                    // ADDI
                    is("b000".U) {
                        out.rd_value := io.instruction.rs1_value + io.instruction.immediate

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value + io.instruction.immediate
                    }
                }
            }
        }
    }
}
