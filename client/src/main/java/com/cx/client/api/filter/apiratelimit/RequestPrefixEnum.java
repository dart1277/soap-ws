package com.cx.client.api.filter.apiratelimit;

import lombok.RequiredArgsConstructor;

import java.util.Objects;

@RequiredArgsConstructor
public enum RequestPrefixEnum {
    RATE1("/public/rate1"),
    RATE2("/public/rate2"),
    RATE3("/public/rate3");
    public final String prefix;

    boolean matches(String requestUri) {
        requestUri = Objects.isNull(requestUri) ? "" : requestUri;
        return requestUri.contains(prefix);
    }
}
