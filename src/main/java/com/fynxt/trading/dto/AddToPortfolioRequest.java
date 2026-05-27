package com.fynxt.trading.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToPortfolioRequest {

    @NotBlank(message = "stock is required")
    private String stock;

    @NotBlank(message = "sector is required")
    private String sector;

    @Min(value = 1, message = "quantity must be at least 1")
    private int quantity;
}
