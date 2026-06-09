package com.firstclub.membership.tierevaluation;

import com.firstclub.membership.common.exception.ResourceNotFoundException;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.TierRepository;
import com.firstclub.membership.tierevaluation.dto.MembershipEligibilityResponse;
import com.firstclub.membership.tierevaluation.evaluator.CriterionEvaluator;
import com.firstclub.membership.tierevaluation.evaluator.CriterionEvaluatorRegistry;
import com.firstclub.membership.tierevaluation.evaluator.EvaluationContext;
import com.firstclub.membership.user.UserAccount;
import com.firstclub.membership.user.UserRepository;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for tier evaluation: highest-eligible selection, the ANY-of-multiple
 * (OR) semantics, the never-evaluated default, and the user guard.
 */
class TierEvaluationServiceTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final TierRepository tierRepository = mock(TierRepository.class);
    private final TierCriterionRepository criterionRepository = mock(TierCriterionRepository.class);
    private final com.firstclub.membership.orderstats.UserOrderStatsRepository statsRepository =
            mock(com.firstclub.membership.orderstats.UserOrderStatsRepository.class);
    private final UserTierStatusRepository statusRepository = mock(UserTierStatusRepository.class);
    private final CriterionEvaluatorRegistry evaluatorRegistry = mock(CriterionEvaluatorRegistry.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-01-15T00:00:00Z"), ZoneOffset.UTC);

    private final TierEvaluationService service = new TierEvaluationService(
            userRepository, tierRepository, criterionRepository, statsRepository,
            statusRepository, evaluatorRegistry, clock);

    private final UserAccount user = mock(UserAccount.class);

    private MembershipTier tier(long id, int rank, String code, String name) {
        MembershipTier t = mock(MembershipTier.class);
        when(t.getId()).thenReturn(id);
        when(t.getRank()).thenReturn(rank);
        when(t.getCode()).thenReturn(code);
        when(t.getName()).thenReturn(name);
        return t;
    }

    private void noOrders() {
        when(statsRepository.findByUserIdAndPeriod(eq(1L), anyString())).thenReturn(Optional.empty());
    }

    @Test
    void staysAtBaseTier_whenHigherCriteriaUnsatisfied() {
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        noOrders();

        MembershipTier silver = tier(1L, 1, "SILVER", "Silver");
        MembershipTier gold = tier(2L, 2, "GOLD", "Gold");
        when(tierRepository.findByActiveTrueOrderByRankAsc()).thenReturn(List.of(silver, gold));

        when(criterionRepository.findByTierId(1L)).thenReturn(List.of()); // base: ungated
        TierCriterion goldCriterion = mock(TierCriterion.class);
        when(goldCriterion.getType()).thenReturn(CriterionType.ORDER_COUNT);
        when(criterionRepository.findByTierId(2L)).thenReturn(List.of(goldCriterion));
        CriterionEvaluator evaluator = mock(CriterionEvaluator.class);
        when(evaluatorRegistry.get(CriterionType.ORDER_COUNT)).thenReturn(evaluator);
        when(evaluator.isSatisfied(eq(goldCriterion), any(EvaluationContext.class))).thenReturn(false);

        when(statusRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MembershipEligibilityResponse response = service.evaluate(1L);

        assertThat(response.getEligibleTierCode()).isEqualTo("SILVER");
    }

    @Test
    void qualifiesForTier_whenAnyOneCriterionIsSatisfied() {
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        noOrders();

        MembershipTier silver = tier(1L, 1, "SILVER", "Silver");
        MembershipTier gold = tier(2L, 2, "GOLD", "Gold");
        when(tierRepository.findByActiveTrueOrderByRankAsc()).thenReturn(List.of(silver, gold));

        when(criterionRepository.findByTierId(1L)).thenReturn(List.of());
        TierCriterion byCount = mock(TierCriterion.class);
        when(byCount.getType()).thenReturn(CriterionType.ORDER_COUNT);
        TierCriterion byCohort = mock(TierCriterion.class);
        when(byCohort.getType()).thenReturn(CriterionType.COHORT);
        when(criterionRepository.findByTierId(2L)).thenReturn(List.of(byCount, byCohort));

        CriterionEvaluator countEval = mock(CriterionEvaluator.class);
        when(countEval.isSatisfied(eq(byCount), any())).thenReturn(false);
        CriterionEvaluator cohortEval = mock(CriterionEvaluator.class);
        when(cohortEval.isSatisfied(eq(byCohort), any())).thenReturn(true);
        when(evaluatorRegistry.get(CriterionType.ORDER_COUNT)).thenReturn(countEval);
        when(evaluatorRegistry.get(CriterionType.COHORT)).thenReturn(cohortEval);

        when(statusRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MembershipEligibilityResponse response = service.evaluate(1L);

        assertThat(response.getEligibleTierCode()).isEqualTo("GOLD");
    }

    @Test
    void getEligibility_whenNeverEvaluated_defaultsToBaseTierWithNullTimestamp() {
        when(user.getId()).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        noOrders();
        MembershipTier silver = tier(1L, 1, "SILVER", "Silver");
        when(tierRepository.findByActiveTrueOrderByRankAsc()).thenReturn(List.of(silver));
        when(statusRepository.findByUserId(1L)).thenReturn(Optional.empty());

        MembershipEligibilityResponse response = service.getEligibility(1L);

        assertThat(response.getEligibleTierCode()).isEqualTo("SILVER");
        assertThat(response.getEvaluatedAt()).isNull();
    }

    @Test
    void evaluate_unknownUser_throwsNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.evaluate(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
