# The Dawn RISC-V Processor Project

We aim to develop a synthesizable high performance open source RISC-V core. We have currently implemented an out of order unprivileged RV32I core with peripherals for interacting with UART and emitting VGA.

## Blogs
1. [Designing a CPU from Scratch](https://www.outercloud.dev/blogs/riscv-1/) - March 9, 2026
2. [Reaching Towards Out Of Order in Our CPU](https://www.outercloud.dev/blogs/riscv-2/) - June 14, 2026
3. [Running DOOM on our Custom CPU and Going Viral](https://armaangomes.com/blogs/doom/) - July 20, 2026
4. [Out of Order, on Technicality - RISCV](https://armaangomes.com/blogs/techooo/) - July 26, 2026

## Getting Started
You'll need [xpack-riscv-none-elf-gcc-15.2.0-1](https://github.com/xpack-dev-tools/riscv-none-elf-gcc-xpack/releases/tag/v15.2.0-1) install locally in this git repository so that scripts can access it at the path `./xpack-riscv-none-elf-gcc-15.2.0-1/...`

The Nix flake is WIP and only supplies scala and verilator which is enough to build and run the core and simulation scripts manually.

Once your have your environment set up, you'll want to run `./scripts/generate-verilog.sh` to generate verilog for verilator to run. Then run `./scripts/simulate.sh` and pass in the path to the c program you wish to simulate. For example, `./scripts/simulate.sh ./programs/pong.c ./generated/pong.bin`.

The simulation will emit `frame.png` under the `./generated/` folder. The image is a recording of the VGA signal and an approximation of what you might see when connected to a real VGA cable. It won't be completely accurate because the VGA clock is triggered at the same speed as the processor clock in simulation, which may not be the case in reality.


## Contributing
If you'd link to contribute, [join the MIT OpenCompute discord](https://discord.gg/jwgPXeFN7C) and reach out to us there!

## License

[MIT](https://raw.githubusercontent.com/MIT-OpenCompute/dawn-cpu/refs/heads/main/LICENSE)

---

Made with love from [Liam Hanrahan](https://outercloud.dev), [Armaan Gomes](https://armaangomes.com/), and contributors!