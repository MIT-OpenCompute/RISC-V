#include <stdint.h>

__attribute__((naked)) void _start(void) {
    __asm__ volatile(
        "li sp, 0x8000000\n"
        "call main\n"
        "loop: j loop\n"
    );
}

/* ---- provided debug hooks ---- */
void debug_log(char* character) {
    while (*character != '\0') {
        *((volatile unsigned int*)0x70000000) = *(character);
        *((volatile unsigned char*)0x8000034) = *(character);
        character++;
    }
}

static void debug_putc(char c) {
    char s[2] = { c, 0 };
    debug_log(s);
}

static void debug_hex32(unsigned int value) {
    static const char hex[] = "0123456789ABCDEF";
    for (int i = 7; i >= 0; i--) {
        debug_putc(hex[(value >> (i * 4)) & 0xF]);
    }
}

static void trace(char *label, unsigned int value) {
    debug_log(label);
    debug_hex32(value);
    debug_log("\n");
}

/* ---- framebuffer ---- */
#define FRAME_BASE  ((volatile unsigned int*)0x10000000)
#define FRAME_W     320
#define FRAME_H     240
#define COLOR_GREEN 0x00FF00
#define COLOR_RED   0xFF0000
#define COLOR_BLUE  0x0000FF /* used as "in progress" so red/green are unambiguous */

static void fill_screen(unsigned int color) {
    volatile unsigned int* f = FRAME_BASE;
    for (int i = 0; i < FRAME_W * FRAME_H; i++) {
        f[i] = color;
    }
}

/* ---- test region ----
 * RAM is 0x00000000 - 0x07FFFFFF (128MB). Stack starts at 0x08000000 and
 * grows down, so this sits well clear of it, and well clear of the
 * loaded program at the bottom of RAM. */
#define TEST_BASE  ((volatile unsigned int*)0x01000000)
#define TEST_WORDS 0x8000u   /* 32768 words = 128KB */
#define BURST      16u       /* outstanding loads issued back-to-back per check */

static unsigned int lfsr_state;

static unsigned int lfsr_next(void) {
    unsigned int x = lfsr_state;
    x ^= x << 13;
    x ^= x >> 17;
    x ^= x << 5;
    lfsr_state = x;
    return x;
}

/* ---- failure path: stop, log everything, paint the screen red ---- */
static void fail(char *stage, volatile unsigned int *addr, unsigned int expected, unsigned int got) {
    debug_log("FAIL @ stage: ");
    debug_log(stage);
    debug_log("\n");
    trace("  addr=", (unsigned int)(uintptr_t)addr);
    trace("  expected=", expected);
    trace("  got=", got);
    fill_screen(COLOR_RED);
    while (1) {
        __asm__ volatile("nop");
    }
}

/* Write a pattern across the whole test region. */
static void write_pattern(char *name, unsigned int (*pattern)(unsigned int idx)) {
    debug_log("writing pattern: ");
    debug_log(name);
    debug_log("\n");
    volatile unsigned int *mem = TEST_BASE;
    for (unsigned int i = 0; i < TEST_WORDS; i++) {
        mem[i] = pattern(i);
    }
}

/* Hammer the region with bursts of BURST non-blocking-style loads (issued
 * back-to-back before any of them are checked), then verify every value.
 * Runs the sweep forward, then backward, then strided, to vary the access
 * order the load queue sees. */
#define PROGRESS_EVERY 128u   /* print every N groups of BURST */

static void progress(char *stage, unsigned int group, unsigned int total_groups) {
    if (group % PROGRESS_EVERY != 0) return;
    debug_log("  ");
    debug_log(stage);
    debug_log(": group ");
    debug_hex32(group);
    debug_log(" / ");
    debug_hex32(total_groups);
    debug_log("\n");
}

static void verify_burst(char *name, unsigned int (*pattern)(unsigned int idx)) {
    debug_log("verifying pattern (burst): ");
    debug_log(name);
    debug_log("\n");
    volatile unsigned int *mem = TEST_BASE;
    unsigned int vals[BURST];
    unsigned int groups = TEST_WORDS / BURST;

    /* forward sweep */
    for (unsigned int g = 0; g < groups; g++) {
        progress("fwd", g, groups);
        unsigned int base = g * BURST;
        for (unsigned int j = 0; j < BURST; j++) {
            vals[j] = mem[base + j];        /* issue all loads before checking any */
        }
        for (unsigned int j = 0; j < BURST; j++) {
            unsigned int expected = pattern(base + j);
            if (vals[j] != expected) {
                fail(name, &mem[base + j], expected, vals[j]);
            }
        }
    }

    /* reverse sweep */
    for (unsigned int g = groups; g > 0; g--) {
        progress("rev", groups - g, groups);
        unsigned int base = (g - 1) * BURST;
        for (unsigned int j = 0; j < BURST; j++) {
            vals[j] = mem[base + j];
        }
        for (unsigned int j = 0; j < BURST; j++) {
            unsigned int expected = pattern(base + j);
            if (vals[j] != expected) {
                fail(name, &mem[base + j], expected, vals[j]);
            }
        }
    }

    /* strided sweep: hit every 7th burst-group first, wrapping around, to
     * decorrelate access order from anything the memory system might be
     * pipelining based on sequential stride */
    for (unsigned int g = 0; g < groups; g++) {
        progress("stride", g, groups);
        unsigned int base = ((g * 7u) % groups) * BURST;
        for (unsigned int j = 0; j < BURST; j++) {
            vals[j] = mem[base + j];
        }
        for (unsigned int j = 0; j < BURST; j++) {
            unsigned int expected = pattern(base + j);
            if (vals[j] != expected) {
                fail(name, &mem[base + j], expected, vals[j]);
            }
        }
    }
}

/* ---- patterns ---- */
static unsigned int pat_address(unsigned int idx) {
    return (unsigned int)(uintptr_t)&TEST_BASE[idx];
}
static unsigned int pat_inv_address(unsigned int idx) {
    return ~pat_address(idx);
}
static unsigned int pat_checkerboard(unsigned int idx) {
    return (idx & 1) ? 0xAAAAAAAAu : 0x55555555u;
}
static unsigned int lfsr_expected[TEST_WORDS];
static unsigned int pat_lfsr(unsigned int idx) {
    return lfsr_expected[idx];
}

int main() {
    debug_log("boot: memory stress test\n");
    fill_screen(COLOR_BLUE);

    trace("test base=", (unsigned int)(uintptr_t)TEST_BASE);
    trace("test words=", TEST_WORDS);
    trace("burst depth=", BURST);

    write_pattern("address", pat_address);
    verify_burst("address", pat_address);

    write_pattern("inverted-address", pat_inv_address);
    verify_burst("inverted-address", pat_inv_address);

    write_pattern("checkerboard", pat_checkerboard);
    verify_burst("checkerboard", pat_checkerboard);

    lfsr_state = 0xC001D00D;
    {
        volatile unsigned int *mem = TEST_BASE;
        for (unsigned int i = 0; i < TEST_WORDS; i++) {
            unsigned int v = lfsr_next();
            mem[i] = v;
            lfsr_expected[i] = v;
        }
    }
    verify_burst("lfsr", pat_lfsr);

    debug_log("all patterns verified OK\n");
    fill_screen(COLOR_GREEN);

    while (1) {
        __asm__ volatile("nop");
    }
}