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

void debug_num(unsigned int value) {
    *((volatile unsigned int*)0x70000008) = value;
}

/* debug_num only works in sim (pokes a sim-only trap address) -- on real
 * hardware nothing reads 0x70000008, so all numeric output has to go
 * through debug_log as ASCII hex over the real UART instead. */
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

#define SCALE 1024
#define MAX_ITER 32

/* Map an escape iteration count to a 24-bit RGB color (0x00RRGGBB). */
static unsigned int iter_to_color(int iter) {
    if (iter == MAX_ITER) {
        return 0x000000;
    }

    unsigned int t = (unsigned int)iter;
    unsigned int r = (t * 8) & 0xFF;
    unsigned int g = (t * 5) & 0xFF;
    unsigned int b = (t * 13) & 0xFF;

    return (r << 16) | (g << 8) | b;
}

void draw_mandelbrot(volatile unsigned int* frame, int cx, int cy, int zoom) {
    int x_start = cx - zoom;
    int y_start = cy - (zoom * 240 / 320);
    int x_step = (zoom * 2) / 320;
    int y_step = (zoom * 2 * 240 / 320) / 240;
    if (x_step < 1) x_step = 1;
    if (y_step < 1) y_step = 1;

    trace("draw: cx=", (unsigned int)cx);
    trace("draw: cy=", (unsigned int)cy);
    trace("draw: zoom=", (unsigned int)zoom);
    trace("draw: x_start=", (unsigned int)x_start);
    trace("draw: y_start=", (unsigned int)y_start);
    trace("draw: x_step=", (unsigned int)x_step);
    trace("draw: y_step=", (unsigned int)y_step);

    for (int py = 0; py < 240; py++) {
        int ci = y_start + py * y_step;
        for (int px = 0; px < 320; px++) {
            int cr = x_start + px * x_step;
            int zr = 0;
            int zi = 0;
            int iter = 0;

            while (iter < MAX_ITER) {
                int zr_times_zr = zr * zr;          /* raw mul result, pre-shift */
                int zi_times_zi = zi * zi;
                int zr_times_zi = zr * zi;           /* raw mul result, pre-shift/scale */

                int zr2 = zr_times_zr >> 10;
                int zi2 = zi_times_zi >> 10;

                if (zr2 + zi2 > 4 * SCALE) {
                    break;
                }

                int new_zr = zr2 - zi2 + cr;
                zi = ((2 * zr_times_zi) >> 10) + ci;
                zr = new_zr;
                iter++;
            }

            frame[320 * py + px] = iter_to_color(iter);
        }
    }
}

int main() {
    volatile unsigned int* frame = (volatile unsigned int*)0x10000000;
    volatile unsigned int* timer = (volatile unsigned int*)0x8000004;

    debug_log("boot\n");

    int cx = -768;
    int cy = 0;
    int step = 0;

    while (1) {
        int ctime = *timer;

        debug_log("frame: step=");
        debug_hex32((unsigned int)step);
        debug_log("\n");

        int zoom = 1536;
        for (int i = 0; i < step; i++) {
            zoom = (zoom * 3) / 4;
        }
        draw_mandelbrot(frame, cx, cy, zoom);

        step++;
        if (step > 8) step = 0;

        while (*timer - ctime < 5) {
            __asm__ volatile("nop");
        }
    }
}