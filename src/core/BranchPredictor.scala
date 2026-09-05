import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class JumpCacheEntry extends Bundle {
    val tag = UInt(24.W)
    val jump = Bool()
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

    val jump_cache = RegInit(VecInit(Seq.fill(256)(0.U.asTypeOf(new JumpCacheEntry()))))

    when(io.jump_valid) {
        val lookup = io.jump_instruction_pointer(7, 0)
        val tag = io.jump_instruction_pointer(31, 8)

        jump_cache(lookup).jump := true.B
        jump_cache(lookup).tag := tag
        jump_cache(lookup).target := io.jump_target
    }

    io.predicted_program_pointer := io.program_pointer + 4.U

    val lookup = io.program_pointer(7, 0)
    val tag = io.program_pointer(31, 8)

    when(jump_cache(lookup).jump && jump_cache(lookup).tag === tag) {
        io.predicted_program_pointer := jump_cache(lookup).target

        printf("Predicted jump for instruction at %d to %d\n", io.program_pointer, jump_cache(lookup).target)
    }
}