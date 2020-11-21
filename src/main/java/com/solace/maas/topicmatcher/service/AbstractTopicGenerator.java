package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Topic;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractTopicGenerator {

    private static AtomicInteger lastId = new AtomicInteger();

    abstract public Topic generateTopic(PubOrSub pub_or_sub, String id);
    abstract public List<Topic> getTopics(PubOrSub pub_or_sub);

    public String getNextId() {
        return "T" + lastId.incrementAndGet();
    }

}
