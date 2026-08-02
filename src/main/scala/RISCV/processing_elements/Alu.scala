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
            // AUIPC
            is("b0010111".U) {
                out.rd_value := io.instruction.instruction_pointer + io.instruction.immediate;
            }

            is("b0010011".U) {
                switch(io.instruction.func3) {
                    // ADDI
                    is("b000".U) {
                        out.rd_value := io.instruction.rs1_value + io.instruction.immediate

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value + io.instruction.immediate
                    }

                    // SLLI
                    is("b001".U) {
                        out.rd_value := io.instruction.rs1_value << io.instruction.immediate(5, 0)

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value + io.instruction.immediate
                    }

                    // SRLI and SRAI
                    is("b101".U) {
                        when(io.instruction.immediate(10) === 1.U) { // SRAI
                            out.rd_value := (io.instruction.rs1_value.asSInt >> io.instruction.immediate(5, 0)).asUInt

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := (io.instruction.rs1_value.asSInt >> io.instruction.immediate(5, 0)).asUInt
                        }.otherwise { // SLAI
                            out.rd_value := io.instruction.rs1_value >> io.instruction.immediate(5, 0)

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := io.instruction.rs1_value >> io.instruction.immediate(5, 0)
                        }
                    }

                    // SLTI
                    is("b010".U) {
                        when(io.instruction.rs1_value.asSInt < io.instruction.immediate.asSInt) {
                            out.rd_value := 1.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 1.U
                        }.otherwise {
                            out.rd_value := 0.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 0.U
                        }
                    }

                    // SLTIU
                    is("b011".U) {
                        when(io.instruction.rs1_value < io.instruction.immediate) {
                            out.rd_value := 1.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 1.U
                        }.otherwise {
                            out.rd_value := 0.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 0.U
                        }
                    }

                    // XORI
                    is("b100".U) {
                        out.rd_value := io.instruction.rs1_value ^ io.instruction.immediate

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value ^ io.instruction.immediate
                    }

                    // ORI
                    is("b110".U) {
                        out.rd_value := io.instruction.rs1_value | io.instruction.immediate

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value | io.instruction.immediate
                    }

                    // ANDI
                    is("b111".U) {
                        out.rd_value := io.instruction.rs1_value & io.instruction.immediate

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value & io.instruction.immediate
                    }
                }
            }

            is("b0110011".U) {
                switch(io.instruction.func3) {
                    // ADD
                    is("b0000000".U) {
                        out.rd_value := io.instruction.rs1_value + io.instruction.rs2_value

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value + io.instruction.rs2_value
                    }

                    // SUB
                    is("b0110000".U) {
                        out.rd_value := io.instruction.rs1_value - io.instruction.rs2_value

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value - io.instruction.rs2_value
                    }

                    // SLL
                    is("b001".U) {
                        out.rd_value := io.instruction.rs1_value << io.instruction.rs2_value(5, 0)

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value << io.instruction.rs2_value(5, 0)
                    }

                    // SRL and SRA
                    is("b101".U) {
                        when(io.instruction.immediate(10) === 1.U) { // SRA
                            out.rd_value := (io.instruction.rs1_value.asSInt >> io.instruction.rs2_value(5, 0)).asUInt

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := (io.instruction.rs1_value.asSInt >> io.instruction.rs2_value(5, 0)).asUInt
                        }.otherwise { // SLA
                            out.rd_value := io.instruction.rs1_value >> io.instruction.rs2_value(5, 0)

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := io.instruction.rs1_value >> io.instruction.rs2_value(5, 0)
                        }
                    }

                    // SLT
                    is("b010".U) {
                        when(io.instruction.rs1_value.asSInt < io.instruction.rs2_value.asSInt) {
                            out.rd_value := 1.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 1.U
                        }.otherwise {
                            out.rd_value := 0.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 0.U
                        }
                    }

                    // SLTU
                    is("b011".U) {
                        when(io.instruction.rs1_value < io.instruction.rs2_value) {
                            out.rd_value := 1.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 1.U
                        }.otherwise {
                            out.rd_value := 0.U

                            io.broadcast_free_valid := true.B
                            io.broadcast_free_register := io.instruction.rd
                            io.broadcast_free_value := 0.U
                        }
                    }

                    // XOR
                    is("b100".U) {
                        out.rd_value := io.instruction.rs1_value ^ io.instruction.rs2_value

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value ^ io.instruction.rs2_value
                    }

                    // OR
                    is("b110".U) {
                        out.rd_value := io.instruction.rs1_value | io.instruction.rs2_value

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value | io.instruction.rs2_value
                    }

                    // AND
                    is("b111".U) {
                        out.rd_value := io.instruction.rs1_value & io.instruction.rs2_value

                        io.broadcast_free_valid := true.B
                        io.broadcast_free_register := io.instruction.rd
                        io.broadcast_free_value := io.instruction.rs1_value & io.instruction.rs2_value
                    }
                }
            }
        }
    }
}
