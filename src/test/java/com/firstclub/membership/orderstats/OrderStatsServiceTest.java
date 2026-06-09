package com.firstclub.membership.orderstats;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.orderstats.dto.MembershipOrderStatsResponse;
import com.firstclub.membership.user.UserAccount;
import com.firstclub.membership.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the order-stats rollup: create-on-first-order, increment-on-subsequent,
 * the empty default, and the user-existence guard.
 */
class OrderStatsServiceTest {

    private final UserOrderStatsRepository statsRepository = mock(UserOrderStatsRepository.class);
    private final UserRepository userRepository = mock(UserRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);

    private final OrderStatsService service = new OrderStatsService(statsRepository, userRepository, clock);

    private final UserAccount user = mock(UserAccount.class);

    @Test
    void firstOrderInPeriod_createsRollup() {
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(statsRepository.findByUserIdAndPeriod(eq(1L), anyString())).thenReturn(Optional.empty());

        MembershipOrderStatsResponse response = service.recordOrder(1L, new BigDecimal("5000"));

        assertThat(response.getPeriod()).isEqualTo("2026-01");
        assertThat(response.getOrderCount()).isEqualTo(1);
        assertThat(response.getTotalValue()).isEqualByComparingTo("5000");
    }

    @Test
    void subsequentOrder_incrementsExistingRollup() {
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        UserOrderStats existing = UserOrderStats.create(user, "2026-01");
        existing.addOrder(new BigDecimal("1000"));
        when(statsRepository.findByUserIdAndPeriod(eq(1L), anyString())).thenReturn(Optional.of(existing));

        MembershipOrderStatsResponse response = service.recordOrder(1L, new BigDecimal("2000"));

        assertThat(response.getOrderCount()).isEqualTo(2);
        assertThat(response.getTotalValue()).isEqualByComparingTo("3000");
    }

    @Test
    void recordOrder_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.recordOrder(99L, new BigDecimal("100")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCurrentStats_whenNoRollup_returnsZeroes() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(statsRepository.findByUserIdAndPeriod(eq(1L), anyString())).thenReturn(Optional.empty());

        MembershipOrderStatsResponse response = service.getCurrentStats(1L);

        assertThat(response.getOrderCount()).isZero();
        assertThat(response.getTotalValue()).isEqualByComparingTo("0");
    }

    @Test
    void getCurrentStats_unknownUser_throwsNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> service.getCurrentStats(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
