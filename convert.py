with open("./programs/mandelbrot.bin", "rb") as f:
    data = f.read()

with open("./programs/mandelbrot.hex", "w") as f:
    for i in range(0, len(data), 4):
        word = data[i:i+4]
        val = int.from_bytes(word, "little")
        f.write(f"{val:08x}\n")