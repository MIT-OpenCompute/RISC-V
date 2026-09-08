
module uart_rx #(
    parameter CLK_FREQ  = 100_000_000,
    parameter BAUD_RATE = 115200
)(
    input  wire       clk,
    input  wire       rst_n,
    input  wire       rxd,
    output wire [7:0] data,
    output wire       valid
);

    localparam CYCLES_PER_BIT = CLK_FREQ / BAUD_RATE;
    localparam HALF_BIT       = CYCLES_PER_BIT / 2;

    reg [15:0] counter;
    reg [3:0]  bitIdx;
    reg [7:0]  shiftReg;
    reg [7:0]  dataReg;
    reg        validReg;

    localparam IDLE  = 2'd0;
    localparam START = 2'd1;
    localparam DATA  = 2'd2;
    localparam STOP  = 2'd3;
    reg [1:0] state;

    // Double-flop synchronizer initialized to 1 (Idle State)
    reg rxSync1, rxSync2;
    always @(posedge clk) begin
        if (!rst_n) begin
            rxSync1 <= 1'b1;
            rxSync2 <= 1'b1;
        end else begin
            rxSync1 <= rxd;
            rxSync2 <= rxSync1;
        end
    end

    // Connect internal registers to outputs
    assign data  = dataReg;
    assign valid = validReg;

    always @(posedge clk) begin
        if (!rst_n) begin
            counter  <= 16'h0;
            bitIdx   <= 4'h0;
            shiftReg <= 8'h0;
            dataReg  <= 8'h0;
            validReg <= 1'b0;
            state    <= IDLE;
        end else begin
            validReg <= 1'b0; // Default to single-cycle pulse

            case (state)
                IDLE: begin
                    if (!rxSync2) begin
                        counter <= HALF_BIT[15:0] - 16'd1;
                        state   <= START;
                    end
                end

                START: begin
                    if (counter == 16'd0) begin
                        if (!rxSync2) begin
                            counter <= CYCLES_PER_BIT[15:0] - 16'd1;
                            bitIdx  <= 4'h0;
                            state   <= DATA;
                        end else begin
                            state <= IDLE;
                        end
                    end else begin
                        counter <= counter - 16'd1;
                    end
                end

                DATA: begin
                    if (counter == 16'd0) begin
                        shiftReg <= {rxSync2, shiftReg[7:1]};
                        counter  <= CYCLES_PER_BIT[15:0] - 16'd1;
                        bitIdx   <= bitIdx + 4'd1;
                        
                        if (bitIdx == 4'd7) begin
                            state <= STOP;
                        end
                    end else begin
                        counter <= counter - 16'd1;
                    end
                end

                STOP: begin
                    if (counter == 16'd0) begin
                        if (rxSync2) begin
                            dataReg  <= shiftReg;
                            validReg <= 1'b1;
                        end
                        state <= IDLE;
                    end else begin
                        counter <= counter - 16'd1;
                    end
                end
            endcase
        end
    end
endmodule