package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class ReadStage() extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val broadcast_free_valid = Input(Bool())
        val broadcast_free_register = Input(UInt(5.W))
        val broadcast_free_value = Input(UInt(32.W))

        val read_register_1 = Output(UInt(5.W))
        val read_result_1 = Input(UInt(32.W))
        val read_register_2 = Output(UInt(5.W))
        val read_result_2 = Input(UInt(32.W))

        val next_instruction = Output(new InstructionBundle())
        val next_valid = Output(Bool())

        val flush = Input(Bool())

        val ready = Output(Bool())
    })

    io.read_register_1 := io.instruction.rs1
    io.read_register_2 := io.instruction.rs2

    val held_instruction = RegInit(0.U.asTypeOf(new InstructionBundle()))
    val held_valid = RegInit(false.B)

    io.next_instruction := held_instruction
    io.next_valid := held_valid

    when(io.next_ready) {
        held_instruction := io.instruction
        held_instruction.rs1_value := io.read_result_1
        held_instruction.rs2_value := io.read_result_2
        held_valid := io.valid
    }

    io.ready := io.next_ready

    when(io.flush) {
        held_instruction := 0.U.asTypeOf(new InstructionBundle())
        held_valid := false.B
    }

    // printf("[Read Stage]: rs1 %d, %d\n", io.instruction.rs1, io.read_result_1)
    // printf("[Read Stage]: rs2 %d, %d\n", io.instruction.rs2, io.read_result_2)
}
