package com.cx.server.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping(value = "/test")
public class TestController {
    @RequestMapping(method = RequestMethod.GET)
    public List<String> getData(@RequestParam String id, @RequestParam Long num) {
        return Arrays.asList("one", "test", id, "" + num);
    }
}