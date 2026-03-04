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

    val program_pointer = RegInit(0.U(32.W))

    val registers = Module(new Registers())
    registers.io.write_enable := false.B
    registers.io.write_address := 0.U(5.W)
    registers.io.read_address_a := 0.U(5.W)
    registers.io.read_address_b := 0.U(5.W)
    registers.io.read_address_c := 0.U(5.W)
    registers.io.in := 0.U(32.W)

    io.out_a := registers.io.out_a
    io.out_b := registers.io.out_b
    io.out_c := registers.io.out_c
    registers.io.read_address_a := io.read_address_a
    registers.io.read_address_b := io.read_address_b
    registers.io.read_address_c := io.read_address_c
    registers.io.write_enable := io.write_enable
    registers.io.write_address := io.write_address
    registers.io.in := io.in

    val alu = Module(new ALU())
    alu.io.operation := 0.U(3.W)
    alu.io.signed := false.B
    alu.io.a := 0.U(32.W)
    alu.io.b := 0.U(32.W)

    val memory = Module(new Memory())

    val decoder = Module(new Decoder())
    // 0 - Load Instruction   1 - Execute Instruction A   2 - Execute Instruction B
    val stage = RegInit(0.U(2.W));

    memory.io.write_1 := false.B;
    memory.io.read_1 := false.B;
    memory.io.address_1 := 0.U;
    memory.io.write_2 := false.B;
    memory.io.read_2 := false.B;
    memory.io.address_2 := 0.U;

    val operation_buffer = RegInit(0.U(17.W));
    val rs1_buffer = RegInit(0.U(5.W));
    val rs2_buffer = RegInit(0.U(5.W));
    val rd_buffer = RegInit(0.U(5.W));

    when(io.execute) {
        printf("\n");
        printf("Stage: %d\n", stage);

        when(stage =/= 0.U) {
            printf("Operation: %b\n", decoder.io.operation);
            printf("Program Pointer: %d\n", program_pointer);
            printf("Data 1: %b\n", memory.io.read_value_1);
            printf("Data 2: %b\n", memory.io.read_value_2);
            printf("Register 1: %b\n", registers.io.debug_1);
            printf("Register 2: %b\n", registers.io.debug_2);
            printf("Register 3: %b\n", registers.io.debug_3);
            printf("Register 4: %b\n", registers.io.debug_4);
            printf("Register 5: %b\n", registers.io.debug_5);
            printf("Register 6: %b\n", registers.io.debug_6);
            printf("Register 7: %b\n", registers.io.debug_7);
            printf("Register 8: %b\n", registers.io.debug_8);
            printf("Register 9: %b\n", registers.io.debug_9);
            printf("Register10: %b\n", registers.io.debug_10);
        }

        stage := stage + 1.U;

        when(stage === 0.U) {
          memory.io.read_1 := true.B
          memory.io.address_1 := program_pointer
        }

        when(stage === 1.U) {
            decoder.io.instruction := memory.io.read_value_1
            operation_buffer := decoder.io.operation;
            rs1_buffer := decoder.io.rs1;
            rs2_buffer := decoder.io.rs2;
            rd_buffer := decoder.io.rd;

            switch(decoder.io.operation) {
                // LB
                is("b000_0000011".U) {
                    registers.io.read_address_a := decoder.io.rs1

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    printf(
                      "[LB] Rs1: %d Immediate: %b\n",
                      decoder.io.rs1,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // LH
                is("b001_0000011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    printf(
                      "[LH] Rs1: %d Immediate: %b\n",
                      decoder.io.rs1,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // LW
                is("b010_0000011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    printf(
                      "[LW] Rs1: %d Immediate: %b\n",
                      decoder.io.rs1,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // LBU
                is("b100_0000011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    printf(
                      "[LBU] Rs1: %d Immediate: %b\n",
                      decoder.io.rs1,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // LHU
                is("b101_0000011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    printf(
                      "[LHU] Rs1: %d Immediate: %b\n",
                      decoder.io.rs1,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // SB
                is("b000_0100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    // memory.io.write := true.B
                    // memory.io.address := registers.io.out_a + decoder.io.immediate
                    // memory.io.write_value := registers.io.out_b(7, 0);

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SB] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // SH
                is("b001_0100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    // memory.io.write := true.B
                    // memory.io.address := registers.io.out_a + decoder.io.immediate
                    // memory.io.write_value := registers.io.out_b(15, 0);

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SH] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // SW
                is("b010_0100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    memory.io.read_1 := true.B
                    memory.io.address_1 := (registers.io.out_a + decoder.io.immediate) / 4.U;
                    memory.io.read_2 := true.B
                    memory.io.address_2 := (registers.io.out_a + decoder.io.immediate) / 4.U + 1.U;

                    // memory.io.write := true.B
                    // memory.io.address := registers.io.out_a + decoder.io.immediate
                    // memory.io.write_value := registers.io.out_b(31, 0);

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SW] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      registers.io.out_a + decoder.io.immediate
                    );
                }

                // LUI
                is("b0110111".U) {
                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := decoder.io.immediate;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[LUI] Rd: %d Immediate: %b\n",
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // AUIPC
                is("b0010111".U) {
                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := program_pointer + decoder.io.immediate;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[AUIPC] Rd: %d Immediate: %b\n",
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // ADDI
                is("b000_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a + decoder.io.immediate;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[ADDI] Rs1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // SLTI
                is("b010_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;

                    when(
                      registers.io.out_a.asSInt < decoder.io.immediate.asSInt
                    ) {
                        registers.io.in := 1.U;
                    }.otherwise {
                        registers.io.in := 0.U;
                    }

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SLTI] Rs1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
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

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SLTIU] Rs1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // XORI
                is("b100_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a ^ decoder.io.immediate;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[XORI] Rs1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // ORI
                is("b110_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a | decoder.io.immediate;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[ORI] Rs1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // ANDI
                is("b111_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a & decoder.io.immediate;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[ANDI] Rs1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // ADD
                is("b0000000_000_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a + registers.io.out_b;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[ADD] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // SUB
                is("b0110000_000_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a - registers.io.out_b;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SUB] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // SLLI
                is("b001_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a << decoder.io
                        .immediate(5, 0);

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SLLI] Rs1: %d Rd: %d Shift: %d\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate(5, 0)
                    );
                }

                // SRLI and SRAI
                is("b101_0010011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;

                    when(decoder.io.immediate(10) === 1.U) { // SRAI
                        registers.io.in := (registers.io.out_a.asSInt >> decoder.io
                            .immediate(5, 0)).asUInt;
                    }.otherwise { // SLAI
                        registers.io.in := registers.io.out_a >> decoder.io
                            .immediate(5, 0);
                    }

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    when(decoder.io.immediate(10) === 1.U) { // SRAI
                        printf(
                          "[SRAI] Rs1: %d Rd: %d Shift: %d\n",
                          decoder.io.rs1,
                          decoder.io.rd,
                          decoder.io.immediate(5, 0)
                        );
                    }.otherwise { // SLAI
                        printf(
                          "[SRLI] Rs1: %d Rd: %d Shift: %d\n",
                          decoder.io.rs1,
                          decoder.io.rd,
                          decoder.io.immediate(5, 0)
                        );
                    }
                }

                // SLL
                is("b001_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a << registers.io.out_b(
                      5,
                      0
                    );

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SLL] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // SRL and SRA
                is("b101_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;

                    when(decoder.io.immediate(10) === 1.U) { // SRA
                        registers.io.in := (registers.io.out_a.asSInt >> decoder.io
                            .immediate(5, 0)).asUInt;
                    }.otherwise { // SLA
                        registers.io.in := registers.io.out_a >> decoder.io
                            .immediate(5, 0);
                    }

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    when(decoder.io.immediate(10) === 1.U) { // SRA
                        printf(
                          "[SRA] Rs1: %d Rd: %d Shift: %d\n",
                          decoder.io.rs1,
                          decoder.io.rd,
                          decoder.io.immediate(5, 0)
                        );
                    }.otherwise { // SLA
                        printf(
                          "[SRL] Rs1: %d Rd: %d Shift: %d\n",
                          decoder.io.rs1,
                          decoder.io.rd,
                          decoder.io.immediate(5, 0)
                        );
                    }
                }

                // SLT
                is("b010_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;

                    when(
                      registers.io.out_a.asSInt < registers.io.out_b.asSInt
                    ) {
                        registers.io.in := 1.U;
                    }.otherwise {
                        registers.io.in := 0.U;
                    }

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SLT] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
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

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[SLTU] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // XOR
                is("b100_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a ^ registers.io.out_b;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[XOR] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // OR
                is("b110_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a | registers.io.out_b;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[OR] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // AND
                is("b111_0110011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := registers.io.out_a & registers.io.out_b;

                    program_pointer := program_pointer + 4.U;
                    stage := 0.U;

                    printf(
                      "[AND] Rs1: %d Rs2: %d Rd: %d\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.rd
                    );
                }

                // JAL
                is("b1101111".U) {
                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := program_pointer + 1.U;

                    program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    stage := 0.U;

                    printf(
                      "[JAL] Rd: %d Immediate: %b\n",
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // JALR
                is("b000_1100111".U) {
                    registers.io.read_address_a := decoder.io.rs1;

                    registers.io.write_address := decoder.io.rd;
                    registers.io.write_enable := true.B;
                    registers.io.in := program_pointer + 1.U;

                    program_pointer := (registers.io.out_a.zext + decoder.io.immediate.asSInt).asUInt;
                    stage := 0.U;

                    printf(
                      "[JALR] RS1: %d Rd: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rd,
                      decoder.io.immediate
                    );
                }

                // BEQ
                is("b000_1100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    when(registers.io.out_a === registers.io.out_b) {
                        program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    }.otherwise {
                        program_pointer := program_pointer + 4.U;
                    }

                    stage := 0.U;

                    printf(
                      "[BEQ] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.immediate
                    );
                }

                // BNEQ
                is("b001_1100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    when(registers.io.out_a =/= registers.io.out_b) {
                        program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    }.otherwise {
                        program_pointer := program_pointer + 4.U;
                    }

                    stage := 0.U;

                    printf(
                      "[BNEQ] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.immediate
                    );
                }

                // BLT
                is("b100_1100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    when(
                      registers.io.out_a.asSInt < registers.io.out_b.asSInt
                    ) {
                        program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    }.otherwise {
                        program_pointer := program_pointer + 4.U;
                    }

                    stage := 0.U;

                    printf(
                      "[BLT] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.immediate
                    );
                }

                // BGE
                is("b101_1100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    when(
                      registers.io.out_a.asSInt >= registers.io.out_b.asSInt
                    ) {
                        program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    }.otherwise {
                        program_pointer := program_pointer + 4.U;
                    }

                    stage := 0.U;

                    printf(
                      "[BGE] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.immediate
                    );
                }

                // BLTU
                is("b110_1100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    when(registers.io.out_a < registers.io.out_b) {
                        program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    }.otherwise {
                        program_pointer := program_pointer + 4.U;
                    }

                    stage := 0.U;

                    printf(
                      "[BLTU] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.immediate
                    );
                }

                // BGEU
                is("b111_1100011".U) {
                    registers.io.read_address_a := decoder.io.rs1;
                    registers.io.read_address_b := decoder.io.rs2;

                    when(registers.io.out_a >= registers.io.out_b) {
                        program_pointer := (program_pointer.zext + decoder.io.immediate.asSInt).asUInt;
                    }.otherwise {
                        program_pointer := program_pointer + 4.U;
                    }

                    stage := 0.U;

                    printf(
                      "[BGEU] Rs1: %d Rs2: %d Immediate: %b\n",
                      decoder.io.rs1,
                      decoder.io.rs2,
                      decoder.io.immediate
                    );
                }

                // FENCE - NoOP
                is("b000_0001111".U) {
                    program_pointer := program_pointer + 4.U;

                    stage := 0.U;

                    printf("[FENCE]");
                }
            }
        }

        when(stage === 2.U) {
            stage := 0.U;

            switch(operation_buffer) {
                // LB
                is("b000_0000011".U) {
                    registers.io.write_address := rd_buffer
                    registers.io.write_enable := true.B

                    val address = registers.io.out_a + decoder.io.immediate
                    val data = (memory.io.read_value_1 >> (address % 4.U)) | (memory.io.read_value_1 << (4.U - (address % 4.U)))

                    registers.io.in := Fill(24, data(7)) ## data(7, 0)

                    program_pointer := program_pointer + 4.U

                    printf(
                      "[LB] Rd: %d Data: %b\n",
                      rd_buffer,
                      Fill(24, data(7)) ## data(7, 0)
                    );
                }

                // LH
                is("b001_0000011".U) {
                    registers.io.write_address := rd_buffer;
                    registers.io.write_enable := true.B;

                    val address = registers.io.out_a + decoder.io.immediate
                    val data = (memory.io.read_value_1 >> (address % 4.U)) | (memory.io.read_value_1 << (4.U - (address % 4.U)))

                    registers.io.in := Fill(16, data(15)) ## data(15, 0)

                    program_pointer := program_pointer + 4.U;

                    printf(
                      "[LH] Rd: %d Data: %b\n",
                      rd_buffer,
                      Fill(16, data(15)) ## data(15, 0)
                    );
                }

                // LW
                is("b010_0000011".U) {
                    registers.io.write_address := rd_buffer;
                    registers.io.write_enable := true.B;
                    
                    val address = registers.io.out_a + decoder.io.immediate
                    val data = (memory.io.read_value_1 >> (address % 4.U)) | (memory.io.read_value_1 << (4.U - (address % 4.U)))

                    registers.io.in := data

                    program_pointer := program_pointer + 4.U;

                    printf(
                      "[LW] Rd: %d Data: %b\n",
                      rd_buffer,
                      data
                    );
                }

                // LBU
                is("b100_0000011".U) {
                    registers.io.write_address := rd_buffer;
                    registers.io.write_enable := true.B;

                    val address = registers.io.out_a + decoder.io.immediate
                    val data = (memory.io.read_value_1 >> (address % 4.U)) | (memory.io.read_value_1 << (4.U - (address % 4.U)))

                    registers.io.in := 0.U(24.W) ## data(7, 0)

                    program_pointer := program_pointer + 4.U;

                    printf(
                      "[LBU] Rd: %d Data: %b\n",
                      rd_buffer,
                      0.U(24.W) ## data(7, 0)
                    );
                }

                // LHU
                is("b101_0000011".U) {
                    registers.io.write_address := rd_buffer;
                    registers.io.write_enable := true.B;
                    
                    val address = registers.io.out_a + decoder.io.immediate
                    val data = (memory.io.read_value_1 >> (address % 4.U)) | (memory.io.read_value_1 << (4.U - (address % 4.U)))

                    registers.io.in := 0.U(16.W) ## data(15, 0)

                    program_pointer := program_pointer + 4.U;

                    printf(
                      "[LHU] Rd: %d Data: %b\n",
                      rd_buffer,
                      0.U(16.W) ## data(15, 0)
                    );
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
