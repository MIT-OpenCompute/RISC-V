package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DecoderSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "R-type instruction should decode correctly" in {
    simulate(new Decoder()) { dut =>
      // Example: add x1, x2, x3 -> opcode=0b0110011, func3=0b000, func7=0b0000000
      val instr = "b0000000_00011_00010_000_00001_0110011".U
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.rs1.expect(2.U)
      dut.io.rs2.expect(3.U)
      dut.io.rd.expect(1.U)
      dut.io.operation.expect("b0000000_000_0110011".U)
    }
  }

  "I-type instruction should decode correctly" in {
    simulate(new Decoder()) { dut =>
      // Example: addi x1, x2, 5 -> opcode=0b0010011, func3=0b000, imm=5
      val instr = "b000000000101_00010_000_00001_0010011".U
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.rs1.expect(2.U)
      dut.io.rd.expect(1.U)
      dut.io.operation.expect("b000_0010011".U)
      dut.io.immediate.expect("b00000000000000000000000000101".U)
    }
  }

  "S-type instruction should decode correctly" in {
    simulate(new Decoder()) { dut =>
      // Example: sw x2, 8(x1) -> opcode=0b0100011, func3=0b010, imm=8
      val instr = "b0000000_00010_00001_010_01000_0100011".U
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.rs1.expect(1.U)
      dut.io.rs2.expect(2.U)
      dut.io.operation.expect("b010_0100011".U)
      dut.io.immediate.expect("b000000000000000000001000".U)
    }
  }

  "B-type instruction should decode correctly" in {
    simulate(new Decoder()) { dut =>
      // beq x1, x2, 16 -> opcode=0b1100011, func3=0b000, imm=16
      val instr = "b0000000_00010_00001_000_10000_1100011".U
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.rs1.expect(1.U)
      dut.io.rs2.expect(2.U)
      dut.io.operation.expect("b000_1100011".U)
      dut.io.immediate.expect("b0000000000000000000000010000".U(32.W))
    }
  }

  "U-type instruction should decode correctly" in {
    simulate(new Decoder()) { dut =>
      // Example: LUI x1, 0x12345 -> opcode=0b0110111
      val instr = "b00010010001101000101_00001_0110111".U
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.rd.expect(1.U)
      dut.io.operation.expect("b0110111".U)
      dut.io.immediate.expect("b00010010001101000101000000000000".U)
    }
  }

  "J-type instruction should decode correctly" in {
    simulate(new Decoder()) { dut =>
      // Example: JAL x1, 0x100 -> opcode=0b1101111
      val instr = "b00000000000100000000_00001_1101111".U
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.rd.expect(1.U)
      dut.io.operation.expect("b1101111".U)
      dut.io.immediate.expect("b00000000000100000000000000000000".U)
    }
  }

}
