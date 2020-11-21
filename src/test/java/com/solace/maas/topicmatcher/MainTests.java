package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.TopicAnalyzer;
import com.solace.maas.topicmatcher.service.TopicService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class MainTests {

    private final Logger log = LoggerFactory.getLogger(MainTests.class);

    @Autowired
    Config config;

    @Autowired
    TopicService topicService;

    @BeforeEach
    public void init() {
        config.setHardCodedTopics(true);
        //config.setMaxLevelLength(20);
        topicService.init();
    }

    @Test
    void testMatching() {
        //testPublisherToSubscriber();
        testSubscriberToPublisher();
    }

    private void testPublisherToSubscriber() {
        log.info("Matching publisher to subscribers");
        doSearch(PubOrSub.pub, "AAA/BBB/CCC");
    }

    private void testSubscriberToPublisher() {
        log.info("Matching subscriber to publishers");
        //doSearch( PubOrSub.sub, "A/>");
        doSearch( PubOrSub.sub, "AA*/*/CCC");
    }

    private void doSearch(PubOrSub pubOrSub, String searchTopic) {

        List<String> matchingTopics = topicService.getMatches(pubOrSub, searchTopic);

        if (matchingTopics.size() > 20) {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics.size()));
        } else {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics));
        }
    }

}
