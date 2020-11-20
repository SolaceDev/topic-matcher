package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.model.Application;
import com.solace.maas.topicmatcher.service.TopicService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TopicController {

    @Autowired
    TopicService topicService;

    @GetMapping("/")
    public String hello() {
        return "Hello from TopicMatcher!";
    }

    @GetMapping("/applications")
    public List<Application> getApplications() {
        return topicService.getApplications();
    }

    @GetMapping("/applications/{name}")
    public Application getApplication(@PathVariable String name) {
        return topicService.getApplication(name);
    }

    @PostMapping("/applications/{name}/subscriptions")
    public Application addSubscription(@PathVariable String name, @RequestBody String body) {
        return topicService.addSubscription(name, body);
    }
}
