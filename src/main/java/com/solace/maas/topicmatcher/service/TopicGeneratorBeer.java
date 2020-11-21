package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.Config;
import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//@Component
public class TopicGeneratorBeer extends AbstractTopicGenerator {
    private Logger log = LoggerFactory.getLogger(TopicGeneratorBeer.class);
    private StringBuilder stringBuilder = new StringBuilder();

    private BeerTopicGenerator publishingTopicGenerator = new BeerTopicGenerator(0, 0);
    private BeerTopicGenerator subscriberGenerator = new BeerTopicGenerator(.2, .05);

    @Autowired
    Config config;

    @Override
    public Topic generateTopic(PubOrSub pub_or_sub, String id) {
        stringBuilder.delete(0, stringBuilder.length());
        List<String> topicLevels;

        if (pub_or_sub == PubOrSub.pub) {
            topicLevels = publishingTopicGenerator.generateOrderParts();
        } else {
            topicLevels = subscriberGenerator.generateOrderParts();
            topicLevels = subscriberGenerator.generateSubscriptionParts(topicLevels);
        }
        return new Topic(id, topicLevels.size(), String.join("/", topicLevels), topicLevels);
    }

    @Override
    public List<Topic> getTopics(PubOrSub pub_or_sub) {
        List<Topic> topics = new ArrayList<>();

        for (int i = 0; i < config.getNumTopics(); i++) {
            topics.add(generateTopic(pub_or_sub, getNextId()));
        }

        return topics;
    }

}
