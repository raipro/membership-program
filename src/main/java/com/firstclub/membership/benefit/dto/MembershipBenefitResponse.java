package com.firstclub.membership.benefit.dto;

import com.firstclub.membership.benefit.BenefitType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * API view of a resolved tier benefit: the static catalog fields plus the
 * normalized, handler-computed {@code summary} and {@code attributes}.
 */
@Getter
@AllArgsConstructor
public class MembershipBenefitResponse {

    private final String code;
    private final BenefitType type;
    private final String description;
    private final String summary;
    private final Map<String, Object> attributes;
}
