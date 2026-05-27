package com.fynxt.trading.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "portfolio_positions")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioPosition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trader_id", nullable = false, length = 50)
    private String traderId;

    @Column(name = "stock", nullable = false, length = 20)
    private String stock;

    @Column(name = "sector", nullable = false, length = 50)
    private String sector;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
