package com.splitpay.transaction.aspect;

import com.splitpay.transaction.model.AuditLog;
import com.splitpay.transaction.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Pointcut("execution(* com.splitpay.transaction.TaxRuleController.*(..)) || execution(* com.splitpay.transaction.BulkController.*(..))")
    public void auditPointcut() {}

    @AfterReturning(pointcut = "auditPointcut()", returning = "result")
    public void logAuditAction(JoinPoint joinPoint, Object result) {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        
        AuditLog auditLog = AuditLog.builder()
                .action(methodName.toUpperCase())
                .entityName(className)
                .timestamp(LocalDateTime.now())
                .details("Executed " + methodName + " in " + className)
                .username("system") // In production, get from SecurityContext
                .build();

        auditLogRepository.save(auditLog);
        log.info("Audit log saved: {} - {}", auditLog.getAction(), auditLog.getEntityName());
    }
}
