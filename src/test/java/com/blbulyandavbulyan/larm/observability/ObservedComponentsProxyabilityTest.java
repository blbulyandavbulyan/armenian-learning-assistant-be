package com.blbulyandavbulyan.larm.observability;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * {@link ComponentObservationAspect} CGLIB-proxies the beans it matches, so those must not be final and must not
 * have public final methods (those would run on the proxy instance, whose fields are null).
 */
class ObservedComponentsProxyabilityTest {
    @Test
    void componentsAreProxyable() throws ClassNotFoundException {
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Service.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Component.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        List<String> violations = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.blbulyandavbulyan.larm")) {
            Class<?> type = Class.forName(definition.getBeanClassName());
            String name = type.getName();
            if (name.contains(".config.") || name.contains(".security.") || name.contains(".validation.")
                    || name.contains(".observability.") || name.endsWith("Mapper")) {
                continue;
            }
            if (Modifier.isFinal(type.getModifiers())) {
                violations.add(name + " is final");
            }
            for (Method method : type.getDeclaredMethods()) {
                if (Modifier.isPublic(method.getModifiers()) && Modifier.isFinal(method.getModifiers())) {
                    violations.add(name + "." + method.getName() + " is public final");
                }
            }
        }
        assertThat(violations).isEmpty();
    }
}
