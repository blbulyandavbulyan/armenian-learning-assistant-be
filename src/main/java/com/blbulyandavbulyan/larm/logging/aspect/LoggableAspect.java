package com.blbulyandavbulyan.larm.logging.aspect;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

import com.blbulyandavbulyan.larm.logging.Loggable;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggableAspect {

    @Around("@annotation(com.blbulyandavbulyan.larm.logging.Loggable)")
    public Object logMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass());
        Method method = ((MethodSignature) joinPoint.getSignature()).getMethod();
        Loggable loggable = method.getAnnotation(Loggable.class);
        Loggable.LogLevel level = loggable.logLevel();

        if (!isLevelEnabled(logger, level)) {
            return joinPoint.proceed();
        }

        String methodName = joinPoint.getSignature().getName();
        String params = Arrays.stream(joinPoint.getArgs())
                .map(String::valueOf)
                .collect(Collectors.joining(", "));

        log(logger, level, ">> {}({})", methodName, params);

        try {
            Object result = joinPoint.proceed();
            String resultString = result != null ? result.toString() : "null";
            log(logger, level, "<< {}() -> {}", methodName, resultString);
            return result;
        } catch (Throwable t) {
            log(logger, level, "<< {}() -> throws {}", methodName, t.getClass().getSimpleName(), t);
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
