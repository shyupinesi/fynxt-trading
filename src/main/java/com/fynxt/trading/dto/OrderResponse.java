package com.fynxt.trading.dto;

import com.fynxt.trading.enums.OrderSide;
import com.fynxt.trading.enums.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private String traderId;
    private String stock;
    private String sector;
    private int quantity;
    private OrderSide side;
    private OrderStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
