package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class Core() extends Module {
    val io = IO(new Bundle {
        val execute = Input(Bool())

        val program_memory_requested = Output(Bool())
        val program_memory_adress = Output(UInt(32.W))
        val program_memory_value = Input(UInt(32.W))
        val program_memory_ready = Input(Bool())
        val program_memory_valid = Input(Bool())
    })

    val program_pointer = RegInit(0.U(32.W))
    val registers = Module(new Registers())
    val fetch_stage = Module(new FetchStage())
    val decode_stage = Module(new DecodeStage())
    val register_scoreboard = Module(new RegisterScoreboard())
    val instruction_dispatch_queue = Module(new InstructionDispatchQueue())
    val alu_pe = Module(new Alu)
    val reorder_buffer = Module(new ReorderBuffer())

    io.program_memory_adress := program_pointer
    io.program_memory_requested := fetch_stage.io.memory_read_requested

    registers.io.write_enable := false.B
    registers.io.write_address := 0.U(5.W)
    registers.io.in := 0.U(32.W)
    registers.io.read_address_a := 0.U(5.W)
    registers.io.read_address_b := 0.U(5.W)

    fetch_stage.io.next_ready := decode_stage.io.ready
    fetch_stage.io.execute := io.execute
    fetch_stage.io.program_pointer := program_pointer
    fetch_stage.io.flush := false.B
    fetch_stage.io.memory_read_ready := io.program_memory_ready
    fetch_stage.io.memory_read_value := io.program_memory_value
    fetch_stage.io.memory_read_valid := io.program_memory_valid

    when(fetch_stage.io.ready) {
        program_pointer := program_pointer + 4.U
    }

    decode_stage.io.next_ready := register_scoreboard.io.ready
    decode_stage.io.instruction := fetch_stage.io.next_instruction
    decode_stage.io.instruction_pointer := fetch_stage.io.next_instruction_pointer
    decode_stage.io.valid := fetch_stage.io.next_valid
    decode_stage.io.flush := false.B

    register_scoreboard.io.instruction := decode_stage.io.next_instruction
    register_scoreboard.io.valid := decode_stage.io.next_valid
    register_scoreboard.io.broadcast_free_value := 0.U
    register_scoreboard.io.broadcast_free_valid := false.B
    register_scoreboard.io.broadcast_free_register := 0.U

    register_scoreboard.io.read_result_1 := registers.io.out_a
    register_scoreboard.io.read_result_2 := registers.io.out_b
    registers.io.read_address_a := register_scoreboard.io.read_register_1
    registers.io.read_address_b := register_scoreboard.io.read_register_2

    instruction_dispatch_queue.io.instruction := register_scoreboard.io.next_instruction
    instruction_dispatch_queue.io.valid := register_scoreboard.io.next_valid
    instruction_dispatch_queue.io.broadcast_free_valid := false.B
    instruction_dispatch_queue.io.broadcast_free_register := 0.U
    instruction_dispatch_queue.io.broadcast_free_value := 0.U

    register_scoreboard.io.next_ready := instruction_dispatch_queue.io.ready
    register_scoreboard.io.broadcast_mark_valid := instruction_dispatch_queue.io.broadcast_mark_valid
    register_scoreboard.io.broadcast_mark_register := instruction_dispatch_queue.io.broadcast_mark_register

    alu_pe.io.instruction := instruction_dispatch_queue.io.alu_out
    alu_pe.io.valid := instruction_dispatch_queue.io.alu_out_valid

    instruction_dispatch_queue.io.alu_ready := alu_pe.io.ready

    reorder_buffer.io.buffer_entry.value := decode_stage.io.next_instruction.rd_value
    reorder_buffer.io.buffer_entry.rd := decode_stage.io.next_instruction.rd
    reorder_buffer.io.buffer_entry.program_pointer := decode_stage.io.next_instruction.instruction_pointer
    reorder_buffer.io.buffer_entry.mode := decode_stage.io.next_instruction.write_mode
    reorder_buffer.io.buffer_entry.complete := false.B
    reorder_buffer.io.valid := decode_stage.io.next_valid
    reorder_buffer.io.write_complete := true.B

    reorder_buffer.io.complete_pointer := alu_pe.io.out.reorder_pointer
    reorder_buffer.io.complete_valid := alu_pe.io.out_valid

    alu_pe.io.next_ready := true.B

    when(io.execute) {
        printf("Program Pointer: %d\n\n", program_pointer);

        printf("[Fetch] Next Ready: %b\n", fetch_stage.io.next_ready);
        printf("[Fetch] Execute: %b\n", fetch_stage.io.execute);
        printf("[Fetch] Program Pointer: %b\n", fetch_stage.io.program_pointer);
        printf("[Fetch] Flush: %b\n", fetch_stage.io.flush);
        printf("[Fetch] Memory Read Requested: %b\n", fetch_stage.io.memory_read_requested);
        printf("[Fetch] Memory Read Ready: %b\n", fetch_stage.io.memory_read_ready);
        printf("[Fetch] Memory Read Value: %b\n", fetch_stage.io.memory_read_value);
        printf("[Fetch] Memory Read Valid: %b\n", fetch_stage.io.memory_read_valid);
        printf("[Fetch] Next Instruction: %b\n", fetch_stage.io.next_instruction);
        printf("[Fetch] Next Instruction Pointer: %b\n", fetch_stage.io.next_instruction_pointer);
        printf("[Fetch] Next Valid: %b\n", fetch_stage.io.next_valid);
        printf("[Fetch] Ready: %b\n\n", fetch_stage.io.ready);

        printf("[Decode] Next Ready: %b\n", decode_stage.io.next_ready);
        printf("[Decode] Instruction: %b\n", decode_stage.io.instruction);
        printf("[Decode] Instruction Pointer: %b\n", decode_stage.io.instruction_pointer);
        printf("[Decode] Valid: %b\n", decode_stage.io.valid);
        printf("[Decode] Flush: %b\n", decode_stage.io.flush);
        printf("[Decode] Next Instruction Opcode: %b\n", decode_stage.io.next_instruction.opcode);
        printf("[Decode] Next Valid: %b\n", decode_stage.io.next_valid);
        printf("[Decode] Ready: %b\n\n", decode_stage.io.ready);

        printf("[RSB] Next Ready: %b\n", register_scoreboard.io.next_ready);
        printf("[RSB] Instruction Opcode: %b\n", register_scoreboard.io.instruction.opcode);
        printf("[RSB] Instruction Rs1: %b\n", register_scoreboard.io.instruction.rs1);
        printf("[RSB] Instruction Rs2: %b\n", register_scoreboard.io.instruction.rs2);
        printf("[RSB] Valid: %b\n", register_scoreboard.io.valid);
        printf("[RSB] Broadcast Free Valid: %b\n", register_scoreboard.io.broadcast_free_valid);
        printf("[RSB] Broadcast Free Register: %b\n", register_scoreboard.io.broadcast_free_register);
        printf("[RSB] Broadcast Free Value: %b\n", register_scoreboard.io.broadcast_free_value);
        printf("[RSB] Broadcast Mark Valid: %b\n", register_scoreboard.io.broadcast_mark_valid);
        printf("[RSB] Broadcast Mark Register: %b\n", register_scoreboard.io.broadcast_mark_register);
        printf("[RSB] Read Register 1: %b\n", register_scoreboard.io.read_register_1);
        printf("[RSB] Read Result 1: %b\n", register_scoreboard.io.read_result_1);
        printf("[RSB] Read Register 2: %b\n", register_scoreboard.io.read_register_2);
        printf("[RSB] Read Result 2: %b\n", register_scoreboard.io.read_result_2);
        printf("[RSB] Next Instruction Opcode: %b\n", register_scoreboard.io.next_instruction.opcode);
        printf("[RSB] Next Instruction Rs1: %b\n", register_scoreboard.io.next_instruction.rs1);
        printf("[RSB] Next Instruction Rs1 Valid: %b\n", register_scoreboard.io.next_instruction.rs1_valid);
        printf("[RSB] Next Instruction Rs1 Value: %b\n", register_scoreboard.io.next_instruction.rs1_value);
        printf("[RSB] Next Instruction Rs2: %b\n", register_scoreboard.io.next_instruction.rs2);
        printf("[RSB] Next Instruction Rs2 Valid: %b\n", register_scoreboard.io.next_instruction.rs2_valid);
        printf("[RSB] Next Instruction Rs2 Value: %b\n", register_scoreboard.io.next_instruction.rs2_value);
        printf("[RSB] Next Valid: %b\n", register_scoreboard.io.next_valid);
        printf("[RSB] Ready: %b\n\n", register_scoreboard.io.ready);

        printf("[IDQ] Instruction: %b\n", instruction_dispatch_queue.io.instruction.opcode);
        printf("[IDQ] Valid: %b\n", instruction_dispatch_queue.io.valid);
        printf("[IDQ] ALU Out Opcode: %b\n", instruction_dispatch_queue.io.alu_out.opcode);
        printf("[IDQ] ALU Out Valid: %b\n", instruction_dispatch_queue.io.alu_out_valid);
        printf("[IDQ] ALU Out Ready: %b\n", instruction_dispatch_queue.io.alu_ready);
        printf("[IDQ] Broadcast Free Valid: %b\n", instruction_dispatch_queue.io.broadcast_free_valid);
        printf("[IDQ] Broadcast Free Register: %b\n", instruction_dispatch_queue.io.broadcast_free_register);
        printf("[IDQ] Broadcast Free Value: %b\n", instruction_dispatch_queue.io.broadcast_free_value);
        printf("[IDQ] Broadcast Mark Valid: %b\n", instruction_dispatch_queue.io.broadcast_mark_valid);
        printf("[IDQ] Broadcast Mark Register: %b\n", instruction_dispatch_queue.io.broadcast_mark_register);
        printf("[IDQ] Ready: %b\n\n", instruction_dispatch_queue.io.ready);

        printf("[ALU] Next Ready: %b\n", alu_pe.io.next_ready);
        printf("[ALU] Instruction Opcode: %b\n", alu_pe.io.instruction.opcode);
        printf("[ALU] Instruction Rs1: %b\n", alu_pe.io.instruction.rs1);
        printf("[ALU] Instruction Rs1 Valid: %b\n", alu_pe.io.instruction.rs1_valid);
        printf("[ALU] Instruction Rs1 Value: %b\n", alu_pe.io.instruction.rs1_value);
        printf("[ALU] Instruction Rs2: %b\n", alu_pe.io.instruction.rs2);
        printf("[ALU] Instruction Rs2 Valid: %b\n", alu_pe.io.instruction.rs2_valid);
        printf("[ALU] Instruction Rs2 Value: %b\n", alu_pe.io.instruction.rs2_value);
        printf("[ALU] Valid: %b\n", alu_pe.io.valid);
        printf("[ALU] Next Instruction Opcode: %b\n", alu_pe.io.out.opcode);
        printf("[ALU] Next Instruction Immediate: %b\n", alu_pe.io.out.immediate);
        printf("[ALU] Next Instruction Rs1: %b\n", alu_pe.io.out.rs1);
        printf("[ALU] Next Instruction Rs1 Valid: %b\n", alu_pe.io.out.rs1_valid);
        printf("[ALU] Next Instruction Rs1 Value: %b\n", alu_pe.io.out.rs1_value);
        printf("[ALU] Next Instruction Rs2: %b\n", alu_pe.io.out.rs2);
        printf("[ALU] Next Instruction Rs2 Valid: %b\n", alu_pe.io.out.rs2_valid);
        printf("[ALU] Next Instruction Rs2 Value: %b\n", alu_pe.io.out.rs2_value);
        printf("[ALU] Next Instruction Rd: %b\n", alu_pe.io.out.rd);
        printf("[ALU] Next Instruction Rd Value: %b\n", alu_pe.io.out.rd_value);
        printf("[ALU] Next Valid: %b\n", alu_pe.io.out_valid);
        printf("[ALU] Ready: %b\n\n", alu_pe.io.ready);

        printf("\n\n\n");
    }
}

object Core extends App {
    ChiselStage.emitSystemVerilogFile(
      new Core(),
      firtoolOpts = Array(
        "-disable-all-randomization",
        "-strip-debug-info",
        "-default-layer-specialization=enable"
      ),
      args = Array("--target-dir", "generated")
    )
}
