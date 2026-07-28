package RISCV
import chisel3._
import _root_.circt.stage.ChiselStage
import scala.math._
import chisel3.util._ 

class Read() extends Module {
  val io = IO(new Bundle {
    val instruction = Input(Valid(new InstructionBundle()))
    val register_read_a = Output(UInt(5.W))
    val register_read_b = Output(UInt(5.W))
    val register_value_a = Input(UInt(32.W))
    val register_value_b = Input(UInt(32.W))
    val next_instruction = Output(Valid(new InstructionBundle()))
    val flush = Input(Bool())
    val stall = Input(Bool())
    val rum  = Input(UInt(32.W))
    val raw_hazard_stall = Output(Bool())
  })

  val rum_t = io.rum & "hFFFFFFFE".U
  val raw_hazard = io.instruction.valid && (rum_t(io.instruction.bits.rs1) || rum_t(io.instruction.bits.rs2) || (io.instruction.bits.rd_wen && rum_t(io.instruction.bits.rd)))
  io.raw_hazard_stall := raw_hazard

  io.register_read_a := io.instruction.bits.rs1
  io.register_read_b := io.instruction.bits.rs2

  val bundle = RegInit(0.U.asTypeOf(new InstructionBundle()))
  val valid  = RegInit(false.B)
  val bundle_w = WireDefault(bundle)
  when(io.flush) {
      valid := false.B
  }.elsewhen(io.stall) {
  }.elsewhen(raw_hazard) {
      valid := false.B
  }.otherwise {
      bundle := io.instruction.bits
      valid := io.instruction.valid
      bundle.rs1_val := io.register_value_a
      bundle.rs2_val := io.register_value_b
  }

  // when(!io.stall){
  //   bundle_w.rs1_val := io.register_value_a
  //   bundle_w.rs2_val := io.register_value_b
  // }
  io.next_instruction.bits := bundle_w


  io.next_instruction.valid := valid

  // printf("READ STAGE  ASDFNAKFNDA rs1 %d rs1val %d\n",io.instruction.bits.rs1,io.next_instruction.bits.rs1_val)
  
  
}