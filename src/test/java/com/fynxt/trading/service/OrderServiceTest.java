//package com.fynxt.trading.service;
//
//import com.fynxt.trading.dto.PlaceOrderRequest;
//import com.fynxt.trading.entity.Order;
//import com.fynxt.trading.entity.PortfolioPosition;
//import com.fynxt.trading.enums.OrderSide;
//import com.fynxt.trading.enums.OrderStatus;
//import com.fynxt.trading.exception.BusinessRuleException;
//import com.fynxt.trading.exception.ResourceNotFoundException;
//import com.fynxt.trading.repository.OrderRepository;
//import com.fynxt.trading.repository.PortfolioRepository;
//import com.fynxt.trading.repository.TraderRepository;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.assertThatThrownBy;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@DisplayName("OrderService")
//@ExtendWith(MockitoExtension.class)
//class OrderServiceTest {
//
//    @Mock private OrderRepository     orderRepository;
//    @Mock private PortfolioRepository portfolioRepository;
//    @Mock private TraderRepository    traderRepository;
//    @Mock private PortfolioService    portfolioService;
//    @Mock private OrderMapper         orderMapper;
//
//    @InjectMocks
//    private OrderService orderService;
//
//    private static final String TRADER_ID = "T001";
//
//    @BeforeEach
//    void commonStubs() {
//        when(traderRepository.existsById(TRADER_ID)).thenReturn(true);
//    }
//
//    // -----------------------------------------------------------------------
//    // placeOrder
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("Rejects order when trader already has 3 PENDING orders")
//    void placeOrder_maxPendingReached_throwsBusinessRule() {
//        when(orderRepository.countByTraderIdAndStatus(TRADER_ID, OrderStatus.PENDING))
//                .thenReturn(3);
//
//        PlaceOrderRequest req = buildRequest(OrderSide.BUY);
//
//        assertThatThrownBy(() -> orderService.placeOrder(req))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("3 pending orders");
//    }
//
//    @Test
//    @DisplayName("Rejects SELL when trader lacks sufficient holdings")
//    void placeOrder_sellWithInsufficientHoldings_throwsBusinessRule() {
//        when(orderRepository.countByTraderIdAndStatus(TRADER_ID, OrderStatus.PENDING))
//                .thenReturn(0);
//        when(portfolioRepository.findByTraderIdAndStock(TRADER_ID, "AAPL"))
//                .thenReturn(Optional.of(positionWith(10)));
//
//        PlaceOrderRequest req = buildRequest(OrderSide.SELL);  // qty = 50
//
//        assertThatThrownBy(() -> orderService.placeOrder(req))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("Insufficient holdings");
//    }
//
//    @Test
//    @DisplayName("Rejects order when trader does not exist")
//    void placeOrder_unknownTrader_throwsNotFound() {
//        when(traderRepository.existsById("UNKNOWN")).thenReturn(false);
//
//        PlaceOrderRequest req = buildRequest(OrderSide.BUY);
//        req.setTraderId("UNKNOWN");
//
//        assertThatThrownBy(() -> orderService.placeOrder(req))
//                .isInstanceOf(ResourceNotFoundException.class);
//    }
//
//    // -----------------------------------------------------------------------
//    // fillOrder
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("Cannot fill a CANCELLED order")
//    void fillOrder_cancelledOrder_throwsBusinessRule() {
//        Order cancelled = orderWithStatus(OrderStatus.CANCELLED);
//        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(cancelled));
//
//        assertThatThrownBy(() -> orderService.fillOrder(1L))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("CANCELLED");
//    }
//
//    @Test
//    @DisplayName("Cannot fill an already FILLED order")
//    void fillOrder_alreadyFilled_throwsBusinessRule() {
//        Order filled = orderWithStatus(OrderStatus.FILLED);
//        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(filled));
//
//        assertThatThrownBy(() -> orderService.fillOrder(1L))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("FILLED");
//    }
//
//    @Test
//    @DisplayName("fillOrder on non-existent order throws ResourceNotFoundException")
//    void fillOrder_notFound_throwsNotFound() {
//        when(orderRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> orderService.fillOrder(99L))
//                .isInstanceOf(ResourceNotFoundException.class);
//    }
//
//    // -----------------------------------------------------------------------
//    // cancelOrder
//    // -----------------------------------------------------------------------
//
//    @Test
//    @DisplayName("Cannot cancel a FILLED order")
//    void cancelOrder_filledOrder_throwsBusinessRule() {
//        Order filled = orderWithStatus(OrderStatus.FILLED);
//        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(filled));
//
//        assertThatThrownBy(() -> orderService.cancelOrder(1L))
//                .isInstanceOf(BusinessRuleException.class)
//                .hasMessageContaining("FILLED");
//    }
//
//    @Test
//    @DisplayName("Cancelling a PENDING order succeeds")
//    void cancelOrder_pendingOrder_savesAsCancelled() {
//        Order pending = orderWithStatus(OrderStatus.PENDING);
//        when(orderRepository.findByIdWithLock(1L)).thenReturn(Optional.of(pending));
//        when(orderRepository.save(any())).thenReturn(pending);
//        when(orderMapper.toResponse(any())).thenReturn(null);
//
//        orderService.cancelOrder(1L);
//
//        verify(orderRepository).save(argThat(o -> o.getStatus() == OrderStatus.CANCELLED));
//    }
//
//    // -----------------------------------------------------------------------
//    // Helpers
//    // -----------------------------------------------------------------------
//
//    private PlaceOrderRequest buildRequest(OrderSide side) {
//        PlaceOrderRequest req = new PlaceOrderRequest();
//        req.setTraderId(TRADER_ID);
//        req.setStock("AAPL");
//        req.setSector("TECH");
//        req.setQuantity(50);
//        req.setSide(side);
//        return req;
//    }
//
//    private Order orderWithStatus(OrderStatus status) {
//        Order o = new Order();
//        o.setId(1L);
//        o.setStatus(status);
//        o.setTraderId(TRADER_ID);
//        o.setStock("AAPL");
//        o.setSector("TECH");
//        o.setQuantity(50);
//        o.setSide(OrderSide.BUY);
//        return o;
//    }
//
//    private PortfolioPosition positionWith(int qty) {
//        PortfolioPosition p = new PortfolioPosition();
//        p.setTraderId(TRADER_ID);
//        p.setStock("AAPL");
//        p.setSector("TECH");
//        p.setQuantity(qty);
//        return p;
//    }
//}
