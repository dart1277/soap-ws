package com.cx.client.api.controller;

import com.cx.client.api.service.DataMockService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping(value="/public")
public class RateLimitTestController {
    private final DataMockService dataMockService;

    @GetMapping("rate1")
    public String rateLimit1() {
        return dataMockService.getResponse();
    }

    @GetMapping("rate2")
    public String rateLimit2() {
        return dataMockService.getResponse();
    }

    @GetMapping("rate3")
    public String rateLimit3() {
        return dataMockService.getResponse();
    }

    @GetMapping("rate4")
    public String rateLimit4() {
        return dataMockService.getAnnotatedRateLimitedResponse();
    }
}
