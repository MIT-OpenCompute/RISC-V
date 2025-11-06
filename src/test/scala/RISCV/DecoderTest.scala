package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DecoderTest extends AnyFreeSpec with Matchers with ChiselSim {
  "Decoder should pass tests" in {
    simulate(new Decoder()) { dut =>
      dut.io.instruction.poke(0b00000000000000000101_00001_0110111.U);
      dut.io.operation.expect(0b0000000_000_0110111.U);
      dut.io.rd.expect(0b00001.U);
      dut.io.immediate.expect(0b00000000000000000101_000000000000.U);
    }
  }
}