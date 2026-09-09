`timescale 1ns / 1ps

// ============================================================================
// ddr4_line_memory
//
// Parameterized with NUM_BEATS: the number of 128-bit AXI beats that make
// up one logical "line". Default is 1 (current behavior, unchanged).
// Bump NUM_BEATS to widen the line -- burst length (AWLEN/ARLEN), address
// alignment, and the beat-sequencing counters all scale automatically.
//
// Requirements: NUM_BEATS must be a power of two (so the line size stays
// a clean address-aligned power-of-two boundary), and NUM_BEATS-1 must
// fit in AWLEN/ARLEN's 8-bit field (i.e. NUM_BEATS <= 256).
//
// LINE_BYTES = NUM_BEATS * 16   (16 bytes per 128-bit AXI beat)
// LINE_BITS  = NUM_BEATS * 128
// ============================================================================
module ddr4_line_memory #(
    parameter integer NUM_BEATS = 1
) (
    input  wire         c0_sys_clk_p, c0_sys_clk_n,
    input  wire         sys_rst,              // active-high board reset -> PHY/init reset
    output wire         mem_clk, mem_rst,
    output wire         ui_clk1, ui_clk2, ui_clk3,   // piped-out addn_ui_clkout1/2/3
    input  wire         req_valid,
    output wire         req_ready,
    input  wire [31:0]  req_addr,
    input  wire         req_write,
    input  wire [NUM_BEATS*128-1:0] req_wdata,
    output reg           resp_valid,
    output reg  [NUM_BEATS*128-1:0] resp_data,
    output wire          init_calib_complete,

    output wire [16:0]  ddr4_adr,
    output wire [1:0]   ddr4_ba,
    output wire [0:0]   ddr4_bg,
    output wire [0:0]   ddr4_cke,
    output wire [0:0]   ddr4_cs_n,
    output wire [0:0]   ddr4_odt,
    output wire         ddr4_act_n,
    output wire [0:0]   ddr4_ck_c,
    output wire [0:0]   ddr4_ck_t,
    output wire         ddr4_reset_n,
    inout  wire [1:0]   ddr4_dm_dbi_n,
    inout  wire [15:0]  ddr4_dq,
    inout  wire [1:0]   ddr4_dqs_c,
    inout  wire [1:0]   ddr4_dqs_t
);

    // ------------------------------------------------------------------
    // Derived sizing
    // ------------------------------------------------------------------
    localparam integer LINE_BITS   = NUM_BEATS * 128;
    localparam integer ADDR_LSB    = 4 + $clog2(NUM_BEATS);   // bits to zero for line alignment
    localparam [7:0]   AXI_LEN     = NUM_BEATS - 1;           // AWLEN/ARLEN field value
    localparam integer BEAT_BITS   = (NUM_BEATS <= 1) ? 1 : $clog2(NUM_BEATS);

    wire ui_clk, ui_clk_sync_rst;
    assign mem_clk = ui_clk;
    assign mem_rst = ui_clk_sync_rst;

    // AXI-side reset: standard pattern of tying the IP's own synchronous
    // reset output back into its active-low AXI reset input.
    wire c0_ddr4_aresetn = ~ui_clk_sync_rst;

    reg  [29:0]  s_axi_awaddr_r;
    reg          s_axi_awvalid_r;
    wire         s_axi_awready;
    reg          s_axi_wvalid_r;
    wire         s_axi_wready;
    wire         s_axi_bvalid;
    reg          s_axi_bready_r;

    reg  [29:0]  s_axi_araddr_r;
    reg          s_axi_arvalid_r;
    wire         s_axi_arready;
    wire [127:0] s_axi_rdata;
    wire         s_axi_rvalid;
    reg          s_axi_rready_r;

    // wdata_reg holds the full line stable for the duration of the write
    // transaction; s_axi_wdata is a beat-indexed 128-bit slice of it,
    // selected by w_beat_cnt. Indexed part-select ([base +: width]) with a
    // runtime base is legal Verilog, so this works for any NUM_BEATS.
    reg  [LINE_BITS-1:0] wdata_reg;
    reg  [BEAT_BITS-1:0] w_beat_cnt, r_beat_cnt;

    wire [127:0] s_axi_wdata = wdata_reg[w_beat_cnt*128 +: 128];
    wire         s_axi_wlast = (w_beat_cnt == NUM_BEATS-1);

    ddr4_0 u_ddr4_0 (
        .c0_init_calib_complete (init_calib_complete),
        .dbg_clk                (),
        .dbg_bus                (),
        .c0_sys_clk_p           (c0_sys_clk_p),
        .c0_sys_clk_n           (c0_sys_clk_n),

        .c0_ddr4_adr            (ddr4_adr),
        .c0_ddr4_ba             (ddr4_ba),
        .c0_ddr4_bg             (ddr4_bg),
        .c0_ddr4_cke            (ddr4_cke),
        .c0_ddr4_cs_n           (ddr4_cs_n),
        .c0_ddr4_odt            (ddr4_odt),
        .c0_ddr4_act_n          (ddr4_act_n),
        .c0_ddr4_ck_c           (ddr4_ck_c),
        .c0_ddr4_ck_t           (ddr4_ck_t),
        .c0_ddr4_reset_n        (ddr4_reset_n),
        .c0_ddr4_dm_dbi_n       (ddr4_dm_dbi_n),
        .c0_ddr4_dq             (ddr4_dq),
        .c0_ddr4_dqs_c          (ddr4_dqs_c),
        .c0_ddr4_dqs_t          (ddr4_dqs_t),

        .c0_ddr4_ui_clk          (ui_clk),
        .c0_ddr4_ui_clk_sync_rst (ui_clk_sync_rst),
        .c0_ddr4_aresetn         (c0_ddr4_aresetn),
        .sys_rst                 (sys_rst),

        .c0_ddr4_s_axi_awid    (4'h0),
        .c0_ddr4_s_axi_awaddr  (s_axi_awaddr_r),
        .c0_ddr4_s_axi_awlen   (AXI_LEN),
        .c0_ddr4_s_axi_awsize  (3'b100),      // 16 bytes/beat (128-bit), fixed regardless of NUM_BEATS
        .c0_ddr4_s_axi_awburst (2'b01),
        .c0_ddr4_s_axi_awlock  (1'b0),
        .c0_ddr4_s_axi_awcache (4'h0),
        .c0_ddr4_s_axi_awprot  (3'h0),
        .c0_ddr4_s_axi_awqos   (4'h0),
        .c0_ddr4_s_axi_awvalid (s_axi_awvalid_r),
        .c0_ddr4_s_axi_awready (s_axi_awready),

        .c0_ddr4_s_axi_wdata   (s_axi_wdata),
        .c0_ddr4_s_axi_wstrb   (16'hFFFF),    // 128/8 = 16 strobe bits, fixed regardless of NUM_BEATS
        .c0_ddr4_s_axi_wlast   (s_axi_wlast),
        .c0_ddr4_s_axi_wvalid  (s_axi_wvalid_r),
        .c0_ddr4_s_axi_wready  (s_axi_wready),

        .c0_ddr4_s_axi_bready  (s_axi_bready_r),
        .c0_ddr4_s_axi_bid     (),
        .c0_ddr4_s_axi_bresp   (),
        .c0_ddr4_s_axi_bvalid  (s_axi_bvalid),

        .c0_ddr4_s_axi_arid    (4'h0),
        .c0_ddr4_s_axi_araddr  (s_axi_araddr_r),
        .c0_ddr4_s_axi_arlen   (AXI_LEN),
        .c0_ddr4_s_axi_arsize  (3'b100),      // 16 bytes/beat (128-bit), fixed regardless of NUM_BEATS
        .c0_ddr4_s_axi_arburst (2'b01),
        .c0_ddr4_s_axi_arlock  (1'b0),
        .c0_ddr4_s_axi_arcache (4'h0),
        .c0_ddr4_s_axi_arprot  (3'h0),
        .c0_ddr4_s_axi_arqos   (4'h0),
        .c0_ddr4_s_axi_arvalid (s_axi_arvalid_r),
        .c0_ddr4_s_axi_arready (s_axi_arready),

        .c0_ddr4_s_axi_rready  (s_axi_rready_r),
        .c0_ddr4_s_axi_rlast   (),
        .c0_ddr4_s_axi_rvalid  (s_axi_rvalid),
        .c0_ddr4_s_axi_rresp   (),
        .c0_ddr4_s_axi_rid     (),
        .c0_ddr4_s_axi_rdata   (s_axi_rdata),

        .addn_ui_clkout1 (ui_clk1),
        .addn_ui_clkout2 (ui_clk2),
        .addn_ui_clkout3 (ui_clk3)
    );

    // AW+W issued together, one or more beats per burst per NUM_BEATS, then
    // B; or AR issued, one or more beats read back, tracked by beat
    // counters sized for the configured NUM_BEATS.
    localparam S_IDLE  = 3'd0, S_WRITE = 3'd1, S_BRESP = 3'd2,
               S_AR    = 3'd3, S_READ  = 3'd4;

    reg [2:0] state;
    reg aw_done, w_done;
    assign req_ready = (state == S_IDLE) && init_calib_complete;

    always @(posedge ui_clk or posedge ui_clk_sync_rst) begin
        if (ui_clk_sync_rst) begin
            state <= S_IDLE; s_axi_awvalid_r <= 0; s_axi_wvalid_r <= 0; s_axi_bready_r <= 0;
            s_axi_arvalid_r <= 0; s_axi_rready_r <= 0; resp_valid <= 0; aw_done <= 0; w_done <= 0;
            w_beat_cnt <= 0; r_beat_cnt <= 0;
        end else begin
            resp_valid <= 1'b0;

            case (state)
                S_IDLE: begin
                    if (req_valid && req_ready) begin
                        wdata_reg <= req_wdata;
                        if (req_write) begin
                            s_axi_awaddr_r  <= {req_addr[29:ADDR_LSB], {ADDR_LSB{1'b0}}};
                            s_axi_awvalid_r <= 1'b1;
                            s_axi_wvalid_r  <= 1'b1;
                            aw_done         <= 1'b0;
                            w_done          <= 1'b0;
                            w_beat_cnt      <= 0;
                            state           <= S_WRITE;
                        end else begin
                            s_axi_araddr_r  <= {req_addr[29:ADDR_LSB], {ADDR_LSB{1'b0}}};
                            s_axi_arvalid_r <= 1'b1;
                            r_beat_cnt      <= 0;
                            state           <= S_AR;
                        end
                    end
                end

                S_WRITE: begin
                    if (s_axi_awvalid_r && s_axi_awready) begin
                        s_axi_awvalid_r <= 1'b0; aw_done <= 1'b1;
                    end
                    if (s_axi_wvalid_r && s_axi_wready) begin
                        if (w_beat_cnt == NUM_BEATS-1) begin
                            s_axi_wvalid_r <= 1'b0; w_done <= 1'b1;
                        end else begin
                            w_beat_cnt <= w_beat_cnt + 1'b1;
                        end
                    end
                    if ((aw_done || (s_axi_awvalid_r && s_axi_awready)) &&
                        (w_done  || (s_axi_wvalid_r && s_axi_wready && w_beat_cnt == NUM_BEATS-1))) begin
                        s_axi_bready_r <= 1'b1; state <= S_BRESP;
                    end
                end

                S_BRESP: begin
                    if (s_axi_bvalid && s_axi_bready_r) begin
                        s_axi_bready_r <= 1'b0; state <= S_IDLE;
                    end
                end

                S_AR: begin
                    if (s_axi_arvalid_r && s_axi_arready) begin
                        s_axi_arvalid_r <= 1'b0; s_axi_rready_r <= 1'b1;
                        state <= S_READ;
                    end
                end

                S_READ: begin
                    if (s_axi_rvalid && s_axi_rready_r) begin
                        resp_data[r_beat_cnt*128 +: 128] <= s_axi_rdata;
                        if (r_beat_cnt == NUM_BEATS-1) begin
                            s_axi_rready_r <= 1'b0;
                            resp_valid     <= 1'b1;
                            state          <= S_IDLE;
                        end else begin
                            r_beat_cnt <= r_beat_cnt + 1'b1;
                        end
                    end
                end
            endcase
        end
    end
endmodule


// ============================================================================
// ddr4_line_memory_cdc_v2
//
// Also parameterized with NUM_BEATS -- passed straight through to the
// inner ddr4_line_memory instance, and used to size the request/response
// async FIFOs. Everything else (CDC structure, reset synchronization,
// core_clk derivation from ui_clk1) is unchanged.
// ============================================================================
module ddr4_line_memory_cdc_v2 #(
    parameter integer NUM_BEATS = 1
) (
    output wire         core_clk, core_rst,
    input  wire         req_valid,
    output wire         req_ready,
    input  wire [31:0]  req_addr,
    input  wire         req_write,
    input  wire [NUM_BEATS*128-1:0] req_wdata,
    output reg           resp_valid,
    output reg  [NUM_BEATS*128-1:0] resp_data,
    output wire          init_calib_complete,
    input  wire         c0_sys_clk_p, c0_sys_clk_n, sys_rst,
    output wire [16:0]  ddr4_adr,
    output wire [1:0]   ddr4_ba,
    output wire [0:0]   ddr4_bg,
    output wire [0:0]   ddr4_cke,
    output wire [0:0]   ddr4_cs_n,
    output wire [0:0]   ddr4_odt,
    output wire         ddr4_act_n,
    output wire [0:0]   ddr4_ck_c,
    output wire [0:0]   ddr4_ck_t,
    output wire         ddr4_reset_n,
    inout  wire [1:0]   ddr4_dm_dbi_n,
    inout  wire [15:0]  ddr4_dq,
    inout  wire [1:0]   ddr4_dqs_c,
    inout  wire [1:0]   ddr4_dqs_t
);
    localparam integer LINE_BITS      = NUM_BEATS * 128;
    localparam integer REQ_FIFO_WIDTH = 1 + 32 + LINE_BITS;   // write + addr + wdata

    wire mem_clk, mem_rst, mem_req_ready, mem_resp_valid, mem_init_calib_complete;
    wire ui_clk1, ui_clk2, ui_clk3;
    reg  mem_req_valid_r, mem_req_write_r;
    reg  [31:0]  mem_req_addr_r;
    reg  [LINE_BITS-1:0] mem_req_wdata_r;
    wire [LINE_BITS-1:0] mem_resp_data;

    ddr4_line_memory #(.NUM_BEATS(NUM_BEATS)) u_ddr4_line_memory (
        .c0_sys_clk_p(c0_sys_clk_p), .c0_sys_clk_n(c0_sys_clk_n), .sys_rst(sys_rst),
        .mem_clk(mem_clk), .mem_rst(mem_rst),
        .ui_clk1(ui_clk1), .ui_clk2(ui_clk2), .ui_clk3(ui_clk3),
        .req_valid(mem_req_valid_r), .req_ready(mem_req_ready), .req_addr(mem_req_addr_r),
        .req_write(mem_req_write_r), .req_wdata(mem_req_wdata_r),
        .resp_valid(mem_resp_valid), .resp_data(mem_resp_data),
        .init_calib_complete(mem_init_calib_complete),
        .ddr4_adr(ddr4_adr), .ddr4_ba(ddr4_ba), .ddr4_bg(ddr4_bg), .ddr4_cke(ddr4_cke),
        .ddr4_cs_n(ddr4_cs_n), .ddr4_odt(ddr4_odt), .ddr4_act_n(ddr4_act_n),
        .ddr4_ck_c(ddr4_ck_c), .ddr4_ck_t(ddr4_ck_t), .ddr4_reset_n(ddr4_reset_n),
        .ddr4_dm_dbi_n(ddr4_dm_dbi_n), .ddr4_dq(ddr4_dq),
        .ddr4_dqs_c(ddr4_dqs_c), .ddr4_dqs_t(ddr4_dqs_t)
    );

    // core_clk is ui_clk1 (addn_ui_clkout1) -- exposed so external logic
    // (e.g. the CPU) can be clocked from the same net.
    assign core_clk = ui_clk1;

    // Reset synchronizer into the core_clk (ui_clk1) domain, asynchronously
    // asserted from mem_rst (the primary ui_clk domain's sync reset).
    (* ASYNC_REG = "TRUE" *) reg [1:0] core_rst_sync = 2'b11;
    always @(posedge core_clk or posedge mem_rst) begin
        if (mem_rst) core_rst_sync <= 2'b11;
        else         core_rst_sync <= {core_rst_sync[0], 1'b0};
    end
    assign core_rst = core_rst_sync[1];

    (* ASYNC_REG = "TRUE" *) reg calib_sync_ff1 = 1'b0, calib_sync_ff2 = 1'b0;
    always @(posedge core_clk) begin
        if (core_rst) {calib_sync_ff2, calib_sync_ff1} <= 2'b0;
        else          {calib_sync_ff2, calib_sync_ff1} <= {calib_sync_ff1, mem_init_calib_complete};
    end
    assign init_calib_complete = calib_sync_ff2;

    wire req_fifo_full, req_fifo_empty, resp_fifo_full, resp_fifo_empty;
    wire [REQ_FIFO_WIDTH-1:0] req_fifo_dout;
    reg  req_fifo_rd_en, resp_fifo_wr_en, resp_fifo_rd_en;
    wire [LINE_BITS-1:0] resp_fifo_dout;
    reg  [LINE_BITS-1:0] resp_fifo_din;

    reg core_busy;
    assign req_ready = !core_busy && !req_fifo_full;

    async_fifo #(.DATA_WIDTH(REQ_FIFO_WIDTH), .ADDR_WIDTH(2)) u_req_fifo (
        .wr_clk(core_clk), .wr_rst(core_rst), .wr_en(req_valid && req_ready), .din({req_write, req_addr, req_wdata}), .full(req_fifo_full),
        .rd_clk(mem_clk), .rd_rst(mem_rst), .rd_en(req_fifo_rd_en), .dout(req_fifo_dout), .empty(req_fifo_empty)
    );

    async_fifo #(.DATA_WIDTH(LINE_BITS), .ADDR_WIDTH(2)) u_resp_fifo (
        .wr_clk(mem_clk), .wr_rst(mem_rst), .wr_en(resp_fifo_wr_en), .din(resp_fifo_din), .full(resp_fifo_full),
        .rd_clk(core_clk), .rd_rst(core_rst), .rd_en(resp_fifo_rd_en), .dout(resp_fifo_dout), .empty(resp_fifo_empty)
    );

    reg pending_is_read;
    always @(posedge core_clk) begin
        if (core_rst) begin
            core_busy       <= 1'b0;
            pending_is_read <= 1'b0;
            resp_fifo_rd_en <= 1'b0;
            resp_valid      <= 1'b0;
            resp_data       <= {LINE_BITS{1'b0}};
        end else begin
            resp_fifo_rd_en <= 1'b0;
            resp_valid      <= 1'b0;

            if (req_valid && req_ready) begin
                core_busy       <= 1'b1;
                pending_is_read <= !req_write;
            end

            if (core_busy && !resp_fifo_empty && !resp_fifo_rd_en) begin
                resp_fifo_rd_en <= 1'b1;
                core_busy       <= 1'b0;
                if (pending_is_read) begin
                    resp_data  <= resp_fifo_dout;
                    resp_valid <= 1'b1;
                end
            end
        end
    end

    reg [1:0] mstate;
    reg mem_write_latched;

    always @(posedge mem_clk) begin
        if (mem_rst) begin
            mstate <= 0; req_fifo_rd_en <= 0; mem_req_valid_r <= 0; resp_fifo_wr_en <= 0;
        end else begin
            req_fifo_rd_en <= 0; resp_fifo_wr_en <= 0;
            case (mstate)
                0: if (!req_fifo_empty && !req_fifo_rd_en) begin
                    req_fifo_rd_en    <= 1;
                    mem_req_addr_r    <= req_fifo_dout[LINE_BITS +: 32];
                    mem_req_write_r   <= req_fifo_dout[LINE_BITS+32];
                    mem_req_wdata_r   <= req_fifo_dout[LINE_BITS-1:0];
                    mem_write_latched <= req_fifo_dout[LINE_BITS+32];
                    mem_req_valid_r   <= 1; mstate <= 1;
                end
                1: if (mem_req_valid_r && mem_req_ready) begin
                    mem_req_valid_r <= 0; mstate <= 2;
                end
                2: begin
                    if (mem_write_latched && mem_req_ready) begin
                        resp_fifo_wr_en <= 1; resp_fifo_din <= {LINE_BITS{1'b0}}; mstate <= 0;
                    end else if (!mem_write_latched && mem_resp_valid) begin
                        resp_fifo_wr_en <= 1; resp_fifo_din <= mem_resp_data; mstate <= 0;
                    end
                end
            endcase
        end
    end
endmodule