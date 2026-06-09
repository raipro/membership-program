package com.firstclub.membership.orderstats;

import com.firstclub.membership.common.domain.AuditEntity;
import com.firstclub.membership.user.UserAccount;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;

import java.math.BigDecimal;
import java.time.Instant;

import static lombok.AccessLevel.PROTECTED;

/**
 * Per-user, per-period order rollup — the read model tier evaluation runs against.
 * Fed by simulated order signals ({@code POST /users/{id}/orders}). One row per
 * (user, period), where period is a {@code yyyy-MM} month key.
 */
@Entity
@Table(
        name = "user_order_stats",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_user_period",
                columnNames = {"user_id", "period"}))
@Getter
@NoArgsConstructor(access = PROTECTED)
public class UserOrderStats extends AuditEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserAccount user;

    /** Month key, e.g. "2026-06". */
    @Column(nullable = false, length = 7)
    private String period;

    @Column(name = "order_count", nullable = false)
    private int orderCount;

    @Column(name = "total_value", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalValue;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    public static UserOrderStats create(UserAccount user, String period) {
        UserOrderStats stats = new UserOrderStats();
        stats.user = user;
        stats.period = period;
        stats.orderCount = 0;
        stats.totalValue = BigDecimal.ZERO;
        return stats;
    }

    /** Record one order of the given value into this period's rollup. */
    public void addOrder(BigDecimal amount) {
        this.orderCount += 1;
        this.totalValue = this.totalValue.add(amount);
    }
}
