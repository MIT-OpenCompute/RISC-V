// ============================================================================
// async_fifo.v
//
// Standard dual-clock (asynchronous) FIFO, Gray-code pointer synchronization,
// first-word-fall-through (FWFT) read interface. This is the well-known
// Cummings-style reference architecture for async FIFOs -- the industry
// standard approach, not a custom scheme -- chosen specifically because a
// hand-rolled toggle-handshake CDC (ddr3_line_memory_cdc's original design)
// turned out to have a latent race under tight back-to-back request timing.
//
// FWFT semantics: whenever `empty` is low, `dout` is ALREADY the correct
// next word (no extra cycle needed to "prime" it). Asserting `rd_en` for
// one cycle while !empty consumes that word and advances to the next one
// for the following cycle. Do not assert rd_en while empty.
//
// DEPTH = 2^ADDR_WIDTH. Default depth 4 is generous for this application
// (single-outstanding-request protocols), leaving headroom without being
// wastefully deep.
// ============================================================================

`timescale 1ns / 1ps

module async_fifo #(
    parameter DATA_WIDTH = 32,
    parameter ADDR_WIDTH = 2      // depth = 2^ADDR_WIDTH = 4
) (
    // Write side
    input  wire                   wr_clk,
    input  wire                   wr_rst,      // synchronous reset, wr_clk domain
    input  wire                   wr_en,
    input  wire [DATA_WIDTH-1:0]  din,
    output wire                   full,

    // Read side
    input  wire                   rd_clk,
    input  wire                   rd_rst,      // synchronous reset, rd_clk domain
    input  wire                   rd_en,
    output wire [DATA_WIDTH-1:0]  dout,
    output wire                   empty
);

    localparam DEPTH = (1 << ADDR_WIDTH);

    // ------------------------------------------------------------------
    // Storage -- combinational (asynchronous) read gives natural FWFT
    // behavior with no extra latency. Fine for the small depths used here
    // (synthesizes as distributed RAM/LUTRAM or plain registers).
    // ------------------------------------------------------------------
    reg [DATA_WIDTH-1:0] mem [0:DEPTH-1];

    // ------------------------------------------------------------------
    // Write side: binary + Gray write pointer
    //
    // IMPORTANT: wr_ptr_bin_plus1/wr_ptr_gray_plus1 are computed
    // UNCONDITIONALLY from the current registered wr_ptr_bin -- they do
    // NOT depend on wr_en or full. full is then derived from
    // wr_ptr_gray_plus1. The actual register update is separately gated
    // by (wr_en && !full) in the sequential block below. Making full
    // depend on a "next pointer" that itself depends on full (as an
    // earlier version of this file did) creates a genuine combinational
    // loop -- this structure avoids that entirely.
    // ------------------------------------------------------------------
    reg [ADDR_WIDTH:0] wr_ptr_bin;
    reg [ADDR_WIDTH:0] wr_ptr_gray;

    wire [ADDR_WIDTH:0] wr_ptr_bin_plus1  = wr_ptr_bin + 1'b1;
    wire [ADDR_WIDTH:0] wr_ptr_gray_plus1 = (wr_ptr_bin_plus1 >> 1) ^ wr_ptr_bin_plus1;

    always @(posedge wr_clk) begin
        if (wr_rst) begin
            wr_ptr_bin  <= {(ADDR_WIDTH+1){1'b0}};
            wr_ptr_gray <= {(ADDR_WIDTH+1){1'b0}};
        end else if (wr_en && !full) begin
            wr_ptr_bin  <= wr_ptr_bin_plus1;
            wr_ptr_gray <= wr_ptr_gray_plus1;
        end
    end

    always @(posedge wr_clk) begin
        if (wr_en && !full) begin
            mem[wr_ptr_bin[ADDR_WIDTH-1:0]] <= din;
        end
    end

    // ------------------------------------------------------------------
    // Read side: binary + Gray read pointer -- same unconditional-plus1
    // structure, for the same reason (and for consistency/symmetry, even
    // though this side didn't actually form a loop since empty is
    // compared against the CURRENT rd_ptr_gray, not a "next" value).
    // ------------------------------------------------------------------
    reg [ADDR_WIDTH:0] rd_ptr_bin;
    reg [ADDR_WIDTH:0] rd_ptr_gray;

    wire [ADDR_WIDTH:0] rd_ptr_bin_plus1  = rd_ptr_bin + 1'b1;
    wire [ADDR_WIDTH:0] rd_ptr_gray_plus1 = (rd_ptr_bin_plus1 >> 1) ^ rd_ptr_bin_plus1;

    always @(posedge rd_clk) begin
        if (rd_rst) begin
            rd_ptr_bin  <= {(ADDR_WIDTH+1){1'b0}};
            rd_ptr_gray <= {(ADDR_WIDTH+1){1'b0}};
        end else if (rd_en && !empty) begin
            rd_ptr_bin  <= rd_ptr_bin_plus1;
            rd_ptr_gray <= rd_ptr_gray_plus1;
        end
    end

    assign dout = mem[rd_ptr_bin[ADDR_WIDTH-1:0]];   // FWFT: always valid when !empty

    // ------------------------------------------------------------------
    // Cross-domain pointer synchronization (2-FF Gray-code synchronizers,
    // the standard technique -- Gray code guarantees only one bit changes
    // per increment, so a synchronizer sampling mid-transition can only
    // ever be off by one count, never garbage).
    // ------------------------------------------------------------------
    (* ASYNC_REG = "TRUE" *) reg [ADDR_WIDTH:0] rd_ptr_gray_sync1 = 0;
    (* ASYNC_REG = "TRUE" *) reg [ADDR_WIDTH:0] rd_ptr_gray_sync2 = 0;

    always @(posedge wr_clk) begin
        if (wr_rst) begin
            rd_ptr_gray_sync1 <= {(ADDR_WIDTH+1){1'b0}};
            rd_ptr_gray_sync2 <= {(ADDR_WIDTH+1){1'b0}};
        end else begin
            rd_ptr_gray_sync1 <= rd_ptr_gray;
            rd_ptr_gray_sync2 <= rd_ptr_gray_sync1;
        end
    end

    (* ASYNC_REG = "TRUE" *) reg [ADDR_WIDTH:0] wr_ptr_gray_sync1 = 0;
    (* ASYNC_REG = "TRUE" *) reg [ADDR_WIDTH:0] wr_ptr_gray_sync2 = 0;

    always @(posedge rd_clk) begin
        if (rd_rst) begin
            wr_ptr_gray_sync1 <= {(ADDR_WIDTH+1){1'b0}};
            wr_ptr_gray_sync2 <= {(ADDR_WIDTH+1){1'b0}};
        end else begin
            wr_ptr_gray_sync1 <= wr_ptr_gray;
            wr_ptr_gray_sync2 <= wr_ptr_gray_sync1;
        end
    end

    // ------------------------------------------------------------------
    // Full/empty flags -- standard Gray-code comparison logic.
    // Full: next write pointer (Gray) equals the synced read pointer with
    // its top two bits inverted (the classic "wrapped around and caught
    // up" condition for Gray-coded pointers).
    // Empty: read pointer (Gray) equals the synced write pointer exactly.
    // ------------------------------------------------------------------
    assign full  = (wr_ptr_gray_plus1 == {~rd_ptr_gray_sync2[ADDR_WIDTH:ADDR_WIDTH-1],
                                            rd_ptr_gray_sync2[ADDR_WIDTH-2:0]});

    assign empty = (rd_ptr_gray == wr_ptr_gray_sync2);

endmodule