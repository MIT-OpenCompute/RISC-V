package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

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

	io.operation := 0.U;
	
	io.immediate := 0.U;
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