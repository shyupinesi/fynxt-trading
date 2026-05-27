package com.fynxt.trading.dto;

import com.fynxt.trading.enums.OrderSide;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlaceOrderRequest {

    @NotBlank(message = "traderId is required")
    private String traderId;

    @NotBlank(message = "stock is required")
    private String stock;

    @NotBlank(message = "sector is required")
    private String sector;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;

    @NotNull(message = "side is required (BUY or SELL)")
    private OrderSide side;
}
