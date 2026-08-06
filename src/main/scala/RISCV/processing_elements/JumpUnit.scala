package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class JumpUnit() extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val out = Output(new InstructionBundle())
        val out_valid = Output(Bool())

        val flush = Output(Bool())
        val target_program_pointer = Output(UInt(32.W))

        val ready = Output(Bool())
    })

    val out = RegInit(0.U.asTypeOf(new InstructionBundle))
    io.out := out
    val out_valid = RegInit(false.B)
    io.out_valid := out_valid

    when(io.next_ready && out_valid) {
        out_valid := false.B
    }

    io.ready := io.next_ready

    io.flush := false.B
    io.target_program_pointer := 0.U

    when(io.next_ready && io.valid) {
        out := io.instruction
        io.flush := true.B

        switch(io.instruction.opcode) {
            is("b1101111".U) { // JAL
                printf("JUMP!!!!!!! %d %d", io.instruction.instruction_pointer.zext, io.instruction.immediate.asSInt)

                out.rd_value := io.instruction.instruction_pointer + 4.U
                io.target_program_pointer := (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
            }
        }
    }
}
