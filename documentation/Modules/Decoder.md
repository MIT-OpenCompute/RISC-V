## Overview
The decoder will decode the immediates, registers, and opcode from the instruction bytes. Right now it just takes in the 32 bit wide instruction since we aren't supporting the compressed or variable length instructions yet. 
## Input Bundle
```scala
val io = IO(new Bundle {
    val instruction = Input(UInt(32.W));

    val operation = Output(UInt(17.W));
    val rs1 = Output(UInt(5.W));
    val rs2 = Output(UInt(5.W));
    val rd = Output(UInt(5.W));
    val immediate = Output(UInt(32.W));
})
```
### Instruction
This is the combined instruction bytes in little [endian](https://en.wikipedia.org/wiki/Endianness) order.
### Operation
The emitted opcode. Consists of opcode + funct3 + funct7 if they are used in the instruction.
### Rs1 Rs2 and Rd
These are the source and destination registers. Since they always exist in the same spot in RV32I, They'll always emit these bytes, however not all instruction use them, so any instructions not using them will just ignore the values.
### Immediate
This will be the complicated part of the decoder. We'll basically just emit the full adjusted immediate depending on the instruction format. See [[Instruction Format]]