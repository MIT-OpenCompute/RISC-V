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
        val source_program_pointer = Output(UInt(32.W))
        val target_program_pointer = Output(UInt(32.W))

        val broadcast_free_valid = Output(Bool())
        val broadcast_free_register = Output(UInt(5.W))
        val broadcast_free_value = Output(UInt(32.W))

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
    io.source_program_pointer := 0.U

    io.broadcast_free_valid := false.B
    io.broadcast_free_register := 0.U
    io.broadcast_free_value := 0.U

    when(io.next_ready && io.valid) {
        out := io.instruction
        out_valid := true.B

        switch(io.instruction.opcode) {
            is("b1101111".U) { // JAL
                val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                out.rd_value := io.instruction.instruction_pointer + 4.U
                io.target_program_pointer := jump_target
                io.source_program_pointer := io.instruction.instruction_pointer

                io.broadcast_free_valid := true.B
                io.broadcast_free_register := out.rd
                io.broadcast_free_value := io.instruction.instruction_pointer + 4.U
            }

            is("b1100111".U) { // JALR
                val jump_target = (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt & ~1.U(32.W)
                io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                out.rd_value := io.instruction.instruction_pointer + 4.U
                io.target_program_pointer := jump_target
                io.source_program_pointer := io.instruction.instruction_pointer

                io.broadcast_free_valid := true.B
                io.broadcast_free_register := out.rd
                io.broadcast_free_value := io.instruction.instruction_pointer + 4.U
            }

            is("b1100011".U) {
                // printf("[JU] branching! func3: %b\n", io.instruction.func3)

                switch(io.instruction.func3) {
                    // BEQ
                    is("b000".U) {
                        when(io.instruction.rs1_value === io.instruction.rs2_value) {
                            val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }.otherwise {
                            val jump_target = io.instruction.instruction_pointer + 4.U
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }
                    }

                    // BNEQ
                    is("b001".U) {
                        when(io.instruction.rs1_value =/= io.instruction.rs2_value) {
                            val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }.otherwise {
                            val jump_target = io.instruction.instruction_pointer + 4.U
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }
                    }

                    // BLT
                    is("b100".U) {
                        when(io.instruction.rs1_value.asSInt < io.instruction.rs2_value.asSInt) {
                            val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }.otherwise {
                            val jump_target = io.instruction.instruction_pointer + 4.U
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }
                    }

                    // BLTU
                    is("b110".U) {
                        when(io.instruction.rs1_value < io.instruction.rs2_value) {
                            val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }.otherwise {
                            val jump_target = io.instruction.instruction_pointer + 4.U
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }
                    }

                    // BGE
                    is("b101".U) {
                        when(io.instruction.rs1_value.asSInt >= io.instruction.rs2_value.asSInt) {
                            val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }.otherwise {
                            val jump_target = io.instruction.instruction_pointer + 4.U
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }
                    }

                    // BGEU
                    is("b111".U) {
                        when(io.instruction.rs1_value >= io.instruction.rs2_value) {
                            val jump_target = (io.instruction.instruction_pointer.zext + io.instruction.immediate.asSInt).asUInt
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }.otherwise {
                            val jump_target = io.instruction.instruction_pointer + 4.U
                            io.flush := io.instruction.predicted_instruction_pointer =/= jump_target

                            io.target_program_pointer := jump_target
                            io.source_program_pointer := io.instruction.instruction_pointer
                        }
                    }
                }
            }
        }
    }
}
