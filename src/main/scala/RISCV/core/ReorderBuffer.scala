package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/*
The reorder buffer is meant to track the true ordering of instructions so we can make sure that our modifcations to the persistent sate of the processor anr memory
are in order. Otherwise, exceptions would leave the processor in a non deterministic state.

The buffer is a cyclic buffer that tracks a head and tail pointer. We have the size of the buffer hardcoded at 256 for now. Each entry in the buffer corresponds
to an instruction and contains information about how to write to memory or registers. The entry also tracks wether the instruction is complete. The reorder buffer
"retires" completed instructions at the tail in order. When an instruciton is retired it requests a write to the registers or memory.

The reorder buffer can fill up, hence the "full" signal. This would cause the reorder buffer to stall. The reorder buffer also must pause retiring when waiting for the
active memory write to finish.
 */
class BufferEntry extends Bundle {
    val value = UInt(32.W)
    val rd = UInt(32.W)
    val program_pointer = UInt(32.W)
    val mode = WriteMode()
    val complete = Bool()
}

class ReorderBuffer() extends Module {
    val io = IO(new Bundle {
        val buffer_entry = Input(new BufferEntry())
        val valid = Input(Bool())

        val head = Output(UInt(8.W))
        val tail = Output(UInt(8.W))

        val complete_instruction = Input(new InstructionBundle())
        val complete_valid = Input(Bool())

        val write_ready = Input(Bool())
        val write_complete = Input(Bool())
        val write_value = Output(UInt(32.W))
        val write_address = Output(UInt(32.W))
        val write_mode = Output(WriteMode())

        val full = Output(Bool())
    })

    val buffer = RegInit(VecInit(Seq.fill(256)(0.U.asTypeOf(new BufferEntry()))))
    val head = RegInit(0.U(8.W))
    io.head := head
    val tail = RegInit(0.U(8.W))
    io.tail := tail

    val full = RegInit(false.B)
    io.full := full

    val empty = tail === head

    val waiting_on_write = RegInit(false.B)

    io.write_value := 0.U
    io.write_address := 0.U
    io.write_mode := WriteMode.None

    when(io.write_complete) {
        printf("[RB] Write Complete!\n");

        waiting_on_write := false.B
    }

    // printf(
    //   "[RB] Conditions %b %b %b %b\n",
    //   !empty,
    //   !waiting_on_write || io.write_complete,
    //   buffer(tail).complete,
    //   (io.write_ready || buffer(
    //     tail
    //   ).mode =/= WriteMode.Memory)
    // );

    when(
      !empty && (!waiting_on_write || io.write_complete) && buffer(tail).complete && (io.write_ready || buffer(
        tail
      ).mode =/= WriteMode.Memory)
    ) {
        printf("[RB] Retiring! %b %b %b\n", buffer(tail).rd, buffer(tail).value, buffer(tail).mode.asUInt);

        io.write_value := buffer(tail).value
        io.write_address := buffer(tail).rd
        val entry_write_mode = buffer(tail).mode
        io.write_mode := entry_write_mode

        when(entry_write_mode === WriteMode.Memory) {
            waiting_on_write := true.B
        }

        when(!io.valid) {
            full := false.B
        }

        tail := (tail + 1.U) % 256.U
    }

    when(io.complete_valid) {
        printf("[RB] Marking as complete! %d %d\n", io.complete_instruction.reorder_pointer, io.complete_instruction.rd_value);

        buffer(io.complete_instruction.reorder_pointer).complete := true.B
        buffer(io.complete_instruction.reorder_pointer).value := io.complete_instruction.rd_value
    }

    when(!io.full && io.valid) {
        // printf("[RB] Entering!\n");

        buffer(head) := io.buffer_entry

        head := (head + 1.U) % 256.U

        when((head + 1.U) % 256.U === tail) {
            full := true.B
        }
    }

    printf("Head: %d Tail %d\n\n\n", head, tail)
}
