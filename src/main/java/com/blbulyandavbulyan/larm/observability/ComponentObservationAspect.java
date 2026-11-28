package com.blbulyandavbulyan.larm.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Wraps calls to the application's own components and repositories into observations, so that traces show who
 * called whom. Spans are titled {@code Namespace.function}; the observation is named {@code larm.component} and the callee is described by {@code code.namespace} and
 * {@code code.function}.
 *
 * <p>The highest precedence makes the observation wrap the transaction, cache and logging proxies, so that work done
 * at transaction commit (flush) is still inside the span of the component that opened the transaction.
 */
@Aspect
@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
@RequiredArgsConstructor
public class ComponentObservationAspect {
    private static final String BASE_PACKAGE = "com.blbulyandavbulyan.larm";

    private final ObservationRegistry observationRegistry;

    @Around("(within(com.blbulyandavbulyan.larm..*)"
            + " && (@within(org.springframework.stereotype.Service)"
            + " || @within(org.springframework.stereotype.Component)"
            + " || @within(org.springframework.web.bind.annotation.RestController))"
            + " || execution(* com.blbulyandavbulyan.larm..*Repository+.*(..)))"
            + " && !within(com.blbulyandavbulyan.larm..config..*)"
            + " && !within(com.blbulyandavbulyan.larm..security..*)"
            + " && !within(com.blbulyandavbulyan.larm..validation..*)"
            + " && !within(com.blbulyandavbulyan.larm..observability..*)"
            + " && !within(com.blbulyandavbulyan.larm..*Mapper)")
    public Object observe(ProceedingJoinPoint joinPoint) throws Throwable {
        final String namespace = namespace(joinPoint);
        final String function = joinPoint.getSignature().getName();
        return Observation.createNotStarted("larm.component", observationRegistry)
                .contextualName(namespace + "." + function)
                .lowCardinalityKeyValue("code.namespace", namespace)
                .lowCardinalityKeyValue("code.function", function)
                .observeChecked((Observation.CheckedCallable<Object, Throwable>) joinPoint::proceed);
    }

    private static String namespace(ProceedingJoinPoint joinPoint) {
        final Object proxy = joinPoint.getThis();
        if (proxy != null && AopUtils.isJdkDynamicProxy(proxy)) {
            for (Class<?> type : AopProxyUtils.proxiedUserInterfaces(proxy)) {
                if (type.getName().startsWith(BASE_PACKAGE)) {
                    return type.getSimpleName();
                }
            }
        }
        final Object target = joinPoint.getTarget() != null ? joinPoint.getTarget() : proxy;
        if (target == null) {
            return joinPoint.getSignature().getDeclaringType().getSimpleName();
        }
        return AopUtils.getTargetClass(target).getSimpleName();
    }
}
