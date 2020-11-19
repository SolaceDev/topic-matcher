package com.solace.maas.topicmatcher;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RestController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TopicMatcher!";
    }
}
