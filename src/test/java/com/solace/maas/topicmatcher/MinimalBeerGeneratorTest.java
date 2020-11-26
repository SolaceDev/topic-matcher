package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.BeerTopicGenerator;
import com.solace.maas.topicmatcher.service.MinimalBeerTopicGenerator;
import com.solace.maas.topicmatcher.service.TopicGeneratorBeer;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

// A visual inspection test
public class MinimalBeerGeneratorTest {
    private final Logger log = LoggerFactory.getLogger(MinimalBeerGeneratorTest.class);

    @Test
    public void testSubscriptionFromOrderTopic() {
        log.info("testSubscriptionFromOrderTopic:");
        TopicGeneratorBeer generator = new TopicGeneratorBeer();
        generator.getTopics(PubOrSub.pub);
        log.info("");
        generator.getTopics(PubOrSub.sub);
    }

}
