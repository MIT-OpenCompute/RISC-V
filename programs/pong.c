/* ALU testbench -- newer core.
 *
 * Adapted from the display-only version: framebuffer at 0x10000000,
 * 24-bit color, stack at 0x7000000, and UART/debug MMIO available so
 * every test also prints its name and value.
 *
 * Blocks on screen:
 *   black -- never reached
 *   BLUE  -- hung on this instruction
 *   GREEN -- correct
 *   RED   -- wrong result
 *
 * Row A (y=20)   ADD / SUB      (func3 000, bit 30 selects SUB)
 * Row B (y=60)   SRA / SRL      (bit 30 selects arithmetic)
 * Row C (y=100)  shift amounts  (RV32 masks to 5 bits)
 * Row D (y=140)  other R-type   -- all expected GREEN
 * Row E (y=180)  I-type         -- all expected GREEN
 */

#define FRAME_BASE 0x10000000
#define IMG_W 320
#define IMG_H 240
#define FRAME ((volatile unsigned int*)FRAME_BASE)

#define DBGCH  ((volatile unsigned int*)0x70000000)
#define DBGNUM ((volatile unsigned int*)0x70000008)
#define UARTTX ((volatile unsigned char*)0x8000034)

#define BLACK 0x000000
#define WHITE 0xFFFFFF
#define RED   0xFF0000
#define GREEN 0x00FF00
#define BLUE  0x0000FF

__attribute__((naked)) void _start(void) {
    __asm__ volatile(
        "li sp, 0x7000000\n"
        "call main\n"
        "loop: j loop\n"
    );
}

void debug_log(char* c) {
    while (*c != '\0') {
        *DBGCH = *c;
        *UARTTX = *c;
        c++;
    }
}

void debug_num(unsigned int v) {
    *DBGNUM = v;
}

#define PAINT(slot, row, color)                                        \
    do {                                                               \
        unsigned int p_x0 = 8u + ((unsigned)(slot) << 5);              \
        for (unsigned int p_y = 0; p_y < 20u; p_y++) {                 \
            unsigned int p_r = (unsigned)(row) + p_y;                  \
            unsigned int p_b = (p_r << 8) + (p_r << 6) + p_x0;         \
            for (unsigned int p_x = 0; p_x < 20u; p_x++)               \
                FRAME[p_b + p_x] = (color);                            \
        }                                                              \
    } while (0)

/* Paint the verdict and log it in one place. */
#define VERDICT(slot, row, name, got, want)                            \
    do {                                                               \
        unsigned int v_g = (got), v_w = (want);                        \
        if (v_g == v_w) {                                              \
            PAINT(slot, row, GREEN);                                   \
            debug_log("[ ok ] " name " = ");                           \
            debug_num(v_g);                                            \
            debug_log("\n");                                           \
        } else {                                                       \
            PAINT(slot, row, RED);                                     \
            failures++;                                                \
            debug_log("[FAIL] " name " got=");                         \
            debug_num(v_g);                                            \
            debug_log(" want=");                                       \
            debug_num(v_w);                                            \
            debug_log("\n");                                           \
        }                                                              \
    } while (0)

#define ROW_A  20
#define ROW_B  60
#define ROW_C 100
#define ROW_D 140
#define ROW_E 180

static volatile int failures = 0;

int main(void) {
    unsigned int r;

    /* sign of life: blue flash, then black */
    for (unsigned int i = 0; i < (unsigned)(IMG_W * IMG_H); i++)
        FRAME[i] = BLUE;
    for (unsigned int i = 0; i < (unsigned)(IMG_W * IMG_H); i++)
        FRAME[i] = BLACK;

    debug_log("=== ALU bench ===\n");

    /* ================= row A: ADD / SUB ================= */
    debug_log("\n-- add/sub --\n");

    PAINT(0, ROW_A, BLUE);
    asm volatile("add %0, %1, %2"
                 : "=r"(r) : "r"(0xF0F0F0F0u), "r"(0x0FF00FF0u));
    VERDICT(0, ROW_A, "add", r, 0x00E100E0u);

    PAINT(1, ROW_A, BLUE);
    asm volatile("sub %0, %1, %2"
                 : "=r"(r) : "r"(0xF0F0F0F0u), "r"(0x0FF00FF0u));
    VERDICT(1, ROW_A, "sub.big", r, 0xE100E100u);

    PAINT(2, ROW_A, BLUE);
    asm volatile("sub %0, %1, %2" : "=r"(r) : "r"(10u), "r"(3u));
    VERDICT(2, ROW_A, "sub.small", r, 7u);

    PAINT(3, ROW_A, BLUE);
    asm volatile("sub %0, %1, %2" : "=r"(r) : "r"(5u), "r"(10u));
    VERDICT(3, ROW_A, "sub.neg", r, 0xFFFFFFFBu);

    PAINT(4, ROW_A, BLUE);
    asm volatile("sub %0, %1, %2" : "=r"(r) : "r"(7u), "r"(7u));
    VERDICT(4, ROW_A, "sub.zero", r, 0u);

    /* ================= row B: SRA / SRL ================= */
    debug_log("\n-- sra/srl --\n");

    PAINT(0, ROW_B, BLUE);
    asm volatile("sra %0, %1, %2" : "=r"(r) : "r"(0x80000000u), "r"(31u));
    VERDICT(0, ROW_B, "sra.neg31", r, 0xFFFFFFFFu);

    PAINT(1, ROW_B, BLUE);
    asm volatile("sra %0, %1, %2" : "=r"(r) : "r"(0xF0000000u), "r"(4u));
    VERDICT(1, ROW_B, "sra.neg4", r, 0xFF000000u);

    /* positive operand: passes even if sra is really doing srl */
    PAINT(2, ROW_B, BLUE);
    asm volatile("sra %0, %1, %2" : "=r"(r) : "r"(0x40000000u), "r"(4u));
    VERDICT(2, ROW_B, "sra.pos4", r, 0x04000000u);

    PAINT(3, ROW_B, BLUE);
    asm volatile("srl %0, %1, %2" : "=r"(r) : "r"(0xF0000000u), "r"(4u));
    VERDICT(3, ROW_B, "srl.neg4", r, 0x0F000000u);

    PAINT(4, ROW_B, BLUE);
    asm volatile("srai %0, %1, 4" : "=r"(r) : "r"(0x80000000u));
    VERDICT(4, ROW_B, "srai.neg4", r, 0xF8000000u);

    /* ============== row C: shift amount masking ============== */
    debug_log("\n-- shift amounts --\n");

    PAINT(0, ROW_C, BLUE);
    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(1u), "r"(1u));
    VERDICT(0, ROW_C, "sll.1", r, 2u);

    PAINT(1, ROW_C, BLUE);
    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(1u), "r"(31u));
    VERDICT(1, ROW_C, "sll.31", r, 0x80000000u);

    PAINT(2, ROW_C, BLUE);
    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(1u), "r"(32u));
    VERDICT(2, ROW_C, "sll.32", r, 1u);

    PAINT(3, ROW_C, BLUE);
    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(1u), "r"(33u));
    VERDICT(3, ROW_C, "sll.33", r, 2u);

    PAINT(4, ROW_C, BLUE);
    asm volatile("srl %0, %1, %2" : "=r"(r) : "r"(0x80000000u), "r"(33u));
    VERDICT(4, ROW_C, "srl.33", r, 0x40000000u);

    /* ============== row D: remaining R-type ============== */
    debug_log("\n-- other r-type --\n");

    PAINT(0, ROW_D, BLUE);
    asm volatile("and %0, %1, %2"
                 : "=r"(r) : "r"(0xF0F0F0F0u), "r"(0x0FF00FF0u));
    VERDICT(0, ROW_D, "and", r, 0x00F000F0u);

    PAINT(1, ROW_D, BLUE);
    asm volatile("or %0, %1, %2"
                 : "=r"(r) : "r"(0xF0F0F0F0u), "r"(0x0FF00FF0u));
    VERDICT(1, ROW_D, "or", r, 0xFFF0FFF0u);

    PAINT(2, ROW_D, BLUE);
    asm volatile("xor %0, %1, %2"
                 : "=r"(r) : "r"(0xF0F0F0F0u), "r"(0x0FF00FF0u));
    VERDICT(2, ROW_D, "xor", r, 0xFF00FF00u);

    PAINT(3, ROW_D, BLUE);
    asm volatile("slt %0, %1, %2" : "=r"(r) : "r"(-1), "r"(1));
    VERDICT(3, ROW_D, "slt", r, 1u);

    PAINT(4, ROW_D, BLUE);
    asm volatile("sltu %0, %1, %2" : "=r"(r) : "r"(-1), "r"(1));
    VERDICT(4, ROW_D, "sltu", r, 0u);

    PAINT(5, ROW_D, BLUE);
    asm volatile("sll %0, %1, %2" : "=r"(r) : "r"(0x00000FFFu), "r"(8u));
    VERDICT(5, ROW_D, "sll.reg", r, 0x000FFF00u);

    /* ============== row E: I-type ============== */
    debug_log("\n-- i-type --\n");

    PAINT(0, ROW_E, BLUE);
    asm volatile("addi %0, %1, 0x123" : "=r"(r) : "r"(0x12345678u));
    VERDICT(0, ROW_E, "addi", r, 0x1234579Bu);

    /* negative immediate sets instruction bit 30 -- must NOT become sub */
    PAINT(1, ROW_E, BLUE);
    asm volatile("addi %0, %1, -1" : "=r"(r) : "r"(0x12345678u));
    VERDICT(1, ROW_E, "addi.neg", r, 0x12345677u);

    PAINT(2, ROW_E, BLUE);
    asm volatile("andi %0, %1, 0xFF" : "=r"(r) : "r"(0x12345678u));
    VERDICT(2, ROW_E, "andi", r, 0x78u);

    PAINT(3, ROW_E, BLUE);
    asm volatile("ori %0, %1, 0x7FF" : "=r"(r) : "r"(0x12345678u));
    VERDICT(3, ROW_E, "ori", r, 0x123457FFu);

    PAINT(4, ROW_E, BLUE);
    asm volatile("xori %0, %1, 0x7FF" : "=r"(r) : "r"(0x12345678u));
    VERDICT(4, ROW_E, "xori", r, 0x12345187u);

    PAINT(5, ROW_E, BLUE);
    asm volatile("slli %0, %1, 4" : "=r"(r) : "r"(0x12345678u));
    VERDICT(5, ROW_E, "slli", r, 0x23456780u);

    PAINT(6, ROW_E, BLUE);
    asm volatile("srli %0, %1, 4" : "=r"(r) : "r"(0x12345678u));
    VERDICT(6, ROW_E, "srli", r, 0x01234567u);

    /* negative immediate again: slti must stay a compare */
    PAINT(7, ROW_E, BLUE);
    asm volatile("slti %0, %1, -5" : "=r"(r) : "r"(-10));
    VERDICT(7, ROW_E, "slti.neg", r, 1u);

    /* ---- summary ---- */
    debug_log("\nfailures=");
    debug_num((unsigned int)failures);
    debug_log("\n");

    for (unsigned int y = 215; y < (unsigned)IMG_H; y++) {
        unsigned int b = (y << 8) + (y << 6);
        for (unsigned int x = 0; x < (unsigned)IMG_W; x++)
            FRAME[b + x] = (failures == 0) ? GREEN : RED;
    }

    while (1) __asm__ volatile("nop");
}