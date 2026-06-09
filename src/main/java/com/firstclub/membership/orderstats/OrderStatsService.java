package com.firstclub.membership.orderstats;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.orderstats.dto.MembershipOrderStatsResponse;
import com.firstclub.membership.user.UserAccount;
import com.firstclub.membership.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.YearMonth;

/**
 * Ingests simulated order signals into the per-user, per-period rollup that tier
 * evaluation reads. Stands in for a real order service.
 */
@Service
@RequiredArgsConstructor
public class OrderStatsService {

    private final UserOrderStatsRepository statsRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Transactional
    public MembershipOrderStatsResponse recordOrder(Long userId, BigDecimal amount) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("User", userId));

        String period = currentPeriod();
        UserOrderStats stats = statsRepository.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> UserOrderStats.create(user, period));
        stats.addOrder(amount);
        statsRepository.save(stats);

        return toResponse(stats);
    }

    @Transactional(readOnly = true)
    public MembershipOrderStatsResponse getCurrentStats(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw ResourceNotFoundException.of("User", userId);
        }
        String period = currentPeriod();
        return statsRepository.findByUserIdAndPeriod(userId, period)
                .map(this::toResponse)
                .orElseGet(() -> new MembershipOrderStatsResponse(userId, period, 0, BigDecimal.ZERO));
    }

    private String currentPeriod() {
        return YearMonth.now(clock).toString();
    }

    private MembershipOrderStatsResponse toResponse(UserOrderStats stats) {
        return new MembershipOrderStatsResponse(
                stats.getUser().getId(), stats.getPeriod(), stats.getOrderCount(), stats.getTotalValue());
    }
}
