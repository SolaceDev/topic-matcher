package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.PubOrSub;

import java.util.List;

public abstract class AbstractTopicGenerator {
    abstract public List<String> getTopics(PubOrSub pub_or_sub);
    abstract public String generateTopic(PubOrSub pub_or_sub);
}
