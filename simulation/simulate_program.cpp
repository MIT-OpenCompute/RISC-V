#include "VMain.h"
#include "verilated.h"
#include <cstdio>
#include <vector>
#include <fstream>
#include <cstdlib>

static constexpr int H_VISIBLE = 640;
static constexpr int H_FRONT   = 16;
static constexpr int H_SYNC    = 96;
static constexpr int H_BACK    = 48;
static constexpr int H_TOTAL   = H_VISIBLE + H_FRONT + H_SYNC + H_BACK;

static constexpr int V_VISIBLE = 480;
static constexpr int V_FRONT   = 10;
static constexpr int V_SYNC    = 2;
static constexpr int V_BACK    = 33;
static constexpr int V_TOTAL   = V_VISIBLE + V_FRONT + V_SYNC + V_BACK;

int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);
    auto dut = std::make_unique<VMain>();

    dut->io_execute = 0;
    dut->io_flash = 0;
    dut->io_flash_address = 0;
    dut->io_flash_value = 0;
    dut->io_btns = 0;

    dut->reset = 1;
    dut->clock = 0;
    dut->io_vga_clk = 0;

    for (int i = 0; i < 4; i++) {
        dut->clock ^= 1;
        dut->io_vga_clk = dut->clock;
        dut->eval();
    }

    dut->reset = 0;

    std::vector<uint8_t> pixels(H_VISIBLE * V_VISIBLE * 3, 0);

    int hCount = 0;
    int vCount = 0;

    if (argc < 2) {
    	fprintf(stderr, "Usage: %s <path-to-bin-file>\n", argv[0]);
		return 1;
	}

	std::ifstream file(argv[1], std::ios::binary);
	if (!file) {
		fprintf(stderr, "Failed to open file: %s\n", argv[1]);
		return 1;
	}

	int address = 0;
	uint32_t value;

	while (file.read(reinterpret_cast<char*>(&value), sizeof(value))) {
		dut->io_flash = 1;
		dut->io_flash_address = address;
		dut->io_flash_value = value;

		printf("Writing %x at %d\n", value, address);

		dut->clock = 1;
		dut->io_vga_clk = 1;
		dut->eval();
		dut->clock = 0;
		dut->io_vga_clk = 0;
		dut->eval();

		address++;
	}

    dut->io_flash = 0;
    dut->io_flash_address = 0;
    dut->io_flash_value = 0;

    for (int i = 0; i < 4; i++) {
        dut->clock ^= 1;
        dut->io_vga_clk = dut->clock;
        dut->eval();
    }

    dut->io_execute = 1;

    bool prev_vsync = 1;
    int pixelIdx = 0;

    while (1) {
        pixelIdx = 0;

        while (true) {
            dut->clock = 1; dut->io_vga_clk = 1; dut->eval();
            bool vsync = dut->io_vsync;
            dut->clock = 0; dut->io_vga_clk = 0; dut->eval();

            if (prev_vsync && !vsync) break;
            prev_vsync = vsync;
        }
        prev_vsync = 0;

        for (int cycle = 0; cycle < H_TOTAL * V_TOTAL; cycle++) {
            dut->clock = 1; dut->io_vga_clk = 1; dut->eval();

            bool vsync    = dut->io_vsync;
            bool blanking = dut->io_blanking;
            uint16_t rgb12 = dut->io_rgb;

            dut->clock = 0; dut->io_vga_clk = 0; dut->eval();

            if (prev_vsync && !vsync) {
                printf("vsync mid-frame at cycle %d — counter mismatch!\n", cycle);
            }
            prev_vsync = vsync;

            if (!blanking && pixelIdx < H_VISIBLE * V_VISIBLE) {
                pixels[pixelIdx * 3 + 0] = ((rgb12 >> 8) & 0xF) * 17;
                pixels[pixelIdx * 3 + 1] = ((rgb12 >> 4) & 0xF) * 17;
                pixels[pixelIdx * 3 + 2] = ((rgb12 >> 0) & 0xF) * 17;
                pixelIdx++;
            }
        }

        printf("Captured %d pixels (expected %d)\n", pixelIdx, H_VISIBLE * V_VISIBLE);

        FILE* f = fopen("./generated/frame.ppm", "wb");
        if (!f) { perror("fopen"); return 1; }
        fprintf(f, "P6\n%d %d\n255\n", H_VISIBLE, V_VISIBLE);
        fwrite(pixels.data(), 1, pixels.size(), f);
        fclose(f);
        system("ffmpeg -i ./generated/frame.ppm ./generated/frame.png -y");
    }

    return 0;
}