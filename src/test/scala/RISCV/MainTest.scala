package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainTest extends AnyFreeSpec with Matchers with ChiselSim {
    "Main" in {
        simulate(new Main()) { dut =>
            dut.io.execute.poke(false.B)
            dut.io.flash.poke(true.B)
            dut.io.flash_address.poke(0.U)
            dut.io.flash_value.poke(0x00608093L.U)

            dut.clock.step(1)

            dut.io.flash.poke(true.B)
            dut.io.flash_address.poke(1.U)
            dut.io.flash_value.poke(0xffdff06fL.U)

            dut.clock.step(1)

            dut.io.flash.poke(false.B)
            dut.io.execute.poke(true.B)

            dut.clock.step(20)
        }
    }
}
