package com.gym.training.logging;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class ServiceLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(ServiceLoggingAspect.class);

    @Before("execution(* com.gym.training.service.*.*(..))")
    public void logBefore(JoinPoint joinPoint) {
        log.info("Operation START: {} args={} [txId={}]",
                joinPoint.getSignature().toShortString(),
                Arrays.toString(joinPoint.getArgs()),
                MDC.get("transactionId"));
    }

    @AfterReturning(pointcut = "execution(* com.gym.training.service.*.*(..))", returning = "result")
    public void logAfter(JoinPoint joinPoint, Object result) {
        log.info("Operation END: {} result={} [txId={}]",
                joinPoint.getSignature().toShortString(),
                result,
                MDC.get("transactionId"));
    }
}
