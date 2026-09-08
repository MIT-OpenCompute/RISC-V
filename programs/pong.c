/* Memory hammer -- newer core.
 *
 * Framebuffer at 0x10000000, 24-bit color, stack at 0x7000000.
 * Logs to UART/debug MMIO and paints result blocks. Writes to MMIO only;
 * never reads from it (no timer, no keytracker), so a stale MMIO read
 * tag can't corrupt results.
 *
 * Blocks:
 *   black -- never reached
 *   BLUE  -- hung here
 *   GREEN -- passed
 *   RED   -- failed
 *
 * Row A (y=20)   basic word load/store
 * Row B (y=60)   store-to-load gap
 * Row C (y=100)  subword
 * Row D (y=140)  bulk patterns and hammering
 * Bottom bar: green if everything passed, red otherwise.
 */

#define IMG_W 320
#define IMG_H 240
#define FRAME ((volatile unsigned int*)0x10000000)

#define DBGCH  ((volatile unsigned int*)0x70000000)
#define DBGNUM ((volatile unsigned int*)0x70000008)
#define UARTTX ((volatile unsigned char*)0x8000034)

#define BLACK 0x000000
#define WHITE 0xFFFFFF
#define RED   0xFF0000
#define GREEN 0x00FF00
#define BLUE  0x0000FF

#define ROW_A  20
#define ROW_B  60
#define ROW_C 100
#define ROW_D 140

__attribute__((naked)) void _start(void) {
    __asm__ volatile(
        "li sp, 0x7000000\n"
        ".option push\n.option norelax\n"
        "la gp, __global_pointer$\n"
        ".option pop\n"
        "call main\n"
        "loop: j loop\n"
    );
}

static volatile int failures = 0;

void debug_log(char* c) {
    while (*c != '\0') { *DBGCH = *c; *UARTTX = *c; c++; }
}

void debug_num(unsigned int v) { *DBGNUM = v; }

void paint(int slot, int row, unsigned int color) {
    unsigned int x0 = 8u + ((unsigned)slot << 5);
    for (unsigned int y = 0; y < 20u; y++) {
        unsigned int r = (unsigned)row + y;
        unsigned int b = (r << 8) + (r << 6) + x0;
        for (unsigned int x = 0; x < 20u; x++) FRAME[b + x] = color;
    }
}

void fill(unsigned int color) {
    for (int i = 0; i < IMG_W * IMG_H; i++) FRAME[i] = color;
}

/* Paint verdict and log it. */
void verdict(int slot, int row, char* name, unsigned int got, unsigned int want) {
    if (got == want) {
        paint(slot, row, GREEN);
        debug_log("[ ok ] ");
        debug_log(name);
        debug_log(" = ");
        debug_num(got);
        debug_log("\n");
    } else {
        paint(slot, row, RED);
        failures++;
        debug_log("[FAIL] ");
        debug_log(name);
        debug_log(" got=");
        debug_num(got);
        debug_log(" want=");
        debug_num(want);
        debug_log("\n");
    }
}

static unsigned int   buf[256];
static unsigned short hbuf[8];
static unsigned char  bbuf[8];

int main(void) {
    unsigned int r, r2, r3, r4;
    unsigned int ok;

    fill(BLUE);
    fill(BLACK);

    debug_log("=== memory hammer ===\n");

    /* ============ row A: basic word load/store ============ */
    debug_log("\n-- word memory --\n");

    paint(0, ROW_A, BLUE);
    asm volatile("sw %1, 0(%2)\n lw %0, 0(%2)"
                 : "=&r"(r) : "r"(0xCAFEBABEu), "r"(&buf[0]) : "memory");
    verdict(0, ROW_A, "sw.lw", r, 0xCAFEBABEu);

    paint(1, ROW_A, BLUE);
    asm volatile(
        "sw %4, 0(%8)\n  sw %5, 4(%8)\n"
        "sw %6, 8(%8)\n  sw %7, 12(%8)\n"
        "lw %0, 0(%8)\n  lw %1, 4(%8)\n"
        "lw %2, 8(%8)\n  lw %3, 12(%8)\n"
        : "=&r"(r), "=&r"(r2), "=&r"(r3), "=&r"(r4)
        : "r"(0x11111111u), "r"(0x22222222u),
          "r"(0x33333333u), "r"(0x44444444u), "r"(&buf[0])
        : "memory");
    ok = (r == 0x11111111u) && (r2 == 0x22222222u)
      && (r3 == 0x33333333u) && (r4 == 0x44444444u);
    verdict(1, ROW_A, "four.offsets", ok, 1u);

    paint(2, ROW_A, BLUE);
    for (unsigned int i = 0; i < 32u; i++) buf[i] = 1u << i;
    ok = 1;
    for (unsigned int i = 0; i < 32u; i++)
        if (buf[i] != (1u << i)) ok = 0;
    verdict(2, ROW_A, "walking.ones", ok, 1u);

    paint(3, ROW_A, BLUE);
    {
        unsigned int v = 0x12345678u;
        for (unsigned int i = 0; i < 64u; i++) { buf[i] = v; v += 0x9E3779B9u; }
        v = 0x12345678u;
        ok = 1;
        for (unsigned int i = 0; i < 64u; i++) {
            if (buf[i] != v) ok = 0;
            v += 0x9E3779B9u;
        }
    }
    verdict(3, ROW_A, "additive.64", ok, 1u);

    /* ============ row B: store-to-load gap ============ */
    debug_log("\n-- store to load gap --\n");

    paint(0, ROW_B, BLUE);
    buf[8] = 0;
    asm volatile("sw %1, 32(%2)\n lw %0, 32(%2)"
                 : "=&r"(r) : "r"(0xA5A5A5A5u), "r"(&buf[0]) : "memory");
    verdict(0, ROW_B, "gap0", r, 0xA5A5A5A5u);

    paint(1, ROW_B, BLUE);
    buf[8] = 0;
    asm volatile("sw %1, 32(%2)\n nop\n lw %0, 32(%2)"
                 : "=&r"(r) : "r"(0x5A5A5A5Au), "r"(&buf[0]) : "memory");
    verdict(1, ROW_B, "gap1", r, 0x5A5A5A5Au);

    paint(2, ROW_B, BLUE);
    buf[8] = 0;
    asm volatile("sw %1, 32(%2)\n nop\n nop\n nop\n nop\n lw %0, 32(%2)"
                 : "=&r"(r) : "r"(0xDEADBEEFu), "r"(&buf[0]) : "memory");
    verdict(2, ROW_B, "gap4", r, 0xDEADBEEFu);

    paint(3, ROW_B, BLUE);
    buf[8] = 0;
    asm volatile("sw %1, 32(%2)\n"
                 "nop\n nop\n nop\n nop\n nop\n nop\n nop\n nop\n"
                 "nop\n nop\n nop\n nop\n nop\n nop\n nop\n nop\n"
                 "lw %0, 32(%2)"
                 : "=&r"(r) : "r"(0xFEEDFACEu), "r"(&buf[0]) : "memory");
    verdict(3, ROW_B, "gap16", r, 0xFEEDFACEu);

    /* ============ row C: subword ============ */
    debug_log("\n-- subword --\n");

    paint(0, ROW_C, BLUE);
    buf[0] = 0;
    asm volatile("sb %0, 0(%1)" :: "r"(0xAAu), "r"(&buf[0]) : "memory");
    asm volatile("sb %0, 1(%1)" :: "r"(0xBBu), "r"(&buf[0]) : "memory");
    asm volatile("sb %0, 2(%1)" :: "r"(0xCCu), "r"(&buf[0]) : "memory");
    asm volatile("sb %0, 3(%1)" :: "r"(0xDDu), "r"(&buf[0]) : "memory");
    asm volatile("lw %0, 0(%1)" : "=r"(r) : "r"(&buf[0]) : "memory");
    verdict(0, ROW_C, "sb.same.word", r, 0xDDCCBBAAu);

    paint(1, ROW_C, BLUE);
    buf[0] = 0;
    asm volatile("sh %0, 0(%1)" :: "r"(0x1122u), "r"(&buf[0]) : "memory");
    asm volatile("sh %0, 2(%1)" :: "r"(0x3344u), "r"(&buf[0]) : "memory");
    asm volatile("lw %0, 0(%1)" : "=r"(r) : "r"(&buf[0]) : "memory");
    verdict(1, ROW_C, "sh.same.word", r, 0x33441122u);

    paint(2, ROW_C, BLUE);
    bbuf[0] = 0x7F; bbuf[1] = 0x80; bbuf[2] = 0xFF;
    asm volatile("lb %0, 1(%1)" : "=r"(r)  : "r"(&bbuf[0]) : "memory");
    asm volatile("lb %0, 2(%1)" : "=r"(r2) : "r"(&bbuf[0]) : "memory");
    ok = (r == 0xFFFFFF80u) && (r2 == 0xFFFFFFFFu);
    verdict(2, ROW_C, "lb.signext", ok, 1u);

    paint(3, ROW_C, BLUE);
    hbuf[0] = 0x8000;
    asm volatile("lbu %0, 1(%1)" : "=r"(r)  : "r"(&bbuf[0]) : "memory");
    asm volatile("lhu %0, 0(%1)" : "=r"(r2) : "r"(&hbuf[0]) : "memory");
    ok = (r == 0x80u) && (r2 == 0x8000u);
    verdict(3, ROW_C, "lbu.lhu", ok, 1u);

    /* ============ row D: bulk and hammering ============ */
    debug_log("\n-- bulk --\n");

    paint(0, ROW_D, BLUE);
    {
        unsigned int v = 0xC0FFEE11u;
        for (unsigned int i = 0; i < 256u; i++) { buf[i] = v; v += 0x01000193u; }
        v = 0xC0FFEE11u;
        ok = 1;
        for (unsigned int i = 0; i < 256u; i++) {
            if (buf[i] != v) ok = 0;
            v += 0x01000193u;
        }
    }
    verdict(0, ROW_D, "fill256", ok, 1u);

    paint(1, ROW_D, BLUE);
    {
        unsigned int idx = 0, v = 0x1000u;
        for (unsigned int i = 0; i < 256u; i++) {
            buf[idx] = v + idx;
            idx = (idx + 37u) & 255u;
            v += 16u;
        }
        idx = 0; v = 0x1000u;
        ok = 1;
        for (unsigned int i = 0; i < 256u; i++) {
            if (buf[idx] != (v + idx)) ok = 0;
            idx = (idx + 37u) & 255u;
            v += 16u;
        }
    }
    verdict(1, ROW_D, "scattered", ok, 1u);

    paint(2, ROW_D, BLUE);
    {
        unsigned int bad = 0;
        for (unsigned int i = 0; i < 500u; i++) {
            unsigned int got;
            asm volatile("sw %1, 0(%2)\n lw %0, 0(%2)"
                         : "=&r"(got) : "r"(i), "r"(&buf[16]) : "memory");
            if (got != i) bad++;
        }
        if (bad) { debug_log("  same-addr bad count = "); debug_num(bad); debug_log("\n"); }
        verdict(2, ROW_D, "hammer.same", bad, 0u);
    }

    paint(3, ROW_D, BLUE);
    {
        unsigned int bad = 0;
        for (unsigned int i = 0; i < 500u; i++) {
            unsigned int g1, g2;
            asm volatile(
                "sw %2, 0(%4)\n"
                "sw %3, 64(%4)\n"
                "lw %0, 0(%4)\n"
                "lw %1, 64(%4)\n"
                : "=&r"(g1), "=&r"(g2)
                : "r"(i), "r"(i + 0x8000u), "r"(&buf[32])
                : "memory");
            if (g1 != i) bad++;
            if (g2 != (i + 0x8000u)) bad++;
        }
        if (bad) { debug_log("  two-addr bad count = "); debug_num(bad); debug_log("\n"); }
        verdict(3, ROW_D, "hammer.two", bad, 0u);
    }

    /* ---- summary ---- */
    debug_log("\nfailures=");
    debug_num((unsigned int)failures);
    debug_log("\n");

    for (int y = 200; y < IMG_H; y++)
        for (int x = 0; x < IMG_W; x++)
            FRAME[y * IMG_W + x] = (failures == 0) ? GREEN : RED;

    while (1) __asm__ volatile("nop");
}