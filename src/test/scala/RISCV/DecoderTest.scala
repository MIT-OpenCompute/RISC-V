package RISCV

import chisel3._
import chisel3.experimental.BundleLiterals._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class DecoderTest extends AnyFreeSpec with Matchers with ChiselSim {
  "Decoder should pass tests" in {
    simulate(new Decoder()) { dut =>
      dut.io.instruction.poke(0.U);
    }
  }
}