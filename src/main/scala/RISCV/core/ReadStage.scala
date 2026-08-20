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

        val broadcast_mark_valid = Input(Bool())
        val broadcast_mark_register = Input(UInt(5.W))
        val broadcast_mark_reorder_pointer = Input(UInt(8.W))

        val broadcast_retire_valid = Input(Bool())
        val broadcast_retire_register = Input(UInt(5.W))
        val broadcast_retire_reorder_pointer = Input(UInt(8.W))

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

    val register_alias = RegInit(VecInit(Seq.fill(32.toInt)(0.U(8.W))))
    val register_live = RegInit(VecInit(Seq.fill(32.toInt)(false.B)))
    val register_value = RegInit(VecInit(Seq.fill(32.toInt)(0.U(32.W))))

    when(io.broadcast_retire_valid && register_alias(io.broadcast_retire_register) === io.broadcast_retire_reorder_pointer) {
        register_live(io.broadcast_retire_register) := false.B
    }

    when(io.broadcast_mark_valid) {
        register_alias(io.broadcast_mark_register) := io.broadcast_mark_reorder_pointer
        register_live(io.broadcast_mark_register) := true.B
    }

    when(io.broadcast_free_valid) {
        register_value(io.broadcast_free_register) := io.broadcast_free_value
    }

    io.next_instruction := held_instruction
    io.next_valid := held_valid

    when(io.next_ready) {
        held_instruction := io.instruction

        held_instruction.rs1_value := io.read_result_1
        when(register_live(io.instruction.rs1)) {
            held_instruction.rs1_value := register_value(io.instruction.rs1)
        }

        when(io.broadcast_free_valid && io.instruction.rs1 === io.broadcast_free_register) {
            held_instruction.rs1_value := io.broadcast_free_value
        }

        held_instruction.rs2_value := io.read_result_2
        when(register_live(io.instruction.rs2)) {
            held_instruction.rs2_value := register_value(io.instruction.rs2)
        }

        when(io.broadcast_free_valid && io.instruction.rs2 === io.broadcast_free_register) {
            held_instruction.rs2_value := io.broadcast_free_value
        }

        held_valid := io.valid
    }

    io.ready := io.next_ready

    when(io.flush) {
        held_instruction := 0.U.asTypeOf(new InstructionBundle())
        held_valid := false.B

        for (n <- 0 to 31) {
            register_live(n.U) := 0.U
        }
    }

    // printf("[Read Stage]: rs1 %d, %d\n", io.instruction.rs1, io.read_result_1)
    // printf("[Read Stage]: rs2 %d, %d\n", io.instruction.rs2, io.read_result_2)
    // printf("[Register Alias 1]: %b %d %d\n", register_live(1.U), register_alias(1.U), register_value(1.U))
}
