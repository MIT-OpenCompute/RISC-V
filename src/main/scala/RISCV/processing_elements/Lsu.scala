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
    val waiting_on_instruction = RegInit(0.U.asTypeOf(new InstructionBundle))
    val ignore_next_response = RegInit(false.B)

    io.ready := io.next_ready && !waiting_on_read && io.memory_read_ready

    io.broadcast_free_valid := false.B
    io.broadcast_free_register := 0.U
    io.broadcast_free_value := 0.U

    io.memory_read_requested := false.B
    io.memory_read_address := 0.U

    // printf(
    //   "[LSU] next ready: %b valid: %b waiting on read: %b memory read ready: %b \n",
    //   io.next_ready,
    //   io.valid,
    //   waiting_on_read,
    //   io.memory_read_ready
    // )

    when(io.next_ready && io.valid && !waiting_on_read && io.memory_read_ready) {
        out := io.instruction

        // printf("[LSU] opcode: %b func3: %b\n", io.instruction.opcode, io.instruction.func3)

        switch(io.instruction.opcode) {
            is("b0000011".U) {
                switch(io.instruction.func3) {
                    // LB
                    is("b000".U) {
                        waiting_on_read := true.B
                        waiting_on_instruction := io.instruction

                        io.memory_read_requested := true.B

                        io.memory_read_address := ((io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt >> 2) << 2
                    }

                    // LH
                    is("b001".U) {
                        waiting_on_read := true.B
                        waiting_on_instruction := io.instruction

                        io.memory_read_requested := true.B

                        io.memory_read_address := ((io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt >> 2) << 2
                    }

                    // LW
                    is("b010".U) {
                        waiting_on_read := true.B
                        waiting_on_instruction := io.instruction

                        io.memory_read_requested := true.B

                        io.memory_read_address := (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt
                    }
                }
            }

            is("b0100011".U) {
                switch(io.instruction.func3) {
                    // SB
                    is("b000".U) {
                        waiting_on_read := true.B
                        waiting_on_instruction := io.instruction

                        io.memory_read_requested := true.B

                        io.memory_read_address := ((io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt >> 2) << 2
                    }
                    // SH
                    is("b001".U) {
                        waiting_on_read := true.B
                        waiting_on_instruction := io.instruction

                        io.memory_read_requested := true.B

                        io.memory_read_address := ((io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt >> 2) << 2
                    }
                    // SW
                    is("b010".U) {
                        out_valid := true.B

                        out.rd := (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt
                        out.rd_value := io.instruction.rs2_value
                    }
                }
            }
        }
    }

    when(waiting_on_read && io.memory_read_valid && !ignore_next_response) {
        waiting_on_read := false.B
        out_valid := true.B

        switch(waiting_on_instruction.opcode) {
            is("b0000011".U) {
                io.broadcast_free_valid := true.B
                io.broadcast_free_register := out.rd

                switch(waiting_on_instruction.func3) {
                    // LB
                    is("b000".U) {
                        switch((waiting_on_instruction.rs1_value.zext + waiting_on_instruction.immediate.asSInt).asUInt % 4.U) {
                            is(0.U) {
                                out.rd_value := io.memory_read_value & 0xff.U
                                io.broadcast_free_value := io.memory_read_value & 0xff.U
                            }
                            is(1.U) {
                                out.rd_value := (io.memory_read_value >> 8) & 0xff.U
                                io.broadcast_free_value := (io.memory_read_value >> 8) & 0xff.U
                            }
                            is(2.U) {
                                out.rd_value := (io.memory_read_value >> 16) & 0xff.U
                                io.broadcast_free_value := (io.memory_read_value >> 16) & 0xff.U
                            }
                            is(3.U) {
                                out.rd_value := io.memory_read_value >> 24
                                io.broadcast_free_value := io.memory_read_value >> 24
                            }
                        }
                    }

                    // LH
                    is("b001".U) {
                        switch((waiting_on_instruction.rs1_value.zext + waiting_on_instruction.immediate.asSInt).asUInt % 4.U) {
                            is(0.U) {
                                out.rd_value := io.memory_read_value & 0xffff.U
                                io.broadcast_free_value := io.memory_read_value & 0xffff.U
                            }
                            is(2.U) {
                                out.rd_value := io.memory_read_value >> 16
                                io.broadcast_free_value := io.memory_read_value >> 16
                            }
                        }
                    }

                    // LW
                    is("b010".U) {
                        out.rd_value := io.memory_read_value
                        io.broadcast_free_value := io.memory_read_value
                    }
                }
            }

            is("b0100011".U) {
                out.rd := ((waiting_on_instruction.rs1_value.zext + waiting_on_instruction.immediate.asSInt).asUInt >> 2) << 2

                switch(waiting_on_instruction.func3) {
                    // LB
                    is("b000".U) {
                        switch((waiting_on_instruction.rs1_value.zext + waiting_on_instruction.immediate.asSInt).asUInt % 4.U) {
                            is(0.U) {
                                out.rd_value := (io.memory_read_value & 0xffffff00L.U) | (waiting_on_instruction.rs2_value & 0xff.U)
                            }
                            is(1.U) {
                                out.rd_value := (io.memory_read_value & 0xffff00ffL.U) | ((waiting_on_instruction.rs2_value & 0xff.U) << 8)
                            }
                            is(2.U) {
                                out.rd_value := (io.memory_read_value & 0xff00ffffL.U) | ((waiting_on_instruction.rs2_value & 0xff.U) << 16)
                            }
                            is(3.U) {
                                out.rd_value := (io.memory_read_value & 0x00ffffffL.U) | ((waiting_on_instruction.rs2_value & 0xff.U) << 24)
                            }
                        }
                    }

                    // LH
                    is("b001".U) {
                        switch((waiting_on_instruction.rs1_value.zext + waiting_on_instruction.immediate.asSInt).asUInt % 4.U) {
                            is(0.U) {
                                out.rd_value := (io.memory_read_value & 0xffff0000L.U) | (waiting_on_instruction.rs2_value & 0xffff.U)
                            }
                            is(2.U) {
                                out.rd_value := (io.memory_read_value & 0x0000ffffL.U) | ((waiting_on_instruction.rs2_value & 0xffff.U) << 16)
                            }
                        }
                    }
                }
            }
        }
    }

    when(io.memory_read_valid) {
        ignore_next_response := false.B
    }

    when(io.flush) {
        out := 0.U.asTypeOf(new InstructionBundle)
        out_valid := false.B
        waiting_on_read := false.B

        ignore_next_response := waiting_on_read
    }

    // printf("[LSU] waiting on read: %b\n", waiting_on_read)
    // printf("[LSU] memory read valid: %b\n", io.memory_read_valid)
    // printf("[LSU] ignore next response: %b\n", ignore_next_response)
}
