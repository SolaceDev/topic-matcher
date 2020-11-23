package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.igor.IgorTopicsRepoTreeImpl;
import com.solace.maas.topicmatcher.igor.TopicsRepo;
import com.solace.maas.topicmatcher.model.Topic;
import com.solace.maas.topicmatcher.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
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
    TopicAnalyzer2 newAnalyzer = new TopicAnalyzer2();

    TopicsRepo topicsRepo = new IgorTopicsRepoTreeImpl();

    boolean testRepo = true;
    boolean testAnalyzer = false;
    boolean testNew = true;

    @BeforeEach
    public void init() {
        config.setLargeDataSet(true);
        config.setHardCodedTopics(false);
        config.setLargeDataSetNumTopics(500_000);
        config.setMaxLevelLength(5);
        config.setMaxLevels(6);
        config.setMinLevels(3);
        config.setVocabularySize(7);
    }

    //@Test
    public void testAMillion() {
        log.info("Generating topics...{} ", topicGenerator);
        List<Topic> topics = topicGenerator.getTopics(PubOrSub.sub);

        long start = 0L;
        long end = 0L;

        if (testAnalyzer) {
            log.info("Setting up analyzer...");
            start = new Date().getTime();
            subscriberAnalyzer.analyze(PubOrSub.sub, topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testNew) {
            log.info("Setting up new analyzer...");
            start = new Date().getTime();
            newAnalyzer.analyze(PubOrSub.sub, topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
            //newAnalyzer.dump();
        }

        log.info("Searching...");
        doSearch(PubOrSub.pub, "AA/BB/CC");
        doSearch(PubOrSub.pub, "A/B/C");
        doSearch(PubOrSub.pub, "AA/BBFFF/CCD");
        doSearch(PubOrSub.pub, "BB/B/CAAAD/DD/EEF/GG");
        doSearch(PubOrSub.pub, "BB/B/CAAAD");
        log.info("Done.");
    }

    @Test
    public void testMatchingSubscriptions() {
        log.info("Generating topics...{} ", topicGenerator);
        List<Topic> topics = topicGenerator.getTopics(PubOrSub.pub);

        long start = 0L;
        long end = 0L;

        if (testRepo) {
            start = new Date().getTime();
            log.info("Setting up topicsRepo...");
            for (Topic topic : topics) {
                topicsRepo.registerTopic(topic.getTopicString());
            }
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testAnalyzer) {
            log.info("Setting up analyzer...");
            start = new Date().getTime();
            publisherAnalyzer.analyze(PubOrSub.pub, topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testNew) {
            log.info("Setting up new analyzer...");
            start = new Date().getTime();
            newAnalyzer.analyze(PubOrSub.pub, topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        log.info("Matching topics...");
        doSearch(PubOrSub.sub, "A/B/>");
        doSearch(PubOrSub.sub, "A*/BB/CC");
        doSearch(PubOrSub.sub, "AA/BBFFF/CCD");
        doSearch(PubOrSub.sub, "BB/B/CAAAD");
        doSearch(PubOrSub.sub, "BB/B/CAAAD/DD/EEF/GG");
        doSearch(PubOrSub.sub, ">");
        log.info("Done.");
    }

    private void doSearch(PubOrSub pubOrSub, String searchTopic) {
        if (testRepo) doSearch(pubOrSub, searchTopic, true, false);
        if (testAnalyzer) doSearch(pubOrSub, searchTopic, false, false);
        if (testNew) doSearch(pubOrSub, searchTopic, false, true);
        log.info("");
    }
    
    private void doSearch(PubOrSub pubOrSub, String searchTopic, boolean testTree, boolean testNew) {

        List<String> matchingTopics = null;

        long start = new Date().getTime();
        if (testTree) {
            matchingTopics = topicsRepo.findMatchingTopics(searchTopic);
        } else {
            if (testNew) {
                matchingTopics = pubOrSub == PubOrSub.pub ?
                        newAnalyzer.matchFromPublisher(searchTopic) :
                        newAnalyzer.matchFromSubscriber(searchTopic);
            } else {
                matchingTopics = pubOrSub == PubOrSub.pub ? subscriberAnalyzer.matchFromPublisher(searchTopic)
                        : publisherAnalyzer.matchFromSubscriber(searchTopic);
            }
        }
        long end = new Date().getTime();
        long millis = end - start;

        if (matchingTopics.size() > 50) {
            log.info(String.format("Search: tree: %5s new: %5s time: %4d %16s matches: %s", testTree, testNew, millis,
                    searchTopic,
                    matchingTopics.size()));
        } else {
            log.info(String.format("Search: tree: %5s new: %5s time: %4d %16s matches: %s", testTree, testNew, millis,
                    searchTopic,
                    matchingTopics));
        }
    }


}
