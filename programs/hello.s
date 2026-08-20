	.file	"hello.c"
	.option nopic
	.attribute arch, "rv32i2p1"
	.attribute unaligned_access, 0
	.attribute stack_align, 16
	.text
	.align	2
	.globl	_start
	.type	_start, @function
_start:
 #APP
# 2 "./programs/hello.c" 1
	li sp, 0x4000
call main
loop: j loop

# 0 "" 2
 #NO_APP
	nop
	.size	_start, .-_start
	.align	2
	.globl	main
	.type	main, @function
main:
	addi	sp,sp,-32 // 16
	sw	ra,28(sp) // 20
	sw	s0,24(sp) // 24
	addi	s0,sp,32 // 28
	li	a5,16384 // 32
	sw	a5,-20(s0) // 36
	lw	a5,-20(s0) // 40
	li	a4,255 // 44
	sw	a4,0(a5) // 48
	li	a5,0 // 52
	mv	a0,a5 // 56
	lw	ra,28(sp) // 60
	lw	s0,24(sp) // 64
	addi	sp,sp,32 // 68
	jr	ra // 72
	.size	main, .-main
	.ident	"GCC: (xPack GNU RISC-V Embedded GCC x86_64) 15.2.0"
	.section	.note.GNU-stack,"",@progbits
