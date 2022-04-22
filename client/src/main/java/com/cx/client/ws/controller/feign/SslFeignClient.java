package com.cx.client.ws.controller.feign;

import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

// @FeignClient(url = "", contextId = "", name = "", configuration = SslFeignConfig.class)
public interface SslFeignClient {

    @GetMapping(value = "test")
    List<String> getSslTest(@SpringQueryMap QueryMap queryMap);

}
