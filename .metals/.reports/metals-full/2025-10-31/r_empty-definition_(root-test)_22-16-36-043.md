error id: file://<WORKSPACE>/src/test/scala/RISCV/RegistersSpec.scala:org/scalatest/matchers/must/Matchers#
file://<WORKSPACE>/src/test/scala/RISCV/RegistersSpec.scala
empty definition using pc, found symbol in pc: org/scalatest/matchers/must/Matchers#
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -chisel3/Matchers#
	 -chisel3/experimental/BundleLiterals.Matchers#
	 -org/scalatest/matchers/must/Matchers#
	 -Matchers#
	 -scala/Predef.Matchers#
offset: 296
uri: file://<WORKSPACE>/src/test/scala/RISCV/RegistersSpec.scala
text:
```scala
// See README.md for license details.

package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class RegistersSpec extends AnyFreeSpec with Mat@@chers with ChiselSim {

  "registers should pass tests" in {
    simulate(new Registers()) { dut =>
        // Attempt writing to register 0 (should remain 0)
        dut.io.enable.poke(true.B)
        dut.io.write_enable.poke(true.B)
        dut.io.select.poke(0.U)
        dut.io.in.poke(42.U)
        dut.clock.step(1)

        // Read from register 0
        dut.io.write_enable.poke(false.B)
        dut.io.enable.poke(true.B)
        dut.io.select.poke(0.U)    
        dut.clock.step(1)

        // Register 0 should always read as 0
        dut.io.out.expect(0.U) 

        // Write to all registers 1 to 31
        for (i <- 1 until 32) {
            // 1. Write
            dut.io.select.poke(i.U(5.W))
            dut.io.in.poke((i * 10).U(32.W))
            dut.io.write_enable.poke(true.B)

            // Step clock once to perform the write
            dut.clock.step(1)

            // 2. Read
            dut.io.write_enable.poke(false.B)  // disable write
            dut.io.select.poke(i.U(5.W))

            // Read combinationally after write
            dut.io.out.expect((i * 10).U(32.W))
      }
    }
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: org/scalatest/matchers/must/Matchers#