`timescale 1ns / 1ps

// ============================================================================
// memory_interface_ddr4_bypass_cache_top
//
// DDR4 port of memory_interface_ddr3_bypass_cache_top. Structurally the
// same design (UART bootloader -> DDR4 line memory -> CPU -> HDMI), with
// these changes from the DDR3 version:
//   - DDR4 via ddr4_line_memory_cdc_v2, whose core_clk/core_rst are now
//     OUTPUTS (driven internally from the DDR4 MIG's addn_ui_clkout1).
//     clk_cpu and the CPU-side reset domain are sourced directly from
//     those outputs.
//   - HDMI output goes through a TFP410 parallel RGB interface instead of
//     on-FPGA TMDS serialization.
//   - Seven-segment displays and the extra LED bits from the DDR3 version
//     removed -- this board only has leds[1:0].
//   - Parameterized with NUM_BEATS, matching ddr4_line_memory_cdc_v2's own
//     NUM_BEATS. Main's memory-side ports (io_mem_req_bits_wdata,
//     io_mem_resp) now scale with LINE_BITS directly -- Main is expected
//     to widen along with it, so there's no separate CPU-side width
//     adapter here anymore.
// ============================================================================
module memory_interface_ddr4_bypass_cache_top #(
    parameter integer NUM_BEATS = 4   // must match the NUM_BEATS used for ddr4_line_memory_cdc_v2
) (
    input  wire         CLOCK_27MHZ,

    // DDR4 reference clock (differential)
    input  wire         C0_SYS_CLK_0_clk_p,
    input  wire         C0_SYS_CLK_0_clk_n,

    input  wire         UART_TXD,
    output wire         UART_RXD,

    output wire [1:0]   leds,

    // DDR4 physical interface
    output wire [16:0]  ddr4_pins_adr,
    output wire [1:0]   ddr4_pins_ba,
    output wire [0:0]   ddr4_pins_bg,
    output wire [0:0]   ddr4_pins_cke,
    output wire [0:0]   ddr4_pins_cs_n,
    output wire [0:0]   ddr4_pins_odt,
    output wire         ddr4_pins_act_n,
    output wire [0:0]   ddr4_pins_ck_c,
    output wire [0:0]   ddr4_pins_ck_t,
    output wire         ddr4_pins_reset_n,
    inout  wire [1:0]   ddr4_pins_dm_n,
    inout  wire [15:0]  ddr4_pins_dq,
    inout  wire [1:0]   ddr4_pins_dqs_c,
    inout  wire [1:0]   ddr4_pins_dqs_t,

    // HDMI parallel encoder interface (TFP410)
    output wire         HDMI_CLK,
    output wire [23:0]  HDMI_DATA,
    output wire         HDMI_DE,
    output wire         HDMI_HSYNC,
    output wire         HDMI_VSYNC,
    output wire         HDMI_BSEL,
    output wire         HDMI_CTL1,
    output wire         HDMI_CTL2,
    output wire         HDMI_CTL3,
    output wire         HDMI_DKEN,
    output wire         HDMI_DSEL,
    output wire         HDMI_EDGE,
    output wire         HDMI_ISEL,
    output wire         HDMI_MSEN,
    output wire         HDMI_PD
//    input wire RP2040_UA/RTRX
);

    // ------------------------------------------------------------------
    // Shared power-on reset (no reset button on this board)
    // Feeds both the pixel clock wizard's reset and the DDR4 PHY's
    // sys_rst -- everything downstream (CPU, UART, HDMI logic) ultimately
    // derives its own reset from these through the DDR4 CDC wrapper.
    // ------------------------------------------------------------------
    reg [7:0] por_shift = 8'hFF;
    wire      por_rst = por_shift[7];

    always @(posedge CLOCK_27MHZ) begin
        por_shift <= {por_shift[6:0], 1'b0};
    end

    // ------------------------------------------------------------------
    // Line-size derived constants (must track ddr4_line_memory's NUM_BEATS)
    // ------------------------------------------------------------------
    localparam integer LINE_BYTES = NUM_BEATS * 16;
    localparam integer LINE_BITS  = NUM_BEATS * 128;

    // ------------------------------------------------------------------
    // Pixel clock for HDMI (TFP410 side only -- no serializer clock needed)
    // Bypassed for now: clk_pix is tied straight to CLOCK_27MHZ rather
    // than going through clk_wiz_0. Re-enable the wizard below if/when you
    // want a true ~25 MHz pixel clock instead of 27 MHz.
    // ------------------------------------------------------------------
    wire clk_pix = CLOCK_27MHZ;
    wire pix_locked = 1'b1;

//    clk_wiz_0 u_clk_wiz_0 (
//        .clk_in1  (CLOCK_27MHZ),
//        .reset    (por_rst),
//        .clk_out1 (clk_pix),      // configure for ~25.175/25 MHz out from 27 MHz in
//        .locked   (pix_locked)
//    );

    // ------------------------------------------------------------------
    // DDR4 memory + CDC wrapper.
    // core_clk/core_rst are OUTPUTS -- this is now the CPU's clock domain.
    // ------------------------------------------------------------------
    wire         clk_cpu, core_rst;
    wire         init_calib_complete;

    reg          req_valid;
    wire         req_ready;
    reg  [31:0]  req_addr;
    reg  [LINE_BITS-1:0] req_wdata;
    reg          write_ack_pulse;
    wire         cpu_mem_req_valid;
    wire         cpu_mem_req_ready;
    wire [31:0]  cpu_mem_req_addr;
    wire         cpu_mem_req_write;
    wire [LINE_BITS-1:0] cpu_mem_req_wdata;   // scales with NUM_BEATS -- Main widens to match

    reg          interlock_req_valid;
    reg          interlock_cpu_ready;
    wire [LINE_BITS-1:0] cdc_resp_data;
    wire         cdc_resp_valid;

    // During UART loading, route requests from the loader; once the
    // loader hits EOF, hand control to the CPU-facing interlock FSM.
    wire         cdc_req_valid = eof_reached ? interlock_req_valid : req_valid;
    wire [31:0]  cdc_req_addr  = eof_reached ? cpu_mem_req_addr    : req_addr;
    wire         cdc_req_write = eof_reached ? cpu_mem_req_write   : 1'b1;
    wire [LINE_BITS-1:0] cdc_req_wdata = eof_reached ? cpu_mem_req_wdata : req_wdata;

    wire         cdc_req_ready;
    assign req_ready         = eof_reached ? 1'b0 : cdc_req_ready;
    assign cpu_mem_req_ready = eof_reached ? interlock_cpu_ready : 1'b0;

    localparam M_IDLE       = 2'd0,
               M_ADDR_PHASE = 2'd1,
               M_DATA_WAIT  = 2'd2;

    reg [1:0] mig_bridge_state = M_IDLE;

    always @(posedge clk_cpu or posedge core_rst) begin
        if (core_rst) begin
            mig_bridge_state    <= M_IDLE;
            interlock_req_valid <= 1'b0;
            interlock_cpu_ready <= 1'b0;
            write_ack_pulse     <= 1'b0;
        end else if (eof_reached) begin
            write_ack_pulse <= 1'b0;
            case (mig_bridge_state)
                M_IDLE: begin
                    interlock_cpu_ready <= 1'b0;
                    if (cpu_mem_req_valid) begin
                        interlock_req_valid <= 1'b1;
                        mig_bridge_state    <= M_ADDR_PHASE;
                    end
                end

                M_ADDR_PHASE: begin
                    if (cdc_req_ready) begin
                        interlock_req_valid <= 1'b0;
                        if (cpu_mem_req_write) begin
                            interlock_cpu_ready <= 1'b1;
                            write_ack_pulse     <= 1'b1;
                            mig_bridge_state    <= M_IDLE;
                        end else begin
                            mig_bridge_state    <= M_DATA_WAIT;
                        end
                    end
                end

                M_DATA_WAIT: begin
                    interlock_req_valid <= 1'b0;
                    if (cdc_resp_valid) begin
                        interlock_cpu_ready <= 1'b1;
                        mig_bridge_state    <= M_IDLE;
                    end
                end

                default: mig_bridge_state <= M_IDLE;
            endcase
        end else begin
            mig_bridge_state    <= M_IDLE;
            interlock_req_valid <= 1'b0;
            interlock_cpu_ready <= 1'b0;
            write_ack_pulse     <= 1'b0;
        end
    end

    ddr4_line_memory_cdc_v2 #(.NUM_BEATS(NUM_BEATS)) u_ddr4_line_memory_cdc (
        .core_clk(clk_cpu), .core_rst(core_rst),
        .req_valid(cdc_req_valid), .req_ready(cdc_req_ready),
        .req_addr(cdc_req_addr), .req_write(cdc_req_write),
        .req_wdata(cdc_req_wdata), .resp_valid(cdc_resp_valid),
        .resp_data(cdc_resp_data), .init_calib_complete(init_calib_complete),
        .c0_sys_clk_p(C0_SYS_CLK_0_clk_p), .c0_sys_clk_n(C0_SYS_CLK_0_clk_n), .sys_rst(por_rst),
        .ddr4_adr(ddr4_pins_adr), .ddr4_ba(ddr4_pins_ba), .ddr4_bg(ddr4_pins_bg),
        .ddr4_cke(ddr4_pins_cke), .ddr4_cs_n(ddr4_pins_cs_n), .ddr4_odt(ddr4_pins_odt),
        .ddr4_act_n(ddr4_pins_act_n), .ddr4_ck_c(ddr4_pins_ck_c), .ddr4_ck_t(ddr4_pins_ck_t),
        .ddr4_reset_n(ddr4_pins_reset_n), .ddr4_dm_dbi_n(ddr4_pins_dm_n), .ddr4_dq(ddr4_pins_dq),
        .ddr4_dqs_c(ddr4_pins_dqs_c), .ddr4_dqs_t(ddr4_pins_dqs_t)
    );

    // ------------------------------------------------------------------
    // UART RX Interface
    // ------------------------------------------------------------------
    localparam CPU_CLK_FREQ_HZ = 167_000_000;

    wire [7:0] rx_data;
    wire       rx_valid;
    reg        rx_valid_r;

    uart_rx #(.CLK_FREQ(CPU_CLK_FREQ_HZ), .BAUD_RATE(6000000)) u_uart_rx (
        .clk(clk_cpu), .rst_n(!core_rst), .rxd(UART_TXD),
        .data(rx_data), .valid(rx_valid)
    );

    always @(posedge clk_cpu) rx_valid_r <= rx_valid;
    wire rx_pulse = rx_valid & ~rx_valid_r;

    reg [23:0] uart_act_timer;
    reg        uart_led;
    always @(posedge clk_cpu) begin
        if (rx_pulse) begin uart_act_timer <= 24'd5_000_000; uart_led <= 1'b1; end
        else if (uart_act_timer > 0) uart_act_timer <= uart_act_timer - 1'b1;
        else uart_led <= 1'b0;
    end

    // ------------------------------------------------------------------
    // UART Line Packer & Writer FSM -- accumulates LINE_BYTES bytes
    // (= NUM_BEATS * 16) before firing a write, instead of a fixed 16.
    // ------------------------------------------------------------------
    localparam S_IDLE       = 2'd0,
               S_WRITE_WAIT = 2'd1;

    reg [1:0]   state;
    reg [LINE_BITS-1:0]           line_buf;
    reg [$clog2(LINE_BYTES)-1:0]  byte_cnt;
    reg [5:0]   ff_cnt;
    reg         eof_reached;
    reg         cpu_reset_n;

    always @(posedge clk_cpu or posedge core_rst) begin
        if (core_rst) begin
            state       <= S_IDLE;
            line_buf    <= {LINE_BITS{1'b0}};
            byte_cnt    <= 0;
            ff_cnt      <= 3'h0;
            eof_reached <= 1'b0;
            cpu_reset_n <= 1'b0;
            req_valid   <= 1'b0;
            req_wdata   <= {LINE_BITS{1'b0}};
            req_addr    <= 32'h0;
        end else begin
            case (state)
                S_IDLE: begin
                    req_valid <= 1'b0;

                    if (rx_pulse && !eof_reached && init_calib_complete) begin
                        if (rx_data == 8'hFE) begin
                            ff_cnt <= ff_cnt + 1'b1;
                            if (ff_cnt == 6'd31) begin
                                eof_reached <= 1'b1;
                                cpu_reset_n <= 1'b1;
                            end
                        end else begin
                            ff_cnt <= 3'h0;
                        end

                        line_buf <= {rx_data, line_buf[LINE_BITS-1:8]};

                        if (byte_cnt == LINE_BYTES-1) begin
                            byte_cnt  <= 0;
                            req_wdata <= {rx_data, line_buf[LINE_BITS-1:8]};
                            req_valid <= 1'b1;
                            state     <= S_WRITE_WAIT;
                        end else begin
                            byte_cnt <= byte_cnt + 1'b1;
                        end
                    end
                end

                S_WRITE_WAIT: begin
                    req_valid <= 1'b0;

                    if (req_ready) begin
                        req_addr <= req_addr + LINE_BYTES[31:0];
                        state    <= S_IDLE;
                    end
                end

                default: state <= S_IDLE;
            endcase
        end
    end

    // ------------------------------------------------------------------
    // Main CPU Core Execution Space
    // ------------------------------------------------------------------
    wire [31:0] debug_reg;
    wire [31:0] debug_pc;
    wire        io_hsync;
    wire        io_vsync;
    wire [11:0] io_rgb;
    wire        io_blanking;
    wire        mem_stall;

    Main u_main (
        .clock                  (clk_cpu),
        .reset                  (~cpu_reset_n),
        .io_execute             (eof_reached),

        .io_vga_clk             (clk_pix),
        .io_hsync               (io_hsync),
        .io_vsync               (io_vsync),
        .io_rgb                 (io_rgb),
        .io_blanking            (io_blanking),

        .io_mem_req_ready       (cpu_mem_req_ready),
        .io_mem_req_valid       (cpu_mem_req_valid),
        .io_mem_req_bits_addr   (cpu_mem_req_addr),
        .io_mem_req_bits_write  (cpu_mem_req_write),
        .io_mem_req_bits_wdata  (cpu_mem_req_wdata),
        .io_mem_resp            (cdc_resp_data),
        .io_mem_valid           (cdc_resp_valid || write_ack_pulse),

//        .io_debug_reg           (debug_reg),
//        .io_debug_pc            (debug_pc),

        .io_rxd(UART_TXD || !eof_reached),
        .io_txd(UART_RXD)
//        .io_mem_stall(mem_stall)
    );

    // ------------------------------------------------------------------
    // HDMI output via TFP410 parallel RGB interface
    // ------------------------------------------------------------------
    wire [7:0] hdmi_r = {io_rgb[11:8], 4'b0};
    wire [7:0] hdmi_g = {io_rgb[7:4],  4'b0};
    wire [7:0] hdmi_b = {io_rgb[3:0],  4'b0};

    assign HDMI_CLK    = clk_pix;
    assign HDMI_DATA   = {hdmi_r, hdmi_g, hdmi_b};
    assign HDMI_DE     = !io_blanking;
    assign HDMI_HSYNC  = io_hsync;
    assign HDMI_VSYNC  = io_vsync;

    // TFP410 mode straps: I2C disabled, 24-bit single-edge RGB, single-ended
    // pixel clock, DE-gated video, data latched on the rising clock edge.
    assign HDMI_PD    = 1'b1;
    assign HDMI_ISEL  = 1'b0;
    assign HDMI_BSEL  = 1'b1;
    assign HDMI_DSEL  = 1'b1;
    assign HDMI_EDGE  = 1'b1; 
    assign HDMI_MSEN  = 1'b1;
    assign HDMI_DKEN  = 1'b1;
    assign HDMI_CTL1  = 1'b0;
    assign HDMI_CTL2  = 1'b0;
    assign HDMI_CTL3  = 1'b0;

    // ------------------------------------------------------------------
    // LED status (only 2 LEDs on this board)
    // ------------------------------------------------------------------
    assign leds[0] = init_calib_complete;
    assign leds[1] = eof_reached;

endmodule