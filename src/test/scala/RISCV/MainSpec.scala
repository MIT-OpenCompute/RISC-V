package RISCV

import chisel3._
import chisel3.simulator.scalatest.ChiselSim
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import scala.io.Source
import java.nio.file.{Files, Paths}
import java.nio.ByteBuffer
import chisel3.simulator.PeekPokeAPI.TestableRecord

class MainSpec extends AnyFreeSpec with Matchers with ChiselSim {
//   "Main should execute LUI correctly" in {
//     simulate(new Main()) { dut =>
//       val bytes = Files.readAllBytes(Paths.get("./programs/fib-loop.bin"))

//       val instructions = bytes.grouped(4).map { instrBytes =>
//         val paddedBytes = instrBytes.padTo(4, 0.toByte)
//         val signedInt = ByteBuffer.wrap(paddedBytes).order(java.nio.ByteOrder.BIG_ENDIAN).getInt()
//         signedInt.toLong & 0xFFFFFFFFL
//       }.toSeq

//       dut.io.debug_write.poke(true.B)
//       instructions.zipWithIndex.foreach { case (instr, idx) =>
//         println(s"Instruction: ${instr.toBinaryString.reverse.padTo(32, '0').reverse}")
//         println(s"Address: ${idx * 4}")

//         dut.io.debug_write_data.poke(instr.U(32.W))
//         dut.io.debug_write_addressess.poke((idx * 4).U)
//         dut.clock.step(1)
//       }

//       dut.io.debug_write.poke(false.B);
//       dut.io.execute.poke(true.B);

//       dut.clock.step(1);

//       dut.clock.step(50);
//     }
//   }

    "Main should execute Store and Load Instructions correctly" in {
        simulate(new Main()) { dut =>
            dut.io.debug_write.poke(true.B)

            dut.io.debug_write_address.poke(0.U)
            dut.io.debug_write_data.poke(0x00004137.U)
            dut.clock.step(1)

            dut.io.debug_write_address.poke(1.U)
            dut.io.debug_write_data.poke(0x00408093.U)
            dut.clock.step(1)

            dut.io.debug_write_address.poke(2.U)
            dut.io.debug_write_data.poke(0x00110113.U)
            dut.clock.step(1)

            dut.io.debug_write_address.poke(3.U)
            dut.io.debug_write_data.poke(0x00008023.U)
            dut.clock.step(1)

            dut.io.debug_write_address.poke(4.U)
            dut.io.debug_write_data.poke(0xff1ff06f.U)
            dut.clock.step(1)

            // dut.io.debug_write_address.poke(0.U)
            // dut.io.debug_write_data.poke(0b000000000111_00000_000_00001_0010011.U) // ADDI
            // dut.clock.step(1)

            // dut.io.debug_write_address.poke(1.U)
            // dut.io.debug_write_data.poke(0b0000000_00001_00000_010_00000_0100011.U) // SW
            // dut.clock.step(1)

            // dut.io.debug_write_address.poke(2.U)
            // dut.io.debug_write_data.poke(0b000000000000_00000_010_00010_0000011.U) // LW
            // dut.clock.step(1)

            dut.io.debug_write.poke(false.B)
            dut.clock.step(1)

            dut.io.execute.poke(true.B)
            dut.clock.step(24)
        }
    }
}
