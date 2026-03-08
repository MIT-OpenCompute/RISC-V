package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

/**
  * @param width Bit width (default: 32 bits)
  * 
  * The Arithmetic Logic Unit (ALU) for RISC-V
  * Supports: Addition, Multiplication, Comparison, Bitwise operations
  * 
  * I/O:
    * operation: 4-bit operation code
    * signed: boolean to indicate if operands are signed (Only used for comparisons)
    * a: first operand
    * b: second operand
    * output: result of the operation
  *
  * Operation Codes:
    * 0000: Addition
    * 0001: Multiplication
    * 0010: Comparison (outputs 3 bits: gt, eq, lt)
    * 0011: Bitwise AND
    * 0100: Bitwise OR
    * 0101: Bitwise XOR
    * 0110: Bitwise NOT (outputs NOT a)
    * 0111: Logical shift left
    * 1000: Logical shift right
    * 1001: Arithmetic shift right
  *
  */
class ALU(val width: Int = 32) extends Module {
    val io = IO(new Bundle {
        val operation = Input(UInt(4.W)); // 4-bit operation code
        val signed = Input(Bool()); // Treat operands as signed if true
        val a = Input(UInt(width.W)); // First operand
        val b = Input(UInt(width.W)); // Second operand
        val output = Output(UInt(width.W)); // Result of the operation
    })

    io.output := 0.U;

    switch(io.operation) {
      is("b0000".U) {
        io.output := io.a + io.b; // Addition
      }
      is("b0001".U) {
        io.output := io.a * io.b; // Multiplication
      }
      is("b0010".U) {
        when(io.signed) {
          val a_s = io.a.asSInt
          val b_s = io.b.asSInt
          val gt_s = a_s > b_s
          val eq_s = a_s === b_s
          val lt_s = a_s < b_s
          io.output := Cat(0.U((width - 3).W), gt_s, eq_s, lt_s);
        } .otherwise {
          val gt = io.a > io.b; // Comparison
          val eq = io.a === io.b;
          val lt = io.a < io.b;

          io.output := Cat(0.U((width - 3).W), gt, eq, lt);
        }
      }
      is("b0011".U) {
        io.output := io.a & io.b; // Bitwise AND
      }
      is("b0100".U) {
        io.output := io.a | io.b; // Bitwise OR
      }
      is("b0101".U) {
        io.output := io.a ^ io.b; // Bitwise XOR
      }
      is("b0110".U) {
        io.output := ~io.a; // Bitwise NOT
      }
      is("b0111".U) {
        io.output := io.a << io.b(4,0); // Logical shift left
      }
      is("b1000".U) {
        io.output := io.a >> io.b(4,0); // Logical shift right
      }
      is("b1001".U) {
        io.output := (io.a.asSInt >> io.b(4,0)).asUInt // Arithmetic shift right
      }
    }
}

object ALU extends App {
  ChiselStage.emitSystemVerilogFile(
    new ALU(32),
    firtoolOpts = Array(
      "-disable-all-randomization",
      "-strip-debug-info",
      "-default-layer-specialization=enable"
    ),
    args = Array("--target-dir", "generated")
  )
}