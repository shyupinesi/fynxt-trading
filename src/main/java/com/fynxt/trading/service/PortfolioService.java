package com.fynxt.trading.service;

import com.fynxt.trading.dto.AddToPortfolioRequest;
import com.fynxt.trading.dto.PortfolioResponse;
import com.fynxt.trading.dto.SectorOverlapResponse;
import com.fynxt.trading.entity.Order;
import com.fynxt.trading.entity.PortfolioPosition;
import com.fynxt.trading.enums.OrderSide;
import com.fynxt.trading.exception.BusinessRuleException;
import com.fynxt.trading.exception.ResourceNotFoundException;
import com.fynxt.trading.repository.PortfolioRepository;
import com.fynxt.trading.repository.TraderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Manages portfolio positions and sector overlap analysis.
 *
 * <p>Portfolio mutations always run inside the caller's transaction (MANDATORY
 * propagation is intentionally avoided to keep things simple; the caller — e.g.
 * {@link OrderService#fillOrder}) owns the transaction boundary).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PortfolioRepository  portfolioRepository;
    private final TraderRepository     traderRepository;
    private final SectorOverlapAnalyzer overlapAnalyzer;

    // -----------------------------------------------------------------------
    // 4. Get Portfolio
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(String traderId) {
        validateTraderExists(traderId);

        List<PortfolioPosition> positions = portfolioRepository.findByTraderId(traderId);

        Map<String, Integer> positionMap = positions.stream()
                .filter(p -> p.getQuantity() > 0)
                .collect(Collectors.toMap(
                        PortfolioPosition::getStock,
                        PortfolioPosition::getQuantity));

        Map<String, Integer> sectorBreakdown = positions.stream()
                .filter(p -> p.getQuantity() > 0)
                .collect(Collectors.groupingBy(
                        PortfolioPosition::getSector,
                        Collectors.summingInt(PortfolioPosition::getQuantity)));

        return PortfolioResponse.builder()
                .traderId(traderId)
                .positions(positionMap)
                .sectorBreakdown(sectorBreakdown)
                .build();
    }

    // -----------------------------------------------------------------------
    // 5. Sector Overlap Analysis
    // -----------------------------------------------------------------------

    @Transactional(readOnly = true)
    public SectorOverlapResponse getSectorOverlap(String traderId) {
        validateTraderExists(traderId);

        Set<String> portfolioStocks = portfolioRepository.findByTraderId(traderId).stream()
                .filter(p -> p.getQuantity() > 0)
                .map(PortfolioPosition::getStock)
                .collect(Collectors.toSet());

        return overlapAnalyzer.analyse(portfolioStocks);
    }

    // -----------------------------------------------------------------------
    // 6. Add to Portfolio
    // -----------------------------------------------------------------------

    @Transactional
    public PortfolioResponse addToPortfolio(String traderId, AddToPortfolioRequest request) {
        validateTraderExists(traderId);

        upsertPosition(traderId, request.getStock().toUpperCase(),
                request.getSector().toUpperCase(), request.getQuantity());

        log.info("Added {} shares of {} to portfolio for trader {}",
                request.getQuantity(), request.getStock(), traderId);

        return getPortfolio(traderId);
    }

    // -----------------------------------------------------------------------
    // Internal: called by OrderService when an order is filled
    // -----------------------------------------------------------------------

    /**
     * Applies a FILLED order's effect to the portfolio.
     * Must be called within an active transaction (owned by the caller).
     */
    void applyOrderToPortfolio(Order order) {
        if (order.getSide() == OrderSide.BUY) {
            upsertPosition(order.getTraderId(), order.getStock(),
                    order.getSector(), order.getQuantity());
        } else {
            decreasePosition(order.getTraderId(), order.getStock(), order.getQuantity());
        }
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void upsertPosition(String traderId, String stock, String sector, int delta) {
        PortfolioPosition position = portfolioRepository
                .findByTraderIdAndStockWithLock(traderId, stock)
                .orElseGet(() -> {
                    PortfolioPosition p = new PortfolioPosition();
                    p.setTraderId(traderId);
                    p.setStock(stock);
                    p.setSector(sector);
                    p.setQuantity(0);
                    return p;
                });

        position.setQuantity(position.getQuantity() + delta);
        portfolioRepository.save(position);
    }

    private void decreasePosition(String traderId, String stock, int delta) {
        PortfolioPosition position = portfolioRepository
                .findByTraderIdAndStockWithLock(traderId, stock)
                .orElseThrow(() -> new BusinessRuleException(
                        "No position found for stock " + stock + " in trader " + traderId + "'s portfolio."));

        int newQty = position.getQuantity() - delta;
        if (newQty < 0) {
            throw new BusinessRuleException(
                    "Insufficient holdings: tried to decrease " + stock +
                    " by " + delta + " but only " + position.getQuantity() + " held.");
        }
        position.setQuantity(newQty);
        portfolioRepository.save(position);
    }

    private void validateTraderExists(String traderId) {
        if (!traderRepository.existsById(traderId)) {
            throw new ResourceNotFoundException("Trader not found: " + traderId);
        }
    }
}
