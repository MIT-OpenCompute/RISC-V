package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage

class MemReqWrapped extends Bundle {
  val req = new MemReq
  val rd = UInt(5.W)
  val wen = Bool()
}

class DCacheQueue( lineWidth: Int = 128) extends Module {
    val io = IO(new Bundle {
        val req = Input(new MemReqWrapped)
        val start = Input(Bool())
        val ready = Output(Bool())
        val valid = Output(Bool())
        val data = Output(UInt(32.W))
        val rd = Output(UInt(5.W))
        val wen = Output(Bool())


        val dcache_req = Output(new MemReq)
        val dcache_start = Output(Bool())
        val dcache_ready = Input(Bool())
        val dcache_valid = Input(Bool())
        val dcache_data = Input(UInt(32.W))

    })

  val enq = Wire(Decoupled(new MemReqWrapped))
  val deq = Queue(enq, entries = 64, flow = true)

  enq.bits  := io.req
  enq.valid := io.start

  
  io.ready := enq.ready 
  io.dcache_start := false.B
  io.valid := false.B
  io.rd    := 0.U
  io.wen   := false.B
  deq.ready := false.B


  val current_req = RegInit(0.U.asTypeOf(new MemReqWrapped))
  io.dcache_req:= deq.bits.req
  when(!deq.valid) {
    // printf("Queue ENMPTY")
  }
  when(io.dcache_ready && deq.valid){
    deq.ready := true.B
    current_req := deq.bits
    io.dcache_start := true.B
            when(deq.bits.rd === 8.U){
            // printf("\n\n8 popped 8 popped\n\n")
          }
        // printf("DCACHEQUEUE DEQ: rd=%d wen=%d addr=%d data=%d\n", deq.bits.rd, deq.bits.wen, deq.bits.req.address, deq.bits.req.write_data)
    
  }
  io.data:= 0.U
  when(io.dcache_valid){
    io.valid := true.B
    io.data := io.dcache_data
    io.rd := current_req.rd
    io.wen := current_req.wen
        // printf("DCACHEQUEUE RETURN: rd=%d wen=%d data=%d\n", current_req.rd, current_req.wen, io.dcache_data)    
  }


}