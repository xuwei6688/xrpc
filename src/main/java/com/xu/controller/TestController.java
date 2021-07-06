package com.xu.controller;

import com.xu.server.Remote;
import org.springframework.stereotype.Controller;

@Controller
public class TestController {

    @Remote("print")
    public String print(String str) {
        System.out.println(str);
        return "print success";
    }
}
