package com.cx.client.api.filter.apiratelimit.aspect;

import com.cx.client.api.filter.apiratelimit.RequestPrefixEnum;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimitApi {
    RequestPrefixEnum prefix();
}
