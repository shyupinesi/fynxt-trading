package com.fynxt.trading.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SectorOverlapResponse {
    private List<BasketOverlap> overlaps;
    private String dominantBasket;
    private String riskFlag;

    @Data
    @Builder
    public static class BasketOverlap {
        private String basket;
        private String overlap;  // formatted as "60.00%"
    }
}
