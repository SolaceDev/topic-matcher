package com.solace.maas.topicmatcher.model;

import java.util.ArrayList;
import java.util.List;

public class Application {

    private String name;
    private List<String> subscribingTopics = new ArrayList<>();
    private List<String> publishingTopics = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSubscribingTopics() {
        return subscribingTopics;
    }

    public void setSubscribingTopics(List<String> subscribingTopics) {
        this.subscribingTopics = subscribingTopics;
    }

    public List<String> getPublishingTopics() {
        return publishingTopics;
    }

    public void setPublishingTopics(List<String> publishingTopics) {
        this.publishingTopics = publishingTopics;
    }

    public void addPublishingTopic(String topicString) {
        publishingTopics.add(topicString);
    }

    public void addSubscribingTopic(String topicString) {
        subscribingTopics.add(topicString);
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", subscribingTopics=" + subscribingTopics +
                ", publishingTopics=" + publishingTopics +
                '}';
    }
}
