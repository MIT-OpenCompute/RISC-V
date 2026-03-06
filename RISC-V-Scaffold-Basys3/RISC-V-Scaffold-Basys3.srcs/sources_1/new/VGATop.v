`timescale 1ns / 1ps

module VGATop(
    input  wire        clk,      // 100MHz
    input  wire        btnC,     // reset
    output wire        vgaHSync,
    output wire        vgaVSync,
    output wire [3:0]  vgaRed,
    output wire [3:0]  vgaGreen,
    output wire [3:0]  vgaBlue
);

    // -------------------------------------------------------
    // Clock divider: 100MHz -> 25MHz (/4 using 2-bit counter)
    // -------------------------------------------------------
    reg [1:0] clk_div;
    always @(posedge clk) begin
        if (btnC) clk_div <= 2'h0;
        else      clk_div <= clk_div + 2'h1;
    end
    wire clk_25 = clk_div[1]; // toggles at 25MHz

    // -------------------------------------------------------
    // VGA Controller
    // -------------------------------------------------------
    wire [11:0] rgb;
    wire        blanking;

    VGAController vga (
        .clock           (clk_25),
        .reset           (btnC),
        .io_address      (32'h0),
        .io_write        (1'b0),
        .io_write_value  (32'h0),
        .io_hsync        (vgaHSync),
        .io_vsync        (vgaVSync),
        .io_rgb          (rgb),
        .io_blanking     (blanking),
        .io_hPos         (),   // unused
        .io_vPos         ()    // unused
    );

    // rgb[11:8] = red, rgb[7:4] = green, rgb[3:0] = blue
    // Blank output when not in active region
    assign vgaRed   = blanking ? 4'h0 : rgb[11:8];
    assign vgaGreen = blanking ? 4'h0 : rgb[7:4];
    assign vgaBlue  = blanking ? 4'h0 : rgb[3:0];

endmodule