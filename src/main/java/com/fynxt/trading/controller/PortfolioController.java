package com.fynxt.trading.controller;

import com.fynxt.trading.dto.AddToPortfolioRequest;
import com.fynxt.trading.dto.PortfolioResponse;
import com.fynxt.trading.dto.SectorOverlapResponse;
import com.fynxt.trading.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for portfolio management and sector overlap analysis.
 */
@RestController
@RequestMapping("/api/traders/{traderId}/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    /**
     * GET /api/traders/{traderId}/portfolio
     * Returns holdings grouped by stock and sector.
     */
    @GetMapping
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable String traderId) {
        return ResponseEntity.ok(portfolioService.getPortfolio(traderId));
    }

    /**
     * GET /api/traders/{traderId}/portfolio/overlap
     * Returns sector overlap analysis against benchmark baskets.
     */
    @GetMapping("/overlap")
    public ResponseEntity<SectorOverlapResponse> getSectorOverlap(@PathVariable String traderId) {
        return ResponseEntity.ok(portfolioService.getSectorOverlap(traderId));
    }

    /**
     * POST /api/traders/{traderId}/portfolio
     * Directly add stock holdings to a trader's portfolio.
     */
    @PostMapping
    public ResponseEntity<PortfolioResponse> addToPortfolio(
            @PathVariable String traderId,
            @Valid @RequestBody AddToPortfolioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.addToPortfolio(traderId, request));
    }
}
