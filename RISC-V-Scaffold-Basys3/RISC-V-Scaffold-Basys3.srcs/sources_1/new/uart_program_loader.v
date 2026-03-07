`timescale 1ns / 1ps

module uart_program_loader (
    input  wire        clk,
    input  wire        rst_n,
    input  wire        rx,

    output reg         cpu_reset,
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
    reg [31:0] word_buf;
    reg [1:0]  byte_idx;
    reg [31:0] word_address;
    reg [1:0]  ff_count;
    reg        loading;
    reg        rx_valid_prev;

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
            debug_write   <= 1'b0;

            if (loading && rx_edge) begin
                if (rx_data == 8'hFF) begin
                    ff_count <= ff_count + 2'h1;
                    word_buf <= {rx_data, word_buf[31:8]};

                    if (ff_count == 2'h3) begin
                        // 4th consecutive 0xFF - end sequence, release CPU
                        loading   <= 1'b0;
                        cpu_reset <= 1'b0;
                    end else if (byte_idx == 2'h3) begin
                        // Word complete - write it even though it contains 0xFF
                        debug_write         <= 1'b1;
                        debug_write_address <= word_address;
                        debug_write_data    <= {rx_data, word_buf[23:0]};
                        word_address        <= word_address + 32'h1;
                        byte_idx            <= 2'h0;
                    end else begin
                        byte_idx <= byte_idx + 2'h1;
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