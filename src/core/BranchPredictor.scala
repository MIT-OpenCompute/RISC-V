package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class JumpCacheEntry extends Bundle {
    val tag = UInt(22.W)
    // val jump = Bool()
    val target = UInt(32.W)
}

class BranchPredictor() extends Module {
    val io = IO(new Bundle {
        val program_pointer = Input(UInt(32.W))
        
        val predicted_program_pointer = Output(UInt(32.W))

        val jump_valid = Input(Bool())
        val jump_instruction_pointer = Input(UInt(32.W))
        val jump_target = Input(UInt(32.W))
    })

    val jump_cache = SyncReadMem(256, new JumpCacheEntry)
    val jump_valid = RegInit(VecInit(Seq.fill(256)(false.B)))
    val entry = Wire(new JumpCacheEntry)
    val jlookup = io.jump_instruction_pointer(9, 2)
    entry.tag := io.jump_instruction_pointer(31, 10)
    entry.target :=  io.jump_target
    when(io.jump_valid) {


        jump_valid(jlookup) := true.B
        // entry.jump := true.B
        jump_cache.write(jlookup, entry)
    }

    val pred_program_pointer = WireDefault(RegNext(io.program_pointer + 4.U))
    // io.predicted_program_pointer := 

    val lookup = io.program_pointer(9, 2)
    val tag = RegNext(io.program_pointer(31, 10))
    val read_entry = jump_cache.read(lookup)
    val is_valid = RegNext(jump_valid(lookup))

    when(is_valid && read_entry.tag === tag) {
        pred_program_pointer := read_entry.target

    }
    io.predicted_program_pointer := pred_program_pointer
}