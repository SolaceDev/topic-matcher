package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.Config;
import com.solace.maas.topicmatcher.PubOrSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

//@Component
public class TopicGeneratorBeer extends AbstractTopicGenerator {
    private Logger log = LoggerFactory.getLogger(TopicGeneratorBeer.class);

    //private BeerTopicGenerator publishingTopicGenerator = new BeerTopicGenerator(0, 0);
    //private BeerTopicGenerator subscriberGenerator = new BeerTopicGenerator(.2, .05);
    private MinimalBeerTopicGenerator generator = new MinimalBeerTopicGenerator(.1, 0.0);

    @Autowired
    Config config;

    @Override
    public String generateTopic(PubOrSub pub_or_sub) {
        List<String> topicLevels;

        if (pub_or_sub == PubOrSub.pub) {
            topicLevels = generator.generateOrderParts();
        } else {
            topicLevels = generator.generateOrderParts();
            topicLevels = generator.generateSubscriptionParts(topicLevels);
        }
        return String.join("/", topicLevels);
    }

    @Override
    public List<String> getTopics(PubOrSub pub_or_sub) {
        List<String> topics = generator.generatePublisherTopics();

        if (pub_or_sub == PubOrSub.sub) {
            List<String> subs = new LinkedList<>();
            for (String p : topics) {
                String s = generator.deriveSubscription(p);
                log.info(String.format("%s    %s", p, s));
                subs.add(generator.deriveSubscription(p));
            }
            topics = subs;
        }

        return topics;
    }

}
