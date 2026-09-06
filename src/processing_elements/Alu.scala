import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Alu() extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val out = Output(new InstructionBundle())
        val out_valid = Output(Bool())

        val flush = Input(Bool())

        val ready = Output(Bool())

        val lsu_broadcast_valid = Input(Bool())
        val broadcast_free_valid = Output(Bool())
        val broadcast_free_register = Output(UInt(5.W))
        val broadcast_free_value = Output(UInt(32.W))
    })

    val out = RegInit(0.U.asTypeOf(new InstructionBundle))
    io.out := out
    val out_valid = RegInit(false.B)
    io.out_valid := out_valid

    val ready = io.next_ready && !io.lsu_broadcast_valid

    io.ready := ready

    io.broadcast_free_valid := false.B
    io.broadcast_free_register := 0.U
    io.broadcast_free_value := 0.U

    when(ready) {
        out_valid := false.B
    }

    when(ready && io.valid) {
        out := io.instruction
        out_valid := true.B

        switch(io.instruction.opcode) {
            // LUI
            is("b0110111".U) {
                out.rd_value := io.instruction.immediate

                io.broadcast_free_valid := io.instruction.rd =/= 0.U
                io.broadcast_free_register := io.instruction.rd
                io.broadcast_free_value := io.instruction.immediate
            }

            // AUIPC
            is("b0010111".U) {
                out.rd_value := io.instruction.instruction_pointer + io.instruction.immediate

                io.broadcast_free_valid := io.instruction.rd =/= 0.U
                io.broadcast_free_register := io.instruction.rd
                io.broadcast_free_value := io.instruction.instruction_pointer + io.instruction.immediate
            }

            
            is("b0110011".U,"b0010011".U) {
                val is_r  = io.instruction.opcode(5)
                val shamt = Mux(is_r, io.instruction.rs2_value(4, 0), io.instruction.immediate(4, 0))
                val alt = io.instruction.func7(5)
                val sub_alt  = alt & is_r   
                val a = io.instruction.rs1_value
                val b = Mux(is_r,io.instruction.rs2_value,io.instruction.immediate )

                val result = WireDefault(0.U(32.W))

                switch(io.instruction.func3) {
                    is("b000".U) { result := Mux(sub_alt, a - b, a + b) } //Add/Sub
                    is("b001".U) { result := (a << shamt)(31, 0) } //SLL
                    is("b010".U) { result := (a.asSInt < b.asSInt).asUInt } //SLT
                    is("b011".U) { result := (a < b).asUInt }//SLTU
                    is("b100".U) { result := a ^ b }//XOR
                    is("b101".U) { result := Mux(alt, (a.asSInt >> shamt).asUInt, a >> shamt) } //SRL SRA
                    is("b110".U) { result := a | b } //OR
                    is("b111".U) { result := a & b } //AND
                }

                out.rd_value := result
                io.broadcast_free_valid := io.instruction.rd =/= 0.U
                io.broadcast_free_register := io.instruction.rd
                io.broadcast_free_value := result
            }
            
        }
    }

    when(io.flush) {
        out := 0.U.asTypeOf(new InstructionBundle)
        out_valid := false.B
    }
}