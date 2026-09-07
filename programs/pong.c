
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

}