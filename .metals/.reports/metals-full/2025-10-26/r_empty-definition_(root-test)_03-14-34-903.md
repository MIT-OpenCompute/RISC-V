file://<WORKSPACE>/src/test/scala/RISCV/RegistersSpec.scala
empty definition using pc, found symbol in pc: 
semanticdb not found
empty definition using fallback
non-local guesses:
	 -chisel3/chisel3/experimental/BundleLiterals.
	 -chisel3/experimental/BundleLiterals.chisel3.experimental.BundleLiterals.
	 -chisel3/experimental/BundleLiterals.
	 -scala/Predef.chisel3.experimental.BundleLiterals.
offset: 111
uri: file://<WORKSPACE>/src/test/scala/RISCV/RegistersSpec.scala
text:
```scala
// See README.md for license details.

package `RISCV`

import chisel3._
import chisel3.experimental.BundleLite@@rals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class RegistersSpec extends AnyFreeSpec with Matchers with ChiselSim {

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
        dut.io.clock.step(1)

        // Register 0 should always read as 0
        dut.io.out.expect(0.U) 

        // Write to all registers 1 to 31
        for (i <- 1 until 32) {
            // Write value to register i
            dut.io.enable.poke(true.B)
            dut.io.write_enable.poke(true.B)
            dut.io.select.poke(i.U)
            dut.io.in.poke((i * 10).U)
            dut.clock.step(1)

            // Read back the value
            dut.io.write_enable.poke(false.B)
            dut.io.enable.poke(true.B)
            dut.io.select.poke(i.U)
            dut.clock.step(1)

            // Verify the value
            dut.io.out.expect((i * 10).U)
      }
    }
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: 