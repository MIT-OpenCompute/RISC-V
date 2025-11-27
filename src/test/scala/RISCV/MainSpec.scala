package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {
  "Main should execute LUI correctly" in {
    simulate(new Main()) { dut =>
      dut.io.debug_write.poke(true.B);
      dut.io.debug_write_data.poke("b00010010001101000101_00001_0110111".U(32.W));
      dut.io.debug_write_address.poke(0.U);

      dut.clock.step(1)

      dut.io.debug_write.poke(false.B);
      dut.io.execute.poke(true.B);

      dut.clock.step(1)
      
      dut.clock.step(1)
    }
  }
}
