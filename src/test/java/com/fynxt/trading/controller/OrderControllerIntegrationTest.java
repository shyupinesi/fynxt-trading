//package com.fynxt.trading.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fynxt.trading.dto.PlaceOrderRequest;
//import com.fynxt.trading.enums.OrderSide;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.http.MediaType;
//import org.springframework.test.annotation.DirtiesContext;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.test.web.servlet.MvcResult;
//
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Integration tests using H2 in-memory database.
// * Each test class gets a fresh application context via @DirtiesContext.
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@DisplayName("OrderController Integration Tests")
//class OrderControllerIntegrationTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ObjectMapper objectMapper;
//
//    private static final String TRADER_ID = "T001";
//
//    // -----------------------------------------------------------------------
//    // Place Order
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("POST /api/orders → 201 CREATED for valid BUY order")
//    void placeOrder_validBuy_returns201() throws Exception {
//        PlaceOrderRequest req = buyRequest("AAPL", 10);
//
//        mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.traderId").value(TRADER_ID))
//                .andExpect(jsonPath("$.status").value("PENDING"))
//                .andExpect(jsonPath("$.side").value("BUY"));
//    }
//
//    @Test
//    @DisplayName("POST /api/orders → 422 when more than 3 PENDING orders exist")
//    void placeOrder_exceedsMaxPending_returns422() throws Exception {
//        // Place 3 orders
//        for (int i = 0; i < 3; i++) {
//            mockMvc.perform(post("/api/orders")
//                            .contentType(MediaType.APPLICATION_JSON)
//                            .content(objectMapper.writeValueAsString(buyRequest("AAPL", 1))))
//                    .andExpect(status().isCreated());
//        }
//
//        // 4th should fail
//        mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(buyRequest("AAPL", 1))))
//                .andExpect(status().isUnprocessableEntity())
//                .andExpect(jsonPath("$.message").exists());
//    }
//
//    @Test
//    @DisplayName("POST /api/orders → 422 for SELL with no holdings")
//    void placeOrder_sellNoHoldings_returns422() throws Exception {
//        PlaceOrderRequest req = sellRequest("AAPL", 10);
//
//        mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isUnprocessableEntity())
//                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Insufficient")));
//    }
//
//    @Test
//    @DisplayName("POST /api/orders → 400 when traderId is missing")
//    void placeOrder_missingTraderId_returns400() throws Exception {
//        PlaceOrderRequest req = buyRequest("AAPL", 10);
//        req.setTraderId("");
//
//        mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isBadRequest());
//    }
//
//    // -----------------------------------------------------------------------
//    // Fill Order
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("PATCH /api/orders/{id}/fill → 200 and status becomes FILLED")
//    void fillOrder_pendingOrder_returns200() throws Exception {
//        Long orderId = placeBuyOrder("MSFT", 5);
//
//        mockMvc.perform(patch("/api/orders/{id}/fill", orderId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("FILLED"));
//    }
//
//    @Test
//    @DisplayName("PATCH /api/orders/{id}/fill → 422 when order already FILLED")
//    void fillOrder_alreadyFilled_returns422() throws Exception {
//        Long orderId = placeBuyOrder("MSFT", 5);
//        // Fill once
//        mockMvc.perform(patch("/api/orders/{id}/fill", orderId)).andExpect(status().isOk());
//        // Fill again → error
//        mockMvc.perform(patch("/api/orders/{id}/fill", orderId))
//                .andExpect(status().isUnprocessableEntity());
//    }
//
//    @Test
//    @DisplayName("PATCH /api/orders/{id}/fill → 404 for unknown order id")
//    void fillOrder_notFound_returns404() throws Exception {
//        mockMvc.perform(patch("/api/orders/9999/fill"))
//                .andExpect(status().isNotFound());
//    }
//
//    // -----------------------------------------------------------------------
//    // Cancel Order
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("PATCH /api/orders/{id}/cancel → 200 and status becomes CANCELLED")
//    void cancelOrder_pendingOrder_returns200() throws Exception {
//        Long orderId = placeBuyOrder("TSLA", 3);
//
//        mockMvc.perform(patch("/api/orders/{id}/cancel", orderId))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("CANCELLED"));
//    }
//
//    @Test
//    @DisplayName("PATCH /api/orders/{id}/cancel → 422 when order already CANCELLED")
//    void cancelOrder_alreadyCancelled_returns422() throws Exception {
//        Long orderId = placeBuyOrder("TSLA", 3);
//        mockMvc.perform(patch("/api/orders/{id}/cancel", orderId)).andExpect(status().isOk());
//        mockMvc.perform(patch("/api/orders/{id}/cancel", orderId))
//                .andExpect(status().isUnprocessableEntity());
//    }
//
//    // -----------------------------------------------------------------------
//    // Helpers
//    // -----------------------------------------------------------------------
//
//    private Long placeBuyOrder(String stock, int qty) throws Exception {
//        MvcResult result = mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(buyRequest(stock, qty))))
//                .andExpect(status().isCreated())
//                .andReturn();
//
//        return objectMapper.readTree(result.getResponse().getContentAsString())
//                .get("id").asLong();
//    }
//
//    private PlaceOrderRequest buyRequest(String stock, int qty) {
//        PlaceOrderRequest r = new PlaceOrderRequest();
//        r.setTraderId(TRADER_ID);
//        r.setStock(stock);
//        r.setSector("TECH");
//        r.setQuantity(qty);
//        r.setSide(OrderSide.BUY);
//        return r;
//    }
//
//    private PlaceOrderRequest sellRequest(String stock, int qty) {
//        PlaceOrderRequest r = new PlaceOrderRequest();
//        r.setTraderId(TRADER_ID);
//        r.setStock(stock);
//        r.setSector("TECH");
//        r.setQuantity(qty);
//        r.setSide(OrderSide.SELL);
//        return r;
//    }
//}
