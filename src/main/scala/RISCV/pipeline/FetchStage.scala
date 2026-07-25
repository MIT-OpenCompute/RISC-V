package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class FetchStage() extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val execute = Input(Bool())
        val program_pointer = Input(UInt(32.W))
        val flush = Input(Bool())

        val memory_read_requested = Output(Bool())
        val memory_read_ready = Input(Bool())
        val memory_read_value = Input(UInt(32.W))
        val memory_read_valid = Input(Bool())

        val next_instruction = Output(UInt(32.W))
        val next_instruction_pointer = Output(UInt(32.W))
        val next_valid = Output(Bool())

        val ready = Output(Bool())
    })

    val memory_request_inflight = RegInit(false.B)
    val requested_program_pointer = RegInit(0.U(32.W))

    val next_instruction = RegInit(0.U(32.W))
    val next_instruction_pointer = RegInit(0.U(32.W))
    val next_valid = RegInit(false.B)

    io.next_instruction := next_instruction
    io.next_instruction_pointer := next_instruction_pointer
    io.next_valid := next_valid

    when(io.memory_read_valid) {
        io.next_instruction := io.memory_read_value
        io.next_instruction_pointer := requested_program_pointer
        io.next_valid := true.B

        next_instruction := io.memory_read_value
        next_instruction_pointer := requested_program_pointer
        next_valid := true.B

        memory_request_inflight := false.B
    }

    val request_memory = io.next_ready && io.execute && io.memory_read_ready && (!memory_request_inflight || io.memory_read_valid)

    io.memory_read_requested := request_memory
    when(request_memory) {
        requested_program_pointer := io.program_pointer
        memory_request_inflight := true.B
    }

    when(io.next_ready) {
        next_valid := false.B
    }

    io.ready := request_memory
}
