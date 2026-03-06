`timescale 1ns / 1ps
module Top(
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
    // -------------------------------------------------------
    // Clock divider: 100MHz -> 25MHz
    // -------------------------------------------------------
    reg [1:0] clk_div;
    always @(posedge clk) begin
        if (btnC) clk_div <= 2'h0;
        else      clk_div <= clk_div + 2'h1;
    end
    wire clk_25 = clk_div[1];

    // -------------------------------------------------------
    // UART program loader
    // -------------------------------------------------------
    wire        cpu_reset;
    wire        debug_write;
    wire [31:0] debug_write_address;
    wire [31:0] debug_write_data;

    uart_program_loader loader (
        .clk                 (clk_25),
        .rst_n               (~btnC),
        .rx                  (RsRx),
        .cpu_reset           (cpu_reset),
        .debug_write         (debug_write),
        .debug_write_address (debug_write_address),
        .debug_write_data    (debug_write_data)
    );

    wire reset = btnC | cpu_reset;

    // -------------------------------------------------------
    // ce: single pulse per clock cycle - CPU runs at full speed
    // but we gate with a slow counter so we can see LEDs
    // Counter fires one ce pulse, CPU stage machine runs freely
    // -------------------------------------------------------
    reg  [22:0] ce_counter;
    reg         ce;

    always @(posedge clk_25) begin
        if (reset) begin
            ce_counter <= 23'd0;
            ce         <= 1'b0;
        end else begin
            ce <= 1'b0;
            if (ce_counter == 23'd0) begin
                ce <= 1'b1;
            end
            if (ce_counter == 23'd4_999_999) begin
                ce_counter <= 23'd0;
            end else begin
                ce_counter <= ce_counter + 23'd1;
            end
        end
    end

    // -------------------------------------------------------
    // CPU + VGA
    // -------------------------------------------------------
    wire [31:0] debug_1;
    wire [11:0] rgb;
    wire        blanking;

    Main cpu (
        .clock                  (clk_25),
        .reset                  (reset),
        .io_execute             (ce),
        .io_debug_write         (debug_write),
        .io_debug_write_address (debug_write_address),
        .io_debug_write_data    (debug_write_data),
        .io_debug_1             (debug_1),
        .io_busy                (),
        .io_hsync               (vgaHSync),
        .io_vsync               (vgaVSync),
        .io_rgb                 (rgb),
        .io_blanking            (blanking),
        .io_hPos                (),
        .io_vPos                ()
    );

    assign vgaRed   = blanking ? 4'h0 : rgb[11:8];
    assign vgaGreen = blanking ? 4'h0 : rgb[7:4];
    assign vgaBlue  = blanking ? 4'h0 : rgb[3:0];
    assign led      = debug_1[15:0];
    assign RsTx     = 1'b1;
endmodules