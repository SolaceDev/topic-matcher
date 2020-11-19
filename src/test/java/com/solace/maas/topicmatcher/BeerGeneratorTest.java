package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.BeerTopicGenerator;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// A visual inspection test
public class BeerGeneratorTest {
    private final BeerTopicGenerator generator = new BeerTopicGenerator();
    private final Logger log = LoggerFactory.getLogger(BeerGeneratorTest.class);

    @Test
    public void testDeliveryGenerator() {
        for (String s : generator.generateDeliveryTopics(10)) {
            log.info(s);
        }
    }

    @Test
    public void testOrderGenerator() {
        for (String s : generator.generateOrderTopics(10)) {
            log.info(s);
        }
    }
}
