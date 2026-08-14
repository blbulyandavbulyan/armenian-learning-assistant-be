package com.blbulyandavbulyan.larm.validation.srategies;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.internal.properties.DefaultGetterPropertySelectionStrategy;
import org.hibernate.validator.spi.properties.ConstrainableExecutable;

@Slf4j
public class HybridGetterStrategy extends DefaultGetterPropertySelectionStrategy {

    @Override
    public Optional<String> getProperty(ConstrainableExecutable executable) {
        log.debug("Executing hybrid getter strategy for executable: {}", executable.getName());
        return super.getProperty(executable).or(() -> getPropertyForRecord(executable));
    }

    private Optional<String> getPropertyForRecord(ConstrainableExecutable executable) {
        String methodName = executable.getName();
        if (executable.getParameterTypes().length != 0) {
            log.debug("The amount of parameters is non zero for {}, so it is not record accessor", executable.getName());
            return Optional.empty();
        }

        log.debug("Assuming executable: {} is record accessor method", executable.getName());
        return Optional.of(methodName);
    }

    @Override
    public List<String> getGetterMethodNameCandidates(String propertyName) {
        log.debug("Executing hybrid getter strategy for property: {}", propertyName);
        List<String> getterMethodNameCandidates = new ArrayList<>(super.getGetterMethodNameCandidates(propertyName));
        getterMethodNameCandidates.add(propertyName);
        return getterMethodNameCandidates;
    }
}
