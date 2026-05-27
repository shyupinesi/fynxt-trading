package com.fynxt.trading.service;

import com.fynxt.trading.dto.OrderResponse;
import com.fynxt.trading.dto.PlaceOrderRequest;
import com.fynxt.trading.entity.Order;
import com.fynxt.trading.enums.OrderSide;
import com.fynxt.trading.enums.OrderStatus;
import com.fynxt.trading.exception.BusinessRuleException;
import com.fynxt.trading.exception.ResourceNotFoundException;
import com.fynxt.trading.repository.OrderRepository;
import com.fynxt.trading.repository.PortfolioRepository;
import com.fynxt.trading.repository.TraderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Handles the full lifecycle of an Order: place → fill | cancel.
 *
 * <h3>Concurrency strategy</h3>
 * <ul>
 *   <li>All mutating methods run inside a single DB transaction.</li>
 *   <li>Fill and Cancel acquire a PESSIMISTIC_WRITE lock on the order row,
 *       preventing two threads from concurrently transitioning the same order.</li>
 *   <li>Portfolio updates (fill path) also acquire a PESSIMISTIC_WRITE lock on
 *       the position row so quantity arithmetic is always serialised per position.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final int MAX_PENDING_ORDERS = 3;

    private final OrderRepository     orderRepository;
    private final PortfolioRepository portfolioRepository;
    private final TraderRepository    traderRepository;
    private final PortfolioService    portfolioService;
    private final OrderMapper         orderMapper;

    // -----------------------------------------------------------------------
    // 1. Place Order
    // -----------------------------------------------------------------------

    @Transactional
    public OrderResponse placeOrder(PlaceOrderRequest request) {
        validateTraderExists(request.getTraderId());
        enforceMaxPendingLimit(request.getTraderId());

        if (request.getSide() == OrderSide.SELL) {
            enforceEnoughHoldings(request.getTraderId(), request.getStock(), request.getQuantity());
        }

        Order order = new Order();
        order.setTraderId(request.getTraderId());
        order.setStock(request.getStock().toUpperCase());
        order.setSector(request.getSector().toUpperCase());
        order.setQuantity(request.getQuantity());
        order.setSide(request.getSide());
        order.setStatus(OrderStatus.PENDING);

        Order saved = orderRepository.save(order);
        log.info("Order placed: id={} trader={} {} {} qty={}",
                saved.getId(), saved.getTraderId(), saved.getSide(),
                saved.getStock(), saved.getQuantity());

        return orderMapper.toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // 2. Fill Order
    // -----------------------------------------------------------------------

    @Transactional
    public OrderResponse fillOrder(Long orderId) {
        Order order = getOrderWithLock(orderId);
        requirePending(order, "fill");

        order.setStatus(OrderStatus.FILLED);
        portfolioService.applyOrderToPortfolio(order);

        Order saved = orderRepository.save(order);
        log.info("Order filled: id={} trader={} {} {} qty={}",
                saved.getId(), saved.getTraderId(), saved.getSide(),
                saved.getStock(), saved.getQuantity());

        return orderMapper.toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // 3. Cancel Order
    // -----------------------------------------------------------------------

    @Transactional
    public OrderResponse cancelOrder(Long orderId) {
        Order order = getOrderWithLock(orderId);
        requirePending(order, "cancel");

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        log.info("Order cancelled: id={}", saved.getId());

        return orderMapper.toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Private helpers
    // -----------------------------------------------------------------------

    private void validateTraderExists(String traderId) {
        if (!traderRepository.existsById(traderId)) {
            throw new ResourceNotFoundException("Trader not found: " + traderId);
        }
    }

    private void enforceMaxPendingLimit(String traderId) {
        int pending = orderRepository.countByTraderIdAndStatus(traderId, OrderStatus.PENDING);
        if (pending >= MAX_PENDING_ORDERS) {
            throw new BusinessRuleException(
                    "Trader " + traderId + " already has " + MAX_PENDING_ORDERS +
                    " pending orders. Fill or cancel an existing order before placing a new one.");
        }
    }

    private void enforceEnoughHoldings(String traderId, String stock, int requiredQty) {
        int held = portfolioRepository
                .findByTraderIdAndStock(traderId, stock.toUpperCase())
                .map(p -> p.getQuantity())
                .orElse(0);

        if (held < requiredQty) {
            throw new BusinessRuleException(
                    "Insufficient holdings for SELL. Trader " + traderId +
                    " holds " + held + " shares of " + stock +
                    " but tried to sell " + requiredQty + ".");
        }
    }

    private Order getOrderWithLock(Long orderId) {
        return orderRepository.findByIdWithLock(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderId));
    }

    private void requirePending(Order order, String action) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessRuleException(
                    "Cannot " + action + " order " + order.getId() +
                    " — current status is " + order.getStatus() +
                    ". Only PENDING orders can be " + action + "ed.");
        }
    }
}
