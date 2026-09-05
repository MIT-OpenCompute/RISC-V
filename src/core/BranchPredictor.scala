import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._

class BranchPredictor() extends Module {
    val io = IO(new Bundle {
        val program_pointer = Input(UInt(32.W))
        
        val predicted_program_pointer = Output(UInt(32.W))

        val jump_valid = Input(Bool())
        val jump_instruction_pointer = Input(UInt(32.W))
        val jump_target = Input(UInt(32.W))
    })

    io.predicted_program_pointer := io.program_pointer + 4.U
}