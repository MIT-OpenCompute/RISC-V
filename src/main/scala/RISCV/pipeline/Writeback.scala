package RISCV
import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._
import chisel3.util._

class Writeback() extends Module {
  val io = IO(new Bundle {
    val instruction    = Input(Valid(new InstructionBundle()))
    val write_enable    = Output(Bool())
    val write_address   = Output(UInt(5.W))
    val write_val       = Output(UInt(32.W))

    val mem_write_data = Input(UInt(32.W))
    val mem_rd = Input(UInt(5.W))
    val mem_wen = Input(Bool())

    val reg_mem_rum = Output(UInt(32.W))

    val mem_write_enable    = Output(Bool())
    val mem_write_address   = Output(UInt(5.W))
    val mem_write_val       = Output(UInt(32.W))

    val stall = Input(Bool())
    

  })

  
  io.write_enable  := io.instruction.valid && io.instruction.bits.rd_wen
  io.write_address := io.instruction.bits.rd
  io.write_val     := Mux((io.mem_rd === io.instruction.bits.rd) && io.mem_wen, io.mem_write_data,io.instruction.bits.rd_val)

  io.mem_write_enable  := io.mem_wen
  io.mem_write_address := io.mem_rd
  io.mem_write_val     := io.mem_write_data


  val mem_rum = RegInit(0.U(32.W))
  val mem_rum_w = WireDefault(mem_rum)  
  val mem_rum_w2 =  WireDefault(mem_rum)  

  
  when(io.mem_wen){
    // printf("WRITING MEM: rd: %d   val: %d", io.mem_rd, io.mem_write_data )
  }
  when(io.instruction.valid || io.mem_write_enable){
    when(io.instruction.valid && !io.instruction.bits.hbp && !io.stall &&io.instruction.bits.opcode === "b0000011".U){
      mem_rum_w := mem_rum | (1.U(32.W) << io.instruction.bits.rd)
    }
    mem_rum_w2 := mem_rum_w
    when(io.mem_write_enable){
      mem_rum_w2 := mem_rum_w & ~(1.U(32.W) << io.mem_rd)
    }
  }
  mem_rum := mem_rum_w2
  io.reg_mem_rum := mem_rum_w

  // --- double-outstanding-load detector ---
val issuing_load = io.instruction.valid && !io.instruction.bits.hbp &&
                    io.instruction.bits.opcode === "b0000011".U
val rd_already_pending = mem_rum(io.instruction.bits.rd)
val double_outstanding = issuing_load && rd_already_pending && (io.instruction.bits.rd =/= 0.U)
val lastPC = RegNext(io.instruction.bits.pc)
when(double_outstanding && !io.stall ) {
  printf("!!! DOUBLE-OUTSTANDING LOAD: rd=%d pc=%x mem_rum=%b lastpc %x(existing load into this reg not yet resolved)\n",
    io.instruction.bits.rd, io.instruction.bits.pc, mem_rum,lastPC)
}

//check if its the current oSne in the stage if it is replace, otherwise pipe through

}