package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "main should pass tests" in {
    simulate(new Main()) { dut =>
        // // BEFORE WRITING
        // dut.io.instruction.poke("b00000000000000000000000011111111".U(32.W)) // print contents of register 1 instruction for debugging
        // dut.io.RegFileA_out.expect(0.U)
        // dut.io.RegFileB_out.expect(0.U)

        // // Test LUI instruction: lui x1, 
        // val lui_instruction = "b00000000000000000001000010110111".U(32.W) // opcode for LUI with rd = x1 and imm = b00000000000000000001 (in decimal: 1)

        // // Attempt loading LUI instruction
        // dut.io.instruction.poke(lui_instruction) // Load the immediate into register x1
        
        // dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write

        // // AFTER WRITING
        // dut.io.instruction.poke("b00000000000000000000000011111111".U(32.W)) // print contents of register 1 instruction for debugging
        // dut.io.RegFileA_out.expect("b00000000000000000001000000000000".U(32.W)) // Expecting imm << 12
        // dut.io.RegFileB_out.expect("b00000000000000000001000000000000".U(32.W))

        // // BEFORE WRITING
        // dut.io.instruction.poke("b000000000000000000000000101111111".U(32.W)) // print contents of register 2 instruction for debugging
        // dut.io.RegFileA_out.expect(0.U)
        // dut.io.RegFileB_out.expect(0.U)

        // // Test ADDI instruction: addi x2, x1, b000000000001 (1 in decimal)
        // // b00000000000000000001000000000000 + b000000000000000000000000000001 = b00000000000000000001000000000001
        // val addi_instruction = "b00000000000100001000000100010011".U(32.W) // opcode for ADDI with rd = x2, rs1 = x1, imm = b000000000001 (1 in decimal)
        // dut.io.instruction.poke(addi_instruction) // Load the ADDI instruction
        // dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write

        // // AFTER WRITING
        // dut.io.instruction.poke("b000000000000000000000000101111111".U(32.W)) // print contents of register 2 instruction for debugging
        // dut.io.RegFileA_out.expect("b00000000000000000001000000000001".U(32.W)) // Expecting result of addition
        // dut.io.RegFileB_out.expect("b00000000000000000001000000000001".U(32.W))

        // // prepare for SLTI test
        // val lui_instruction_2 = "b00000000000000000000000010110111".U(32.W) // opcode for LUI with rd = x1 and imm = b00000000000000000000 (in decimal: 0)
        // dut.io.instruction.poke(lui_instruction_2) // Load the immediate into register x1
        // dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write
        // val addi_instruction_2 = "b00000000001100001000001010010011".U(32.W) // opcode for ADDI with rd = x5, rs1 = x1, imm = b000000000011 (3 in decimal)
        // dut.io.instruction.poke(addi_instruction) // Load the ADDI instruction
        // dut.clock.step(1) // Step clock to process instruction. Next clock cycle is when the register updates with the write
        // // Now x1 should be 3

        // // verify x1 is 3
        // dut.io.instruction.poke("b000000000000000000000001011111111".U(32.W)) // print contents of register 5 instruction for debugging
        // dut.io.RegFileA_out.expect(3.U(32.W)) // Expecting 3
        // dut.io.RegFileB_out.expect(3.U(32.W)) 

        // // TEST SLTI instruction: slti x3, x1, 5
        // // x1 = b00000000000000000000000000000011 (3 in decimal)
        // // Since 1 < 3 is true, x3 should be set to 1
        // val slti_instruction = "b00000000010100001010000110010011".U(32.W) // opcode for SLTI with rd = x3, rs1 = x1, imm = 5
        // dut.io.instruction.poke(slti_instruction) // Load the SLTI instruction
        // dut.clock.step(1) // Step clock to process instruction

        // // AFTER WRITING
        // dut.io.instruction.poke("b000000000000000000000000111111111".U(32.W)) // print contents of register 3 instruction for debugging
        // dut.io.RegFileA_out.expect(1.U) // Expecting 1 since 1 < 3 is true
        // dut.io.RegFileB_out.expect(1.U)

        // // x1 = b00000000000000000000000000000001 (1 in decimal)
        // // Since 1 < 3 is true, x3 should be set to 1
        // val slti_instruction_2 = "b00000000000100001010001000010011".U(32.W) // opcode for SLTI with rd = x4, rs1 = x1, imm = 1
        // dut.io.instruction.poke(slti_instruction_2) // Load the SLTI instruction
        // dut.clock.step(1) // Step clock to process instruction

        // // AFTER WRITING
        // dut.io.instruction.poke("b000000000000000000000001001111111".U(32.W)) // print contents of register 4 instruction for debugging
        // dut.io.RegFileA_out.expect(1.U) // Expecting 1 since 1 < 3 is true
        // dut.io.RegFileB_out.expect(1.U)
    }
  }
}