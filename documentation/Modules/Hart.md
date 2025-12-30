## Overview
The Hart is the "hardware execution unit". This is the complex stuff comes together to actually execute the instructions. Right now it's stored in the `main.scala` file but we'll probably want to move it over to `hart.scala` later.
## Instruction Staging
Right now for simplicity, we're not doing anything too complex with how we're loading instructions. The way it works currently is a simple stage counter.
### Stage 0
The instruction at the program pointer is requested from memory.
### Stage 1
The instruction is available from memory and the decoder is now emitting the decoded instruction. We store the decoded instructions in buffers for access in later stages. Most instructions can simply execute in this stage and then set the stage counter back to 0 and increase the program pointer.
## Stage 2
Currently only the `LW` instruction uses stage 2, since in stage 1 `LW` requests the info from memory and then in stage 2 it takes the value from memory and writes it to the register.
## Implemented Instructions
### Main Integer Instructions
- [x] LUI
- [x] AUIPC
- [x] ADDI
- [x] SLTI
- [x] SLTIU
- [x] XORI
- [x] ORI
- [x] ANDI
- [x] SLLI
- [x] SRLI
- [x] SRAI
- [x] ADD
- [x] SUB
- [x] SLL
- [x] SLT
- [x] SLTU
- [x] XOR
- [x] SRL
- [x] SRA
- [x] OR
- [x] AND
- [ ] LB
- [ ] LH
- [x] LW
- [ ] LBU
- [ ] LHU
- [ ] SB
- [ ] SH
- [x] SW
- [x] JAL
- [ ] JALR
- [ ] BEQ
- [ ] BNE
- [ ] BLT
- [ ] BGE
- [ ] BLTU
- [ ] BGEU
### Other Integer Instructions
- [ ] FENCE
- [ ] FENCEI
- [ ] ECALL
- [ ] EBREAK
- [ ] SRET
- [ ] MRET
- [ ] WFI
- [ ] SFENCEVMA
- [ ] CSRRW
- [ ] CSRRS
- [ ] CSRRC
- [ ] CSRRWI
- [ ] CSRRSI
- [ ] CSRRCI