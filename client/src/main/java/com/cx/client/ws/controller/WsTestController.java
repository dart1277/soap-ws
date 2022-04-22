package com.cx.client.ws.controller;

import com.cx.client.ws.client.CountryClient;
import com.cx.client.ws.controller.feign.QueryMap;
import com.cx.client.ws.controller.feign.SslFeignClient;
import com.cx.client.ws.dto.countries.GetCountryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping(value="/test")
public class WsTestController {

    private final CountryClient countryClient;
    private final SslFeignClient sslFeignClient;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public GetCountryResponse getCountries(){
        return countryClient.getCountry("Poland");
    }

    @GetMapping(value = "rest",produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> testRest(){
        return sslFeignClient.getSslTest(new QueryMap("id", 123L));
    }

}
