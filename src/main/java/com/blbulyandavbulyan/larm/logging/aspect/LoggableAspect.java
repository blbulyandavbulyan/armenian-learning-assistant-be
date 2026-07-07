package com.blbulyandavbulyan.larm.logging.aspect;

import com.blbulyandavbulyan.larm.logging.Loggable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggableAspect {

    @Around("@annotation(com.blbulyandavbulyan.larm.logging.Loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Loggable loggable = method.getAnnotation(Loggable.class);
        Loggable.LogLevel level = loggable.logLevel();

        String methodName = joinPoint.getSignature().getName();

        if (isLevelEnabled(logger, level)) {
            String params = Arrays.stream(joinPoint.getArgs())
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            log(logger, level, ">> {}({})", methodName, params);
        }

        try {
            Object result = joinPoint.proceed();
            if (isLevelEnabled(logger, level)) {
                String resultString = result != null ? result.toString() : "null";
                log(logger, level, "<< {}() -> {}", methodName, resultString);
            }
            return result;
        } catch (Throwable t) {
            // Errors should always be logged regardless of the configured level in the annotation
            // TODO NO DUMB ASS, THOSE ERRORS MIGHT BE THEN HANDLED BY SOME TRY CATCH !!!!!!!
            if (logger.isErrorEnabled()) {
                logger.error("<< {}() -> throws {}", methodName, t.getClass().getSimpleName(), t);
            }
            throw t;
        }
    }

    private static boolean isLevelEnabled(Logger logger, Loggable.LogLevel level) {
        return switch (level) {
            case DEBUG -> logger.isDebugEnabled();
            case INFO -> logger.isInfoEnabled();
            case WARN -> logger.isWarnEnabled();
            case ERROR -> logger.isErrorEnabled();
        };
    }

    private static void log(Logger logger, Loggable.LogLevel level, String message, Object... args) {
        switch (level) {
            case DEBUG -> logger.debug(message, args);
            case INFO -> logger.info(message, args);
            case WARN -> logger.warn(message, args);
            case ERROR -> logger.error(message, args);
        }
    }
}
