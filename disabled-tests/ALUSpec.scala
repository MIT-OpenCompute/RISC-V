package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ALUSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "Addition should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke(5.U)
      dut.io.b.poke(7.U)
      dut.io.operation.poke("b0000".U)
      dut.clock.step(1)
      dut.io.output.expect(12.U)
    }
  }

  "Multiplication should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke(3.U)
      dut.io.b.poke(4.U)
      dut.io.operation.poke("b0001".U)
      dut.clock.step(1)
      dut.io.output.expect(12.U)
    }
  }

  "Bitwise AND should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("b1010".U)
      dut.io.b.poke("b1100".U)
      dut.io.operation.poke("b0011".U)
      dut.clock.step(1)
      dut.io.output.expect("b1000".U)
    }
  }

  "Bitwise OR should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("b1010".U)
      dut.io.b.poke("b1100".U)
      dut.io.operation.poke("b0100".U)
      dut.clock.step(1)
      dut.io.output.expect("b1110".U)
    }
  }

  "Bitwise XOR should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("b1010".U)
      dut.io.b.poke("b1100".U)
      dut.io.operation.poke("b0101".U)
      dut.clock.step(1)
      dut.io.output.expect("b0110".U)
    }
  }

  "Bitwise NOT should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("b1010".U)
      dut.io.operation.poke("b0110".U)
      dut.clock.step(1)
      dut.io.output.expect("b11111111111111111111111111110101".U)
    }
  }

  "Logical Shift Left should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("b0001".U)
      dut.io.b.poke(2.U)
      dut.io.operation.poke("b0111".U)
      dut.clock.step(1)
      dut.io.output.expect("b0100".U)
    }
  }

  "Logical Shift Right should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("b0100".U)
      dut.io.b.poke(2.U)
      dut.io.operation.poke("b1000".U)
      dut.clock.step(1)
      dut.io.output.expect("b0001".U)
    }
  }

  "Arithmetic Shift Right should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("hFFFFFFF8".U)
      dut.io.b.poke(2.U)
      dut.io.operation.poke("b1001".U)
      dut.io.signed.poke(true.B)
      dut.clock.step(1)
      dut.io.output.expect("hFFFFFFFE".U)
    }
  }

  "Signed Comparison should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke(5.U)
      dut.io.b.poke(7.U)
      dut.io.operation.poke("b0010".U)
      dut.io.signed.poke(true.B)
      dut.clock.step(1)
      dut.io.output.expect("b001".U) // LT = 1

      dut.io.a.poke(7.U)
      dut.io.b.poke(5.U)
      dut.clock.step(1)
      dut.io.output.expect("b100".U) // GT = 1

      dut.io.a.poke(5.U)
      dut.io.b.poke(5.U)
      dut.clock.step(1)
      dut.io.output.expect("b010".U) // EQ = 1
    }
  }

  "Unsigned Comparison should work correctly" in {
    simulate(new ALU()) { dut =>
      dut.io.a.poke("hFFFFFFFF".U)
      dut.io.b.poke(0.U)
      dut.io.signed.poke(false.B)
      dut.io.operation.poke("b0010".U)
      dut.clock.step(1)
      dut.io.output.expect("b100".U) // GT = 1

      dut.io.a.poke(0.U)
      dut.io.b.poke(0.U)
      dut.clock.step(1)
      dut.io.output.expect("b010".U) // EQ = 1
    }
  }

}
