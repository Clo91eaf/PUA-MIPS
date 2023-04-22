#include <asm.h>
#include <regdef.h>
#include <inst_test.h>

LEAF(n81_ti_ex_test)
    .set noreorder
    addiu s0, s0, 1
    li    t0, 0x800d0000
    li    s2, 0x08
    sw    s2, 0(t0)
##clear cause.TI, status.EXL
    mtc0  zero, c0_compare
    lui   s7,0x0040
	mtc0  s7, c0_status
    nop
    lui   s7, 0x0008      #add ex, ref return value.
###test inst
 ##1
    la    s4, 1f
    TEST_TI_EX(0x45df00e0, 0x45df00e0+0x120)
    bne s2, s7, inst_error
    nop
 ##2
    li    s2, 0x08
    la    s4, 1f
    sw    t0, 4(t0)
    sw    s4, 4(t0) 
    TEST_TI_EX(0xb70e3e49, 0xb70e3e49+0x2)
    sw    s4, 0(t0) 
    lw    t1, 4(t0)
    bne t1, s4, inst_error
    nop
    bne s2, s7, inst_error
    nop
    li    s2, 0x08
    sw    s2, 0(t0)
 ##3
    li    s2, 0x08
    la    s4, 1f
    mthi  t0
    divu  zero, t0, s0
    TEST_TI_EX(0xfffffffc, 0x22)
    mfhi  t1
    beq   t1, t0, inst_error
    nop
    bne s2, s7, inst_error
    nop
 ##4
    li    s2, 0x08
    la    s4, 1f
    TEST_TI_EX(0x5a8d78ce, 0x5a8d78ce+0x77)
    divu  zero, s0, t0
    bne s2, s7, inst_error
    nop
 ##5
    li    s2, 0x08
    la    s4, 1f
    mtlo  t0
    multu t0, s0
    TEST_TI_EX(0x7bd1ac60, 0x7bd1ac60+0x11)
    mfhi  t1
    beq   t1, t0, inst_error
    nop
    bne s2, s7, inst_error
    nop
 ##6
    li    s2, 0x08
    la    s4, 1f
    TEST_TI_EX(0x7e6265cc, 0x7e6265cc+0x1)
    multu t0, s2
    bne s2, s7, inst_error
    nop
 ##7
    li    s2, 0x08
    la    s4, 1f
    mtc0  s2, c0_epc
    TEST_TI_EX(0x9fc55510, 0x9fc55510+0x0)
    mtc0 t0, c0_epc
    bne s2, s7, inst_error
    nop
###score ++
    addiu s3, s3, 1
###output (s0<<24)|s3
inst_error:  
    sll t1, s0, 24
    or t0, t1, s3 
    sw t0, 0(s1)
    jr ra
    nop
END(n81_ti_ex_test)
