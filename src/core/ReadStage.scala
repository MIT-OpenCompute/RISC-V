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

    // val register_alias = RegInit(VecInit(Seq.fill(32.toInt)(0.U(8.W))))
    val register_live = RegInit(VecInit(Seq.fill(32.toInt)(false.B)))
    val register_value = RegInit(VecInit(Seq.fill(32.toInt)(0.U(32.W))))

    when(io.broadcast_free_valid) {
        register_value(io.broadcast_free_register) := io.broadcast_free_value
        register_live(io.broadcast_free_register) := true.B
    }

    io.next_instruction := held_instruction
    io.next_valid := held_valid

    when(io.broadcast_free_valid && held_instruction.rs1 === io.broadcast_free_register) {
        held_instruction.rs1_value := io.broadcast_free_value
    }

    when(io.broadcast_free_valid && held_instruction.rs2 === io.broadcast_free_register) {
        held_instruction.rs2_value := io.broadcast_free_value
    }

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

    // printf("[Live Register 14]: %b %d\n", register_live(14.U), register_value(14.U))
    // printf("Instruction ip: %d rs1: %d\n", io.instruction.instruction_pointer, io.instruction.rs1_value)
    // printf("Held Instruction ip: %d rs1: %d\n", held_instruction.instruction_pointer, held_instruction.rs1_value)
}
