`timescale 1ns / 1ps
module tb_Top;
    reg        clock;
    reg        reset;
    reg        io_execute;
    reg        io_debug_write;
    reg [31:0] io_debug_write_address;
    reg [31:0] io_debug_write_data;
    wire        io_hsync;
    wire        io_vsync;
    wire [11:0] io_rgb;
    wire        io_blanking;
    wire [9:0]  io_hPos;
    wire [9:0]  io_vPos;

    // 25MHz clock: period = 40ns
    initial clock = 0;
    always #20 clock = ~clock;
    Main dut (
        .clock                  (clock),
        .reset                  (reset),
        .io_execute             (io_execute),
        .io_debug_write         (io_debug_write),
        .io_debug_write_address (io_debug_write_address),
        .io_debug_write_data    (io_debug_write_data),

        .io_hsync               (io_hsync),
        .io_vsync               (io_vsync),
        .io_rgb                 (io_rgb),
        .io_blanking            (io_blanking),
        .io_hPos                (io_hPos),
        .io_vPos                (io_vPos)
    );
    // Task: rising edge step (matches dut.clock.step(1))
    task step;
        input integer n;
        integer i;
        begin
            for (i = 0; i < n; i = i + 1) begin
                @(posedge clock);
                #1; // small delay after edge for signal settling
            end
        end
    endtask
    integer cycle;
    initial begin
        // Initial state
        reset                  = 1;
        io_execute             = 0;
        io_debug_write         = 0;
        io_debug_write_address = 0;
        io_debug_write_data    = 0;
        // Hold reset for 2 cycles
        step(2);
        reset = 0;
        // --- Mirror testbench exactly ---
        // dut.io.debug_write.poke(true.B)
        io_debug_write = 1;
        // poke address=0, data=LUI x2, 4  ; step(1)
        io_debug_write_address = 32'd0;
        io_debug_write_data    = 32'b00000000000000000100_00010_0110111;
        step(1);
        // poke address=1, data=ADDI x1,x1,8 ; step(1)
        io_debug_write_address = 32'd1;
        io_debug_write_data    = 32'b000000001000_00001_000_00001_0010011;
        step(1);
        // poke address=2, data=SW x1,0(x2) ; step(1)
        io_debug_write_address = 32'd2;
        io_debug_write_data    = 32'b000000000000_00010_010_00000_0100011;
        step(1);
        // poke address=3, data=JAL x0,-8 ; step(1)
        io_debug_write_address = 32'd3;
        io_debug_write_data    = 32'b11111111100111111111_00000_1101111;
        step(1);
        // dut.io.debug_write.poke(false.B) ; step(1)
        io_debug_write = 0;
        step(1);
        // dut.io.execute.poke(true.B) ; step(36)
        // Extra cycles needed: each instruction now takes 3 cycles (fetch, wait, execute)
        // instead of 2 (fetch, execute), and load/store takes 4 (fetch, wait, execute, complete)
        io_execute = 1;
        $display("--- Executing for 36 cycles ---");
        for (cycle = 0; cycle < 36; cycle = cycle + 1) begin
            @(posedge clock); #1;
            $display("cycle=%0d  rgb=%03x",
                     cycle, io_rgb);
        end
        $display("--- Done ---");
        $finish;
    end
endmodule