//package com.fynxt.trading.concurrency;
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
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
//
///**
// * Concurrency tests verify that business rules hold under simultaneous requests.
// *
// * <p>Key invariant: a trader must never end up with more than 3 PENDING orders,
// * even when many threads race to place orders simultaneously.
// */
//@SpringBootTest
//@AutoConfigureMockMvc
//@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
//@DisplayName("Concurrency: Order Placement")
//class OrderConcurrencyTest {
//
//    @Autowired MockMvc mockMvc;
//    @Autowired ObjectMapper objectMapper;
//
//    /**
//     * Fire 10 concurrent BUY requests for T001.
//     * Due to the 3-order PENDING limit, at most 3 should succeed (201)
//     * and the rest must fail with 422.
//     */
//    @Test
//    @DisplayName("Only 3 of N concurrent order placements succeed (PENDING limit enforced)")
//    void concurrentOrderPlacement_onlyThreeSucceed() throws Exception {
//        int threads = 10;
//        ExecutorService executor = Executors.newFixedThreadPool(threads);
//        CountDownLatch startLatch = new CountDownLatch(1);
//        CountDownLatch doneLatch  = new CountDownLatch(threads);
//
//        AtomicInteger successCount = new AtomicInteger(0);
//        AtomicInteger rejectCount  = new AtomicInteger(0);
//
//        List<Future<?>> futures = new ArrayList<>();
//
//        for (int i = 0; i < threads; i++) {
//            futures.add(executor.submit(() -> {
//                try {
//                    startLatch.await();   // all threads start simultaneously
//                    PlaceOrderRequest req = buildBuyRequest("T001", "AAPL", 1);
//                    int status = mockMvc.perform(post("/api/orders")
//                                    .contentType(MediaType.APPLICATION_JSON)
//                                    .content(objectMapper.writeValueAsString(req)))
//                            .andReturn()
//                            .getResponse()
//                            .getStatus();
//
//                    if (status == 201) successCount.incrementAndGet();
//                    else               rejectCount.incrementAndGet();
//                } catch (Exception e) {
//                    // count unexpected errors as rejections
//                    rejectCount.incrementAndGet();
//                } finally {
//                    doneLatch.countDown();
//                }
//            }));
//        }
//
//        startLatch.countDown();  // release all threads
//        doneLatch.await(10, TimeUnit.SECONDS);
//        executor.shutdown();
//
//        assertThat(successCount.get())
//                .as("No more than 3 orders should be accepted as PENDING")
//                .isLessThanOrEqualTo(3);
//
//        assertThat(successCount.get() + rejectCount.get())
//                .as("All requests should complete")
//                .isEqualTo(threads);
//    }
//
//    /**
//     * Filling the same PENDING order from two concurrent threads:
//     * exactly one fill must succeed and one must fail with 422.
//     */
//    @Test
//    @DisplayName("Concurrent fill of the same order: only one succeeds")
//    void concurrentFill_sameOrder_onlyOneSucceeds() throws Exception {
//        // Place a single order first
//        PlaceOrderRequest req = buildBuyRequest("T001", "AAPL", 5);
//        String body = mockMvc.perform(post("/api/orders")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(req)))
//                .andExpect(status().isCreated())
//                .andReturn()
//                .getResponse()
//                .getContentAsString();
//
//        long orderId = objectMapper.readTree(body).get("id").asLong();
//
//        // Now fill concurrently
//        ExecutorService executor = Executors.newFixedThreadPool(2);
//        CountDownLatch startLatch = new CountDownLatch(1);
//        AtomicInteger ok  = new AtomicInteger(0);
//        AtomicInteger err = new AtomicInteger(0);
//
//        for (int i = 0; i < 2; i++) {
//            executor.submit(() -> {
//                try {
//                    startLatch.await();
//                    int status = mockMvc.perform(patch("/api/orders/{id}/fill", orderId))
//                            .andReturn().getResponse().getStatus();
//                    if (status == 200) ok.incrementAndGet();
//                    else               err.incrementAndGet();
//                } catch (Exception e) {
//                    err.incrementAndGet();
//                }
//            });
//        }
//
//        startLatch.countDown();
//        executor.shutdown();
//        executor.awaitTermination(10, TimeUnit.SECONDS);
//
//        assertThat(ok.get()).as("Only one fill should succeed").isEqualTo(1);
//        assertThat(err.get()).as("Second fill should be rejected").isEqualTo(1);
//    }
//
//    private PlaceOrderRequest buildBuyRequest(String traderId, String stock, int qty) {
//        PlaceOrderRequest r = new PlaceOrderRequest();
//        r.setTraderId(traderId);
//        r.setStock(stock);
//        r.setSector("TECH");
//        r.setQuantity(qty);
//        r.setSide(OrderSide.BUY);
//        return r;
//    }
//}
