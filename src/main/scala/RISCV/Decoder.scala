package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage
import upickle.default

object InstructionFormat extends ChiselEnum {
  val R, I, S, B, U, J = Value
}

object Opcode extends ChiselEnum {
  val ADDI  = Value(0b0010011.U)
  val AUIPC = Value(0b0010111.U)
  val LUI   = Value(0b0110111.U)
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

	switch(opcode) {
		is(Opcode.LUI.asUInt) { format := InstructionFormat.U; }
		is(Opcode.AUIPC.asUInt) { format := InstructionFormat.U; }
		is(Opcode.ADDI.asUInt) { format := InstructionFormat.I; }
	}

	io.operation := 0.U;
	io.immediate := 0.U;

	switch(format){
		is(InstructionFormat.R) {
			io.operation := io.instruction(31,25) ## io.instruction(14,12) ## io.instruction(6,0);
		}
		is(InstructionFormat.I) {
			io.operation := io.instruction(14,12) ## io.instruction(6,0);
			io.immediate := io.instruction(31,31) ## 0.U(20.W) ## io.instruction(30,20);
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