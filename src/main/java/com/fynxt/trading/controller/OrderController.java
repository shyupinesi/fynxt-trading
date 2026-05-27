package com.fynxt.trading.controller;

import com.fynxt.trading.dto.OrderResponse;
import com.fynxt.trading.dto.PlaceOrderRequest;
import com.fynxt.trading.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for order lifecycle: place, fill, cancel.
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    /**
     * POST /api/orders
     * Place a new BUY or SELL order.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> placeOrder(@Valid @RequestBody PlaceOrderRequest request) {
        OrderResponse response = orderService.placeOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * PATCH /api/orders/{id}/fill
     * Transition an order from PENDING → FILLED and update the portfolio.
     */
    @PatchMapping("/{id}/fill")
    public ResponseEntity<OrderResponse> fillOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.fillOrder(id));
    }

    /**
     * PATCH /api/orders/{id}/cancel
     * Transition an order from PENDING → CANCELLED.
     */
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<OrderResponse> cancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
