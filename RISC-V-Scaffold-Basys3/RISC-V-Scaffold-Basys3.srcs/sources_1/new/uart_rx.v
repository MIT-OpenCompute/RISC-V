`timescale 1ns / 1ps
//////////////////////////////////////////////////////////////////////////////////
// Company: 
// Engineer: 
// 
// Create Date: 02/26/2026 12:18:53 AM
// Design Name: 
// Module Name: uart_rx
// Project Name: 
// Target Devices: 
// Tool Versions: 
// Description: 
// 
// Dependencies: 
// 
// Revision:
// Revision 0.01 - File Created
// Additional Comments:
// 
//////////////////////////////////////////////////////////////////////////////////


module uart_rx(
    input wire clk,
    input wire rst_n,
    input wire rx,
    output reg [7:0] data_out,
    output reg data_valid
    );
    
    localparam IDLE  = 2'd0;
    localparam START = 2'd1;
    localparam DATA  = 2'd2;
    localparam STOP  = 2'd3;
    reg rx_sync_0;
    reg rx_sync_1;
    
    reg [7:0] data_packet = 0;
    reg [2:0] bit_idx = 0;
    reg [1:0] state = 0;
    reg [9:0] counter = 0;
    
    
    always @(posedge clk) begin
    if(!rst_n) begin 
        rx_sync_0 <=0;
        rx_sync_1 <=0;
        data_packet <= 0;
        bit_idx <= 0;
        state <= 0;
        counter <= 0;
        data_valid <= 0;
    
    end else begin 
        rx_sync_0 <= rx;
        rx_sync_1 <= rx_sync_0;
        case(state)
        IDLE: begin 
            data_valid <= 0;
            if(!rx_sync_1) begin
                counter <= 1;
                state <= START;
            end            

        end
        START: begin
            counter <= counter +1;
            if(counter == 433) begin
                if(!rx_sync_1) begin
                    counter <= 1;
                    state <= DATA;
                end else begin 
                    state <= IDLE;
                end
            end
        end
        DATA: begin
            counter <= counter +1;
            if(counter == 867) begin
                counter<=0;
                data_packet[bit_idx] <= rx_sync_1;
                if(bit_idx == 7) begin
                    state <= STOP;
                    bit_idx <= 0;
                end else begin 
                    bit_idx <= bit_idx+1;
                end
            end
        end
        STOP: begin
            
            if(rx_sync_1) begin 
                data_valid <= 1;
                data_out <= data_packet;
                state<=IDLE;
            end


            
        end

        endcase
    
    
    
    end
    
    end
endmodule
