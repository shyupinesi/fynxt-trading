package com.fynxt.trading.service;

import com.fynxt.trading.dto.SectorOverlapResponse;
import com.fynxt.trading.dto.SectorOverlapResponse.BasketOverlap;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * Pure-Java sector overlap analyser.
 * No database calls, no Spring dependencies beyond @Component registration.
 *
 * <p>Formula: Overlap = [2 × |common| / (|portfolio| + |basket|)] × 100
 *
 * <p>Risk flag rules (evaluated top-down, first match wins):
 * <ul>
 *   <li>≥ 60% → HIGH</li>
 *   <li>≥ 40% → MEDIUM</li>
 *   <li>otherwise → LOW</li>
 * </ul>
 */
@Component
public class SectorOverlapAnalyzer {

    /** Benchmark baskets — immutable, defined once. */
    private static final Map<String, Set<String>> BASKETS = Map.of(
            "TECH_HEAVY",    Set.of("AAPL", "MSFT", "GOOGL", "TSLA", "NVDA"),
            "FINANCE_HEAVY", Set.of("JPM", "GS", "BAC", "MS", "WFC"),
            "BALANCED",      Set.of("AAPL", "JPM", "XOM", "JNJ", "TSLA")
    );

    /**
     * Analyse overlap between a trader's stock set and every benchmark basket.
     *
     * @param portfolioStocks set of stock tickers the trader currently holds
     * @return overlap analysis with risk flag and dominant basket
     */
    public SectorOverlapResponse analyse(Set<String> portfolioStocks) {
        List<BasketOverlap> overlaps = new ArrayList<>();
        double maxOverlap = 0.0;
        String dominantBasket = null;

        // Iterate in a deterministic order for consistent responses
        for (Map.Entry<String, Set<String>> entry : new TreeMap<>(BASKETS).entrySet()) {
            String basketName = entry.getKey();
            Set<String> basketStocks = entry.getValue();

            double overlap = computeOverlap(portfolioStocks, basketStocks);

            overlaps.add(BasketOverlap.builder()
                    .basket(basketName)
                    .overlap(String.format("%.2f%%", overlap))
                    .build());

            if (overlap > maxOverlap) {
                maxOverlap = overlap;
                dominantBasket = basketName;
            }
        }

        String riskFlag = resolveRiskFlag(maxOverlap);

        return SectorOverlapResponse.builder()
                .overlaps(overlaps)
                .dominantBasket(dominantBasket)
                .riskFlag(riskFlag)
                .build();
    }

    /**
     * Dice-coefficient overlap formula as defined in the assignment spec.
     * Returns 0.0 when either set is empty (avoids division by zero).
     */
    double computeOverlap(Set<String> portfolio, Set<String> basket) {
        if (portfolio.isEmpty() || basket.isEmpty()) {
            return 0.0;
        }

        long commonCount = portfolio.stream()
                .filter(basket::contains)
                .count();

        return (2.0 * commonCount / (portfolio.size() + basket.size())) * 100.0;
    }

    private String resolveRiskFlag(double maxOverlap) {
        if (maxOverlap >= 60.0) return "HIGH";
        if (maxOverlap >= 40.0) return "MEDIUM";
        return "LOW";
    }
}
