package com.firstclub.membership.benefit.handler;

import com.firstclub.membership.benefit.BenefitType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Collects every {@link BenefitHandler} bean and indexes it by {@link BenefitType}.
 *
 * <p>Because handlers are discovered via Spring injection, adding a new benefit type
 * is purely additive — drop in a new {@code @Component} handler and it is wired here
 * automatically.
 */
@Component
public class BenefitHandlerRegistry {

    private final Map<BenefitType, BenefitHandler> handlersByType;

    public BenefitHandlerRegistry(List<BenefitHandler> handlers) {
        this.handlersByType = handlers.stream()
                .collect(Collectors.toMap(BenefitHandler::supportedType, Function.identity()));
    }

    /**
     * @throws IllegalStateException if a configured benefit type has no handler — a
     *         deployment/config error, surfaced loudly rather than silently dropped.
     */
    public BenefitHandler get(BenefitType type) {
        BenefitHandler handler = handlersByType.get(type);
        if (handler == null) {
            throw new IllegalStateException("No BenefitHandler registered for type " + type);
        }
        return handler;
    }
}
