package RISCV

import chisel3._
import chisel3.util._
import _root_.circt.stage.ChiselStage


class ALU(val width: Int = 32) extends Module {
    val io = IO(new Bundle {
        val func7 = Input(UInt(7.W));
        val func3 = Input(UInt(3.W));
        val a = Input(UInt(width.W)); // First operand
        val b = Input(UInt(width.W)); // Second operand
        val output = Output(UInt(width.W)); // Result of the operation
    })

    io.output := 0.U;
    switch(io.func3){
        is("b000".U){
            io.output := io.a + io.b
        }
        //SLLI
        is("b001".U){
            io.output := io.a << io.b(4,0) 
        }
        //SLTI
        is("b010".U){
            io.output := Mux(io.a.asSInt < io.b.asSInt, 1.U, 0.U)
        }
        //SLTIU
        is("b011".U){
            io.output := Mux(io.a < io.b, 1.U, 0.U)
        }
        //XOR
        is("b100".U){
            io.output := io.a ^ io.b;
        }
        //SRAI, SRLI
        is("b101".U) {
            when(io.func7(5)) {
                io.output := (io.a.asSInt >> io.b(4, 0)).asUInt
            }.otherwise {
                io.output := io.a >> io.b(4, 0)
            }
        }
        // OR
        is("b110".U) {
            io.output := io.a | io.b
        }
        //AND
        is("b111".U) {
            io.output := io.a & io.b
        }

    }

  }
