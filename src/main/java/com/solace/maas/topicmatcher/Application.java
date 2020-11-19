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

//    @Autowired
//    Config config;
//    @Autowired
//    TopicGenerator topicGenerator;
//    @Autowired
//    TopicRepository topicRepository;


    @Autowired
    TopicService topicService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        testPublisherToSubscriber();
        testSubscriberToPublisher();
    }

    private void testPublisherToSubscriber() {
        log.info("Matching publisher to subscribers");
//        List<Topic> topics = topicRepository.getSubscriberTopics();
//        //List<Topic> topics = topicGenerator.getKnownTopics();
//        log.info("Analysis started.");
//        subscriberAnalyzer.analyze(topics);
//        log.info("Analysis finished.");
//
//        if (config.getNumTopics() <= 20) {
//            subscriberAnalyzer.dump();
//        }

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
//        List<Topic> topics = topicRepository.getPublisherTopics();
//        //List<Topic> topics = topicGenerator.getKnownTopics();
//        log.info("Analysis started.");
//        publisherAnalyzer.analyze(topics);
//        log.info("Analysis finished.");
//
//        if (config.getNumTopics() <= 20) {
//            publisherAnalyzer.dump();
//        }

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
