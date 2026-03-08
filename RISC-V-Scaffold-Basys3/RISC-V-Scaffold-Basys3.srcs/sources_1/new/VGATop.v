`timescale 1ns / 1ps
module VGATop(
    input  wire        clk,
    input  wire        btnC,
    input  wire        RsRx,
    output wire        RsTx,
    output wire [15:0] led,
    output wire        vgaHSync,
    output wire        vgaVSync,
    output wire [3:0]  vgaRed,
    output wire [3:0]  vgaGreen,
    output wire [3:0]  vgaBlue
);
    reg [1:0] clk_div;
    always @(posedge clk) begin
        if (btnC) clk_div <= 2'h0;
        else      clk_div <= clk_div + 2'h1;
    end
    wire clk_25 = clk_div[1];

    // Write 0xFFF (white) to VGA address 0 on the first cycle, then stop
    reg written;
    reg vga_write;
    always @(posedge clk_25) begin
        if (btnC) begin
            written   <= 1'b0;
            vga_write <= 1'b0;
        end else if (!written) begin
            vga_write <= 1'b1;
            written   <= 1'b1;
        end else begin
            vga_write <= 1'b0;
        end
    end

    wire [11:0] rgb;
    wire        blanking;

    VGAController vga (
        .clock          (clk_25),
        .reset          (btnC),
        .io_address     (32'h0),
        .io_write       (vga_write),
        .io_write_value (32'hFFF),
        .io_hsync       (vgaHSync),
        .io_vsync       (vgaVSync),
        .io_rgb         (rgb),
        .io_blanking    (blanking)
    );

    assign vgaRed   = blanking ? 4'h0 : rgb[11:8];
    assign vgaGreen = blanking ? 4'h0 : rgb[7:4];
    assign vgaBlue  = blanking ? 4'h0 : rgb[3:0];

    reg [25:0] alive_counter;
    always @(posedge clk_25) begin
        if (btnC) alive_counter <= 0;
        else      alive_counter <= alive_counter + 1;
    end
    assign led  = alive_counter[25:10];
    assign RsTx = 1'b1;
endmodule