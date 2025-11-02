error id: file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala:instruction
file://<WORKSPACE>/src/test/scala/RISCV/MainSpec.scala
empty definition using pc, found symbol in pc: instruction
found definition using semanticdb; symbol chisel3/simulator/PeekPokeAPI#toTestableUInt().
empty definition using fallback
non-local guesses:

offset: 2248
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
        // BEFORE WRITING
        dut.io.instruction.poke("b00000000000000000000000011111111".U(32.W)) // print contents of register 1 instruction for debugging
        dut.clock.step(1)

        // Test LUI instruction: lui x1, 
        val lui_instruction = "b00000000000000000001000010110111".U(32.W) // opcode for LUI with rd = x1 and imm = b00000000000000000001 (in decimal: 1)

        // Attempt loading LUI instruction
        dut.io.instruction.poke(lui_instruction) // Load the immediate into register x1
        
        dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write

        // AFTER WRITING
        dut.io.instruction.poke("b00000000000000000000000011111111".U(32.W)) // print contents of register 1 instruction for debugging
        dut.clock.step(1)

        //println(dut.io.regFileA_out.peek().toString() + "\n")
        //println(dut.io.regFileB_out.peek().toString() + "\n")

        // Check that register x1 in both register files has the correct value
        //dut.io.regFileA_out.expect("b00000000000000000001000000000000".U(32.W))
        //dut.io.regFileB_out.expect("b00000000000000000001000000000000".U(32.W))

        //dut.clock.step(1) // Step another clock to ensure stable state before next instruction

        // BEFORE WRITING
        dut.io.instruction.poke("b00000000000000000000000010111111".U(32.W)) // print contents of register 2 instruction for debugging
        dut.clock.step(1)


        // Test ADDI instruction: addi x2, x1, b000000000001 (1 in decimal)
        // b00000000000000000001000000000000 + b000000000000000000000000000001 = b00000000000000000001000000000001
        val addi_instruction = "b00000000000100001000000100010011".U(32.W) // opcode for ADDI with rd = x2, rs1 = x1, imm = b000000000001 (1 in decimal)
        dut.io.instruction@@.poke(addi_instruction) // Load the ADDI instruction
        dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write

        dut.io.instruction.poke("b00000000000000000000000010111111".U(32.W)) // print contents of register 2 instruction for debugging

        //println(dut.io.regFileA_out.peek().toString() + "\n")
        //println(dut.io.regFileB_out.peek().toString() + "\n")

        // Check that register x2 in register file A has the correct value (x1 + (-1))
        //dut.io.regFileA_out.expect("b00000000000000000001000000000001".U(32.W)) // x1 + (-1)
        // Check that register x2 in register file B has the correct value (x1 + (-1))
        //dut.io.regFileB_out.expect("b00000000000000000001000000000001".U(32.W)) // x1 + (-1)
    }
  }
}
```


#### Short summary: 

empty definition using pc, found symbol in pc: instruction