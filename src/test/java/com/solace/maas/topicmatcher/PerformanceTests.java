package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.carlstitching.CriteriaSubscription;
import com.solace.maas.topicmatcher.carlstitching.CriteriaTopic;
import com.solace.maas.topicmatcher.carlstitching.SubscriptionMatcher;
import com.solace.maas.topicmatcher.igor.IgorTopicsRepoTreeImpl;
import com.solace.maas.topicmatcher.igor.TopicsRepo;
import com.solace.maas.topicmatcher.model.Topic;
import com.solace.maas.topicmatcher.service.AbstractTopicGenerator;
import com.solace.maas.topicmatcher.service.TopicAnalyzer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest
@ActiveProfiles("test")
public class PerformanceTests {

    private final Logger log = LoggerFactory.getLogger(PerformanceTests.class);
    @Autowired
    Config config;

    @Autowired
    AbstractTopicGenerator topicGenerator;

    TopicAnalyzer topicAnalyzer = new TopicAnalyzer();
    TopicsRepo topicsRepo = new IgorTopicsRepoTreeImpl();
    SubscriptionMatcher subscriptionMatcher = new SubscriptionMatcher();

    boolean testAnalyzer = true;
    boolean testRepo = false;
    boolean testSubscriptionMatcher = true;

    @BeforeEach
    public void init() {
        config.setLargeDataSet(true);
        config.setHardCodedTopics(true);
        config.setLargeDataSetNumTopics(500);
        config.setMaxLevelLength(5);
        config.setMaxLevels(6);
        config.setMinLevels(3);
        config.setVocabularySize(7);
    }

    @Test
    public void testMatchingTopics() {
        log.info("Generating topics...{} ", topicGenerator);
        List<Topic> topics = topicGenerator.getTopics(PubOrSub.sub);

        long start = 0L;
        long end = 0L;

        if (testAnalyzer) {
            log.info("Setting up analyzer...");
            start = new Date().getTime();
            topicAnalyzer.analyze(PubOrSub.sub, topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testSubscriptionMatcher) {
            log.info("Setting up subscription matcher...");
            start = new Date().getTime();

            List<CriteriaSubscription> subscriptions = topics.stream()
                    .map(topic -> CriteriaSubscription.builder()
                            .matchCriteria(topic.getTopicString())
                            .build())
                    .collect(Collectors.toCollection(LinkedList::new));

            SubscriptionMatcher subscriptionMatcher = new SubscriptionMatcher();
            subscriptionMatcher.setSubscriptions(subscriptions);
            subscriptionMatcher.parseCriterias();
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        log.info("Searching...");
        doSearch(PubOrSub.pub, "AA/BB/CC");
        doSearch(PubOrSub.pub, "A/B/C");
        doSearch(PubOrSub.pub, "AA/BBFFF/CCD");
        doSearch(PubOrSub.pub, "BB/B/CAAAD/DD/EEF/GG");
        doSearch(PubOrSub.pub, "BB/B/CAAAD");
        log.info("Done.");
    }

    //@Test
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
            topicAnalyzer.analyze(PubOrSub.pub, topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testSubscriptionMatcher) {
            log.info("Setting up subscription matcher...");
            start = new Date().getTime();

            List<CriteriaSubscription> subscriptions = topics.stream()
                    .map(topic -> CriteriaSubscription.builder()
                            .matchCriteria(topic.getTopicString())
                            .build())
                    .collect(Collectors.toCollection(LinkedList::new));

            SubscriptionMatcher subscriptionMatcher = new SubscriptionMatcher();
            subscriptionMatcher.setSubscriptions(subscriptions);
            subscriptionMatcher.parseCriterias();
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
        if (testRepo) doSearch(pubOrSub, searchTopic, Implementation.topicsRepo);
        if (testAnalyzer) doSearch(pubOrSub, searchTopic, Implementation.topicAnalyzer);
        if (testSubscriptionMatcher) doSearch(pubOrSub, searchTopic, Implementation.subscriptionMatcher);
        log.info("");
    }

    private void doSearch(PubOrSub pubOrSub, String searchTopic, Implementation implementation) {

        List<String> matchingTopics = null;

        long start = new Date().getTime();
        switch (implementation) {
            case topicsRepo:
                matchingTopics = topicsRepo.findMatchingTopics(searchTopic);
                break;
            case topicAnalyzer:
                matchingTopics = topicAnalyzer.match(pubOrSub, searchTopic);
                break;
            case subscriptionMatcher:
                CriteriaTopic criteriaTopic = subscriptionMatcher.getSubscriptionsForTopic(searchTopic);
                //log.info("criteriaTopic: {}", criteriaTopic);
                matchingTopics = criteriaTopic.getSubscriptions().stream().map(s -> s.getMatchCriteria()).collect(
                        Collectors.toList());
                break;
            default:
                throw new IllegalStateException("Unsupported implementation " + implementation);
        }

        long end = new Date().getTime();
        long millis = end - start;

        if (matchingTopics.size() > 20) {
            log.info(String.format("Search: impl: %19s time: %4d %16s matches: %s", implementation, millis,
                    searchTopic,
                    matchingTopics.size()));
        } else {
            log.info(String.format("Search: impl: %19s time: %4d %16s matches: %s", implementation, millis,
                    searchTopic,
                    matchingTopics));
        }
    }

    private static enum Implementation { subscriptionMatcher, topicAnalyzer, topicsRepo }
}
