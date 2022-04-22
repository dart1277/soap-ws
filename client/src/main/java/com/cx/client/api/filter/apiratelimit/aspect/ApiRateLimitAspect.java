package com.cx.client.api.filter.apiratelimit.aspect;

import com.cx.client.api.filter.apiratelimit.ApiRateLimitManager;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Aspect
@Component
@RequiredArgsConstructor
public class ApiRateLimitAspect {

    private final ApiRateLimitManager apiRateLimitManager;

    @Around("@annotation(RateLimitApi)")
    public Object rateLimitMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimitApi rateLimitApi = AnnotationUtils.getAnnotation(signature.getMethod(), RateLimitApi.class);
        if(Objects.nonNull(rateLimitApi)) {
            apiRateLimitManager.rateLimitRequest(rateLimitApi.prefix());
        } else {
            throw new IllegalArgumentException(RateLimitApi.class.getName()  + " not found on method " + signature.getName());
        }
        return joinPoint.proceed();
    }
}


