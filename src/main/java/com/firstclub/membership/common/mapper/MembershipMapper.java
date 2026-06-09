package com.firstclub.membership.common.mapper;

import com.firstclub.membership.plan.MembershipPlan;
import com.firstclub.membership.plan.dto.MembershipPlanResponse;
import com.firstclub.membership.tier.MembershipTier;
import com.firstclub.membership.tier.dto.MembershipTierResponse;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * Single, project-wide MapStruct mapper for all entity → DTO conversions.
 *
 * <p>Centralising mapping here (rather than one mapper per feature) keeps a single
 * generated Spring bean and one place to look for any conversion. New entities add
 * their methods to this interface as features land (tiers, pricing, subscriptions…).
 *
 * <p>Where source/target field names match 1:1, MapStruct needs no {@code @Mapping}.
 */
@Mapper(componentModel = "spring")
public interface MembershipMapper {

    // --- Plan (Task 2) ---
    MembershipPlanResponse toPlanResponse(MembershipPlan plan);

    List<MembershipPlanResponse> toPlanResponseList(List<MembershipPlan> plans);

    // --- Tier (Task 2) ---
    MembershipTierResponse toTierResponse(MembershipTier tier);

    List<MembershipTierResponse> toTierResponseList(List<MembershipTier> tiers);
}
