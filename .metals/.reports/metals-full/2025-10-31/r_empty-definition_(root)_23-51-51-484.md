error id: file://<WORKSPACE>/src/main/scala/RISCV/Main.scala:select
file://<WORKSPACE>/src/main/scala/RISCV/Main.scala
empty definition using pc, found symbol in pc: select
empty definition using semanticdb
empty definition using fallback
non-local guesses:
	 -chisel3/regFileB/io/select.
	 -chisel3/regFileB/io/select#
	 -chisel3/regFileB/io/select().
	 -chisel3/util/regFileB/io/select.
	 -chisel3/util/regFileB/io/select#
	 -chisel3/util/regFileB/io/select().
	 -scala/math/regFileB/io/select.
	 -scala/math/regFileB/io/select#
	 -scala/math/regFileB/io/select().
	 -regFileB/io/select.
	 -regFileB/io/select#
	 -regFileB/io/select().
	 -scala/Predef.regFileB.io.select.
	 -scala/Predef.regFileB.io.select#
	 -scala/Predef.regFileB.io.select().
offset: 1153
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

/**
  * A simple register file module with parameterizable width and number of registers.
  * 
  * TODO: Set up two simultaneous read ports
  */

class Main() extends Module {
    val io = IO(new Bundle {
        val instruction = Input(UInt(32.W))
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
    regFileA.io.select := 0.U
    regFileA.io.in := 0.U

    regFileB.io.enable := false.B
    regFileB.io.write_enable := false.B
    regFileB.io.sele@@ct := 0.U
    regFileB.io.in := 0.U
    
     // opcode
    val opcode = io.instruction(6,0) // 7 bits
    
    when (opcode === "b0110111".U(7.W)) { // lui instruction, load upper sign extended immediate into register rd
        val rd = io.instruction(11,7) // destination register, 5 bits
        val imm = io.instruction(31,12) // immediate value , 20 bits

        // Sign extend the 20 bit immediate to 32 bits by appending 12 zeros at the end
        val imm_sext = Cat(imm, Fill(12, 0.U)) // 20 bits followed by 12 zeros to be 32 bit total
        
        // Move imm_sext into register rd
        regFileA.io.select := rd // select destination register
        regFileB.io.select := rd
        regFileA.io.enable := true.B // enable destination register
        regFileB.io.enable := true.B
        regFileA.io.write_enable := true.B // enable write in destination register
        regFileB.io.write_enable := true.B
        regFileA.io.in := imm_sext // Write to reg file A
        regFileB.io.in := imm_sext // Write to reg file B
    } 

    when (opcode === "b0010011".U(7.W)) { // immediate-type ALU operations
        val rd = io.instruction(11,7) // destination register, 5 bits
        val funct3 = io.instruction(14,12) // funct3 field, 3 bits
        val rs1 = io.instruction(19,15) // source register 1, 5 bits
        val imm = io.instruction(31,20) // immediate value, 12 bits

        // addi (add immediate)
        when (funct3 === "b000".U(3.W)) {
            // Add the 12-bit sign extended immediate to the value in rs1. put output in rd
            regFileA.io.enable := true.B
            regFileA.io.write_enable := false.B
            regFileA.io.select := rs1
            // Read rs1
            val rs1_value = regFileA.io.out
            // Compute result
            val imm_sext = Cat(Fill(20, imm(11)), imm) // sequence of 20 sign bits followed by the 12-bit immediate to be 32 bit total
            val result = rs1_value + imm_sext

            // Write result to rd
            regFileA.io.select := rd // select destination register
            regFileB.io.select := rd
            
            regFileA.io.enable := true.B // enable destination register
            regFileB.io.enable := true.B
            
            regFileA.io.write_enable := true.B // enable write in destination register
            regFileB.io.write_enable := true.B

            regFileA.io.in := result // Write to reg file A
            regFileB.io.in := result // Write to reg file B
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

empty definition using pc, found symbol in pc: select