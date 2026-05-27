package com.fynxt.trading.repository;

import com.fynxt.trading.entity.Order;
import com.fynxt.trading.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Count PENDING orders for a trader — used to enforce the 3-order limit.
     */
    int countByTraderIdAndStatus(String traderId, OrderStatus status);

    /**
     * Pessimistic write lock: used when filling or cancelling an order
     * to prevent lost-update anomalies under concurrent load.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM Order o WHERE o.id = :id")
    Optional<Order> findByIdWithLock(@Param("id") Long id);
}
