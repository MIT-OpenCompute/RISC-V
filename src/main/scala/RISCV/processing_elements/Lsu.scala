package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Lsu() extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val out = Output(new InstructionBundle())
        val out_valid = Output(Bool())

        val flush = Input(Bool())

        val ready = Output(Bool())

        val broadcast_free_valid = Output(Bool())
        val broadcast_free_register = Output(UInt(5.W))
        val broadcast_free_value = Output(UInt(32.W))

        val memory_read_requested = Output(Bool())
        val memory_read_address = Output(UInt(32.W))
        val memory_read_value = Input(UInt(32.W))
        val memory_read_ready = Input(Bool())
        val memory_read_valid = Input(Bool())
    })

    val out = RegInit(0.U.asTypeOf(new InstructionBundle))
    io.out := out
    val out_valid = RegInit(false.B)
    io.out_valid := out_valid

    when(io.next_ready && out_valid) {
        out_valid := false.B
    }

    val waiting_on_read = RegInit(false.B)
    val ignore_next_response = RegInit(false.B)

    io.ready := io.next_ready && !waiting_on_read && io.memory_read_ready

    io.broadcast_free_valid := false.B
    io.broadcast_free_register := 0.U
    io.broadcast_free_value := 0.U

    io.memory_read_requested := false.B
    io.memory_read_address := 0.U

    when(io.next_ready && io.valid && !waiting_on_read && io.memory_read_ready) {
        out := io.instruction

        switch(io.instruction.opcode) {
            is("b0000011".U) {
                switch(io.instruction.func3) {
                    // LW
                    is("b010".U) {
                        waiting_on_read := true.B

                        io.memory_read_requested := true.B

                        io.memory_read_address := (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt / 4.U
                    }
                }
            }

            is("b0100011".U) {
                switch(io.instruction.func3) {
                    // SW
                    is("b010".U) {
                        out_valid := true.B

                        out.rd := (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt / 4.U
                        out.rd_value := io.instruction.rs2_value

                        printf("__SW rd: %d\n", (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt / 4.U)
                    }
                }
            }
        }
    }

    when(waiting_on_read && io.memory_read_valid && !ignore_next_response) {
        waiting_on_read := false.B
        out_valid := true.B

        out.rd_value := io.memory_read_value

        io.broadcast_free_valid := true.B
        io.broadcast_free_register := out.rd
        io.broadcast_free_value := io.memory_read_value
    }

    when(io.memory_read_valid) {
        ignore_next_response := false.B
    }

    when(io.flush) {
        out := 0.U.asTypeOf(new InstructionBundle)
        out_valid := false.B
        waiting_on_read := false.B

        ignore_next_response := true.B
    }

    printf("[LSU] out valid: %b\n", io.out_valid)
    printf("[LSU] out rd: %d\n", io.out.rd)
}
