package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import scala.math._
import os.read

class Main() extends Module {
    val io = IO(new Bundle {
        val instruction = Input(UInt(32.W))
        val RegFileA_out = Output(UInt(32.W))
        val RegFileB_out = Output(UInt(32.W))
    })

    // set up register files A and B. They will have identical contents so that we can do dual reads.
    val regFileA = Module(new Registers())
    val regFileB = Module(new Registers())

    // Provide safe/default connections for all register-file inputs so
    // there are no uninitialized sinks when a particular opcode path
    // doesn't drive them. We assign defaults first and override in
    // the opcode-specific when blocks below.
    regFileA.io.write_enable := false.B
    regFileA.io.write_addr := 0.U // default write address
    regFileA.io.read_addr := 0.U // default read address
    regFileA.io.in := 0.U

    regFileB.io.write_enable := false.B
    regFileB.io.write_addr := 0.U // default write address
    regFileB.io.read_addr := 0.U // default read address    
    regFileB.io.in := 0.U

    // io for observing register file outputs in testbench    
    io.RegFileA_out := 0.U
    io.RegFileB_out := 0.U

    // Fetch opcode from instruction to determine type of instruction
    val opcode = io.instruction(6,0) // 7 bits

    // Decode logic opcodes
    switch(opcode) {
        is ("b1111111".U(7.W)) // Print all registers instruction (not standard RISC-V, just for testing)
        {
            val i = io.instruction(11,7) // destination register, 5 bits
            regFileA.io.read_addr := i // set read address to rd to observe the written
            regFileB.io.read_addr := i
            regFileA.io.write_enable := false.B
            regFileB.io.write_enable := false.B

            // Read from both register files
            io.RegFileA_out := regFileA.io.out
            io.RegFileB_out := regFileB.io.out
        }
        is ("b0110111".U(7.W)) { // LUI x[rd] = sext(imm << 12)
            printf("Decoding LUI instruction\n")
            // Extract the fields
            val immm = io.instruction(31,12) // immediate value , 20 bits
            val rd = io.instruction(11,7) // destination register, 5 bits
            val imm_sext = Cat(immm, Fill(12, 0.U)) // 20 bits followed by 12 zeros to be 32 bit total

            printf("Extracted fields:\n")

            // Execute

            // Enable and write to the destination register
            regFileA.io.write_addr := rd // select destination register
            regFileB.io.write_addr := rd
            regFileA.io.write_enable := true.B // enable write in destination register
            regFileB.io.write_enable := true.B
            regFileA.io.in := imm_sext // Write to reg file A
            regFileB.io.in := imm_sext // Write to reg file B

            printf(p"LUI: rd = x$rd, imm_sext = ${Binary(imm_sext)}\n")

            // Registers will update on the next clock edge

            printf("LUI instruction executed\n")
        }
        is ("b0010011".U(7.W)) { // Immediate-type ALU operations
            printf("Decoding immediate-type ALU operation\n")
            // Extract the fields
            val rd = io.instruction(11,7) // destination register, 5 bits
            val funct3 = io.instruction(14,12) // funct3 field,
            val rs1 = io.instruction(19,15) // source register 1, 5 bits
            val imm = io.instruction(31,20) // immediate value, 12 bits

            printf("Extracted fields:\n")

            // addi (add immediate)
            when (funct3 === "b000".U(3.W)) { // x[rd] = x[rs1] + sext(imm)
                printf("Decoding ADDI instruction\n")
                // Add the 12-bit sign extended immediate to the value in rs1. put output in rd

                // Enable register file A to read rs1
                regFileA.io.write_enable := false.B
                regFileA.io.read_addr := rs1

                printf(p"ADDI: Reading rs1 = x$rs1\n")

                // Read rs1 (occurs in the same clock cycle because of combinational read)
                val rs1_value = regFileA.io.out

                // Compute result
                val imm_sext = Cat(Fill(20, imm(11)), imm) // sequence of 20 sign bits followed by the 12-bit immediate to be 32 bit total
                val result = rs1_value + imm_sext
                printf(p"ADDI: rs1_value = 0x${Binary(rs1_value)}, imm_sext = 0x${Binary(imm_sext)}, result = 0x${Binary(result)}\n")
                
                // Write result to rd
                regFileA.io.write_addr := rd // select destination register
                regFileB.io.write_addr := rd
                
                regFileA.io.write_enable := true.B // enable write in destination register
                regFileB.io.write_enable := true.B

                regFileA.io.in := result // Write to reg file A
                regFileB.io.in := result // Write to reg file B

                printf(p"ADDI: Writing result to rd = x$rd\n")

                // Registers will update on the next clock edge

                printf("ADDI instruction executed\n")
            }
        
            // slti (set less than immediate)
            when (funct3 === "b010".U(3.W)) {
                printf("Decoding SLTI instruction\n?")
                // read rs1
                regFileA.io.write_enable := false.B
                regFileA.io.read_addr := rs1

                printf(p"SLTI: Reading rs1 = x$rs1\n")

                // Read rs1 (occurs in the same clock cycle because of combinational read)
                val rs1_value = regFileA.io.out

                // Compute result
                val imm_sext = Cat(Fill(20, imm(11)), imm) // sequence of 20 sign bits followed by the 12-bit immediate to be 32 bit total
                when (rs1_value.asSInt < imm_sext.asSInt) {
                    // rs1 < sext(imm)
                    // Write 1 to rd
                    regFileA.io.write_addr := rd // select destination register
                    regFileB.io.write_addr := rd

                    regFileA.io.write_enable := true.B // enable write in destination register
                    regFileB.io.write_enable := true.B

                    regFileA.io.in := 1.U(32.W) // Write to reg file A
                    regFileB.io.in := 1.U(32.W) // Write to reg file B

                    printf(p"SLTI: rs1_value = ${rs1_value.asSInt}, imm_sext = ${imm_sext.asSInt}, Writing 1 to rd = x$rd\n")

                } .otherwise {
                    //rs1 >= sext(imm)
                    // Write 0 to rd
                    regFileA.io.write_addr := rd // select destination register
                    regFileB.io.write_addr := rd

                    regFileA.io.write_enable := true.B // enable write in destination register
                    regFileB.io.write_enable := true.B

                    regFileA.io.in := 0.U(32.W) // Write to reg file A
                    regFileB.io.in := 0.U(32.W) // Write to reg file

                    printf(p"SLTI: rs1_value = ${rs1_value.asSInt}, imm_sext = ${imm_sext.asSInt}, Writing 0 to rd = x$rd\n")
                }
                printf("SLTI instruction executed\n")
            }
        }
    }
}

object Main extends App {
  ChiselStage.emitSystemVerilogFile(
    new Main(),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    ),
    args = Array("--target-dir", "generated")
  )
}
