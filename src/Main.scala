package RISCV

import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._
import chisel3.util._


class Main(lineWidth: Int = 512)  extends Module {
    val io = IO(new Bundle {
        val execute = Input(Bool())

        val flash = Input(Bool())
        val flash_address = Input(UInt(32.W))
        val flash_value = Input(UInt(32.W))

        val vga_clk = Input(Clock());
        val hsync = Output(Bool())
        val vsync = Output(Bool())
        val rgb = Output(UInt(12.W))
        val blanking = Output(Bool())

        val program_pointer = Output(UInt(32.W))

        val btns = Input(UInt(4.W))
    })
    val memory = Module(new MemoryWrapper(lineWidth))

    val memory2 = Module(new Memory())
    val vga_controller = Module(new VGAController())
    val core = Module(new Core())

    memory2.io.btns := io.btns

    val memory_requested_1 = RegInit(false.B)
    when(memory_requested_1) {
        memory_requested_1 := false.B
    }

    memory.io.icache_req.address := core.io.program_memory_address
    memory.io.icache_req.write_data := 0.U
    memory.io.icache_req.op := MemOp.LW
    memory.io.icache_req.read := true.B
    memory.io.icache_req.write := false.B
    memory.io.icache_start := core.io.program_memory_requested
    core.io.program_memory_ready := memory.io.icache_ready
    core.io.program_memory_valid := memory.io.icache_valid
    core.io.program_memory_value := memory.io.icache_data

    when(core.io.program_memory_requested) {
        memory_requested_1 := true.B
    }

    val memory_read_requested_2 = RegInit(false.B)
    when(memory_read_requested_2) {
        memory_read_requested_2 := false.B
    }

    val memory_write_requested_2 = RegInit(false.B)
    when(memory_write_requested_2) {
        memory_write_requested_2 := false.B
    }

    memory2.io.address_2 := Mux(
      core.io.data_memory_read_requested,
      core.io.data_memory_read_address / 4.U,
      core.io.data_memory_write_address / 4.U
    )
    memory2.io.read_2 := core.io.data_memory_read_requested
    memory2.io.write_2 := core.io.data_memory_write_requested
    memory2.io.write_value_2 := core.io.data_memory_write_value
    core.io.data_memory_read_value := memory2.io.read_value_2
    core.io.data_memory_read_ready := true.B &&
    core.io.data_memory_read_valid := memory_read_requested_2
    core.io.data_memory_write_ready := true.B
    core.io.data_memory_write_complete := memory_write_requested_2

    when(core.io.data_memory_read_requested) {
        memory_read_requested_2 := true.B
    }

    when(core.io.data_memory_write_requested) {
        memory_write_requested_2 := true.B
    }

    vga_controller.io.address := memory2.io.address_vga
    vga_controller.io.write := memory2.io.write_vga
    vga_controller.io.write_value := memory2.io.write_value_vga
    vga_controller.io.read_clk := io.vga_clk
    io.hsync := vga_controller.io.hsync
    io.vsync := vga_controller.io.vsync
    io.rgb := vga_controller.io.rgb
    io.blanking := vga_controller.io.blanking

    core.io.execute := io.execute

    when(!io.execute) {
        printf("Loading...\n");

        when(io.flash) {
            memory2.io.read_1 := false.B
            memory2.io.write_1 := true.B
            memory2.io.address_1 := io.flash_address
            memory2.io.write_value_1 := io.flash_value
        }
    }

    io.program_pointer := core.io.program_pointer

    // printf("[Main] dmemory read requested: %b\n", core.io.data_memory_read_requested)
    // printf("[Main] dmemory read valid: %b\n", memory_read_requested_2)
}

object Main extends App {
    ChiselStage.emitSystemVerilogFile(
      new Main(),
      firtoolOpts = Array(
        "-disable-all-randomization",
        "-strip-debug-info",
        "-default-layer-specialization=enable"
      ),
      args = Array("--target-dir", "generated")
    )
}
