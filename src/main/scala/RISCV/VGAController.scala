package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

// 320x240
class VGAController extends Module {
    val H_VISIBLE = 640
    val H_FRONT = 16
    val H_SYNC = 96
    val H_BACK = 48
    val H_TOTAL = H_VISIBLE + H_FRONT + H_SYNC + H_BACK

    val V_VISIBLE = 480
    val V_FRONT = 10
    val V_SYNC = 2
    val V_BACK = 33
    val V_TOTAL = V_VISIBLE + V_FRONT + V_SYNC + V_BACK

    val io = IO(new Bundle {
        val address = Input(UInt(32.W))
        val write = Input(Bool())
        val write_value = Input(UInt(32.W))

        val hsync = Output(Bool())
        val vsync = Output(Bool())
        val rgb = Output(UInt(12.W))
        val blanking = Output(Bool())
    })

    val memory = SyncReadMem(320 * 240, UInt(8.W))

    when(io.write) {
        memory.write(io.address, io.write_value)
    }

    val v_pos = RegInit(0.U(10.W))
    val h_pos = RegInit(0.U(10.W))

    when(h_pos === (H_TOTAL - 1).U) {
        v_pos := 0.U

        when(v_pos === (V_TOTAL - 1).U) {
            h_pos := 0.U
        }.otherwise {
            v_pos := v_pos + 1.U
        }
    }.otherwise {
        h_pos := h_pos + 1.U
    }

    val hSyncStart = (H_VISIBLE + H_FRONT).U
    val hSyncEnd = (H_VISIBLE + H_FRONT + H_SYNC).U
    val vSyncStart = (V_VISIBLE + V_FRONT).U
    val vSyncEnd = (V_VISIBLE + V_FRONT + V_SYNC).U

    io.hsync := !(h_pos >= hSyncStart && h_pos < hSyncEnd)
    io.vsync := !(v_pos >= vSyncStart && v_pos < vSyncEnd)

    val hActive = v_pos < H_VISIBLE.U
    val vActive = h_pos < V_VISIBLE.U
    val active = hActive && vActive

    io.blanking := !active

    val read_address = WireInit(0.U(32.W))

    when(!active) {
        read_address := 0.U
    }.otherwise {
        read_address := h_pos * 320.U + v_pos
    }

    val color = memory.read(read_address, true.B)
    val pixel := color(7, 5) ## color(5) ## color(4, 2) ## color(2) ## color(1, 0) ## color(0) ## color(0)

    io.rgb := Mux(active, pixel, 0.U)
}

object VGAMain extends App {
    ChiselStage.emitSystemVerilogFile(
      new VGAController(),
      firtoolOpts = Array(
        "-disable-all-randomization",
        "-strip-debug-info",
        "-default-layer-specialization=enable"
      ),
      args = Array("--target-dir", "generated")
    )
}
