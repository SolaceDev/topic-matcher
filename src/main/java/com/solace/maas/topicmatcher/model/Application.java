package com.solace.maas.topicmatcher.model;

import java.util.List;

public class Application {

    private String name;
    private List<String> subscriptions;
    private List<String> publishingTopics;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<String> subscriptions) {
        this.subscriptions = subscriptions;
    }

    public List<String> getPublishingTopics() {
        return publishingTopics;
    }

    public void setPublishingTopics(List<String> publishingTopics) {
        this.publishingTopics = publishingTopics;
    }
}
