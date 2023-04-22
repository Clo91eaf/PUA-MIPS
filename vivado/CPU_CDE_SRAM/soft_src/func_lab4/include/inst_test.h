// file name: inst_test.h
#include <cpu_cde.h>

#define TEST_LUI(in_a, ref_base) \
    lui   a0, ref_base; \
    lui   t0, in_a;  \
    NOP4; \
    addu  a0, a0, t1; \
    addu  t1, t1, t2; \
    NOP4; \
    bne   t0, a0, inst_error; \
    nop

/* 2 */
#define TEST_ADDU(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    addu v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 3 */
#define TEST_ADDIU(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (v1, ref); \
    NOP4; \
    addiu v0, t0, in_b; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 4 */
#define TEST_BEQ(in_a, in_b, back_flag, front_flag, b_flag_ref, f_flag_ref) \
    LI (t4, back_flag); \
    LI (t5, front_flag); \
    lui v0, 0x0; \
    lui v1, 0x0; \
    b 2000f; \
    nop; \
1000:; \
    LI (v0, back_flag); \
    beq t1, t0, 3000f; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
2000:; \
    LI (t0, in_a); \
    LI (t1, in_b); \
    NOP4; \
    beq t0, t1, 1000b; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
3000:; \
    LI (v1, front_flag); \
4000:; \
    LI (s5, b_flag_ref); \
    LI (s6, f_flag_ref); \
    NOP4 ; \
    bne v0, s5, inst_error; \
    nop; \
    bne v1, s6, inst_error; \
    nop

/* 5 */
#define TEST_BNE(in_a, in_b, back_flag, front_flag, b_flag_ref, f_flag_ref) \
    LI (t4, back_flag); \
    LI (t5, front_flag); \
    lui v0, 0x0; \
    lui v1, 0x0; \
    b 2000f; \
    nop; \
1000:; \
    LI (v0, back_flag); \
    bne t1, t0, 3000f; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
2000:; \
    LI (t0, in_a); \
    LI (t1, in_b); \
    NOP4; \
    bne t0, t1, 1000b; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
3000:; \
    LI (v1, front_flag); \
4000:; \
    LI (s5, b_flag_ref); \
    LI (s6, f_flag_ref); \
    NOP4 ; \
    bne v0, s5, inst_error; \
    nop; \
    bne v1, s6, inst_error; \
    nop

/* 6 */
#define TEST_LW(data, base_addr, offset, offset_align, ref) \
    LI (t1, data); \
    LI (t0, base_addr); \
    LI (v1, ref); \
    sw t1, offset_align(t0); \
    addiu a0, t0, 4; \
    addiu a1, t0, -8; \
    NOP4; \
    sw a0, offset_align(a0); \
    sw a1, offset_align(a1); \
    lw v0, offset(t0); \
    lw a2, offset_align(a0); \
    lw a0, offset_align(a1); \
    lw a2, offset_align(a1); \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 7 */
#define TEST_OR(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    or v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 8 */
#define TEST_SLT(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    slt v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 11 */
#define TEST_SLL(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (v1, ref); \
    NOP4; \
    sll v0, t0, in_b; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 12 */
#define TEST_SW(data, base_addr, offset, offset_align, ref) \
    LI (t1, data); \
    LI (t0, base_addr); \
    LI (v1, ref); \
    sw t1, offset(t0); \
    addiu a0, t0, 4; \
    addiu a1, t0, -4; \
    NOP4; \
    sw a0, offset(a0); \
    sw a1, offset(a1); \
    lw v0, offset_align(t0); \
    lw a2, offset(a0); \
    lw a0, offset(a1); \
    lw a2, offset(a1); \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 14 */
#define TEST_JAL(back_flag, front_flag, b_flag_ref, f_flag_ref) \
    addu s7, zero, $31; \
    LI (t4, back_flag); \
    LI (t5, front_flag); \
    lui v0, 0x0; \
    lui v1, 0x0; \
    jal 2000f; \
    nop; \
1000:; \
    NOP4; \
    addu a1, ra, zero; \
    LI (v0, back_flag); \
1001:; \
    jal 3000f; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
2000:; \
    nop; \
    nop; \
    nop; \
    nop; \
    addu a0, ra, zero; \
    jal 1000b; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
3000:; \
    NOP4; \
    addu a2, ra, zero; \
    LI (v1, front_flag); \
4000:; \
    NOP4; \
    addu $31, zero, s7; \
    LI (t5, b_flag_ref); \
    LI (t4, f_flag_ref); \
    bne v0, t5, inst_error; \
    nop; \
    addiu a2, a2, 0x28; \
    bne v1, t4, inst_error; \
    nop; \
    NOP4; \
    bne a2, a1, inst_error; \
    nop

/* 15 */
#define TEST_JR(back_flag, front_flag, b_flag_ref, f_flag_ref) \
    addu s7, zero, $31; \
    LI (t4, back_flag); \
    LI (t5, front_flag); \
    lui v0, 0x0; \
    lui v1, 0x0; \
    jal 1f; \
    nop;    \
1:  ;       \
    nop;    \
    nop;    \
    nop;    \
    addiu t0, $31, 7*4; \
    addiu t1, $31, 22*4; \
    b 2000f; \
    nop; \
1000:; \
    LI (v0, back_flag); \
    jr t1; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
2000:; \
    jr t0; \
    nop; \
    b 4000f; \
    nop; \
    nop; \
3000:; \
    LI (v1, front_flag); \
4000:; \
    LI (s5, b_flag_ref); \
    LI (s6, f_flag_ref); \
    addu $31, zero, s7; \
    bne v0, s5, inst_error; \
    nop; \
    bne v1, s6, inst_error; \
    nop

/* 16 */
#define TEST_BEQ_DS(op, dest, ...) \
    addiu s5, zero, 0x1; \
    NOP4; \
    beq s5, zero, 1000f; \
    op  dest , ##__VA_ARGS__; \
    op  s6 , ##__VA_ARGS__; \
    NOP4; \
    bne dest, s6, inst_error; \
    nop; \
    beq s5, s5, 2000f; \
    op  s7 , ##__VA_ARGS__; \
1000: ; \
    b   inst_error; \
    nop;            \
2000: ; \
    NOP4; \
    bne s7, s6, inst_error; \
    nop

/* 17 */
#define TEST_BNE_DS(op, dest, ...) \
    addiu s5, zero, 0x1; \
    NOP4; \
    bne s5, s5, 1000f; \
    op  dest, ##__VA_ARGS__; \
    op  s6, ##__VA_ARGS__; \
    NOP4; \
    bne dest, s6, inst_error; \
    nop; \
    bne s5, zero, 2000f; \
    op  s7, ##__VA_ARGS__; \
1000: ; \
    b   inst_error; \
    nop;            \
2000: ; \
    NOP4; \
    bne s7, s6, inst_error; \
    nop

/* 19 */
#define TEST_JAL_DS(op, dest, ...) \
    addu s7, zero, $31; \
    op  s6, ##__VA_ARGS__; \
    jal 2000f; \
    op  dest, ##__VA_ARGS__; \
1000: ; \
    b   inst_error; \
    nop;            \
2000: ; \
    NOP4; \
    addu $31, zero, s7; \
    bne dest, s6, inst_error; \
    NOP4

/* 20 */
#define TEST_JR_DS(op, dest, ...) \
    addu s7, zero, $31; \
    jal 1f; \
    nop;    \
1:  ;       \
    nop;    \
    nop;    \
    nop;    \
    addiu  s5, $31, 12*4; \
    nop; \
    nop; \
    nop; \
    op  s6, ##__VA_ARGS__; \
    jr  s5; \
    op  dest, ##__VA_ARGS__; \
1000: ; \
    b   inst_error; \
    nop;            \
2000: ; \
    NOP4; \
    addu $31, zero, s7; \
    bne dest, s6, inst_error; \
    NOP4

/* 24 */
#define TEST_SUBU(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    subu v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 25 */
#define TEST_SLTU(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    sltu v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 26 */
#define TEST_AND(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    and v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 28 */
#define TEST_NOR(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    nor v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 30 */
#define TEST_XOR(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (t1, in_b); \
    LI (v1, ref); \
    NOP4; \
    xor v0, t0, t1; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 33 */
#define TEST_SRA(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (v1, ref); \
    NOP4; \
    sra v0, t0, in_b; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop

/* 35 */
#define TEST_SRL(in_a, in_b, ref) \
    LI (t0, in_a); \
    LI (v1, ref); \
    NOP4; \
    srl v0, t0, in_b; \
    NOP4; \
    bne v0, v1, inst_error; \
    nop
