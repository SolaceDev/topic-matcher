package com.solace.maas.topicmatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;
import java.util.stream.Collectors;

@SpringBootApplication
public class Application implements ApplicationRunner {

    private Logger log = LoggerFactory.getLogger(Application.class);
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Autowired
    Config config;

    @Autowired
    TopicGenerator topicGenerator;

    @Autowired
    TopicAnalyzer topicAnalyzer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        testPublisherToSubscriber();
        //testSubscriberToPublisher();
    }

    private void testPublisherToSubscriber() {
        List<Topic> topics = topicGenerator.getSubscriberTopics();
        //List<Topic> topics = topicGenerator.getKnownTopics();
        log.info("Analysis started.");
        topicAnalyzer.analyze(topics);
        log.info("Analysis finished.");

        if (config.getNumTopics() <= 20) {
            topicAnalyzer.dump();
        }

        doSearch(PubOrSub.pub, "A");
        doSearch(PubOrSub.pub,"A/A");
        doSearch(PubOrSub.pub,"A/A/A");
        doSearch(PubOrSub.pub,"A/B/C");
        doSearch(PubOrSub.pub,"B/A/A");
        doSearch(PubOrSub.pub,"B/C/C/E");
        doSearch(PubOrSub.pub,"B");
    }

    private void testSubscriberToPublisher() {
        List<Topic> topics = topicGenerator.getPublisherTopics();
        //List<Topic> topics = topicGenerator.getKnownTopics();
        log.info("Analysis started.");
        topicAnalyzer.analyze(topics);
        log.info("Analysis finished.");

        if (config.getNumTopics() <= 20) {
            topicAnalyzer.dump();
        }

        doSearch( PubOrSub.sub, "A");
        doSearch( PubOrSub.sub, "A/*");
        doSearch( PubOrSub.sub, "A/*/B/>");
        doSearch( PubOrSub.sub, "A/*/*/F/*");
        doSearch( PubOrSub.sub, "A/B/C/E/F");
        doSearch( PubOrSub.sub, "A/>");
        doSearch( PubOrSub.sub, ">");
    }

    private void doSearch(PubOrSub pubOrSub, String searchTopic) {
        List<String> matchingTopics = topicAnalyzer.match(pubOrSub, searchTopic);

        if (true || config.getNumTopics() <= 20)
            matchingTopics = matchingTopics.stream().map( id -> topicGenerator.getTopicString(id)).collect(
                Collectors.toList());
        if (matchingTopics.size() > 20) {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics.size()));
        } else {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics));
        }
    }

}
