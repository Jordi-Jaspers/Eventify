package org.jordijaspers.eventify.common.util.logging;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging the execution time of methods.
 */
@Slf4j
@Aspect
@Component
public class TimerAspect {

    /**
     * Logs the execution time of the annotated method.
     *
     * @param joinPoint The join point.
     * @return The result of the method.
     * @throws Throwable If something goes wrong.
     */
    @Around("@annotation(org.jordijaspers.eventify.common.util.logging.LogExecutionTime)")
    public Object logExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        final long startTime = System.currentTimeMillis();
        final Object proceed = joinPoint.proceed();
        final long endTime = System.currentTimeMillis();
        log.info("[Execution Timer] Method '{}' took {}ms", joinPoint.getSignature().getName(), endTime - startTime);
        return proceed;
    }
}
