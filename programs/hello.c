__attribute__((naked)) void _start(void) {
    __asm__ volatile(
        "li sp, 0x4000\n"  // top of 32KB RAM
        "call main\n"
        "loop: j loop\n"
    );
}

int main() {
    volatile unsigned int* frame = (volatile unsigned int*)0x4000;

    frame[0] = 0xFF;
}