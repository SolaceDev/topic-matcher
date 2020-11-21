package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.AbstractTopicGenerator;
import com.solace.maas.topicmatcher.service.TopicAnalyzer;
import com.solace.maas.topicmatcher.service.AlphabetTopicGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class PerformanceTests {

    private final Logger log = LoggerFactory.getLogger(PerformanceTests.class);
    @Autowired
    Config config;

    @Autowired
    AbstractTopicGenerator topicGenerator;

    TopicAnalyzer subscriberAnalyzer = new TopicAnalyzer();
    TopicAnalyzer publisherAnalyzer = new TopicAnalyzer();

    @BeforeEach
    public void init() {
        config.setLargeDataSet(true);
        config.setHardCodedTopics(false);
        //config.setMaxLevelLength(20);
    }

    @Test
    public void testAMillion() {

        log.info("Analyzing...");
        subscriberAnalyzer.analyze(PubOrSub.sub, topicGenerator.getTopics(PubOrSub.sub));
        log.info("Searching...");
        doSearch(PubOrSub.pub, "AA/BB/CC");
        doSearch(PubOrSub.pub, "A/B/C");
        doSearch(PubOrSub.pub, "AA/BBFFF/CCD");
        doSearch(PubOrSub.pub, "BB/B/CAAAD");
    }

    private void doSearch(PubOrSub pubOrSub, String searchTopic) {


        List<String> matchingTopics = pubOrSub == PubOrSub.pub ? subscriberAnalyzer.matchFromPublisher(searchTopic)
                : publisherAnalyzer.matchFromSubscriber(searchTopic);

        if (matchingTopics.size() > 50) {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics.size()));
        } else {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics));
        }
    }


}
