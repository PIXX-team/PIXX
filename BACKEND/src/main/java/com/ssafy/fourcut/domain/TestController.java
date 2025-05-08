package com.ssafy.fourcut.domain;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {
    @GetMapping("/test")
    public String index() {
        return "test 성공입니다.";
    }

    @GetMapping("/test/more")
    public String index2() {
        return "test2 성공입니다.";
    }

    @GetMapping("/test/more2")
    public String index3() {
        return "test3 성공입니다.";
    }
// jenkins-test
}
