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
import org.springframework.test.context.event.annotation.BeforeTestClass;
import org.springframework.test.context.event.annotation.BeforeTestMethod;

import java.util.List;

@SpringBootTest
class MainTests {

    private final Logger log = LoggerFactory.getLogger(MainTests.class);

    @Autowired
    Config config;

    @Autowired
    TopicService topicService;

    //@BeforeTestMethod
    @BeforeEach
    public void init() {
        log.info("BEFORE --------------------------------");
        config.setLargeDataSet(true);
        topicService.init();
    }

    @Test
    void testMatching() {
        testPublisherToSubscriber();
        testSubscriberToPublisher();
    }

    private void testPublisherToSubscriber() {
        log.info("Matching publisher to subscribers");
        doSearch(PubOrSub.pub, "A");
        doSearch(PubOrSub.pub,"A/A");
        doSearch(PubOrSub.pub,"A/A/A");
        doSearch(PubOrSub.pub,"A/B/C");
        doSearch(PubOrSub.pub,"B/A/A");
        doSearch(PubOrSub.pub,"B/C/C/E");
        doSearch(PubOrSub.pub,"B");
    }

    private void testSubscriberToPublisher() {
        log.info("Matching subscriber to publishers");
        doSearch( PubOrSub.sub, "A");
        doSearch( PubOrSub.sub, "A/*");
        doSearch( PubOrSub.sub, "A/*/B/>");
        doSearch( PubOrSub.sub, "A/*/*/F/*");
        doSearch( PubOrSub.sub, "A/B/C/E/F");
        doSearch( PubOrSub.sub, "A/>");
        doSearch( PubOrSub.sub, ">");
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
