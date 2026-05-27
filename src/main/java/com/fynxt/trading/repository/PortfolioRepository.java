package com.fynxt.trading.repository;

import com.fynxt.trading.entity.PortfolioPosition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<PortfolioPosition, Long> {

    List<PortfolioPosition> findByTraderId(String traderId);

    /**
     * Pessimistic write lock on a single position — prevents race conditions
     * when two threads concurrently fill orders for the same (trader, stock).
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PortfolioPosition p WHERE p.traderId = :traderId AND p.stock = :stock")
    Optional<PortfolioPosition> findByTraderIdAndStockWithLock(
            @Param("traderId") String traderId,
            @Param("stock") String stock);

    Optional<PortfolioPosition> findByTraderIdAndStock(String traderId, String stock);
}
