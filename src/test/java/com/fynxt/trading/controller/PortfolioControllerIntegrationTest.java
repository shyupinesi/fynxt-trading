//package com.fynxt.trading.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fynxt.trading.dto.AddToPortfolioRequest;
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
//@SpringBootTest
//@AutoConfigureMockMvc
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@DisplayName("PortfolioController Integration Tests")
//class PortfolioControllerIntegrationTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ObjectMapper objectMapper;
//
//    private static final String TRADER_ID = "T001";
//    private static final String BASE_URL  = "/api/traders/" + TRADER_ID + "/portfolio";
//
//    // -----------------------------------------------------------------------
//    // Get Portfolio
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("GET portfolio → empty positions for new trader")
//    void getPortfolio_newTrader_emptyPositions() throws Exception {
//        mockMvc.perform(get(BASE_URL))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.traderId").value(TRADER_ID))
//                .andExpect(jsonPath("$.positions").isEmpty());
//    }
//
//    @Test
//    @DisplayName("GET portfolio → 404 for unknown trader")
//    void getPortfolio_unknownTrader_returns404() throws Exception {
//        mockMvc.perform(get("/api/traders/UNKNOWN/portfolio"))
//                .andExpect(status().isNotFound());
//    }
//
//    @Test
//    @DisplayName("GET portfolio → shows holdings after BUY order is filled")
//    void getPortfolio_afterBuyFilled_showsHolding() throws Exception {
//        // Place and fill a BUY order for 100 AAPL
//        Long orderId = placeBuyAndGetId("AAPL", 100);
//        mockMvc.perform(patch("/api/orders/{id}/fill", orderId)).andExpect(status().isOk());
//
//        mockMvc.perform(get(BASE_URL))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.positions.AAPL").value(100))
//                .andExpect(jsonPath("$.sectorBreakdown.TECH").value(100));
//    }
//
//    // -----------------------------------------------------------------------
//    // Add to Portfolio
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("POST portfolio → 201 and position updated")
//    void addToPortfolio_validRequest_returns201() throws Exception {
//        AddToPortfolioRequest req = addRequest("NVDA", "TECH", 50);
//
//        mockMvc.perform(post(BASE_URL)
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andExpect(jsonPath("$.positions.NVDA").value(50));
//    }
//
//    @Test
//    @DisplayName("POST portfolio twice → quantities are accumulated")
//    void addToPortfolio_twice_accumulates() throws Exception {
//        AddToPortfolioRequest req = addRequest("AAPL", "TECH", 30);
//
//        mockMvc.perform(post(BASE_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(req)));
//        mockMvc.perform(post(BASE_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(req)));
//
//        mockMvc.perform(get(BASE_URL))
//                .andExpect(jsonPath("$.positions.AAPL").value(60));
//    }
//
//    // -----------------------------------------------------------------------
//    // Sector Overlap
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("GET overlap → HIGH risk for AAPL+TSLA+NVDA portfolio")
//    void sectorOverlap_techHeavyPortfolio_highRisk() throws Exception {
//        // Seed portfolio
//        addStock("AAPL", "TECH", 100);
//        addStock("TSLA", "TECH", 80);
//        addStock("NVDA", "TECH", 60);
//
//        mockMvc.perform(get(BASE_URL + "/overlap"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.riskFlag").value("HIGH"))
//                .andExpect(jsonPath("$.dominantBasket").value("TECH_HEAVY"))
//                .andExpect(jsonPath("$.overlaps").isArray());
//    }
//
//    @Test
//    @DisplayName("GET overlap → LOW risk for empty portfolio")
//    void sectorOverlap_emptyPortfolio_lowRisk() throws Exception {
//        mockMvc.perform(get(BASE_URL + "/overlap"))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.riskFlag").value("LOW"));
//    }
//
//    // -----------------------------------------------------------------------
//    // Helpers
//    // -----------------------------------------------------------------------
//
//    private Long placeBuyAndGetId(String stock, int qty) throws Exception {
//        PlaceOrderRequest req = new PlaceOrderRequest();
//        req.setTraderId(TRADER_ID);
//        req.setStock(stock);
//        req.setSector("TECH");
//        req.setQuantity(qty);
//        req.setSide(OrderSide.BUY);
//
//        MvcResult result = mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andReturn();
//
//        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
//    }
//
//    private void addStock(String stock, String sector, int qty) throws Exception {
//        mockMvc.perform(post(BASE_URL)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(addRequest(stock, sector, qty))));
//    }
//
//    private AddToPortfolioRequest addRequest(String stock, String sector, int qty) {
//        AddToPortfolioRequest r = new AddToPortfolioRequest();
//        r.setStock(stock);
//        r.setSector(sector);
//        r.setQuantity(qty);
//        return r;
//    }
//}
