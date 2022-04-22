package com.cx.client.api.filter.apiratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ApiRateLimitManager {
    private static final Map<RequestPrefixEnum, RateLimiter> rateLimiterMap = new ConcurrentHashMap<>();

    static {
        Arrays.stream(RequestPrefixEnum.values())
                .forEach(requestPrefixEnum -> rateLimiterMap.put(requestPrefixEnum, new RateLimiter(requestPrefixEnum)));
    }


    public void rateLimitUri(String requestUri) {
        for (RequestPrefixEnum requestPrefixEnum : RequestPrefixEnum.values()) {
            if (requestPrefixEnum.matches(requestUri)) {
                log.debug("Rate limit uri: " + requestUri);
                rateLimitRequest(requestPrefixEnum);
                break;
            }
        }
    }

    public void rateLimitRequest(RequestPrefixEnum requestPrefixEnum) {
        log.debug("Rate limit request: " + requestPrefixEnum.prefix);
        RateLimiter rateLimiter = rateLimiterMap.get(requestPrefixEnum);
        rateLimiter.rateLimitRequest();
    }

}
