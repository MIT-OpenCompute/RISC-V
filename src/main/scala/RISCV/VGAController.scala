package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

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
        val hPos = Output(UInt(10.W))
        val vPos = Output(UInt(10.W))
    })

    val memory = SyncReadMem(1024, UInt(32.W))

    val hCount = RegInit(0.U(10.W))
    val vCount = RegInit(0.U(10.W))
    
    val pixel = WireInit(0.U(12.W))
    pixel := memory.read(0.U, true.B)
    
    when(io.write) {
        memory.write(io.address, io.write_value)
    }

    when(hCount === (H_TOTAL - 1).U) {
        hCount := 0.U
        
        when(vCount === (V_TOTAL - 1).U) {
            vCount := 0.U
        }.otherwise {
            vCount := vCount + 1.U
        }
    }.otherwise {
        hCount := hCount + 1.U
    }

    val hSyncStart = (H_VISIBLE + H_FRONT).U
    val hSyncEnd = (H_VISIBLE + H_FRONT + H_SYNC).U
    val vSyncStart = (V_VISIBLE + V_FRONT).U
    val vSyncEnd = (V_VISIBLE + V_FRONT + V_SYNC).U

    io.hsync := !(hCount >= hSyncStart && hCount < hSyncEnd)
    io.vsync := !(vCount >= vSyncStart && vCount < vSyncEnd)

    val hActive = hCount < H_VISIBLE.U
    val vActive = vCount < V_VISIBLE.U
    val active = hActive && vActive

    io.blanking := !active
    io.hPos := hCount
    io.vPos := vCount

    io.rgb := Mux(active, pixel, 0.U)
    // io.rgb := Mux(active, 0b111111111111.U, 0.U)
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
