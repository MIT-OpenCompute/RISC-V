#include "VVGAController.h"
#include "verilated.h"
#include <cstdio>
#include <vector>

static constexpr int H_TOTAL  = 800;
static constexpr int V_TOTAL  = 525;
static constexpr int H_ACTIVE = 640;
static constexpr int V_ACTIVE = 480;

int main(int argc, char** argv) {
    Verilated::commandArgs(argc, argv);
    auto dut = std::make_unique<VVGAController>();

    // Reset
    dut->reset = 1; dut->clock = 0;
    for (int i = 0; i < 4; i++) {
        dut->clock ^= 1;
        dut->eval();
    }
    dut->reset = 0;

    // Framebuffer
    std::vector<uint8_t> pixels(H_ACTIVE * V_ACTIVE * 3, 0);

    // Run one full frame
    for (int cycle = 0; cycle < H_TOTAL * V_TOTAL; cycle++) {
        dut->clock = 1; dut->eval();
        dut->clock = 0; dut->eval();

        if (!dut->io_blanking) {
            int x = dut->io_hPos;
            int y = dut->io_vPos;
            uint16_t rgb12 = dut->io_rgb;
            int idx = (y * H_ACTIVE + x) * 3;
            pixels[idx + 0] = ((rgb12 >> 8) & 0xF) * 17;
            pixels[idx + 1] = ((rgb12 >> 4) & 0xF) * 17;
            pixels[idx + 2] = ((rgb12 >> 0) & 0xF) * 17;
        }
    }

    // Write PPM
    FILE* f = fopen("frame.ppm", "wb");
    fprintf(f, "P6\n%d %d\n255\n", H_ACTIVE, V_ACTIVE);
    fwrite(pixels.data(), 1, pixels.size(), f);
    fclose(f);

    dut->final();
    printf("Wrote frame.ppm\n");
}