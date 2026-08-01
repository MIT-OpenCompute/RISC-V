package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class Core() extends Module {
    val io = IO(new Bundle {
        val execute = Input(Bool())

        val program_memory_requested = Output(Bool())
        val program_memory_address = Output(UInt(32.W))
        val program_memory_value = Input(UInt(32.W))
        val program_memory_ready = Input(Bool())
        val program_memory_valid = Input(Bool())

        val data_memory_read_requested = Output(Bool())
        val data_memory_read_address = Output(UInt(32.W))
        val data_memory_read_value = Input(UInt(32.W))
        val data_memory_read_ready = Input(Bool())
        val data_memory_read_valid = Input(Bool())

        val data_memory_write_requested = Output(Bool())
        val data_memory_write_address = Output(UInt(32.W))
        val data_memory_write_value = Output(UInt(32.W))
        val data_memory_write_ready = Input(Bool())
        val data_memory_write_complete = Input(Bool())
    })

    val program_pointer = RegInit(0.U(32.W))
    val registers = Module(new Registers())
    val fetch_stage = Module(new FetchStage())
    val decode_stage = Module(new DecodeStage())
    val read_stage = Module(new ReadStage())
    val instruction_dispatch_queue = Module(new InstructionDispatchQueue())
    val alu_pe = Module(new Alu)
    val lsu_pe = Module(new Lsu)
    val reorder_buffer = Module(new ReorderBuffer())

    io.program_memory_address := program_pointer
    io.program_memory_requested := fetch_stage.io.memory_read_requested

    registers.io.write_enable := reorder_buffer.io.write_mode === WriteMode.Register
    registers.io.write_address := reorder_buffer.io.write_address
    registers.io.in := reorder_buffer.io.write_value

    registers.io.read_address_a := read_stage.io.read_register_1
    registers.io.read_address_b := read_stage.io.read_register_2

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

    decode_stage.io.next_ready := read_stage.io.ready
    decode_stage.io.instruction := fetch_stage.io.next_instruction
    decode_stage.io.instruction_pointer := fetch_stage.io.next_instruction_pointer
    decode_stage.io.valid := fetch_stage.io.next_valid
    decode_stage.io.flush := false.B

    read_stage.io.instruction := decode_stage.io.next_instruction
    read_stage.io.instruction.reorder_pointer := reorder_buffer.io.head
    read_stage.io.valid := decode_stage.io.next_valid
    read_stage.io.broadcast_free_valid := reorder_buffer.io.write_mode === WriteMode.Register
    read_stage.io.broadcast_free_value := reorder_buffer.io.write_value
    read_stage.io.broadcast_free_register := reorder_buffer.io.write_address

    read_stage.io.read_result_1 := registers.io.out_a
    read_stage.io.read_result_2 := registers.io.out_b
    read_stage.io.next_ready := instruction_dispatch_queue.io.ready

    instruction_dispatch_queue.io.instruction := read_stage.io.next_instruction
    instruction_dispatch_queue.io.valid := read_stage.io.next_valid
    instruction_dispatch_queue.io.broadcast_free_valid := lsu_pe.io.broadcast_free_valid || alu_pe.io.broadcast_free_valid
    instruction_dispatch_queue.io.broadcast_free_value := Mux(
      lsu_pe.io.broadcast_free_valid,
      lsu_pe.io.broadcast_free_value,
      alu_pe.io.broadcast_free_value
    )
    instruction_dispatch_queue.io.broadcast_free_register := Mux(
      lsu_pe.io.broadcast_free_valid,
      lsu_pe.io.broadcast_free_register,
      alu_pe.io.broadcast_free_register
    )
    instruction_dispatch_queue.io.lsu_ready := lsu_pe.io.ready
    instruction_dispatch_queue.io.alu_ready := alu_pe.io.ready
    instruction_dispatch_queue.io.reorder_buffer_tail := reorder_buffer.io.tail

    lsu_pe.io.instruction := instruction_dispatch_queue.io.lsu_out
    lsu_pe.io.valid := instruction_dispatch_queue.io.lsu_out_valid
    lsu_pe.io.next_ready := !reorder_buffer.io.full
    lsu_pe.io.memory_read_value := io.data_memory_read_value
    lsu_pe.io.memory_read_ready := io.data_memory_read_ready
    lsu_pe.io.memory_read_valid := io.data_memory_read_valid

    io.data_memory_read_requested := lsu_pe.io.memory_read_requested
    io.data_memory_read_address := lsu_pe.io.memory_read_address

    alu_pe.io.instruction := instruction_dispatch_queue.io.alu_out
    alu_pe.io.valid := instruction_dispatch_queue.io.alu_out_valid
    alu_pe.io.next_ready := !reorder_buffer.io.full
    alu_pe.io.lsu_broadcast_valid := lsu_pe.io.broadcast_free_valid

    reorder_buffer.io.buffer_entry.value := decode_stage.io.next_instruction.rd_value
    reorder_buffer.io.buffer_entry.rd := decode_stage.io.next_instruction.rd
    reorder_buffer.io.buffer_entry.program_pointer := decode_stage.io.next_instruction.instruction_pointer
    reorder_buffer.io.buffer_entry.mode := decode_stage.io.next_instruction.write_mode
    reorder_buffer.io.buffer_entry.complete := false.B
    reorder_buffer.io.valid := decode_stage.io.next_valid

    reorder_buffer.io.complete_pointer := Mux(lsu_pe.io.out_valid, lsu_pe.io.out.reorder_pointer, alu_pe.io.out.reorder_pointer)
    reorder_buffer.io.complete_valid := lsu_pe.io.out_valid || alu_pe.io.out_valid

    reorder_buffer.io.write_ready := io.data_memory_write_ready
    reorder_buffer.io.write_complete := io.data_memory_write_complete

    io.data_memory_write_value := reorder_buffer.io.write_value
    io.data_memory_write_address := reorder_buffer.io.write_address
    io.data_memory_write_requested := reorder_buffer.io.write_mode === WriteMode.Memory

    when(io.execute) {
        // printf("Program Pointer: %d\n\n", program_pointer);

        // printf("[Fetch] Next Ready: %b\n", fetch_stage.io.next_ready);
        // printf("[Fetch] Execute: %b\n", fetch_stage.io.execute);
        // printf("[Fetch] Program Pointer: %b\n", fetch_stage.io.program_pointer);
        // printf("[Fetch] Flush: %b\n", fetch_stage.io.flush);
        // printf("[Fetch] Memory Read Requested: %b\n", fetch_stage.io.memory_read_requested);
        // printf("[Fetch] Memory Read Ready: %b\n", fetch_stage.io.memory_read_ready);
        // printf("[Fetch] Memory Read Value: %b\n", fetch_stage.io.memory_read_value);
        // printf("[Fetch] Memory Read Valid: %b\n", fetch_stage.io.memory_read_valid);
        // printf("[Fetch] Next Instruction: %b\n", fetch_stage.io.next_instruction);
        // printf("[Fetch] Next Instruction Pointer: %b\n", fetch_stage.io.next_instruction_pointer);
        // printf("[Fetch] Next Valid: %b\n", fetch_stage.io.next_valid);
        // printf("[Fetch] Ready: %b\n\n", fetch_stage.io.ready);

        // printf("[Decode] Next Ready: %b\n", decode_stage.io.next_ready);
        // printf("[Decode] Instruction: %b\n", decode_stage.io.instruction);
        // printf("[Decode] Instruction Pointer: %b\n", decode_stage.io.instruction_pointer);
        // printf("[Decode] Valid: %b\n", decode_stage.io.valid);
        // printf("[Decode] Flush: %b\n", decode_stage.io.flush);
        // printf("[Decode] Next Instruction Opcode: %b\n", decode_stage.io.next_instruction.opcode);
        // printf("[Decode] Next Valid: %b\n", decode_stage.io.next_valid);
        // printf("[Decode] Ready: %b\n\n", decode_stage.io.ready);

        // printf("[Read] Next Ready: %b\n", read_stage.io.next_ready);
        // printf("[Read] Instruction Opcode: %b\n", read_stage.io.instruction.opcode);
        // printf("[Read] Instruction Rs1: %b\n", read_stage.io.instruction.rs1);
        // printf("[Read] Instruction Rs2: %b\n", read_stage.io.instruction.rs2);
        // printf("[Read] Valid: %b\n", read_stage.io.valid);
        // printf("[Read] Broadcast Free Valid: %b\n", read_stage.io.broadcast_free_valid);
        // printf("[Read] Broadcast Free Register: %b\n", read_stage.io.broadcast_free_register);
        // printf("[Read] Broadcast Free Value: %b\n", read_stage.io.broadcast_free_value);
        // printf("[Read] Read Register 1: %b\n", read_stage.io.read_register_1);
        // printf("[Read] Read Result 1: %b\n", read_stage.io.read_result_1);
        // printf("[Read] Read Register 2: %b\n", read_stage.io.read_register_2);
        // printf("[Read] Read Result 2: %b\n", read_stage.io.read_result_2);
        // printf("[Read] Next Instruction Opcode: %b\n", read_stage.io.next_instruction.opcode);
        // printf("[Read] Next Instruction Rs1: %b\n", read_stage.io.next_instruction.rs1);
        // printf("[Read] Next Instruction Rs1 Valid: %b\n", read_stage.io.next_instruction.rs1_dependence_counter);
        // printf("[Read] Next Instruction Rs1 Value: %b\n", read_stage.io.next_instruction.rs1_value);
        // printf("[Read] Next Instruction Rs2: %b\n", read_stage.io.next_instruction.rs2);
        // printf("[Read] Next Instruction Rs2 Valid: %b\n", read_stage.io.next_instruction.rs2_dependence_counter);
        // printf("[Read] Next Instruction Rs2 Value: %b\n", read_stage.io.next_instruction.rs2_value);
        // printf("[Read] Next Valid: %b\n", read_stage.io.next_valid);
        // printf("[Read] Ready: %b\n\n", read_stage.io.ready);

        // printf("[IDQ] Instruction: %b\n", instruction_dispatch_queue.io.instruction.opcode);
        // printf("[IDQ] Valid: %b\n", instruction_dispatch_queue.io.valid);
        // printf("[IDQ] ALU Out Opcode: %b\n", instruction_dispatch_queue.io.alu_out.opcode);
        // printf("[IDQ] ALU Out Valid: %b\n", instruction_dispatch_queue.io.alu_out_valid);
        // printf("[IDQ] ALU Out Ready: %b\n", instruction_dispatch_queue.io.alu_ready);
        // printf("[IDQ] Broadcast Free Valid: %b\n", instruction_dispatch_queue.io.broadcast_free_valid);
        // printf("[IDQ] Broadcast Free Register: %b\n", instruction_dispatch_queue.io.broadcast_free_register);
        // printf("[IDQ] Broadcast Free Value: %b\n", instruction_dispatch_queue.io.broadcast_free_value);
        // printf("[IDQ] Ready: %b\n\n", instruction_dispatch_queue.io.ready);

        // printf("[ALU] Next Ready: %b\n", alu_pe.io.next_ready);
        // printf("[ALU] Instruction Opcode: %b\n", alu_pe.io.instruction.opcode);
        // printf("[ALU] Instruction Rs1: %b\n", alu_pe.io.instruction.rs1);
        // printf("[ALU] Instruction Rs1 Valid: %b\n", alu_pe.io.instruction.rs1_dependence_counter);
        // printf("[ALU] Instruction Rs1 Value: %b\n", alu_pe.io.instruction.rs1_value);
        // printf("[ALU] Instruction Rs2: %b\n", alu_pe.io.instruction.rs2);
        // printf("[ALU] Instruction Rs2 Valid: %b\n", alu_pe.io.instruction.rs2_dependence_counter);
        // printf("[ALU] Instruction Rs2 Value: %b\n", alu_pe.io.instruction.rs2_value);
        // printf("[ALU] Valid: %b\n", alu_pe.io.valid);
        // printf("[ALU] Next Instruction Opcode: %b\n", alu_pe.io.out.opcode);
        // printf("[ALU] Next Instruction Immediate: %b\n", alu_pe.io.out.immediate);
        // printf("[ALU] Next Instruction Rs1: %b\n", alu_pe.io.out.rs1);
        // printf("[ALU] Next Instruction Rs1 Valid: %b\n", alu_pe.io.out.rs1_dependence_counter);
        // printf("[ALU] Next Instruction Rs1 Value: %b\n", alu_pe.io.out.rs1_value);
        // printf("[ALU] Next Instruction Rs2: %b\n", alu_pe.io.out.rs2);
        // printf("[ALU] Next Instruction Rs2 Valid: %b\n", alu_pe.io.out.rs2_dependence_counter);
        // printf("[ALU] Next Instruction Rs2 Value: %b\n", alu_pe.io.out.rs2_value);
        // printf("[ALU] Next Instruction Rd: %b\n", alu_pe.io.out.rd);
        // printf("[ALU] Next Instruction Rd Value: %b\n", alu_pe.io.out.rd_value);
        // printf("[ALU] Next Instruction Reorder Pointer: %b\n", alu_pe.io.out.reorder_pointer);
        // printf("[ALU] Next Valid: %b\n", alu_pe.io.out_valid);
        // printf("[ALU] Ready: %b\n\n", alu_pe.io.ready);

        // printf("[RB] Full: %b\n", reorder_buffer.io.full);
        // printf("[RB] Buffer Entry Program Pointer: %b\n", reorder_buffer.io.buffer_entry.program_pointer);
        // printf("[RB] Valid: %b\n", reorder_buffer.io.valid);
        // printf("[RB] Complete Pointer: %b\n", reorder_buffer.io.complete_pointer);
        // printf("[RB] Complete Valid: %b\n", reorder_buffer.io.complete_valid);
        // printf("[RB] Write Value: %b\n", reorder_buffer.io.write_value);
        // printf("[RB] Write Address: %b\n", reorder_buffer.io.write_address);
        // printf("[RB] Write Mode: %b\n", reorder_buffer.io.write_mode.asUInt);
        // printf("[RB] Write Complete: %b\n\n", reorder_buffer.io.write_complete);

        // printf("\n\n\n\n\n\n");

        printf("\n\n");

        printf("[Decode] Next Valid: %b\n", decode_stage.io.next_valid);
        printf("[Decode] Next Pe Type: %b\n", decode_stage.io.next_instruction.pe_type.asUInt);

        printf("[Memory] Data Memory Read Requested: %b\n", io.data_memory_read_requested);
        printf("[Memory] Data Memory Read Address: %b\n", io.data_memory_read_address);
        printf("[Memory] Data Memory Read Value: %b\n", io.data_memory_read_value);
        printf("[Memory] Data Memory Read Ready: %b\n", io.data_memory_read_ready);
        printf("[Memory] Data Memory Read Valid: %b\n", io.data_memory_read_valid);

        printf("[LSU] Broadcast Free Valid: %b\n", lsu_pe.io.broadcast_free_valid);
        printf("[ALU] Broadcast Free Valid: %b\n", alu_pe.io.broadcast_free_valid);

        printf("[IDQ] ALU Out Valid: %b\n", instruction_dispatch_queue.io.alu_out_valid);
        printf("[IDQ] LSU Out Valid: %b\n", instruction_dispatch_queue.io.lsu_out_valid);
        printf("[IDQ] Broadcast Free Valid: %b\n", instruction_dispatch_queue.io.broadcast_free_valid);
        printf("[IDQ] Broadcast Free Register: %b\n", instruction_dispatch_queue.io.broadcast_free_register);
        printf("[IDQ] Broadcast Free Value: %b\n", instruction_dispatch_queue.io.broadcast_free_value);

        printf("\n\n");
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
