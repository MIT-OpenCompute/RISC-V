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
	addi	sp,sp,-112
	sw	ra,108(sp)
	sw	s0,104(sp)
	addi	s0,sp,112
	li	a5,16384
	sw	a5,-92(s0)
	li	a5,120
	sw	a5,-96(s0)
	li	a5,120
	sw	a5,-100(s0)
	li	a5,160
	sw	a5,-20(s0)
	li	a5,120
	sw	a5,-24(s0)
	li	a5,1
	sw	a5,-28(s0)
	li	a5,1
	sw	a5,-32(s0)
	sw	zero,-36(s0)
	j	.L3
.L4:
	lw	a5,-36(s0)
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,255
	sw	a4,0(a5)
	lw	a4,-36(s0)
	li	a5,77824
	addi	a5,a5,-1344
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,255
	sw	a4,0(a5)
	lw	a4,-36(s0)
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,8
	addi	a5,a5,4
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,255
	sw	a4,0(a5)
	lw	a4,-36(s0)
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,8
	addi	a5,a5,1276
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,255
	sw	a4,0(a5)
	lw	a5,-36(s0)
	addi	a5,a5,1
	sw	a5,-36(s0)
.L3:
	lw	a4,-36(s0)
	li	a5,319
	ble	a4,a5,.L4
.L37:
	li	a5,-2
	sw	a5,-40(s0)
	j	.L5
.L8:
	li	a5,-2
	sw	a5,-44(s0)
	j	.L6
.L7:
	lw	a4,-24(s0)
	lw	a5,-44(s0)
	add	a4,a4,a5
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,6
	mv	a4,a5
	lw	a5,-20(s0)
	add	a4,a4,a5
	lw	a5,-40(s0)
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	sw	zero,0(a5)
	lw	a5,-44(s0)
	addi	a5,a5,1
	sw	a5,-44(s0)
.L6:
	lw	a4,-44(s0)
	li	a5,2
	ble	a4,a5,.L7
	lw	a5,-40(s0)
	addi	a5,a5,1
	sw	a5,-40(s0)
.L5:
	lw	a4,-40(s0)
	li	a5,2
	ble	a4,a5,.L8
	li	a5,-2
	sw	a5,-48(s0)
	j	.L9
.L12:
	li	a5,-20
	sw	a5,-52(s0)
	j	.L10
.L11:
	lw	a4,-96(s0)
	lw	a5,-52(s0)
	add	a4,a4,a5
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,6
	addi	a4,a5,20
	lw	a5,-48(s0)
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	sw	zero,0(a5)
	lw	a5,-52(s0)
	addi	a5,a5,1
	sw	a5,-52(s0)
.L10:
	lw	a4,-52(s0)
	li	a5,20
	ble	a4,a5,.L11
	lw	a5,-48(s0)
	addi	a5,a5,1
	sw	a5,-48(s0)
.L9:
	lw	a4,-48(s0)
	li	a5,2
	ble	a4,a5,.L12
	li	a5,-2
	sw	a5,-56(s0)
	j	.L13
.L16:
	li	a5,-20
	sw	a5,-60(s0)
	j	.L14
.L15:
	lw	a4,-100(s0)
	lw	a5,-60(s0)
	add	a4,a4,a5
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,6
	addi	a4,a5,299
	lw	a5,-56(s0)
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	sw	zero,0(a5)
	lw	a5,-60(s0)
	addi	a5,a5,1
	sw	a5,-60(s0)
.L14:
	lw	a4,-60(s0)
	li	a5,20
	ble	a4,a5,.L15
	lw	a5,-56(s0)
	addi	a5,a5,1
	sw	a5,-56(s0)
.L13:
	lw	a4,-56(s0)
	li	a5,2
	ble	a4,a5,.L16
	lw	a4,-20(s0)
	lw	a5,-28(s0)
	add	a5,a4,a5
	sw	a5,-20(s0)
	lw	a4,-24(s0)
	lw	a5,-32(s0)
	add	a5,a4,a5
	sw	a5,-24(s0)
	lw	a4,-20(s0)
	li	a5,313
	bne	a4,a5,.L17
	li	a5,-1
	sw	a5,-28(s0)
.L17:
	lw	a4,-20(s0)
	li	a5,6
	bne	a4,a5,.L18
	li	a5,1
	sw	a5,-28(s0)
.L18:
	lw	a4,-24(s0)
	li	a5,233
	bne	a4,a5,.L19
	li	a5,-1
	sw	a5,-32(s0)
.L19:
	lw	a4,-24(s0)
	li	a5,6
	bne	a4,a5,.L20
	li	a5,1
	sw	a5,-32(s0)
.L20:
	lw	a4,-20(s0)
	li	a5,25
	bgt	a4,a5,.L21
	lw	a4,-20(s0)
	li	a5,14
	ble	a4,a5,.L21
	lw	a5,-96(s0)
	addi	a5,a5,25
	lw	a4,-24(s0)
	bgt	a4,a5,.L21
	lw	a5,-96(s0)
	addi	a5,a5,-25
	lw	a4,-24(s0)
	blt	a4,a5,.L21
	li	a5,1
	sw	a5,-28(s0)
.L21:
	lw	a4,-20(s0)
	li	a5,304
	bgt	a4,a5,.L22
	lw	a4,-20(s0)
	li	a5,293
	ble	a4,a5,.L22
	lw	a5,-100(s0)
	addi	a5,a5,25
	lw	a4,-24(s0)
	bgt	a4,a5,.L22
	lw	a5,-100(s0)
	addi	a5,a5,-25
	lw	a4,-24(s0)
	blt	a4,a5,.L22
	li	a5,-1
	sw	a5,-28(s0)
.L22:
	li	a5,-2
	sw	a5,-64(s0)
	j	.L23
.L26:
	li	a5,-2
	sw	a5,-68(s0)
	j	.L24
.L25:
	lw	a4,-24(s0)
	lw	a5,-68(s0)
	add	a4,a4,a5
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,6
	mv	a4,a5
	lw	a5,-20(s0)
	add	a4,a4,a5
	lw	a5,-64(s0)
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,255
	sw	a4,0(a5)
	lw	a5,-68(s0)
	addi	a5,a5,1
	sw	a5,-68(s0)
.L24:
	lw	a4,-68(s0)
	li	a5,2
	ble	a4,a5,.L25
	lw	a5,-64(s0)
	addi	a5,a5,1
	sw	a5,-64(s0)
.L23:
	lw	a4,-64(s0)
	li	a5,2
	ble	a4,a5,.L26
	li	a5,-2
	sw	a5,-72(s0)
	j	.L27
.L30:
	li	a5,-20
	sw	a5,-76(s0)
	j	.L28
.L29:
	lw	a4,-96(s0)
	lw	a5,-76(s0)
	add	a4,a4,a5
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,6
	addi	a4,a5,20
	lw	a5,-72(s0)
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,3
	sw	a4,0(a5)
	lw	a5,-76(s0)
	addi	a5,a5,1
	sw	a5,-76(s0)
.L28:
	lw	a4,-76(s0)
	li	a5,20
	ble	a4,a5,.L29
	lw	a5,-72(s0)
	addi	a5,a5,1
	sw	a5,-72(s0)
.L27:
	lw	a4,-72(s0)
	li	a5,2
	ble	a4,a5,.L30
	li	a5,-2
	sw	a5,-80(s0)
	j	.L31
.L34:
	li	a5,-20
	sw	a5,-84(s0)
	j	.L32
.L33:
	lw	a4,-100(s0)
	lw	a5,-84(s0)
	add	a4,a4,a5
	mv	a5,a4
	slli	a5,a5,2
	add	a5,a5,a4
	slli	a5,a5,6
	addi	a4,a5,299
	lw	a5,-80(s0)
	add	a5,a4,a5
	slli	a5,a5,2
	lw	a4,-92(s0)
	add	a5,a4,a5
	li	a4,224
	sw	a4,0(a5)
	lw	a5,-84(s0)
	addi	a5,a5,1
	sw	a5,-84(s0)
.L32:
	lw	a4,-84(s0)
	li	a5,20
	ble	a4,a5,.L33
	lw	a5,-80(s0)
	addi	a5,a5,1
	sw	a5,-80(s0)
.L31:
	lw	a4,-80(s0)
	li	a5,2
	ble	a4,a5,.L34
	sw	zero,-88(s0)
	j	.L35
.L36:
 #APP
# 92 "./programs/hello.c" 1
	nop
# 0 "" 2
 #NO_APP
	lw	a5,-88(s0)
	addi	a5,a5,1
	sw	a5,-88(s0)
.L35:
	lw	a4,-88(s0)
	li	a5,8192
	addi	a5,a5,-193
	ble	a4,a5,.L36
	j	.L37
	.size	main, .-main
	.ident	"GCC: (xPack GNU RISC-V Embedded GCC x86_64) 15.2.0"
	.section	.note.GNU-stack,"",@progbits
