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
	addi	sp,sp,-32
	sw	ra,28(sp)
	sw	s0,24(sp)
	addi	s0,sp,32
	sw	zero,-24(s0)
	sw	zero,-20(s0)
.L3:
	lw	a5,-20(s0)
	addi	a5,a5,1
	sw	a5,-20(s0)
	lw	a5,-20(s0)
	slli	a5,a5,14
	mv	a4,a5
	lw	a5,-24(s0)
	add	a5,a5,a4
	lw	a4,-20(s0)
	andi	a4,a4,0xff
	sb	a4,0(a5)
	j	.L3
	.size	main, .-main
	.ident	"GCC: (xPack GNU RISC-V Embedded GCC x86_64) 15.2.0"
	.section	.note.GNU-stack,"",@progbits
