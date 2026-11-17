package com.globaltrust.bank.audit;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.globaltrust.bank.security.SecurityUtils;

/**
 * Cross-cutting console trace for every Transformation Procedure on the service
 * layer. This is the AOP companion to the persistent {@link AuditService}: it
 * gives a live, readable trail during a demo without touching the audit table.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger log = LoggerFactory.getLogger("AUDIT-TRACE");

    @Around("execution(* com.globaltrust.bank.service.*Service.*(..))")
    public Object trace(ProceedingJoinPoint pjp) throws Throwable {
        String who = SecurityUtils.currentUsername();
        String what = pjp.getSignature().toShortString();
        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            log.info("OK   user={} tp={} ({} ms)", who, what, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable ex) {
            log.warn("DENY user={} tp={} -> {}: {}", who, what,
                    ex.getClass().getSimpleName(), ex.getMessage());
            throw ex;
        }
    }
}
