`timescale 1ns / 1ps

module Top(
    input  wire        clk,
    input  wire        btnC,
    output wire [15:0] led
);

    // -------------------------------------------------------
    // Generate a 2-cycle wide enable pulse at ~5Hz
    // Period = 20_000_000 cycles at 100MHz
    // We pulse ce high for 2 consecutive cycles each period
    // -------------------------------------------------------
    reg [23:0] ce_counter;
    reg [1:0]  ce_phase;  // counts 0,1,2 then back to 0
    reg        ce;

    always @(posedge clk) begin
        if (btnC) begin
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
                    // First execute pulse - memory latches address
                    ce         <= 1'b1;
                    ce_phase   <= 2'd2;
                end
                2'd2: begin
                    // Second execute pulse - memory returns data
                    ce         <= 1'b1;
                    ce_phase   <= 2'd0;
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
        .clock                 (clk),
        .reset                 (btnC),
        .io_execute            (ce),
        .io_debug_write        (1'b0),
        .io_debug_write_address(32'h0),
        .io_debug_write_data   (32'h0),
        .io_debug_1            (debug_1)
    );

    assign led = debug_1[15:0];

endmodule