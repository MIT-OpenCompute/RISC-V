error id: file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala:regFileA_out
file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala
empty definition using pc, found symbol in pc: regFileA_out
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -chisel3/dut/io/regFileA_out.
	 -chisel3/experimental/BundleLiterals.dut.io.regFileA_out.
	 -dut/io/regFileA_out.
	 -scala/Predef.dut.io.regFileA_out.
offset: 756
uri: file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala
text:
```scala
// See README.md for license details.

package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "main should pass tests" in {
    simulate(new Main()) { dut =>
        // Test LUI instruction: lui x1, 0x12345
        val lui_instruction = "b00000000000100100011010010110111".U(32.W) // opcode for LUI with rd = x1 and imm = 0x12345
        dut.io.instruction.poke(lui_instruction)
        dut.clock.step(1)
        // Check that register x1 in both register files has the correct value

        dut.clock.step(1)

        dut.io.regFil@@eA_out.expect("h12345000".U)
        
       
        dut.clock.step(1)
       
        dut.regFileB.io.out.expect("h12345000".U)
        
        // Test ADDI instruction: addi x2, x1, 0x10
        val addi_instruction = "b00000000000100010000000100010011".U(32.W) // opcode for ADDI with rd = x2, rs1 = x1, imm = 0x10
        dut.io.instruction.poke(addi_instruction)
        dut.clock.step(1)
        // Check that register x2 in regFileA has the correct value (x1 +
        dut.regFileA.io.select.poke(2.U)
        dut.regFileA.io.enable.poke(true.B)
        dut.regFileA.io.write_enable.poke(false.B)
        dut.clock.step(1)
        dut.regFileA.io.out.expect("h12345010".U)
    }
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: regFileA_out