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
    i_alu := 0.U;
    i_alu = Wire(UInt(width.W));
    m_alu = Wire(UInt(width.W));


    val m_in_a = io.a;
    val m_in_b = io.b;
    val mult_out = Wire(64.W);
    mult_out := m_in_a * m_in_b;
    switch(io.func3){
        is("b000".U,"b001".U,"b010".U,"b011".U){
            
            val m_in_a = Mux(io.func3(1) && io.func3(0),io.a, io.a.asSInt)
            val m_in_b = Mux(io.func3(1) ,io.b, io.b.asSInt)
            val mult_out = Wire(64.W)

            mult_out := m_in_a * m_in_b;
            m_alu := Mux(io.func3 === "b000".U, mult_out(31,0), mult_out(63,32))
        }
    }
    
    switch(io.func3){
        is("b000".U){
            i_alu := io.a + io.b
            m_alu := mult_out(31,0)

        }
        //SLLI
        is("b001".U){
            i_alu := io.a << io.b(4,0) 
            m_in_a := io.a.asSInt;
            m_in_b := io.a.asSInt;
            m_alu := mult_out(63,32)
        }
        //SLTI
        is("b010".U){
            i_alu := Mux(io.a.asSInt < io.b.asSInt, 1.U, 0.U)
        }
        //SLTIU
        is("b011".U){
            i_alu := Mux(io.a < io.b, 1.U, 0.U)
        }
        //XOR
        is("b100".U){
            i_alu := io.a ^ io.b;
        }
        //SRAI, SRLI
        is("b101".U) {
            when(io.func7(5)) {
                i_alu := (io.a.asSInt >> io.b(4, 0)).asUInt
            }.otherwise {
                i_alu := io.a >> io.b(4, 0)
            }
        }
        // OR
        is("b110".U) {
            i_alu := io.a | io.b
        }
        //AND
        is("b111".U) {
            i_alu := io.a & io.b
        }

    }
    io.output = Mux(func7(0), m_alu, i_alu);

  }
