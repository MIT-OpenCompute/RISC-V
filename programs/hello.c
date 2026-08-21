__attribute__((naked)) void _start(void) {
    __asm__ volatile(
        "li sp, 0x4000\n"  // top of 32KB RAM
        "call main\n"
        "loop: j loop\n"
    );
}

int main() {
    volatile unsigned int* frame = (volatile unsigned int*)0x4000;

	// frame[0] = 0xFF;
	// frame[239 * 320] = 0xFF;
	// frame[1] = 0xFF;
	// frame[319] = 0xFF;

	for (int i = 0; i < 320; i++) {
        frame[i] = 0xFF;
        frame[239 * 320 + i] = 0xFF;
        frame[320 * i + 1] = 0xFF;
        frame[320 * i + 319] = 0xFF;
    }
}