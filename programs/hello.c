__attribute__((naked)) void _start(void) {
    __asm__ volatile (
        "li sp, 0x8000\n"   // top of 32KB RAM
        "call main\n"
        "loop: j loop\n"
    );
}

int main() {
    volatile unsigned char* mem = (volatile unsigned char*)0x4000;
    int counter = 0;

    while (1) {
        counter++;
        mem[counter] = counter;  // sb x1, 0(x2) — store counter byte

        // busy-wait pause loop (~2000 iterations)
        for (int i = 0; i < 2000; i++) {
            __asm__("nop");  // prevent compiler from optimizing away
        }
    }
}