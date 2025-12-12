package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import upickle.default

object InstructionFormat extends ChiselEnum {
  val R, I, S, B, U, J = Value
}

/**
* @param width Bit width (default: 32 bits)
*/
class Decoder(val width: Int = 32) extends Module {
	val io = IO(new Bundle {
		val instruction = Input(UInt(32.W));
		val operation = Output(UInt(17.W));
		val rs1 = Output(UInt(5.W));
		val rs2 = Output(UInt(5.W));
		val rd = Output(UInt(5.W));
		val immediate = Output(UInt(32.W));
	})

	io.rs1 := io.instruction(19, 15);
	io.rs2 := io.instruction(24, 20);
	io.rd := io.instruction(11, 7);

	val format = Wire(InstructionFormat());
	format := InstructionFormat.R;

	val opcode = io.instruction(6,0);
	val func3 = io.instruction(14,12);
	val func7 = io.instruction(31,25);

	switch(opcode) {
		is(0b0110111.U) { format := InstructionFormat.U; } // lui
		is(0b0010111.U) { format := InstructionFormat.U; } // auipc
		is(0b0010011.U) { format := InstructionFormat.I; } // addi, slti, sltiu, xori, ori, andi, slli, srli, srai
		is(0b0110011.U) { format := InstructionFormat.R; } // add, sub, sll, slt, sltu, xor, srl, sra, or, and
		is(0b0001111.U) { format := InstructionFormat.I; } // fence, fence.i
		is(0b1110011.U) { format := InstructionFormat.S; } // ecall, ebreak, sret, mret, wfi, sfence.vma | the format for these is not within the standard formats but we can basically achieve the same thing with S type and then using the immediate, rs2, and rs1 to differentiate the calls. Also technically sret, mret, and wfi come from the privelleged spec but we'll just include them here.
		is(0b0000011.U) { format := InstructionFormat.I; } // lb, lh, lw, lbu, lhu
		is(0b0100011.U) { format := InstructionFormat.S; } // sb, sh, sw
		is(0b1101111.U) { format := InstructionFormat.U; } // jal
		is(0b1100111.U) { format := InstructionFormat.I; } // jalr
		is(0b1100011.U) { format := InstructionFormat.B; } // beq, bne, blt, bge, bltu, bgeu
	}

	io.operation := 0.U;
	io.immediate := 0.U;

	switch(format){
		is(InstructionFormat.R) {
			io.operation := io.instruction(31,25) ## io.instruction(14,12) ## io.instruction(6,0);
		}
		is(InstructionFormat.I) {
			io.operation := io.instruction(14,12) ## io.instruction(6,0);
			io.immediate := Fill(21, io.instruction(31,31)) ## io.instruction(30,20);
		}
		is(InstructionFormat.S) {
			io.operation := io.instruction(14,12) ## io.instruction(6,0);
			io.immediate := io.instruction(31,31) ## 0.U(20.W) ## io.instruction(31,25) ## io.instruction(11,7);
		}
		is(InstructionFormat.B) {
			io.operation := io.instruction(14,12) ## io.instruction(6,0);
			io.immediate := io.instruction(31,31) ## 0.U(19.W) ## io.instruction(7,7) ## io.instruction(31,25) ## io.instruction(11,8) ## 0.U(1.W);
		}
		is(InstructionFormat.U) {
			io.operation := io.instruction(6,0);
			io.immediate := io.instruction(31,12) ## 0.U(12.W);
		}
		is(InstructionFormat.J) {
			io.operation := io.instruction(6,0);
			io.immediate := io.instruction(31,31) ## 0.U(11.W) ## io.instruction(19,12) ## io.instruction(20,20) ## io.instruction(30,21) ## 0.U(1.W);
		}
	}
}

object Decoder extends App {
	ChiselStage.emitSystemVerilogFile(
		new Decoder(),
		firtoolOpts = Array(
			"-disable-all-randomization",
			"-strip-debug-info",
			"-default-layer-specialization=enable"
		),
		args = Array("--target-dir", "generated")
	)
}