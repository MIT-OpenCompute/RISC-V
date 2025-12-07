package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import scala.math._
import os.read

class Main() extends Module {
    val io = IO(new Bundle {
        val execute = Input(Bool());

        val debug_write = Input(Bool());
        val debug_write_address = Input(UInt(32.W));
        val debug_write_data = Input(UInt(32.W));
        
        // Debugging variables for tests
        val out_A = Output(UInt(32.W))
        val out_B = Output(UInt(32.W))
        val out_C = Output(UInt(32.W)) // Testing output port
        val read_addr_A = Input(UInt(5.W))
        val read_addr_B = Input(UInt(5.W))
        val read_addr_C = Input(UInt(5.W)) // Testing address port
        val write_enable = Input(Bool())
        val write_addr = Input(UInt(5.W))
        val in = Input(UInt(32.W))
    })
    
    val program_pointer = RegInit(0.U(32.W));

    val memory = SRAM(1024, UInt(32.W), 1, 1, 0);

    // Set up register file
    val regFile = Module(new Registers())
    // Default connections for register file inputs

    regFile.io.write_enable := false.B
    regFile.io.write_addr := 0.U(5.W)
    regFile.io.read_addr_A := 0.U(5.W)
    regFile.io.read_addr_B := 0.U(5.W)
    regFile.io.read_addr_C := 0.U(5.W)
    regFile.io.in := 0.U(32.W) 

    // Set up testing register outputs
    io.out_A := regFile.io.out_A
    io.out_B := regFile.io.out_B
    io.out_C := regFile.io.out_C
    regFile.io.read_addr_A := io.read_addr_A
    regFile.io.read_addr_B := io.read_addr_B
    regFile.io.read_addr_C := io.read_addr_C
    regFile.io.write_enable := io.write_enable
    regFile.io.write_addr := io.write_addr
    regFile.io.in := io.in

    // Set up ALU
    val alu = Module(new ALU())
    // Default connections for ALU inputs
    alu.io.operation := 0.U(3.W)
    alu.io.signed := false.B
    alu.io.a := 0.U(32.W)
    alu.io.b := 0.U(32.W)

    val decoder = Module(new Decoder())
    decoder.io.instruction := memory.readPorts(0).data;

    val loading_instruction_stage = RegInit(true.B);

    memory.readPorts(0).enable := io.execute && loading_instruction_stage;
    memory.readPorts(0).address := program_pointer;

    memory.writePorts(0).enable := io.debug_write;
    memory.writePorts(0).address := io.debug_write_address;
    memory.writePorts(0).data := io.debug_write_data;

    when(io.execute) {
        printf("\n")
        printf("Operation: %b\n", decoder.io.operation)
        printf("Program Pointer: %d\n", program_pointer)
        printf("Loading Instruction Stage: %d\n", loading_instruction_stage)
        printf("Data: %b\n", memory.readPorts(0).data)

        when(loading_instruction_stage) {
            loading_instruction_stage := false.B;
        }.otherwise {
            loading_instruction_stage := true.B;

    switch(decoder.io.operation) {
        // U-type instructions
        is("b01101_11".U(7.W)) {  // LUI opcode
            // LUI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#lui
            
            // Get additional fields from decoder
            val rd = decoder.io.rd
            val immediate = decoder.io.immediate(31,12)

            // Execute LUI: x[rd] = sext(imm << 12)

            // Pad 20 bit immediate with 12 zeros to the right
            val sext_imm  = Cat(immediate, Fill(12, 0.U)) 

            printf("Register: %d Immediate: %b\n", rd, sext_imm)

            // Write to register file
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := sext_imm

            program_pointer := program_pointer + 1.U;
        }
        is("b00101_11".U) {
            // AUIPC instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#auipc

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val immediate = decoder.io.immediate(31, 12)

            // Execute AUIPC: x[rd] = pc + sext(imm << 12)

            // Pad 20 bit immediate with 12 zeros to the right
            val sext_imm  = Cat(immediate, Fill(12, 0.U)) 

            val pc = 0.U(32.W) // Placeholder for program counter value
            
            // Set up ALU
            alu.io.operation := "b0000".U // Addition
            alu.io.signed := false.B // Default
            alu.io.a := pc
            alu.io.b := sext_imm
            // Get ALU result
            val alu_result = alu.io.output

            // Write to register file
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result // pc + sext(imm << 12)
        }

        // I-type instructions
        is("b000_00100_11".U) {
            // ADDI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#addi

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val immediate = decoder.io.immediate

            // Execute ADDI: x[rd] = x[rs1] + sext(imm)

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Sign extend immediate
            val sext_imm = Cat(Fill(20, immediate(11)), immediate) // sext(imm << 12)

            // Set up ALU
            alu.io.operation := "b0000".U // Addition
            alu.io.signed := false.B // Default
            alu.io.a := rs1_value
            alu.io.b := sext_imm
            // Get ALU result
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b010_00100_11".U) {
            // SLTI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#slti
            // Note that this is a signed comparison

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val immediate = decoder.io.immediate

            // Execute SLTI: x[rd] = (x[rs1] < sext(imm)) ? 1 : 0

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Sign extend immediate
            val sext_imm = Cat(Fill(20, immediate(11)), immediate) // sext(imm << 12)

            // Set up ALU
            alu.io.operation := "b0010".U // Comparison
            alu.io.signed := true.B // Signed comparison
            alu.io.a := rs1_value
            alu.io.b := sext_imm
            // Get ALU result
            val alu_result = alu.io.output(0) // Least significant bit is the comparison result (LT)

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b011_00100_11".U) {
            // SLTIU instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#sltiu
            // Note that this is an unsigned comparison

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val immediate = decoder.io.immediate

            // Execute SLTIU: x[rd] = (x[rs1] < zext(imm)) ? 1 : 0

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Zero extend immediate
            val zext_imm = Cat(Fill(20, 0.U), immediate) // zext(imm)

            // Set up ALU
            alu.io.operation := "b0010".U // Comparison
            alu.io.signed := false.B // Unsigned comparison
            alu.io.a := rs1_value
            alu.io.b := zext_imm
            // Get ALU result
            val alu_result = alu.io.output(0) // Least significant bit is the comparison result (LT)

            // Compare and write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b100_00100_11".U) {
            // XORI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#xori

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val immediate = decoder.io.immediate

            // Execute XORI: x[rd] = x[rs1] ^ sext(imm)

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Sign-extend immediate
            val sext_imm = Cat(Fill(20, immediate(11)), immediate) // sext(imm)

            // Set up ALU
            alu.io.operation := "b0101".U // XOR operation
            alu.io.signed := false.B // Default
            alu.io.a := rs1_value
            alu.io.b := sext_imm
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b110_00100_11".U) {
            // ORI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#ori

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val immediate = decoder.io.immediate

            // Execute ORI: x[rd] = x[rs1] | sext(imm)

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Sign-extend immediate
            val sext_imm = Cat(Fill(20, immediate(11)), immediate) // sext(imm)

            // Set up ALU
            alu.io.operation := "b0100".U // OR operation
            alu.io.signed := false.B // Default
            alu.io.a := rs1_value
            alu.io.b := sext_imm
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b111_00100_11".U) {
            // ANDI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#andi

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val immediate = decoder.io.immediate

            // Execute ANDI: x[rd] = x[rs1] & sext(imm)

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Sign-extend immediate
            val sext_imm = Cat(Fill(20, immediate(11)), immediate) // sext(imm)

            // Set up ALU
            alu.io.operation := "b0011".U // AND operation
            alu.io.signed := false.B // Default
            alu.io.a := rs1_value
            alu.io.b := sext_imm
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b001_00100_11".U) {
            // SLLI instructions https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#slli

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val shamt = decoder.io.immediate(4,0) // Shift amount is in the lower 5 bits of the immediate

            // Execute SLLI: x[rd] = x[rs1] << shamt

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            // Set up ALU
            alu.io.operation := "b0111".U // Logical shift left
            alu.io.signed := false.B // Default
            alu.io.a := rs1_value
            alu.io.b := shamt
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is("b101_00100_11".U) {
            // SRLI / SRAI instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#srli

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val shamt = decoder.io.immediate(4,0) // Shift amount is in lower 5 bits of immediate
            val srli_srai_bit = decoder.io.immediate(9) // Bit 30 differentiates SRLI and SRAI

            // Read rs1 value
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A

            when (srli_srai_bit === 0.U) {
                // SRLI: x[rd] = x[rs1] >> shamt (logical)
                
                // Set up ALU
                alu.io.operation := "b1000".U // Logical shift right
                alu.io.signed := false.B // Default
                alu.io.a := rs1_value
                alu.io.b := Cat(0.U(27.W), shamt) // Zero-extend shamt to 32 bits
                val alu_result = alu.io.output

                // Write result to rd
                regFile.io.write_addr := rd
                regFile.io.write_enable := true.B
                regFile.io.in := alu_result

            } .elsewhen (srli_srai_bit === 1.U) {
                // SRAI: x[rd] = x[rs1] >> shamt (arithmetic)

                // Set up ALU
                alu.io.operation := "b1001".U // Arithmetic shift right
                alu.io.a := rs1_value
                alu.io.b := Cat(0.U(27.W), shamt) // Zero-extend shamt to 32 bits
                val alu_result = alu.io.output

                // Write result to rd
                regFile.io.write_addr := rd
                regFile.io.write_enable := true.B
                regFile.io.in := alu_result
            }
        }
        
        // R-type instructions
        is ("b0000000000_01100_11".U) {
            // ADD instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#add

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val rs2 = decoder.io.rs2

            // Execute ADD: x[rd] = x[rs1] + x[rs2]

            // Read rs1 and rs2 values
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A
            regFile.io.read_addr_B := rs2
            val rs2_value = regFile.io.out_B

            // Set up ALU
            alu.io.operation := "b0000".U // Addition
            alu.io.a := rs1_value
            alu.io.b := rs2_value
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }

        is ("b0100000000_01100_11".U) {
            // SUB instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#sub

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val rs2 = decoder.io.rs2

            // Execute SUB: x[rd] = x[rs1] - x[rs2]

            // Read rs1 and rs2 values
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A
            regFile.io.read_addr_B := rs2
            val rs2_value = regFile.io.out_B

            // Set up ALU
            alu.io.operation := "b0000".U // Addition
            alu.io.a := rs1_value
            alu.io.b := (~rs2_value).asUInt + 1.U // Two's complement for subtraction
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        } 
        
        is ("b0000000001_01100_11".U) {
            // SLL instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#sll

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val rs2 = decoder.io.rs2

            // Execute SLL: x[rd] = x[rs1] << (x[rs2] & 0x1F)

            // Read rs1 and rs2 values
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A
            regFile.io.read_addr_B := rs2
            val rs2_value = regFile.io.out_B

            val shamt = rs2_value(4,0) // Shift amount is lower 5 bits of rs2

            // Set up ALU
            alu.io.operation := "b0111".U // Logical shift left
            alu.io.a := rs1_value
            alu.io.b := shamt
            val alu_result = alu.io.output

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := alu_result
        }
        is ("b0000000010_01100_11".U) {
            // SLT instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#slt

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val rs2 = decoder.io.rs2

            // Execute SLT: x[rd] = (x[rs1] < x[rs2]) ? 1 : 0

            // Read rs1 and rs2 values
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A
            regFile.io.read_addr_B := rs2
            val rs2_value = regFile.io.out_B

            // Set up ALU
            alu.io.operation := "b0010".U // Comparison
            alu.io.signed := true.B // Signed comparison
            alu.io.a := rs1_value
            alu.io.b := rs2_value
            val alu_result = alu.io.output(0) // Least significant bit is the comparison result (LT)

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := Cat(0.U(31.W), alu_result) // Zero extend to 32 bits
        }
        is ("b0000000011_01100_11".U) {
            // SLTU instruction https://msyksphinz-self.github.io/riscv-isadoc/html/rvi.html#sltu

            // Get additional fields from decoder
            val rd = decoder.io.rd
            val rs1 = decoder.io.rs1
            val rs2 = decoder.io.rs2

            // Execute SLTU: x[rd] = (x[rs1] < x[rs2]) ? 1 : 0 (unsigned)

            // Read rs1 and rs2 values
            regFile.io.read_addr_A := rs1
            val rs1_value = regFile.io.out_A
            regFile.io.read_addr_B := rs2
            val rs2_value = regFile.io.out_B

            // Set up ALU
            alu.io.operation := "b0010".U // Comparison
            alu.io.signed := false.B // Unsigned comparison
            alu.io.a := rs1_value
            alu.io.b := rs2_value
            val alu_result = alu.io.output(0) // Least significant bit is the comparison result (LT)

            // Write result to rd
            regFile.io.write_addr := rd
            regFile.io.write_enable := true.B
            regFile.io.in := Cat(0.U(31.W), alu_result) // Zero extend to 32 bits
        }
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
