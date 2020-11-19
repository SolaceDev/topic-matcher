package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.BeerTopicGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.IntStream;

// A visual inspection test
public class BeerGeneratorTest {
    private final BeerTopicGenerator generator = new BeerTopicGenerator(.4,.1);
    private final Logger log = LoggerFactory.getLogger(BeerGeneratorTest.class);

    @Test
    public void testDeliveryGenerator() {
        log.info(generator.generateDeliveryTopic());
    }

    @Test
    public void testOrderGenerator() {
        log.info(generator.generateOrderTopic());
    }

    @Test
    public void testSubscriptionFromOrderTopic() {
        String topic = generator.generateOrderTopic();
        log.info(topic);
        log.info(generator.generateSubscription(topic));
    }

    @Test
    public void testSubscriptionFromDeliveryTopic() {
        String topic = generator.generateDeliveryTopic();
        log.info(topic);
        log.info(generator.generateSubscription(topic));
    }

    @Test
    public void testDeliverySubscriptionGenerator() {
        IntStream.range(0, 5).forEach(i -> {
            log.info(generator.generateDeliverySubscription());
            log.info(generator.generateOrderSubscription());
        });
    }
}
