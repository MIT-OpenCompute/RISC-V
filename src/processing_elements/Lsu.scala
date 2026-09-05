package RISCV


import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class Lsu() extends Module {
    val io = IO(new Bundle {
        val next_ready = Input(Bool())

        val instruction = Input(new InstructionBundle())
        val valid = Input(Bool())

        val out = Output(new InstructionBundle())
        val out_valid = Output(Bool())

        val flush = Input(Bool())

        val ready = Output(Bool())

        val broadcast_free_valid = Output(Bool())
        val broadcast_free_register = Output(UInt(5.W))
        val broadcast_free_value = Output(UInt(32.W))

        val dcache_req = Output(new MemReq)
        val dcache_start = Output(Bool())
        val dcache_ready = Input(Bool())
        val dcache_valid = Input(Bool())
        val dcache_data = Input(UInt(32.W))
        val dcache_rd = Output(UInt(5.W))
        val dcache_wen = Output(Bool())

        val dcache_rd_out = Input(UInt(5.W))
        val dcache_wen_out = Input(Bool())
    })

    val out = RegInit(0.U.asTypeOf(new InstructionBundle))
    io.out := out
    val out_valid = RegInit(false.B)
    io.out_valid := out_valid

    when(io.next_ready && out_valid) {
        out_valid := false.B
    }

    val waiting_on_read = RegInit(false.B)
    val waiting_on_instruction = RegInit(0.U.asTypeOf(new InstructionBundle))
    val ignore_next_response = RegInit(false.B)

    io.ready := io.next_ready && !waiting_on_read && io.dcache_ready

    io.broadcast_free_valid := io.dcache_rd_out =/=0.U && io.dcache_valid
    io.broadcast_free_register := io.dcache_rd_out
    io.broadcast_free_value := io.dcache_data

    io.dcache_start := false.B
    io.dcache_wen := false.B
    io.dcache_rd := 0.U

    val addr = io.instruction.rs1_value + io.instruction.immediate

    io.dcache_req.address := addr
    io.dcache_req.read := true.B
    io.dcache_req.write := false.B
    io.dcache_req.write_data := 0.U
    io.dcache_req.op := MuxLookup(io.instruction.func3, MemOp.LW)(Seq(
    "b000".U -> MemOp.LB,
    "b001".U -> MemOp.LH,
    "b010".U -> MemOp.LW,
    "b100".U -> MemOp.LBU,  
    "b101".U -> MemOp.LHU   
                ))
    // printf("Dcache D  %d waiting read %b io.dcacheready %b\n",  io.dcache_data, waiting_on_read,io.dcache_ready)

    when(io.next_ready && io.valid && io.dcache_ready) {
        out := io.instruction
        
            switch(io.instruction.opcode) {
                is("b0000011".U) {
                    io.dcache_start := io.dcache_ready
                    io.dcache_rd := io.instruction.rd
                    io.dcache_wen := true.B
                    out_valid :=  false.B
                    waiting_on_read := true.B
                
                }

                is("b0100011".U) {

                    out_valid := true.B
                    out.rd := (io.instruction.rs1_value.zext + io.instruction.immediate.asSInt).asUInt
                    out.rd_value := io.instruction.rs2_value
                }
            }


        
     
    


    }

    when(waiting_on_read && io.dcache_valid){
        out.rd_value := io.dcache_data 
        out_valid :=  true.B
        waiting_on_read := false.B
        // printf("herehereherehere\n")
    }
    when(io.flush) {
        out := 0.U.asTypeOf(new InstructionBundle)
        out_valid := false.B
        waiting_on_read := false.B

        ignore_next_response := waiting_on_read
    }
}
