`timescale 1ns / 1ps

module tb_Top;

    reg  clk  = 0;
    reg  btnC = 1;

    wire [15:0] led;
    wire        vgaHSync, vgaVSync;
    wire [3:0]  vgaRed, vgaGreen, vgaBlue;

    always #5 clk = ~clk;  // 100MHz

    Top dut (
        .clk      (clk),
        .btnC     (btnC),
        .led      (led),
        .vgaHSync (vgaHSync),
        .vgaVSync (vgaVSync),
        .vgaRed   (vgaRed),
        .vgaGreen (vgaGreen),
        .vgaBlue  (vgaBlue)
    );

    initial begin
        repeat(20) @(posedge clk);
        btnC = 0;
        #10_000_000;  // run for 10ms
        $finish;
    end

endmodule