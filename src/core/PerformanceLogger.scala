import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class PerformanceLogger extends Module {
    val io = IO(new Bundle {
        val execute = Input(Bool())
        val flush = Input(Bool())
        val retire = Input(Bool())
    })

    val cycle_counter = RegInit(0.U(32.W))
    val flush_counter = RegInit(0.U(32.W))
    val retire_counter = RegInit(0.U(32.W))

    when(io.execute) {
        cycle_counter := cycle_counter + 1.U

        when(io.flush) {
            flush_counter := flush_counter + 1.U
        }

        when(io.retire) {
            retire_counter := retire_counter + 1.U
        }

        printf("[PERF] Cycles: %d Retires: %d Flushes: %d\n", cycle_counter, retire_counter, flush_counter)
    }
}
