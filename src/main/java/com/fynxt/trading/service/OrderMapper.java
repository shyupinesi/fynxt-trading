package com.fynxt.trading.service;

import com.fynxt.trading.dto.OrderResponse;
import com.fynxt.trading.entity.Order;
import org.springframework.stereotype.Component;

/**
 * Stateless mapper: converts Order entity → OrderResponse DTO.
 * Kept separate to honour SRP and make testing trivial.
 */
@Component
public class OrderMapper {

    public OrderResponse toResponse(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .traderId(order.getTraderId())
                .stock(order.getStock())
                .sector(order.getSector())
                .quantity(order.getQuantity())
                .side(order.getSide())
                .status(order.getStatus())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}
