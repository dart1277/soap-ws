package com.cx.client.api.filter;


import com.cx.client.api.filter.apiratelimit.ApiRateLimitManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(Ordered.LOWEST_PRECEDENCE - 1)
public class ApiRateLimitingFilter implements Filter {

    private final ApiRateLimitManager apiRateLimitManager;

    @Override
    public void doFilter(
            ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        String requestURI = req.getRequestURI();
        if (requestURI.contains("/public")) {
            try {
                apiRateLimitManager.rateLimitUri(requestURI);
                chain.doFilter(request, response);
            } catch (Throwable ex) {
                log.error("Request failed: " + ex.getMessage(), ex);
                if (ex instanceof IOException || ex instanceof ServletException) {
                    throw ex;
                } else {
                    sendErrorDto((HttpServletResponse) response, ex);
                }
            }
        } else {
            chain.doFilter(request, response);
        }
    }

    private void sendErrorDto(HttpServletResponse res, Throwable ex) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        res.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        res.setHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(res.getWriter(), new ErrorDto(ex.getMessage()));
        res.flushBuffer();
    }

    @RequiredArgsConstructor
    @Getter
    static class ErrorDto {
         final String message;
    }

}

