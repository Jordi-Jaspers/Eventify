package nl.vodafoneziggo.smc.cucc.common.util.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Aspect for logging the execution time of methods.
 */
@Aspect
@Component
public class TimerAspect {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerAspect.class);

    /**
     * Logs the execution time of the annotated method.
     *
     * @param joinPoint The join point.
     * @return The result of the method.
     * @throws Throwable If something goes wrong.
     */
    @Around("@annotation(LogExecutionTime)")
    public Object logExecutionTime(final ProceedingJoinPoint joinPoint) throws Throwable {
        final long startTime = System.currentTimeMillis();
        final Object proceed = joinPoint.proceed();
        final long endTime = System.currentTimeMillis();
        LOGGER.info("[Execution Timer] Method '{}' took {}ms", joinPoint.getSignature().getName(), endTime - startTime);
        return proceed;
    }
}
