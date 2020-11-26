package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.carlstitching.CriteriaSubscription;
import com.solace.maas.topicmatcher.carlstitching.CriteriaTopic;
import com.solace.maas.topicmatcher.carlstitching.SubscriptionMatcher;
import com.solace.maas.topicmatcher.carlstitching.TopicMatcher;
import com.solace.maas.topicmatcher.eh.SubscriptionSet;
import com.solace.maas.topicmatcher.eh.TopicSet;
import com.solace.maas.topicmatcher.igor.IgorTopicsRepoTreeImpl;
import com.solace.maas.topicmatcher.igor.TopicsRepo;
import com.solace.maas.topicmatcher.service.AbstractTopicGenerator;
import com.solace.maas.topicmatcher.service.TopicAnalyzer;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;
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
    TopicMatcher topicMatcher = new TopicMatcher();
    SubscriptionSet subscriptionSet = null;
    TopicSet topicSet = null;
    TopicSet cachedTopicSet = null;
    List<Pair<Implementation, Collection<String>>> results = new ArrayList<>();

    boolean testAnalyzer = true;
    boolean testTopicsRepo = true;
    boolean testSubscriptionMatcher = true;
    boolean testTopicMatcher = true;
    boolean testSubscriptionSet = true;
    boolean testTopicSet = true;
    boolean testCachedTopicSet = true;
    boolean waitForProfiler = false; // It true, we'll wait 10 seconds so that we can hook up VisualVM.


    @BeforeEach
    public void init() {
        config.setLargeDataSet(true);
        config.setHardCodedTopics(false);
        config.setLargeDataSetNumTopics(100_000);
        config.setMaxLevelLength(5); // number of chars on each level
        config.setMaxLevels(8);  // max number of levels
        config.setMinLevels(3);
        config.setMaxNumTopicsLogged(20);
        config.setVocabularySize(7); // Number of different alphabet chars used to generate topics.

        if (waitForProfiler) {
            try {
                Thread.sleep(20000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testFindSubscriptionsForPublisher() {
        log.info("Generating topics...{} ", topicGenerator);
        List<String> topics = topicGenerator.getTopics(PubOrSub.sub);

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
                            .matchCriteria(topic)
                            .build())
                    .collect(Collectors.toCollection(LinkedList::new));

            subscriptionMatcher = new SubscriptionMatcher();
            subscriptionMatcher.setSubscriptions(subscriptions);
            subscriptionMatcher.parseCriterias();
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testSubscriptionSet) {
            log.info("Setting up subscription set...");
            start = new Date().getTime();
            subscriptionSet = new SubscriptionSet(topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        // TopicsRepo doesn't yet support matching a pub against a list of subs.
        boolean lastTopicsRepoVal = testTopicsRepo;
        boolean lastTopicMatcherVal = testTopicMatcher;
        testTopicsRepo = false;
        testTopicMatcher = false;
        testTopicSet = false;
        testCachedTopicSet = false;

        log.info("Searching...");
        doSearch(PubOrSub.pub, "AA/BB/CC");
        doSearch(PubOrSub.pub, "A/B/C");
        doSearch(PubOrSub.pub, "AA/BBFFF/CCD");
        doSearch(PubOrSub.pub, "BB/B/CAAAD/DD/EEF/GG");
        doSearch(PubOrSub.pub, "BB/B/CAAAD");
        log.info("Done.");

        testTopicsRepo = lastTopicsRepoVal;
        testTopicMatcher = lastTopicMatcherVal;
    }

    @Test
    public void testFindPublishersForSubscription() {
        log.info("Generating topics...{} ", topicGenerator);
        List<String> topics = topicGenerator.getTopics(PubOrSub.pub);

        long start = 0L;
        long end = 0L;

        if (testTopicsRepo) {
            start = new Date().getTime();
            log.info("Setting up topicsRepo...");
            topics.forEach(topicsRepo::registerTopic);
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

        if (testTopicMatcher) {
            log.info("Setting up topicMatcher...");
            start = new Date().getTime();
            topicMatcher.setTopics(topics);
            topicMatcher.parseTopics();
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testTopicSet) {
            log.info("Setting up topicSet...");
            start = new Date().getTime();
            topicSet = new TopicSet(topics);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        if (testTopicSet) {
            log.info("Setting up cachedTopicSet...");
            start = new Date().getTime();
            cachedTopicSet = new TopicSet(topics, true);
            end = new Date().getTime();
            log.info("Duration: {}", end - start);
        }

        boolean lastVal = testSubscriptionMatcher;
        testSubscriptionMatcher = false;
        testSubscriptionSet = false;

        log.info("Matching topics...");
        doSearch(PubOrSub.sub, "*/B*/C*/*/>");
        doSearch(PubOrSub.sub, "*/*/*/*/>");
        doSearch(PubOrSub.sub, "A/B/>");
        doSearch(PubOrSub.sub, "A*/BB/CC");
        doSearch(PubOrSub.sub, "AA/BBFFF/CCD");
        doSearch(PubOrSub.sub, "BB/B/CAAAD");
        doSearch(PubOrSub.sub, "BB/B/CAAAD/DD/EEF/GG");
        doSearch(PubOrSub.sub, ">");
        log.info("Done.");
        testSubscriptionMatcher = lastVal;
    }

    private void doSearch(PubOrSub pubOrSub, String searchTopic) {

        // TODO: Make these test* flags properties of Implementations so we can just loop through.
        if (testAnalyzer) {
            results.add(Pair.of(Implementation.topicAnalyzer, doSearch(pubOrSub, searchTopic,
                    Implementation.topicAnalyzer)));
        }
        if (testSubscriptionMatcher) {
            results.add(Pair.of(Implementation.subscriptionMatcher, doSearch(pubOrSub, searchTopic,
                    Implementation.subscriptionMatcher)));
        }
        if (testTopicMatcher) {
            results.add(Pair.of(Implementation.topicMatcher, doSearch(pubOrSub, searchTopic,
                    Implementation.topicMatcher)));
        }
        if (testTopicsRepo) {
            results.add(Pair.of(Implementation.topicsRepo, doSearch(pubOrSub, searchTopic, Implementation.topicsRepo)));
        }
        if (testSubscriptionSet) {
            results.add(Pair.of(Implementation.subscriptionSet, doSearch(pubOrSub, searchTopic, Implementation.subscriptionSet)));
        }
        if (testTopicSet) {
            results.add(Pair.of(Implementation.topicSet, doSearch(pubOrSub, searchTopic, Implementation.topicSet)));
        }
        if (testCachedTopicSet) {
            results.add(Pair.of(Implementation.cachedTopicSet, doSearch(pubOrSub, searchTopic, Implementation.cachedTopicSet)));
        }

        // log.debug("Results: {}", results);

        for (int i = 0; i < results.size(); i++) {
            Pair<Implementation, Collection<String>> r1 = results.get(i);
                //log.info("\ti: {} {} {}", i, r1.getLeft(), r1.getRight().size());
                for (int j = i + 1; j < results.size(); j++) {
                    Pair<Implementation, Collection<String>> r2 = results.get(j);
                    //log.info("\t\tj: {} {} {}", j, r2.getLeft(), r2.getRight().size());
                    if (r1.getRight().size() != r2.getRight().size()) {
                        Set<String> s1 = r1.getRight().stream().collect(Collectors.toSet());
                        Set<String> s2 = r2.getRight().stream().collect(Collectors.toSet());
                        Set<String> s1Not2 = new HashSet<>(s1);
                        s1Not2.removeIf(s -> s2.contains(s));
                        Set<String> s2Not1 = new HashSet<>(s2);
                        s2Not1.removeIf(s -> s1.contains(s));

                        if (s1Not2.size() > 0) {
                            log.info("In {} but not in {}: {}", r1.getLeft(), r2.getLeft(), s1Not2,
                                    s1Not2.size() > config.getMaxNumTopicsLogged() ?
                                    s1Not2.size() : s1Not2);
                        }

                        if (s2Not1.size() > 0) {
                            log.info("In {} but not in {}: {}", r2.getLeft(), r1.getLeft(), s2Not1.size() > config.getMaxNumTopicsLogged() ?
                                     s2Not1.size() : s2Not1);
                        }
                    }
                }
        }
        results.clear();
        log.info("");
    }

    private Collection<String> doSearch(PubOrSub pubOrSub, String searchTopic, Implementation implementation) {

        Collection<String> matchingTopics = null;

        long start = new Date().getTime();
        switch (implementation) {
            case topicsRepo:
                matchingTopics = topicsRepo.findMatchingTopics(searchTopic);
                break;
            case topicAnalyzer:
                matchingTopics = topicAnalyzer.match(pubOrSub, searchTopic);
                break;
            case topicMatcher:
                matchingTopics = topicMatcher.getTopicsForSubscription(searchTopic);
                break;
            case subscriptionMatcher:
                CriteriaTopic criteriaTopic = subscriptionMatcher.getSubscriptionsForTopic(searchTopic);
                //log.info("criteriaTopic: {}", criteriaTopic);
                matchingTopics = criteriaTopic.getSubscriptions().stream().map(s -> s.getMatchCriteria()).collect(
                        Collectors.toList());
                break;
            case subscriptionSet:
                matchingTopics = subscriptionSet.match(searchTopic);
                break;
            case topicSet:
                matchingTopics = topicSet.match(searchTopic);
                break;
            case cachedTopicSet:
                matchingTopics = cachedTopicSet.match(searchTopic);
                break;
            default:
                throw new IllegalStateException("Unsupported implementation " + implementation);
        }

        long end = new Date().getTime();
        long millis = end - start;

        if (matchingTopics.size() >= config.getMaxNumTopicsLogged()) {
            log.info(String.format("Search: impl: %19s time: %4d %16s matches: %s", implementation, millis,
                    searchTopic,
                    matchingTopics.size()));
        } else {
            log.info(String.format("Search: impl: %19s time: %4d %16s matches: %s", implementation, millis,
                    searchTopic,
                    matchingTopics));
        }

        return matchingTopics;
    }

    private static enum Implementation {subscriptionMatcher, topicAnalyzer, topicMatcher, topicsRepo, subscriptionSet, cachedTopicSet, topicSet}
}
