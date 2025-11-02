error id: file://<WORKSPACE>/src/main/scala/RISCV/Main.scala:local0
file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
empty definition using pc, found symbol in pc: local0
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -chisel3/regFileA/io/write_addr.
	 -chisel3/regFileA/io/write_addr#
	 -chisel3/regFileA/io/write_addr().
	 -chisel3/util/regFileA/io/write_addr.
	 -chisel3/util/regFileA/io/write_addr#
	 -chisel3/util/regFileA/io/write_addr().
	 -scala/math/regFileA/io/write_addr.
	 -scala/math/regFileA/io/write_addr#
	 -scala/math/regFileA/io/write_addr().
	 -regFileA/io/write_addr.
	 -regFileA/io/write_addr#
	 -regFileA/io/write_addr().
	 -scala/Predef.regFileA.io.write_addr.
	 -scala/Predef.regFileA.io.write_addr#
	 -scala/Predef.regFileA.io.write_addr().
offset: 973
uri: file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
text:
```scala
// See README.md for license details.

package RISCV

import chisel3._
import chisel3.util._
// _root_ disambiguates from packages like chisel3.util.circt if imported
import _root_.circt.stage.ChiselStage
import scala.math._

class Main() extends Module {
    val io = IO(new Bundle {
        val instruction = Input(UInt(32.W))
        val regFileA_out = Output(UInt(32.W))
        val regFileB_out = Output(UInt(32.W))
    })

    // set up register files A and B. They will have identical contents so that we can do dual reads.
    val regFileA = Module(new Registers())
    val regFileB = Module(new Registers())

    // Provide safe/default connections for all register-file inputs so
    // there are no uninitialized sinks when a particular opcode path
    // doesn't drive them. We assign defaults first and override in
    // the opcode-specific when blocks below.
    regFileA.io.enable := false.B
    regFileA.io.write_enable := false.B
    regFileA.io.write_add@@r := 0.U
    
    regFileA.io.in := 0.U

    regFileB.io.enable := false.B
    regFileB.io.write_enable := false.B
    regFileB.io.select := 0.U
    regFileB.io.in := 0.U

    // io for observing register file outputs in testbench    
    io.regFileA_out := regFileA.io.out
    io.regFileB_out := regFileB.io.out

    // Fetch opcode from instruction to determine type of instruction
    val opcode = io.instruction(6,0) // 7 bits

    // Decode logic opcodes
    switch(opcode) {
        is ("b0110111".U(7.W)) { // LUI x[rd] = sext(imm << 12)
            printf("Decoding LUI instruction\n")
            // Extract the fields
            val immm = io.instruction(31,12) // immediate value , 20 bits
            val rd = io.instruction(11,7) // destination register, 5 bits
            val imm_sext = Cat(immm, Fill(12, 0.U)) // 20 bits followed by 12 zeros to be 32 bit total

            // Execute

            // Enable and write to the destination register
            regFileA.io.select := rd // select destination register
            regFileB.io.select := rd
            regFileA.io.enable := true.B // enable destination register
            regFileB.io.enable := true.B
            regFileA.io.write_enable := true.B // enable write in destination register
            regFileB.io.write_enable := true.B
            regFileA.io.in := imm_sext // Write to reg file A
            regFileB.io.in := imm_sext // Write to reg file B

            printf(p"LUI: rd = x$rd, imm_sext = ${Binary(imm_sext)}\n")

            // Registers will update on the next clock edge
        }
        is ("b0010011".U(7.W)) { // Immediate-type ALU operations
            printf("Decoding immediate-type ALU operation\n")
            // Extract the fields
            val rd = io.instruction(11,7) // destination register, 5 bits
            val funct3 = io.instruction(14,12) // funct3 field,
            val rs1 = io.instruction(19,15) // source register 1, 5 bits
            val imm = io.instruction(31,20) // immediate value, 12 bits

            // addi (add immediate)
            when (funct3 === "b000".U(3.W)) { // x[rd] = x[rs1] + sext(imm)
                printf("Decoding ADDI instruction\n")
                // Add the 12-bit sign extended immediate to the value in rs1. put output in rd

                // Enable register file A to read rs1
                regFileA.io.enable := true.B
                regFileA.io.write_enable := false.B
                regFileA.io.select := rs1

                printf(p"ADDI: Reading rs1 = x$rs1\n")

                // Read rs1 (occurs in the same clock cycle because of combinational read)
                val rs1_value = regFileA.io.out

                // Compute result
                val imm_sext = Cat(Fill(20, imm(11)), imm) // sequence of 20 sign bits followed by the 12-bit immediate to be 32 bit total
                val result = rs1_value + imm_sext
                printf(p"ADDI: rs1_value = 0x${Binary(rs1_value)}, imm_sext = 0x${Binary(imm_sext)}, result = 0x${Binary(result)}\n")
                /*
                // Write result to rd
                regFileA.io.select := rd // select destination register
                regFileB.io.select := rd
                
                regFileA.io.enable := true.B // enable destination register
                regFileB.io.enable := true.B
                
                regFileA.io.write_enable := true.B // enable write in destination register
                regFileB.io.write_enable := true.B

                regFileA.io.in := result // Write to reg file A
                regFileB.io.in := result // Write to reg file B
                */
            }
        }
    }
}

/**
  * Object to generate Verilog/SystemVerilog for the module.
  * Customize firtoolOpts if needed.
  */
object Main extends App {
  ChiselStage.emitSystemVerilogFile(
    new Main(),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    )
  )
}

```


#### Short summary: 

empty definition using pc, found symbol in pc: local0