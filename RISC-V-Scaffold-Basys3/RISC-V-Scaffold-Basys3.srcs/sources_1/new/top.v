`timescale 1ns / 1ps

module Top(
    input  wire        clk,
    input  wire        btnC,   // external reset
    input  wire        RsRx,   // UART RX
    output wire        RsTx,   // UART TX (unused)
    output wire [15:0] led
);

    wire rst_n = ~btnC;

    // -------------------------------------------------------
    // UART program loader
    // -------------------------------------------------------
    wire        cpu_reset;
    wire        debug_write;
    wire [31:0] debug_write_address;
    wire [31:0] debug_write_data;

    uart_program_loader loader (
        .clk                 (clk),
        .rst_n               (rst_n),
        .rx                  (RsRx),
        .cpu_reset           (cpu_reset),
        .debug_write         (debug_write),
        .debug_write_address (debug_write_address),
        .debug_write_data    (debug_write_data)
    );

    // -------------------------------------------------------
    // 2-cycle wide enable pulse at ~5Hz
    // Only runs when CPU is not being loaded
    // -------------------------------------------------------
    reg [23:0] ce_counter;
    reg [1:0]  ce_phase;
    reg        ce;

    always @(posedge clk) begin
        if (btnC | cpu_reset) begin
            ce_counter <= 24'd0;
            ce_phase   <= 2'd0;
            ce         <= 1'b0;
        end else begin
            case (ce_phase)
                2'd0: begin
                    ce <= 1'b0;
                    if (ce_counter == 24'd19_999_997) begin
                        ce_counter <= 24'd0;
                        ce_phase   <= 2'd1;
                    end else begin
                        ce_counter <= ce_counter + 24'd1;
                    end
                end
                2'd1: begin
                    ce       <= 1'b1;
                    ce_phase <= 2'd2;
                end
                2'd2: begin
                    ce       <= 1'b1;
                    ce_phase <= 2'd0;
                end
                default: ce_phase <= 2'd0;
            endcase
        end
    end

    // -------------------------------------------------------
    // CPU
    // -------------------------------------------------------
    wire [31:0] debug_1;

    Main cpu (
        .clock                  (clk),
        .reset                  (cpu_reset | btnC),
        .io_execute             (ce),
        .io_debug_write         (debug_write),
        .io_debug_write_address (debug_write_address),
        .io_debug_write_data    (debug_write_data),
        .io_debug_1             (debug_1)
    );

    assign led  = debug_1[15:0];
    assign RsTx = 1'b1;

endmodule