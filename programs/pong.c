/* hopper-cpu self-test.
 *
 * Every test body is `asm volatile` on purpose: written in plain C, GCC at
 * -O2/-O3 constant-folds the arithmetic at compile time and the CPU never
 * executes anything. The asm blocks force real instructions to issue.
 *
 * Reports each failure over UART, then fills the screen green (all pass)
 * or red (any fail).
 */

#define IMG_W 320
#define IMG_H 240

#define VGA    ((volatile unsigned int*)0x10000000)
#define DBGCH  ((volatile unsigned int*)0x70000000)
#define DBGNUM ((volatile unsigned int*)0x70000008)
#define UARTTX ((volatile unsigned char*)0x8000034)

#define GREEN 0x00FF00
#define RED   0xFF0000

__attribute__((naked)) void _start(void) {
    __asm__ volatile(
        "li sp, 0x7000000\n"
        "call main\n"
        "loop: j loop\n"
    );
}

void debug_log(char* character) {
    while (*character != '\0') {
        *DBGCH = *character;
        *UARTTX = *character;
        character++;
    }
}

void debug_num(unsigned int value) {
    *DBGNUM = value;
}

/* ------------------------------------------------------------------ */

static volatile int failures = 0;
static volatile int ran = 0;

static void check(char* name, unsigned int got, unsigned int want) {
    ran++;
    if (got != want) {
        failures++;
        debug_log("[FAIL] ");
        debug_log(name);
        debug_log(" got=");
        debug_num(got);
        debug_log(" want=");
        debug_num(want);
        debug_log("\n");
    } else {
        debug_log("[ ok ] ");
        debug_log(name);
        debug_log(" = ");
        debug_num(got);
        debug_log("\n");
    }
}

static void section(char* name) {
    debug_log("\n--- ");
    debug_log(name);
    debug_log(" ---\n");
}

/* Scratch memory for load/store tests. */
static unsigned int   wbuf[16];
static unsigned short hbuf[16];
static unsigned char  bbuf[16];

/* ================================================================== */
/* 1. ALU immediate                                                    */
/* ================================================================== */
static void test_alu_imm(void) {
    unsigned int r;
    unsigned int a = 0x12345678;

    asm volatile("addi %0, %1, 0x123" : "=r"(r) : "r"(a));
    check("addi", r, 0x12345678 + 0x123);

    asm volatile("addi %0, %1, -1" : "=r"(r) : "r"(a));
    check("addi.neg", r, 0x12345677);

    asm volatile("andi %0, %1, 0xFF" : "=r"(r) : "r"(a));
    check("andi", r, 0x78);

    asm volatile("ori %0, %1, 0x7FF" : "=r"(r) : "r"(a));
    check("ori", r, 0x12345678 | 0x7FF);

    asm volatile("xori %0, %1, 0x7FF" : "=r"(r) : "r"(a));
    check("xori", r, 0x12345678 ^ 0x7FF);

    asm volatile("slli %0, %1, 4" : "=r"(r) : "r"(a));
    check("slli", r, 0x23456780);

    asm volatile("srli %0, %1, 4" : "=r"(r) : "r"(a));
    check("srli", r, 0x01234567);

    asm volatile("srai %0, %1, 4" : "=r"(r) : "r"(0x80000000u));
    check("srai", r, 0xF8000000);

    asm volatile("slti %0, %1, 100" : "=r"(r) : "r"(-5));
    check("slti", r, 1);

    asm volatile("sltiu %0, %1, 100" : "=r"(r) : "r"(-5));
    check("sltiu", r, 0);
}

/* ================================================================== */
/* 2. ALU register-register                                            */
/* ================================================================== */
static void test_alu_reg(void) {
    unsigned int r;
    unsigned int a = 0xF0F0F0F0, b = 0x0FF00FF0;

    asm volatile("add %0, %1, %2" : "=r"(r) : "r"(a), "r"(b));
    check("add", r, 0xF0F0F0F0u + 0x0FF00FF0u);

    asm volatile("sub %0, %1, %2" : "=r"(r) : "r"(a), "r"(b));
    check("sub", r, 0xF0F0F0F0u - 0x0FF00FF0u);

    asm volatile("and %0, %1, %2" : "=r"(r) : "r"(a), "r"(b));
    check("and", r, 0x00F000F0);

    asm volatile("or %0, %1, %2"  : "=r"(r) : "r"(a), "r"(b));
    check("or",  r, 0xFFF0FFF0);

    asm volatile("xor %0, %1, %2" : "=r"(r) : "r"(a), "r"(b));
    check("xor", r, 0xFF00FF00);

    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(1u), "r"(31u));
    check("sll", r, 0x80000000);

    asm volatile("srl %0, %1, %2" : "=r"(r) : "r"(0x80000000u), "r"(31u));
    check("srl", r, 1);

    asm volatile("sra %0, %1, %2" : "=r"(r) : "r"(0x80000000u), "r"(31u));
    check("sra", r, 0xFFFFFFFF);

    asm volatile("slt %0, %1, %2"  : "=r"(r) : "r"(-1), "r"(1));
    check("slt",  r, 1);

    asm volatile("sltu %0, %1, %2" : "=r"(r) : "r"(-1), "r"(1));
    check("sltu", r, 0);

    /* Shift amount must use only the low 5 bits. */
    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(1u), "r"(33u));
    check("sll.mod32", r, 2);
}

/* ================================================================== */
/* 3. x0 semantics -- reads zero, writes discarded                     */
/* ================================================================== */
static void test_x0(void) {
    unsigned int r;

    asm volatile("mv %0, x0" : "=r"(r));
    check("x0.read", r, 0);

    /* Write to x0, then read it again. */
    asm volatile("addi x0, %0, 5\n mv %1, x0" : "=r"(r) : "r"(0x1234));
    check("x0.write.discarded", r, 0);

    /* x0 as a source operand. */
    asm volatile("add %0, x0, %1" : "=r"(r) : "r"(0x55));
    check("x0.as.src", r, 0x55);

    /* nop is `addi x0, x0, 0` -- must not poison the scoreboard. */
    asm volatile("nop\n nop\n nop\n addi %0, x0, 876" : "=r"(r));
    check("x0.after.nops", r, 876);

    /* Long run of nops followed by an x0 reader. */
    asm volatile(
        "nop\n nop\n nop\n nop\n nop\n nop\n nop\n nop\n"
        "addi %0, x0, 1234"
        : "=r"(r));
    check("x0.many.nops", r, 1234);
}

/* ================================================================== */
/* 4. Dependency chains / WAW / WAR                                    */
/* ================================================================== */
static void test_hazards(void) {
    unsigned int r;

    /* Back-to-back RAW chain. */
    asm volatile(
        "mv   t0, %1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "addi t0, t0, 1\n"
        "mv   %0, t0\n"
        : "=r"(r) : "r"(100u) : "t0");
    check("raw.chain8", r, 108);

    /* WAW: same destination written twice, second must win. */
    asm volatile(
        "addi t0, x0, 111\n"
        "addi t0, x0, 222\n"
        "mv   %0, t0\n"
        : "=r"(r) :: "t0");
    check("waw", r, 222);

    /* WAR: read then overwrite the source. */
    asm volatile(
        "addi t0, x0, 7\n"
        "add  t1, t0, t0\n"
        "addi t0, x0, 99\n"
        "mv   %0, t1\n"
        : "=r"(r) :: "t0", "t1");
    check("war", r, 14);

    /* Interleaved independent chains -- stresses the scoreboard. */
    asm volatile(
        "addi t0, x0, 1\n"
        "addi t1, x0, 2\n"
        "addi t2, x0, 3\n"
        "add  t0, t0, t1\n"
        "add  t1, t1, t2\n"
        "add  t2, t2, t0\n"
        "add  t0, t0, t2\n"
        "add  %0, t0, t1\n"
        : "=r"(r) :: "t0", "t1", "t2");
    /* t0=3 t1=5 t2=6 t0=9 -> 9+5 = 14 */
    check("interleaved.chains", r, 14);

    /* Same register as both sources. */
    asm volatile("add %0, %1, %1" : "=r"(r) : "r"(21u));
    check("same.src.twice", r, 42);
}

/* ================================================================== */
/* 5. Word load/store                                                  */
/* ================================================================== */
static void test_word_mem(void) {
    unsigned int r;

    wbuf[0] = 0x11223344;
    wbuf[1] = 0x55667788;
    wbuf[2] = 0x99AABBCC;

    asm volatile("lw %0, 0(%1)" : "=r"(r) : "r"(&wbuf[0]) : "memory");
    check("lw.0", r, 0x11223344);

    asm volatile("lw %0, 4(%1)" : "=r"(r) : "r"(&wbuf[0]) : "memory");
    check("lw.offset4", r, 0x55667788);

    asm volatile("sw %1, 12(%2)\n lw %0, 12(%2)"
                 : "=&r"(r) : "r"(0xCAFEBABEu), "r"(&wbuf[0]) : "memory");
    check("sw.lw.roundtrip", r, 0xCAFEBABE);

    /* Negative offset. */
    asm volatile("lw %0, -4(%1)" : "=r"(r) : "r"(&wbuf[2]) : "memory");
    check("lw.negoffset", r, 0x55667788);
}

/* ================================================================== */
/* 6. Store-to-load hazard window                                      */
/*                                                                     */
/* Stores commit at ROB retire, so a load issued too soon after a store */
/* to the same address can read stale memory. Varying the gap shows how */
/* wide that window is.                                                */
/* ================================================================== */
#define SL_TEST(NAME, PAD)                                        \
    do {                                                          \
        unsigned int got;                                         \
        wbuf[4] = 0;                                              \
        asm volatile("sw %1, 16(%2)\n" PAD "lw %0, 16(%2)\n"      \
                     : "=&r"(got)                                 \
                     : "r"(0xA5A5A5A5u), "r"(&wbuf[0])            \
                     : "memory");                                 \
        check(NAME, got, 0xA5A5A5A5);                             \
    } while (0)

static void test_store_load_window(void) {
    SL_TEST("st.ld.gap0",  "");
    SL_TEST("st.ld.gap1",  "nop\n");
    SL_TEST("st.ld.gap2",  "nop\n nop\n");
    SL_TEST("st.ld.gap4",  "nop\n nop\n nop\n nop\n");
    SL_TEST("st.ld.gap8",  "nop\n nop\n nop\n nop\n nop\n nop\n nop\n nop\n");
    SL_TEST("st.ld.gap16", "nop\n nop\n nop\n nop\n nop\n nop\n nop\n nop\n"
                           "nop\n nop\n nop\n nop\n nop\n nop\n nop\n nop\n");

    /* Two stores then two loads, different addresses. */
    unsigned int a, b;
    asm volatile(
        "sw %2, 0(%4)\n"
        "sw %3, 4(%4)\n"
        "lw %0, 0(%4)\n"
        "lw %1, 4(%4)\n"
        : "=&r"(a), "=&r"(b)
        : "r"(0x11111111u), "r"(0x22222222u), "r"(&wbuf[8])
        : "memory");
    check("st.st.ld.ld.a", a, 0x11111111);
    check("st.st.ld.ld.b", b, 0x22222222);

    /* Store, load, and immediately use the loaded value. */
    unsigned int c;
    asm volatile(
        "sw %1, 0(%2)\n"
        "lw t0, 0(%2)\n"
        "addi %0, t0, 1\n"
        : "=&r"(c) : "r"(1000u), "r"(&wbuf[12]) : "memory", "t0");
    check("st.ld.use", c, 1001);
}

/* ================================================================== */
/* 7. Sub-word load/store and sign extension                           */
/* ================================================================== */
static void test_subword(void) {
    unsigned int r;

    bbuf[0] = 0x7F; bbuf[1] = 0x80; bbuf[2] = 0xFF; bbuf[3] = 0x01;

    asm volatile("lb %0, 0(%1)" : "=r"(r) : "r"(&bbuf[0]) : "memory");
    check("lb.pos", r, 0x7F);

    asm volatile("lb %0, 1(%1)" : "=r"(r) : "r"(&bbuf[0]) : "memory");
    check("lb.neg", r, 0xFFFFFF80);

    asm volatile("lbu %0, 1(%1)" : "=r"(r) : "r"(&bbuf[0]) : "memory");
    check("lbu", r, 0x80);

    asm volatile("lb %0, 2(%1)" : "=r"(r) : "r"(&bbuf[0]) : "memory");
    check("lb.ff", r, 0xFFFFFFFF);

    /* Byte stores at each offset within a word. */
    wbuf[0] = 0;
    asm volatile("sb %0, 0(%1)" :: "r"(0xAAu), "r"(&wbuf[0]) : "memory");
    asm volatile("sb %0, 1(%1)" :: "r"(0xBBu), "r"(&wbuf[0]) : "memory");
    asm volatile("sb %0, 2(%1)" :: "r"(0xCCu), "r"(&wbuf[0]) : "memory");
    asm volatile("sb %0, 3(%1)" :: "r"(0xDDu), "r"(&wbuf[0]) : "memory");
    asm volatile("lw %0, 0(%1)" : "=r"(r) : "r"(&wbuf[0]) : "memory");
    check("sb.all4", r, 0xDDCCBBAA);

    hbuf[0] = 0x8000; hbuf[1] = 0x1234;

    asm volatile("lh %0, 0(%1)" : "=r"(r) : "r"(&hbuf[0]) : "memory");
    check("lh.neg", r, 0xFFFF8000);

    asm volatile("lhu %0, 0(%1)" : "=r"(r) : "r"(&hbuf[0]) : "memory");
    check("lhu", r, 0x8000);

    wbuf[0] = 0;
    asm volatile("sh %0, 0(%1)" :: "r"(0x1122u), "r"(&wbuf[0]) : "memory");
    asm volatile("sh %0, 2(%1)" :: "r"(0x3344u), "r"(&wbuf[0]) : "memory");
    asm volatile("lw %0, 0(%1)" : "=r"(r) : "r"(&wbuf[0]) : "memory");
    check("sh.both", r, 0x33441122);
}

/* ================================================================== */
/* 8. Branches                                                         */
/* ================================================================== */
static void test_branches(void) {
    unsigned int r;

#define BR_TEST(NAME, OP, A, B, EXPECT)                     \
    do {                                                    \
        asm volatile(                                       \
            OP " %1, %2, 1f\n"                              \
            "addi %0, x0, 0\n"                              \
            "j 2f\n"                                        \
            "1: addi %0, x0, 1\n"                           \
            "2:\n"                                          \
            : "=&r"(r) : "r"((unsigned)(A)), "r"((unsigned)(B)));  \
        check(NAME, r, EXPECT);                             \
    } while (0)

    BR_TEST("beq.taken",     "beq",  5, 5, 1);
    BR_TEST("beq.nottaken",  "beq",  5, 6, 0);
    BR_TEST("bne.taken",     "bne",  5, 6, 1);
    BR_TEST("bne.nottaken",  "bne",  5, 5, 0);
    BR_TEST("blt.taken",     "blt", -1, 1, 1);
    BR_TEST("blt.nottaken",  "blt",  1,-1, 0);
    BR_TEST("bge.taken",     "bge",  1,-1, 1);
    BR_TEST("bge.eq",        "bge",  5, 5, 1);
    BR_TEST("bltu.taken",    "bltu", 1,-1, 1);
    BR_TEST("bltu.nottaken", "bltu",-1, 1, 0);
    BR_TEST("bgeu.taken",    "bgeu",-1, 1, 1);

    /* Branch on a value produced by the immediately preceding op. */
    asm volatile(
        "addi t0, %1, -5\n"
        "beq  t0, x0, 1f\n"
        "addi %0, x0, 0\n"
        "j 2f\n"
        "1: addi %0, x0, 1\n"
        "2:\n"
        : "=&r"(r) : "r"(5u) : "t0");
    check("br.on.prev.result", r, 1);

    /* Branch on a freshly loaded value. */
    wbuf[0] = 42;
    asm volatile(
        "lw   t0, 0(%1)\n"
        "addi t1, x0, 42\n"
        "beq  t0, t1, 1f\n"
        "addi %0, x0, 0\n"
        "j 2f\n"
        "1: addi %0, x0, 1\n"
        "2:\n"
        : "=&r"(r) : "r"(&wbuf[0]) : "t0", "t1", "memory");
    check("br.on.loaded", r, 1);

    /* Two branches back to back. */
    asm volatile(
        "addi %0, x0, 0\n"
        "beq  x0, x0, 1f\n"
        "addi %0, x0, 99\n"
        "1: beq x0, x0, 2f\n"
        "addi %0, x0, 88\n"
        "2: addi %0, %0, 7\n"
        : "=&r"(r));
    check("br.back.to.back", r, 7);

#undef BR_TEST
}

/* ================================================================== */
/* 9. Jumps and link registers                                         */
/* ================================================================== */
static unsigned int leaf_fn(unsigned int x) { return x * 3 + 1; }

static void test_jumps(void) {
    unsigned int r;

    /* jal writes the return address and the target executes. */
    asm volatile(
        "jal  t0, 1f\n"
        "addi %0, x0, 0\n"
        "j 2f\n"
        "1: addi %0, x0, 1\n"
        "2:\n"
        : "=&r"(r) :: "t0");
    check("jal.target", r, 1);

    /* jalr through a register. */
    asm volatile(
        "la   t0, 1f\n"
        "jalr x0, 0(t0)\n"
        "addi %0, x0, 0\n"
        "j 2f\n"
        "1: addi %0, x0, 1\n"
        "2:\n"
        : "=&r"(r) :: "t0");
    check("jalr.target", r, 1);

    /* Real call/return. */
    check("call.return", leaf_fn(10), 31);

    /* Nested calls. */
    check("call.nested", leaf_fn(leaf_fn(2)), 22);
}

/* ================================================================== */
/* 10. lui / auipc                                                     */
/* ================================================================== */
static void test_upper(void) {
    unsigned int r;

    asm volatile("lui %0, 0x12345" : "=r"(r));
    check("lui", r, 0x12345000);

    asm volatile("lui %0, 0x10000" : "=r"(r));
    check("lui.vga", r, 0x10000000);

    /* auipc + addi must produce a working address. */
    unsigned int* p;
    asm volatile("la %0, wbuf" : "=r"(p));
    wbuf[3] = 0xFEEDFACE;
    check("la.addr", p[3], 0xFEEDFACE);
}

/* ================================================================== */
/* 11. Loops and register pressure                                     */
/* ================================================================== */
static void test_loops(void) {
    unsigned int sum;

    /* sum 1..100 = 5050, computed in asm so it can't be folded. */
    asm volatile(
        "addi t0, x0, 0\n"      /* sum */
        "addi t1, x0, 1\n"      /* i   */
        "addi t2, x0, 101\n"
        "1:\n"
        "add  t0, t0, t1\n"
        "addi t1, t1, 1\n"
        "bne  t1, t2, 1b\n"
        "mv   %0, t0\n"
        : "=r"(sum) :: "t0", "t1", "t2");
    check("loop.sum100", sum, 5050);

    /* Nested loop: 10 x 10 increments. */
    asm volatile(
        "addi t0, x0, 0\n"
        "addi t1, x0, 0\n"
        "1:\n"
        "addi t2, x0, 0\n"
        "2:\n"
        "addi t0, t0, 1\n"
        "addi t2, t2, 1\n"
        "addi t3, x0, 10\n"
        "bne  t2, t3, 2b\n"
        "addi t1, t1, 1\n"
        "addi t3, x0, 10\n"
        "bne  t1, t3, 1b\n"
        "mv   %0, t0\n"
        : "=r"(sum) :: "t0", "t1", "t2", "t3");
    check("loop.nested", sum, 100);

    /* Memory loop: fill and sum an array. */
    for (int i = 0; i < 16; i++) wbuf[i] = i * 7;
    unsigned int acc = 0;
    for (int i = 0; i < 16; i++) acc += wbuf[i];
    check("loop.array.sum", acc, 7 * (15 * 16 / 2));

    /* Many live registers at once. */
    unsigned int t;
    asm volatile(
        "addi t0, x0, 1\n"  "addi t1, x0, 2\n"
        "addi t2, x0, 3\n"  "addi t3, x0, 4\n"
        "addi t4, x0, 5\n"  "addi t5, x0, 6\n"
        "addi t6, x0, 7\n"  "addi a4, x0, 8\n"
        "add  t0, t0, t1\n" "add  t2, t2, t3\n"
        "add  t4, t4, t5\n" "add  t6, t6, a4\n"
        "add  t0, t0, t2\n" "add  t4, t4, t6\n"
        "add  %0, t0, t4\n"
        : "=r"(t) :: "t0","t1","t2","t3","t4","t5","t6","a4");
    check("many.live.regs", t, 36);
}

/* ================================================================== */
/* 12. Multiply / divide, if the toolchain targets M                   */
/* ================================================================== */
static void test_muldiv(void) {
#ifdef __riscv_mul
    unsigned int r;
    asm volatile("mul %0, %1, %2" : "=r"(r) : "r"(1234u), "r"(5678u));
    check("mul", r, 1234u * 5678u);

    asm volatile("mulh %0, %1, %2" : "=r"(r) : "r"(0x40000000), "r"(4));
    check("mulh", r, 1);

    asm volatile("mul %0, %1, %2" : "=r"(r) : "r"(-3), "r"(7));
    check("mul.neg", r, (unsigned)(-21));
#endif
#ifdef __riscv_div
    unsigned int q;
    asm volatile("div %0, %1, %2" : "=r"(q) : "r"(100), "r"(7));
    check("div", q, 14);

    asm volatile("rem %0, %1, %2" : "=r"(q) : "r"(100), "r"(7));
    check("rem", q, 2);

    asm volatile("divu %0, %1, %2" : "=r"(q) : "r"(0xFFFFFFFFu), "r"(2u));
    check("divu", q, 0x7FFFFFFF);
#endif
}

/* ================================================================== */
/* 13. Repeat-hammer -- catches intermittent scheduling bugs           */
/* ================================================================== */
static void test_hammer(void) {
    int bad = 0;

    for (int i = 0; i < 200; i++) {
        unsigned int r;
        asm volatile(
            "sw   %1, 0(%2)\n"
            "lw   t0, 0(%2)\n"
            "addi t0, t0, 1\n"
            "add  t1, t0, t0\n"
            "sw   t1, 4(%2)\n"
            "lw   %0, 4(%2)\n"
            : "=&r"(r) : "r"((unsigned)i), "r"(&wbuf[0])
            : "t0", "t1", "memory");
        if (r != (unsigned)((i + 1) * 2)) {
            if (!bad) {
                debug_log("FAIL hammer at i=");
                debug_num(i);
                debug_log(" got=");
                debug_num(r);
                debug_log("\n");
            }
            bad++;
        }
    }
    ran++;
    if (bad) {
        failures++;
        debug_log("[FAIL] hammer, bad iterations = ");
        debug_num(bad);
        debug_log(" of 200\n");
    } else {
        debug_log("[ ok ] hammer, 200 iterations clean\n");
    }
}

/* ================================================================== */

static void fill_screen(unsigned int color) {
    for (int i = 0; i < IMG_W * IMG_H; i++) {
        VGA[i] = color;
    }
}

int main(void) {
    debug_log("=== hopper-cpu self test ===\n");

    test_alu_imm();
    test_alu_reg();
    test_x0();
    test_hazards();
    test_word_mem();
    test_store_load_window();
    test_subword();
    test_branches();
    test_jumps();
    test_upper();
    test_loops();
    test_muldiv();
    test_hammer();

    debug_log("ran=");
    debug_num(ran);
    debug_log(" failures=");
    debug_num(failures);
    debug_log("\n");

    if (failures == 0) {
        debug_log("ALL PASS -> green\n");
        fill_screen(GREEN);
    } else {
        debug_log("FAILURES -> red\n");
        fill_screen(RED);
    }

    while (1) {
        __asm__ volatile("nop");
    }
}