package com.fynxt.trading.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class PortfolioResponse {
    private String traderId;
    private Map<String, Integer> positions;        // stock → quantity
    private Map<String, Integer> sectorBreakdown;  // sector → total quantity
}
