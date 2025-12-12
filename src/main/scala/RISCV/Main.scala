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

    val memory = SRAM(1024, UInt(32.W), 2, 1, 0);

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

    val stage = RegInit(0.U(2.W)); // 0 - Load Instruction   1 - Execute Instruction A   2 - Execute Instruction B

    memory.readPorts(0).enable := io.execute && stage === 0.U;
    memory.readPorts(0).address := program_pointer;

    memory.readPorts(1).enable := false.B;
    memory.readPorts(1).address := 0.U;

    memory.writePorts(0).enable := io.debug_write;
    memory.writePorts(0).address := io.debug_write_address;
    memory.writePorts(0).data := io.debug_write_data;

    val operation_buffer = RegInit(0.U(17.W));
    val rd_buffer = RegInit(0.U(5.W));

    when(io.execute) {
        printf("\n");
        printf("Stage: %d\n", stage);
        printf("Operation: %b\n", decoder.io.operation);
        printf("Program Pointer: %d\n", program_pointer);
        printf("Data: %b\n", memory.readPorts(0).data);
        printf("Register 1: %b\n", regFile.io.debug_1);

        stage := stage + 1.U;

        when(stage === 1.U) {
            operation_buffer := decoder.io.operation;
            rd_buffer := decoder.io.rd;

            switch(decoder.io.operation) {
                // LW
                is("b010_00000_11".U) {
                    regFile.io.read_addr_A := decoder.io.rs1;

                    memory.readPorts(1).enable := true.B;
                    memory.readPorts(1).address := regFile.io.out_A + decoder.io.immediate;
                    
                    printf("[LW] Rs1: %d Immediate: %b\n", decoder.io.rs1, regFile.io.out_A + decoder.io.immediate);
                }
                
                // LUI
                is("b01101_11".U(7.W)) { 
                    regFile.io.write_addr := decoder.io.rd;
                    regFile.io.write_enable := true.B;
                    regFile.io.in := decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[LUI] Rd: %d Immediate: %b\n", decoder.io.rd, decoder.io.immediate);
                }

                // ADDI
                is("b000_00100_11".U(7.W)) {
                    regFile.io.read_addr_A := decoder.io.rs1;

                    regFile.io.write_addr := decoder.io.rd;
                    regFile.io.write_enable := true.B;
                    regFile.io.in := regFile.io.out_A + decoder.io.immediate;

                    program_pointer := program_pointer + 1.U;
                    stage := 0.U;

                    printf("[ADDI] Rs1: %d Rd: %d Immediate: %b\n", decoder.io.rs1, decoder.io.rd, decoder.io.immediate);
                }
            }
        }

        when(stage === 2.U) {
            stage := 0.U;

            switch(operation_buffer) {
                is("b010_00000_11".U) {
                    regFile.io.write_addr := rd_buffer;
                    regFile.io.write_enable := true.B;
                    regFile.io.in := memory.readPorts(1).data;

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
