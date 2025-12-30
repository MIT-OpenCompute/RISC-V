package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {
  "Main should execute LUI correctly" in {
    simulate(new Main()) { dut =>
      dut.io.debug_write.poke(true.B);
      dut.io.debug_write_data.poke("b000000000011_00000_000_00001_0010011".U(32.W));
      dut.io.debug_write_addressess.poke(0.U);

      dut.clock.step(1);

      dut.io.debug_write.poke(true.B);
      dut.io.debug_write_data.poke("b000000000000_00001_000_00001_1100111".U(32.W));
      dut.io.debug_write_addressess.poke(1.U);

      dut.clock.step(1);

      dut.io.debug_write.poke(true.B);
      dut.io.debug_write_data.poke("b000000000110_00000_000_00010_0010011".U(32.W));
      dut.io.debug_write_addressess.poke(2.U);

      dut.clock.step(1);

      dut.io.debug_write.poke(true.B);
      dut.io.debug_write_data.poke("b000000000111_00000_000_00010_0010011".U(32.W));
      dut.io.debug_write_addressess.poke(3.U);

      dut.clock.step(1);

      dut.io.debug_write.poke(false.B);
      dut.io.execute.poke(true.B);

      dut.clock.step(1);

      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
      dut.clock.step(1);
    }
  }
}