package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class RegistersSpec extends AnyFreeSpec with Matchers with ChiselSim {

  "registers should pass basic write/read tests" in {
    simulate(new Registers()) { dut =>
      // Write to x1
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(1.U)
      dut.io.in.poke(123.U(32.W))
      dut.clock.step(1)

      // Write to x2
      dut.io.write_addr.poke(2.U)
      dut.io.in.poke(456.U(32.W))
      dut.clock.step(1)

      // Read both registers
      dut.io.read_addr_A.poke(1.U)
      dut.io.read_addr_B.poke(2.U)
      dut.io.out_A.expect(123.U(32.W))
      dut.io.out_B.expect(456.U(32.W))
    }
  }

  "register x0 should always read as 0 and ignore writes" in {
    simulate(new Registers()) { dut =>
      // Attempt to write to x0
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(0.U)
      dut.io.in.poke(999.U(32.W))
      dut.clock.step(1)

      // Read back x0
      dut.io.read_addr_A.poke(0.U)
      dut.io.read_addr_B.poke(0.U)
      dut.io.out_A.expect(0.U(32.W))
      dut.io.out_B.expect(0.U(32.W))
    }
  }

  "registers should update only when write_enable is true" in {
    simulate(new Registers()) { dut =>
      // Disable writing
      dut.io.write_enable.poke(false.B)
      dut.io.write_addr.poke(3.U)
      dut.io.in.poke(111.U(32.W))
      dut.clock.step(1)

      // Read back should still be 0
      dut.io.read_addr_A.poke(3.U)
      dut.io.out_A.expect(0.U(32.W))
    }
  }

  "registers should correctly handle multiple writes and reads" in {
    simulate(new Registers()) { dut =>
      // Write multiple registers
      dut.io.write_enable.poke(true.B)
      dut.io.write_addr.poke(4.U)
      dut.io.in.poke(10.U(32.W))
      dut.clock.step(1)

      dut.io.write_addr.poke(5.U)
      dut.io.in.poke(20.U(32.W))
      dut.clock.step(1)

      dut.io.write_addr.poke(6.U)
      dut.io.in.poke(30.U(32.W))
      dut.clock.step(1)

      // Read them back
      dut.io.read_addr_A.poke(4.U)
      dut.io.read_addr_B.poke(5.U)
      dut.io.out_A.expect(10.U(32.W))
      dut.io.out_B.expect(20.U(32.W))

      dut.io.read_addr_A.poke(6.U)
      dut.io.read_addr_B.poke(4.U)
      dut.io.out_A.expect(30.U(32.W))
      dut.io.out_B.expect(10.U(32.W))
    }
  }
}
