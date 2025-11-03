package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class RegistersSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "registers should pass tests" in {
    simulate(new Registers()) { dut =>
        // Attempt writing to register 0 (should remain 0)
        dut.io.write_enable.poke(true.B)
        dut.io.write_addr.poke(0.U)
        dut.io.in.poke(42.U)
        dut.clock.step(1)

        // Read from register 0
        dut.io.write_enable.poke(false.B)
        dut.io.read_addr.poke(0.U)    
        dut.clock.step(1)

        // Register 0 should always read as 0
        dut.io.out.expect(0.U) 

        // Write to all registers 1 to 31
        for (i <- 1 until 32) {
            // 1. Write
            dut.io.write_addr.poke(i.U(5.W))
            dut.io.in.poke((i * 10).U(32.W))
            dut.io.write_enable.poke(true.B)

            // Step clock once to perform the write
            dut.clock.step(1)

            // 2. Read
            dut.io.write_enable.poke(false.B)  // disable write
            dut.io.read_addr.poke(i.U(5.W))

            // Read combinationally after write
            dut.io.out.expect((i * 10).U(32.W))
      }
    }
  }
}