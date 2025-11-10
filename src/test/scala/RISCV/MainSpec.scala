package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {
  "Main should execute LUI correctly" in {
    simulate(new Main()) { dut =>
      // LUI x1, 0x12345
      val instr = "b00010010001101000101_00001_0110111".U(32.W) // opcode=0110111
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      // Expect register x1 to hold immediate shifted left 12 bits
      dut.io.read_addr_C.poke(1.U)
      dut.io.out_C.expect("h12345000".U)
    }
  }
  
  "Main should execute AUIPC correctly" in {
    simulate(new Main()) { dut =>
      // AUIPC x2, 0x1
      val instr = "b00000000000000000001_00010_0010111".U(32.W) // opcode=0010111
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      // PC is 0 in this design, so x2 = pc + imm << 12
      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b00000000000000000001000000000000".U)
    }
  }
  
  "Main should execute ADDI correctly" in {
    simulate(new Main()) { dut =>
      // First, write x1 = 5
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(5.U)
      dut.clock.step(1)

      // ADDI x2, x1, 7
      val instr = "b000000000111_00001_000_00010_0010011".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      // x2 should be 5 + 7 = 12
      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect(12.U)
    }
  }
  
  "Main should execute SLTI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 5
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(5.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SLTI x2, x1, 7 -> 5 < 7 ? 1 : 0
      val instr = "b00000000111_00001_010_00010_0010011".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      // x2 should be 1
      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect(1.U)
    }
  }
  
  "Main should execute SLTIU correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 5
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(5.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SLTIU x2, x1, 7 -> 5 < 7 ? 1 : 0 (unsigned)
      val instr = "b00000000111_00001_011_00010_0010011".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      // x2 should be 1
      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect(1.U)
    }
  }
  
  "Main should execute XORI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 0b1010
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("b1010".U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // XORI x2, x1, 0b1100 -> 0b1010 ^ 0b1100 = 0b0110
      val instr = "b000000001100_00001_100_00010_00100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b0110".U)
    }
  }

  "Main should execute ORI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 0b1010
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("b1010".U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // ORI x2, x1, 0b1100 -> 0b1010 | 0b1100 = 0b1110
      val instr = "b00000001100_00001_110_00010_00100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b1110".U)
    }
  }

  "Main should execute ANDI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 0b1010
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("b1010".U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // ANDI x2, x1, 0b1100 -> 0b1010 & 0b1100 = 0b1000
      val instr = "b00000001100_00001_111_00010_00100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b1000".U)
    }
  }
  
  "Main should execute SLLI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 0b0001
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("b0001".U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SLLI x2, x1, 2 -> 0b0001 << 2 = 0b0100
      val instr = "b00000_0_00010_00001_001_00010_00100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b0100".U)
    }
  }
  
  "Main should execute SRLI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 0b0100
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("b0100".U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SRLI x2, x1, 2 -> 0b0100 >> 2 = 0b0001
      val instr = "b00000000010_00001_101_00010_0010011".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b0001".U)
    }
  }
  
  "Main should execute SRAI correctly" in {
    simulate(new Main()) { dut =>
      // x1 = -8
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("b11111111111111111111111111111000".U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SRAI x2, x1, 2 -> -8 >> 2 = -2
      val instr = "b01000_0_00010_00001_101_00010_00100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(2.U)
      dut.io.out_C.expect("b11111111111111111111111111111110".U)
    }
  }
  
  "Main should execute ADD correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 5, x2 = 7
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(5.U)
      dut.clock.step(1)
      dut.io.write_addr.poke(2.U)
      dut.io.in.poke(7.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // ADD x3, x1, x2 -> 5 + 7 = 12
      val instr = "b0000000_00010_00001_000_00011_0110011".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(3.U)
      dut.io.out_C.expect(12.U)
    }
  }

  "Main should execute SUB correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 10, x2 = 3
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(10.U)
      dut.clock.step(1)
      dut.io.write_addr.poke(2.U)
      dut.io.in.poke(3.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SUB x3, x1, x2 -> 10 - 3 = 7
      val instr = "b0100000_00010_00001_000_00011_01100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(3.U)
      dut.io.out_C.expect(7.U)
    }
  }
  
  "Main should execute SLL correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 3, x2 = 2
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(3.U)
      dut.clock.step(1)
      dut.io.write_addr.poke(2.U)
      dut.io.in.poke(2.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SLL x3, x1, x2 -> 3 << (2 & 0x1F) = 12
      val instr = "b0000000_00010_00001_001_00011_01100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(3.U)
      dut.io.out_C.expect(12.U)
    }
  }

  "Main should execute SLT (signed) correctly" in {
    simulate(new Main()) { dut =>
      // x1 = -1 (0xFFFFFFFF), x2 = 1
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("hFFFFFFFF".U) // -1 as 32-bit two's complement
      dut.clock.step(1)
      dut.io.write_addr.poke(2.U)
      dut.io.in.poke(1.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SLT x3, x1, x2 -> (-1 < 1) ? 1 : 0  => 1
      val instr = "b0000000_00010_00001_010_00011_01100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(3.U)
      dut.io.out_C.expect(1.U)
    }
  }

  "Main should execute SLTU (unsigned) correctly" in {
    simulate(new Main()) { dut =>
      // x1 = 0xFFFFFFFF (unsigned 4294967295), x2 = 1
      // unsigned: 0xFFFFFFFF > 1  -> result should be 0
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke("hFFFFFFFF".U) // large unsigned value
      dut.clock.step(1)
      dut.io.write_addr.poke(2.U)
      dut.io.in.poke(1.U)
      dut.clock.step(1)
      dut.io.write_enable.poke(false.B)

      // SLTU x3, x1, x2 -> (0xFFFFFFFF < 1) ? 1 : 0  => 0 (unsigned comparison)
      val instr = "b0000000_00010_00001_011_00011_01100_11".U(32.W)
      dut.io.instruction.poke(instr)
      dut.clock.step(1)

      dut.io.read_addr_C.poke(3.U)
      dut.io.out_C.expect(0.U)
    }
  }
  
}
