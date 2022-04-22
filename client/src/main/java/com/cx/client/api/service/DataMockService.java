package com.cx.client.api.service;

import com.cx.client.api.filter.apiratelimit.RequestPrefixEnum;
import com.cx.client.api.filter.apiratelimit.aspect.RateLimitApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class DataMockService {
    // private final RateLimiter rateLimiter;
    private long lastReqTime = 0;

    public String getResponse() {
        // long token = rateLimiter.getToken();
        long now = System.currentTimeMillis();
        if (now - lastReqTime < 1000/8) {
            //log.info("" +now/1000);
        }
        lastReqTime = now;
        return Long.toString(now/1000);

    }

    @RateLimitApi(prefix = RequestPrefixEnum.RATE3)
    public String getAnnotatedRateLimitedResponse() {
        // long token = rateLimiter.getToken();
        long now = System.currentTimeMillis();
        if (now - lastReqTime < 1000/8) {
            //log.info("" +now/1000);
        }
        lastReqTime = now;
        return Long.toString(now/1000);

    }
}
