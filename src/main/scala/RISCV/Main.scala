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
        val debug_write_addressess = Input(UInt(32.W));
        val debug_write_data = Input(UInt(32.W));
        
        // Debugging variables for tests
        val out_a = Output(UInt(32.W))
        val out_b = Output(UInt(32.W))
        val out_c = Output(UInt(32.W)) // Testing output port
        val read_address_a = Input(UInt(5.W))
        val read_address_b = Input(UInt(5.W))
        val read_address_c = Input(UInt(5.W)) // Testing address port
        val write_enable = Input(Bool())
        val write_address = Input(UInt(5.W))
        val in = Input(UInt(32.W))
    })
    
    val program_pointer = RegInit(0.U(32.W));

    val memory = SRAM(1024, UInt(32.W), 2, 1, 0);

    // Set up register file
    val registers = Module(new Registers())
    // Default connections for register file inputs

    registers.io.write_enable := false.B
    registers.io.write_address := 0.U(5.W)
    registers.io.read_address_a := 0.U(5.W)
    registers.io.read_address_b := 0.U(5.W)
    registers.io.read_address_c := 0.U(5.W)
    registers.io.in := 0.U(32.W) 

    // Set up testing register outputs
    io.out_a := registers.io.out_a
    io.out_b := registers.io.out_b
    io.out_c := registers.io.out_c
    registers.io.read_address_a := io.read_address_a
    registers.io.read_address_b := io.read_address_b
    registers.io.read_address_c := io.read_address_c
    registers.io.write_enable := io.write_enable
    registers.io.write_address := io.write_address
    registers.io.in := io.in

    // Set up ALU
    val alu = Module(new ALU())
    // Default connections for ALU inputs
    alu.io.operation := 0.U(3.W)
    alu.io.signed := false.B
    alu.io.a := 0.U(32.W)
    alu.io.b := 0.U(32.W)

    val decoder = Module(new Decoder())
    decoder.io.instruction := memory.readPorts(0).data;

    val stage = RegInit(0.U(2.W)); // 0 - Load Instruction   1 - Execute Instruction A   2 - Execute Instruction B

    memory.readPorts(0).enable := io.execute && stage === 0.U;
    memory.readPorts(0).address := program_pointer;

    memory.readPorts(1).enable := false.B;
    memory.readPorts(1).address := 0.U;

    memory.writePorts(0).enable := io.debug_write;
    memory.writePorts(0).address := io.debug_write_addressess;
    memory.writePorts(0).data := io.debug_write_data;

    val operation_buffer = RegInit(0.U(17.W));
    val rs1_buffer = RegInit(0.U(5.W));
    val rs2_buffer = RegInit(0.U(5.W));
    val rd_buffer = RegInit(0.U(5.W));

    when(io.execute) {
        printf("\n");
        printf("Stage: %d\n", stage);
        printf("Operation: %b\n", decoder.io.operation);
        printf("Program Pointer: %d\n", program_pointer);
        printf("Data: %b\n", memory.readPorts(0).data);
        printf("Register 1: %b\n", registers.io.debug_1);
        printf("Register 2: %b\n", registers.io.debug_2);

        stage := stage + 1.U;

        when(stage === 1.U) {
            operation_buffer := decoder.io.operation;
            rs1_buffer := decoder.io.rs1;
            rs2_buffer := decoder.io.rs2;
            rd_buffer := decoder.io.rd;

            switch(decoder.io.operation) {
                // LW
                is("b010_0000011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    memory.readPorts(1).enable := true.B;
                    memory.readPorts(1).address := registers.io.out_a + decoder.io.immediate;
                    
                    printf("[LW] Rs1: %d Immediate: %b\n", decoder.io.rs1, registers.io.out_a + decoder.io.immediate);
                }

                // SW
                is("b010_0100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    memory.writePorts(0).enable := true.B;
                    memory.writePorts(0).address := registers.io.out_a + decoder.io.immediate;
                    memory.writePorts(0).data := registers.io.out_b;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;
                    
                    printf("[SW] Rs1: %d Rs2: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rs2, registers.io.out_a + decoder.io.immediate);
                }
                
                // LUI
                is("b0110111".U) { 
                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[LUI] Rd: %d Immediate: %b\n", decoder.io.rd, decoder.io.immediate);
                }

                // AUIPC
                is("b0010111".U) { 
                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := program_pointer + decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[AUIPC] Rd: %d Immediate: %b\n", decoder.io.rd, decoder.io.immediate);
                }

                // ADDI
                is("b000_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a + decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[ADDI] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }

                // SLTI
                is("b010_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;

                    when(registers.io.out_a.asSInt < decoder.io.immediate.asSInt) {
                       registers.io.in := 1.U;
                    }.otherwise {
                       registers.io.in := 0.U;
                    }

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SLTI] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }

                // SLTIU
                is("b011_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;

                    when(registers.io.out_a < decoder.io.immediate) {
                       registers.io.in := 1.U;
                    }.otherwise {
                       registers.io.in := 0.U;
                    }

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SLTIU] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }

                // XORI
                is("b100_0010011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a ^ decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[XORI] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }

                // ORI
                is("b110_0010011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a | decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[ORI] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }

                // ANDI
                is("b111_0010011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a & decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[ANDI] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }

                // ADD
                is("b0000000_000_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a + registers.io.out_b;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[ADD] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // SUB
                is("b0110000_000_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a - registers.io.out_b;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SUB] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // SLLI
                is("b001_0010011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a << decoder.io.immediate(5, 0);

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SLLI] Rs1: %d Rd: %d Shift: %d\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate(5, 0));
                }

                // SRLI and SRAI
                is("b101_0010011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    
                    when(decoder.io.immediate(10) === 1.U) { // SRAI
                        registers.io.in := (registers.io.out_a.asSInt >> decoder.io.immediate(5, 0)).asUInt;
                    }.otherwise { // SLAI
                        registers.io.in := registers.io.out_a >> decoder.io.immediate(5, 0);
                    }

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    when(decoder.io.immediate(10) === 1.U) { // SRAI
                        printf("[SRAI] Rs1: %d Rd: %d Shift: %d\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate(5, 0));
                    }.otherwise { // SLAI
                        printf("[SRLI] Rs1: %d Rd: %d Shift: %d\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate(5, 0));
                    }
                }

                // SLL
                is("b001_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a << registers.io.out_b(5, 0);

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SLL] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // SRL and SRA
                is("b101_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    
                    when(decoder.io.immediate(10) === 1.U) { // SRA
                        registers.io.in := (registers.io.out_a.asSInt >> decoder.io.immediate(5, 0)).asUInt;
                    }.otherwise { // SLA
                        registers.io.in := registers.io.out_a >> decoder.io.immediate(5, 0);
                    }

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    when(decoder.io.immediate(10) === 1.U) { // SRA
                        printf("[SRA] Rs1: %d Rd: %d Shift: %d\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate(5, 0));
                    }.otherwise { // SLA
                        printf("[SRL] Rs1: %d Rd: %d Shift: %d\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate(5, 0));
                    }
                }

                // SLT
                is("b010_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    
                    when(registers.io.out_a.asSInt < registers.io.out_b.asSInt) {
                        registers.io.in := 1.U;
                    }.otherwise {
                        registers.io.in := 0.U;
                    }

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SLT] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // SLTU
                is("b011_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    
                    when(registers.io.out_a < registers.io.out_b) {
                        registers.io.in := 1.U;
                    }.otherwise {
                        registers.io.in := 0.U;
                    }

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[SLTU] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // XOR
                is("b100_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a ^ registers.io.out_b;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[XOR] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // OR
                is("b110_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a | registers.io.out_b;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[OR] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // AND
                is("b111_0110011".U) { 
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a & registers.io.out_b;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[AND] Rs1: %d Rs2: %d Rd: %d\n", decoder.io.rs1, decoder.io.rs2, decoder.io.rd);
                }

                // JAL
                is("b1101111".U) { 
                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := program_pointer + 1.U;

                    program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    stage := 0.U;

                    printf("[JAL] Rd: %d Immediate: %b\n", decoder.io.rd, decoder.io.immediate);
                }

                // JALR
                is("b000_1100111".U) { 
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := program_pointer + 1.U;

                    program_pointer := (registers.io.out_a.zext + decoder.io.immediate.asSInt).asUInt;
                    stage := 0.U;

                    printf("[JALR] RS1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }
            }
        }

        when(stage === 2.U) {
            stage := 0.U;

            switch(operation_buffer) {
                // LW
                is("b010_0000011".U) {
                    registers.io.write_address := rd_buffer;
                    registers.io.write_enable := true.B;
                    registers.io.in := memory.readPorts(1).data;

                    program_pointer := program_pointer + 1.U;

                    printf("[LW] Rd: %d Data: %b\n", rd_buffer, memory.readPorts(1).data);
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