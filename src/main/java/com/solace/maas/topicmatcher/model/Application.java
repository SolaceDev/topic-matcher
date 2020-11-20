package com.solace.maas.topicmatcher.model;

import java.util.ArrayList;
import java.util.List;

public class Application {

    private String name;
    private List<String> subscribingTopics = new ArrayList<>();
    private List<String> publishingTopics = new ArrayList<>();
    private List<String> topicsMatchingSubscriptions = new ArrayList<>();

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

    public List<String> getTopicsMatchingSubscriptions() {
        return topicsMatchingSubscriptions;
    }

    public void setTopicsMatchingSubscriptions(List<String> topicsMatchingSubscriptions) {
        this.topicsMatchingSubscriptions = topicsMatchingSubscriptions;
    }

    @Override
    public String toString() {
        return "Application{" +
                "name='" + name + '\'' +
                ", publishingTopics=" + publishingTopics +
                ", subscribingTopics=" + subscribingTopics +
                ", topicsMatchingSubscriptions=" + topicsMatchingSubscriptions +
                '}';
    }
}
