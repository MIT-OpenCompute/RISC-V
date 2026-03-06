`timescale 1ns / 1ps

// Receives a RISC-V program over UART and writes it into CPU memory
// via io_debug_write. Holds CPU in reset until a 4x 0xFF end sequence
// is received.
//
// Protocol:
//   - Send program bytes little-endian, 4 bytes per word
//   - Send 0xFF 0xFF 0xFF 0xFF to signal end of program
//   - CPU is released from reset automatically after end sequence

module uart_program_loader (
    input  wire        clk,
    input  wire        rst_n,       // external reset (active low)
    input  wire        rx,          // UART RX pin

    // CPU control
    output reg         cpu_reset,   // hold high to keep CPU in reset
    output reg         debug_write,
    output reg  [31:0] debug_write_address,
    output reg  [31:0] debug_write_data
);

    // -------------------------------------------------------
    // UART RX
    // -------------------------------------------------------
    wire [7:0] rx_data;
    wire       rx_valid;

    uart_rx rx_inst (
        .clk       (clk),
        .rst_n     (rst_n),
        .rx        (rx),
        .data_out  (rx_data),
        .data_valid(rx_valid)
    );

    // -------------------------------------------------------
    // Loader FSM
    // -------------------------------------------------------
    // Accumulate 4 bytes into a 32-bit word (little-endian)
    // Detect end sequence: 4 consecutive 0xFF bytes

    reg [31:0] word_buf;          // accumulates current word
    reg [1:0]  byte_idx;          // which byte in current word (0-3)
    reg [31:0] word_address;      // current word address in memory
    reg [1:0]  ff_count;          // consecutive 0xFF bytes seen
    reg        loading;           // are we in loading mode?
    reg        rx_valid_prev;     // edge detect

    // One-cycle pulse on new byte
    wire rx_edge = rx_valid && !rx_valid_prev;

    always @(posedge clk) begin
        if (!rst_n) begin
            cpu_reset           <= 1'b1;
            debug_write         <= 1'b0;
            debug_write_address <= 32'h0;
            debug_write_data    <= 32'h0;
            word_buf            <= 32'h0;
            byte_idx            <= 2'h0;
            word_address        <= 32'h0;
            ff_count            <= 2'h0;
            loading             <= 1'b1;
            rx_valid_prev       <= 1'b0;
        end else begin
            rx_valid_prev <= rx_valid;
            debug_write   <= 1'b0;   // default: no write

            if (loading && rx_edge) begin
                if (rx_data == 8'hFF) begin
                    // Count consecutive 0xFF bytes
                    ff_count <= ff_count + 2'h1;

                    // Still assemble into word in case it's data not end seq
                    word_buf <= {rx_data, word_buf[31:8]};
                    byte_idx <= byte_idx + 2'h1;

                    if (ff_count == 2'h3) begin
                        // 4th 0xFF - end sequence complete, release CPU
                        loading   <= 1'b0;
                        cpu_reset <= 1'b0;
                        ff_count  <= 2'h0;
                    end else begin
                        // Might still be end sequence, don't write yet
                        if (byte_idx == 2'h3) begin
                            byte_idx <= 2'h0;
                        end
                    end
                end else begin
                    // Non-0xFF byte resets end sequence counter
                    ff_count <= 2'h0;

                    // Assemble word little-endian
                    case (byte_idx)
                        2'h0: word_buf <= {word_buf[31:8],  rx_data};
                        2'h1: word_buf <= {word_buf[31:16], rx_data, word_buf[7:0]};
                        2'h2: word_buf <= {word_buf[31:24], rx_data, word_buf[15:0]};
                        2'h3: word_buf <= {rx_data, word_buf[23:0]};
                    endcase

                    if (byte_idx == 2'h3) begin
                        // Full word assembled - write to memory
                        debug_write         <= 1'b1;
                        debug_write_address <= word_address;
                        debug_write_data    <= {rx_data, word_buf[23:0]};
                        word_address        <= word_address + 32'h1;
                        byte_idx            <= 2'h0;
                    end else begin
                        byte_idx <= byte_idx + 2'h1;
                    end
                end
            end
        end
    end

endmodule