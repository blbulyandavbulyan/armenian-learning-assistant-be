package com.blbulyandavbulyan.larm.observability;

import java.util.HashMap;
import java.util.Map;

import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationHandler;
import io.micrometer.observation.ObservationRegistry;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ComponentObservationAspectTest {
    interface Greeter {
        String greet();
    }

    static class GreeterImpl implements Greeter, Runnable {
        @Override
        public String greet() {
            return "hello";
        }

        @Override
        public void run() {
            // only here so the proxy also exposes an interface outside the application package
        }
    }

    static class Plain {
    }

    private final Map<String, String> tags = new HashMap<>();
    private ComponentObservationAspect aspect;

    @BeforeEach
    void setUp() {
        ObservationRegistry registry = ObservationRegistry.create();
        registry.observationConfig().observationHandler(new ObservationHandler<>() {
            @Override
            public void onStop(Observation.Context context) {
                for (KeyValue keyValue : context.getLowCardinalityKeyValues()) {
                    tags.put(keyValue.getKey(), keyValue.getValue());
                }
            }

            @Override
            public boolean supportsContext(Observation.Context context) {
                return true;
            }
        });
        aspect = new ComponentObservationAspect(registry);
    }

    private ProceedingJoinPoint joinPoint(Object proxy, Object target) throws Throwable {
        Signature signature = mock(Signature.class);
        when(signature.getName()).thenReturn("greet");
        when(signature.getDeclaringType()).thenReturn(Greeter.class);
        ProceedingJoinPoint joinPoint = mock(ProceedingJoinPoint.class);
        when(joinPoint.getThis()).thenReturn(proxy);
        when(joinPoint.getTarget()).thenReturn(target);
        when(joinPoint.getSignature()).thenReturn(signature);
        when(joinPoint.proceed()).thenReturn("result");
        return joinPoint;
    }

    private static Object jdkProxy(Object target, Class<?>... interfaces) {
        ProxyFactory factory = new ProxyFactory(target);
        factory.setInterfaces(interfaces);
        return factory.getProxy();
    }

    @Test
    void jdkProxyIsNamedAfterItsApplicationInterface() throws Throwable {
        GreeterImpl target = new GreeterImpl();

        Object result = aspect.observe(joinPoint(jdkProxy(target, Runnable.class, Greeter.class), target));

        assertThat(result).isEqualTo("result");
        assertThat(tags)
                .containsEntry("code.namespace", "Greeter")
                .containsEntry("code.function", "greet");
    }

    @Test
    void jdkProxyWithoutApplicationInterfaceIsNamedAfterItsTargetClass() throws Throwable {
        GreeterImpl target = new GreeterImpl();

        aspect.observe(joinPoint(jdkProxy(target, Runnable.class), target));

        assertThat(tags).containsEntry("code.namespace", "GreeterImpl");
    }

    @Test
    void targetWinsOverANonProxyThis() throws Throwable {
        aspect.observe(joinPoint(new Plain(), new GreeterImpl()));

        assertThat(tags).containsEntry("code.namespace", "GreeterImpl");
    }

    @Test
    void thisIsUsedWhenThereIsNoTarget() throws Throwable {
        aspect.observe(joinPoint(new Plain(), null));

        assertThat(tags).containsEntry("code.namespace", "Plain");
    }

    @Test
    void declaringTypeIsUsedWhenThereIsNeitherThisNorTarget() throws Throwable {
        aspect.observe(joinPoint(null, null));

        assertThat(tags).containsEntry("code.namespace", "Greeter");
    }
}
