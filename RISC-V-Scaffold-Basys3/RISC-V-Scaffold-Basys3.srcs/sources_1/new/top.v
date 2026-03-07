`timescale 1ns / 1ps
module Top(
    input  wire        clk,
    input  wire        btnC,
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
    // Hardcoded program loader state machine
    // Writes 4 instructions then releases CPU to execute
    // -------------------------------------------------------
    reg        reset_reg;
    reg        debug_write;
    reg [31:0] debug_write_address;
    reg [31:0] debug_write_data;
    reg        execute;
    reg [2:0]  load_state;

    always @(posedge clk_25) begin
        if (btnC) begin
            reset_reg           <= 1;
            debug_write         <= 0;
            debug_write_address <= 0;
            debug_write_data    <= 0;
            execute             <= 0;
            load_state          <= 0;
        end else begin
            case (load_state)
                3'd0: begin
                    // Release reset, start writing
                    reset_reg           <= 0;
                    debug_write         <= 1;
                    debug_write_address <= 32'd0;
                    debug_write_data    <= 32'h00004137; // LUI x2, 4
                    load_state          <= 3'd1;
                end
                3'd1: begin
                    debug_write_address <= 32'd1;
                    debug_write_data    <= 32'h00808093; // ADDI x1, x1, 8
                    load_state          <= 3'd2;
                end
                3'd2: begin
                    debug_write_address <= 32'd2;
                    debug_write_data    <= 32'h00112023; // SW x0, 0(x2)
                    load_state          <= 3'd3;
                end
                3'd3: begin
                    debug_write_address <= 32'd3;
                    debug_write_data    <= 32'hFF9FF06F; // JAL x0, -8
                    load_state          <= 3'd4;
                end
                3'd4: begin
                    // Stop writing, start executing
                    debug_write         <= 0;
                    debug_write_address <= 0;
                    debug_write_data    <= 0;
                    execute             <= 1;
                    load_state          <= 3'd5;
                end
                default: begin
                    // Stay in execute mode
                end
            endcase
        end
    end

    // -------------------------------------------------------
    // CPU + VGA
    // -------------------------------------------------------
    wire [11:0] rgb;
    wire        blanking;
    wire [31:0] debug_1, debug_2;

    Main cpu (
        .clock                  (clk_25),
        .reset                  (reset_reg),
        .io_execute             (execute),
        .io_debug_write         (debug_write),
        .io_debug_write_address (debug_write_address),
        .io_debug_write_data    (debug_write_data),
        .io_debug_1             (debug_1),
        .io_debug_2             (debug_2),
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

    // debug_1 = register 1, debug_2 = program counter
    assign led = debug_1[15:0];
endmodule