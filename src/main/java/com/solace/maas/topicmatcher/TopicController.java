package com.solace.maas.topicmatcher;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TopicController {

    @GetMapping("/hello")
    public String hello() {
        return "Hello from TopicMatcher!";
    }
}
