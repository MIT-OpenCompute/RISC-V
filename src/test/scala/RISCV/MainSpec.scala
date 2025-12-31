package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.io.Source
import java.nio.file.{Files, Paths}
import java.nio.ByteBuffer

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {
  "Main should execute LUI correctly" in {
    simulate(new Main()) { dut =>
      val bytes = Files.readAllBytes(Paths.get("./programs/fib-loop.bin"))
      
      val instructions = bytes.grouped(4).map { instrBytes =>
        val paddedBytes = instrBytes.padTo(4, 0.toByte)
        val signedInt = ByteBuffer.wrap(paddedBytes).order(java.nio.ByteOrder.BIG_ENDIAN).getInt()
        signedInt.toLong & 0xFFFFFFFFL
      }.toSeq

      dut.io.debug_write.poke(true.B)
      instructions.zipWithIndex.foreach { case (instr, idx) =>
        println(s"Instruction: ${instr.toBinaryString.reverse.padTo(32, '0').reverse}")
        println(s"Address: ${idx * 4}")

        dut.io.debug_write_data.poke(instr.U(32.W))
        dut.io.debug_write_addressess.poke((idx * 4).U)
        dut.clock.step(1)
      }

      dut.io.debug_write.poke(false.B);
      dut.io.execute.poke(true.B);

      dut.clock.step(1);

      dut.clock.step(50);
    }
  }
}