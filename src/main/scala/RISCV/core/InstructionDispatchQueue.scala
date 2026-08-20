package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class QueueEntry extends Bundle {
    val instruction = new InstructionBundle()
    val valid = Bool()
}

class InstructionDispatchQueue() extends Module {
    val io = IO(new Bundle {
        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val jump_unit_out = Output(new InstructionBundle())
        val jump_unit_out_valid = Output(Bool())
        val jump_unit_ready = Input(Bool())

        val alu_out = Output(new InstructionBundle())
        val alu_out_valid = Output(Bool())
        val alu_ready = Input(Bool())

        val lsu_out = Output(new InstructionBundle())
        val lsu_out_valid = Output(Bool())
        val lsu_ready = Input(Bool())

        val reorder_buffer_tail = Input(UInt(8.W))

        val broadcast_free_valid = Input(Bool())
        val broadcast_free_register = Input(UInt(5.W))
        val broadcast_free_value = Input(UInt(32.W))

        val broadcast_mark_valid = Output(Bool())
        val broadcast_mark_register = Output(UInt(5.W))
        val broadcast_mark_reorder_pointer = Output(UInt(8.W))

        val flush = Input(Bool())

        val ready = Output(Bool())
    })

    val queue = RegInit(VecInit(Seq.fill(8)(0.U.asTypeOf(new QueueEntry))))
    val dependence_size = RegInit(VecInit(Seq.fill(32)(0.U(32.W))))

    val last_free_entry = WireInit(0.U(3.W))
    for (n <- 0 to 7) {
        when(queue(n).valid) {
            last_free_entry := n.U(3.W) + 1.U
        }
    }
    val full = queue(7.U).valid

    val first_valid_entry = WireInit(0.U(3.W))
    val first_valid_entry_valid = WireInit(false.B)
    for (n <- 0 to 7) {
        when(
          queue(7.U - n.U).valid && queue(7.U - n.U).instruction.rs1_dependence_counter === 0.U && queue(
            7.U - n.U
          ).instruction.rs2_dependence_counter === 0.U && ((io.alu_ready && queue(
            7.U - n.U
          ).instruction.pe_type === PeType.Alu) || (io.lsu_ready && queue(7.U - n.U).instruction.pe_type === PeType.Lsu && queue(
            7.U - n.U
          ).instruction.reorder_pointer === io.reorder_buffer_tail) || (io.lsu_ready && queue(
            7.U - n.U
          ).instruction.pe_type === PeType.JumpUnit && queue(
            7.U - n.U
          ).instruction.reorder_pointer === io.reorder_buffer_tail))
        ) {
            first_valid_entry := 7.U - n.U
            first_valid_entry_valid := true.B
        }
    }

    when(io.broadcast_free_valid) {
        dependence_size(io.broadcast_free_register) := dependence_size(io.broadcast_free_register) - 1.U

        for (n <- 0 to 7) {
            when(queue(n.U).instruction.rs1 === io.broadcast_free_register && queue(n.U).instruction.rs1_dependence_counter > 0.U) {
                queue(n.U).instruction.rs1_value := io.broadcast_free_value
                queue(n.U).instruction.rs1_dependence_counter := queue(n.U).instruction.rs1_dependence_counter - 1.U
            }

            when(queue(n.U).instruction.rs2 === io.broadcast_free_register && queue(n.U).instruction.rs2_dependence_counter > 0.U) {
                queue(n.U).instruction.rs2_value := io.broadcast_free_value
                queue(n.U).instruction.rs2_dependence_counter := queue(n.U).instruction.rs2_dependence_counter - 1.U
            }
        }
    }

    io.jump_unit_out := queue(first_valid_entry).instruction
    io.jump_unit_out_valid := first_valid_entry_valid && queue(first_valid_entry).instruction.pe_type === PeType.JumpUnit

    io.lsu_out := queue(first_valid_entry).instruction
    io.lsu_out_valid := first_valid_entry_valid && queue(first_valid_entry).instruction.pe_type === PeType.Lsu

    io.alu_out := queue(first_valid_entry).instruction
    io.alu_out_valid := first_valid_entry_valid && queue(first_valid_entry).instruction.pe_type === PeType.Alu

    io.broadcast_mark_valid := false.B
    io.broadcast_mark_register := 0.U
    io.broadcast_mark_reorder_pointer := 0.U

    when(first_valid_entry_valid) {
        for (n <- 1 to 7) {
            when(n.U > first_valid_entry) {
                queue((n - 1).U) := queue(n.U)

                when(
                  io.broadcast_free_valid && queue(n.U).instruction.rs1 === io.broadcast_free_register && queue(
                    n.U
                  ).instruction.rs1_dependence_counter > 0.U
                ) {
                    queue((n - 1).U).instruction.rs1_dependence_counter := queue(n.U).instruction.rs1_dependence_counter - 1.U
                }

                when(
                  io.broadcast_free_valid && queue(n.U).instruction.rs2 === io.broadcast_free_register && queue(
                    n.U
                  ).instruction.rs2_dependence_counter > 0.U
                ) {
                    queue((n - 1).U).instruction.rs2_dependence_counter := queue(n.U).instruction.rs2_dependence_counter - 1.U
                }
            }
        }

        queue(7.U).valid := false.B

        when(queue(first_valid_entry).instruction.pe_type === PeType.Alu) {
            io.broadcast_mark_valid := true.B
            io.broadcast_mark_register := queue(first_valid_entry).instruction.rd
            io.broadcast_mark_reorder_pointer := queue(first_valid_entry).instruction.reorder_pointer
        }
    }

    io.ready := !full

    when(io.valid && !full) {
        when(io.instruction.write_mode === WriteMode.Register) {
            dependence_size(io.instruction.rd(4, 0)) := dependence_size(io.instruction.rd(4, 0)) + 1.U

            when(io.broadcast_free_valid && io.instruction.rd === io.broadcast_free_register) {
                dependence_size(io.instruction.rd(4, 0)) := dependence_size(io.instruction.rd(4, 0))
            }
        }

        when(first_valid_entry_valid) {
            queue(last_free_entry - 1.U).instruction := io.instruction
            queue(last_free_entry - 1.U).valid := true.B

            queue(last_free_entry - 1.U).instruction.rs1_dependence_counter := dependence_size(io.instruction.rs1)
            queue(last_free_entry - 1.U).instruction.rs2_dependence_counter := dependence_size(io.instruction.rs2)

            when(io.broadcast_free_valid && io.broadcast_free_register === io.instruction.rs1) {
                queue(last_free_entry - 1.U).instruction.rs1_value := io.broadcast_free_value
                queue(last_free_entry - 1.U).instruction.rs1_dependence_counter := dependence_size(io.broadcast_free_register) - 1.U
            }

            when(io.broadcast_free_valid && io.broadcast_free_register === io.instruction.rs2) {
                queue(last_free_entry - 1.U).instruction.rs2_value := io.broadcast_free_value
                queue(last_free_entry - 1.U).instruction.rs2_dependence_counter := dependence_size(io.broadcast_free_register) - 1.U
            }
        }.otherwise {
            queue(last_free_entry).instruction := io.instruction
            queue(last_free_entry).valid := true.B

            queue(last_free_entry).instruction.rs1_dependence_counter := dependence_size(io.instruction.rs1)
            queue(last_free_entry).instruction.rs2_dependence_counter := dependence_size(io.instruction.rs2)

            when(io.broadcast_free_valid && io.broadcast_free_register === io.instruction.rs1) {
                queue(last_free_entry).instruction.rs1_value := io.broadcast_free_value
                queue(last_free_entry).instruction.rs1_dependence_counter := dependence_size(io.broadcast_free_register) - 1.U
            }

            when(io.broadcast_free_valid && io.broadcast_free_register === io.instruction.rs2) {
                queue(last_free_entry).instruction.rs2_value := io.broadcast_free_value
                queue(last_free_entry).instruction.rs2_dependence_counter := dependence_size(io.broadcast_free_register) - 1.U
            }
        }
    }

    when(io.flush) {
        for (n <- 0 to 31) {
            dependence_size(n.U) := 0.U
        }

        for (n <- 0 to 7) {
            queue(n.U) := 0.U.asTypeOf(new QueueEntry)
        }
    }

    // printf("\n\n")

    // // printf(
    // //   "First valid entry: %d First valid entry valid: %b Valid: %b\n",
    // //   first_valid_entry,
    // //   first_valid_entry_valid,
    // //   io.valid
    // // )

    // // printf(
    // //   "Last free entry: %d\n",
    // //   last_free_entry
    // // )

    // // for (n <- 0 to 31) {
    // //     printf(
    // //       "Rs %d %d\n",
    // //       n.U,
    // //       dependence_size(n.U)
    // //     )
    // // }

    // for (n <- 0 to 7) {
    //     when(queue(n.U).valid) {
    //         printf(
    //           "%d -> opcode: %b rp: %d ip: %d rs1: %d %d %d rs2: %d %d %d \n",
    //           n.U,
    //           queue(n.U).instruction.opcode,
    //           queue(n.U).instruction.reorder_pointer,
    //           queue(n.U).instruction.instruction_pointer,
    //           queue(n.U).instruction.rs1,
    //           queue(n.U).instruction.rs1_value,
    //           queue(n.U).instruction.rs1_dependence_counter,
    //           queue(n.U).instruction.rs2,
    //           queue(n.U).instruction.rs2_value,
    //           queue(n.U).instruction.rs2_dependence_counter
    //         )
    //     }.otherwise {
    //         printf("%d -> \n", n.U)
    //     }
    // }

    // // // printf("\n\n")

    // when(io.jump_unit_out_valid) {
    //     printf(
    //       "Dispatching to jump unit! op: %b rp: %d ip: %d\n",
    //       io.jump_unit_out.opcode,
    //       io.jump_unit_out.reorder_pointer,
    //       io.jump_unit_out.instruction_pointer
    //     )
    // }

    // when(io.lsu_out_valid) {
    //     printf(
    //       "Dispatching to lsu! op: %b rp: %d ip: %d\n",
    //       io.lsu_out.opcode,
    //       io.lsu_out.reorder_pointer,
    //       io.lsu_out.instruction_pointer
    //     )
    // }

    // when(io.alu_out_valid) {
    //     printf(
    //       "Dispatching to alu! op: %b rp: %d ip: %d\n",
    //       io.alu_out.opcode,
    //       io.alu_out.reorder_pointer,
    //       io.alu_out.instruction_pointer
    //     )
    // }

    // when(io.broadcast_free_valid) {
    //     printf(
    //       "Register freed! %d %d\n",
    //       io.broadcast_free_register,
    //       io.broadcast_free_value
    //     )
    // }

    // when(io.broadcast_mark_valid) {
    //     printf(
    //       "Register marked! %d %d\n",
    //       io.broadcast_mark_register,
    //       io.broadcast_mark_reorder_pointer
    //     )
    // }
}
