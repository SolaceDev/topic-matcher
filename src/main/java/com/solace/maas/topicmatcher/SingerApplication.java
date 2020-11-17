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
public class SingerApplication implements ApplicationRunner {

    private Logger log = LoggerFactory.getLogger(SingerApplication.class);
    public static void main(String[] args) {
        SpringApplication.run(SingerApplication.class, args);
    }

    @Autowired
    Config config;

    @Autowired
    TopicGenerator topicGenerator;

    @Autowired
    TopicAnalyzer topicAnalyzer;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Topic> topics = topicGenerator.getTopics();
        //List<Topic> topics = topicGenerator.getKnownTopics();
        log.info("Analysis started.");
        topicAnalyzer.analyze(topics);
        log.info("Analysis finished.");

        if (config.getNumTopics() <= 20) {
            topicAnalyzer.dump();
        }

        doSearch("A");
        doSearch("A/*");
        doSearch("A/*/B/>");
        doSearch("A/*/*/F/*");
        doSearch("A/B/C/E/F");
        doSearch("A/>");
        doSearch(">");
    }

    private void doSearch(String searchTopic) {
        List<String> matchingTopics = topicAnalyzer.match(searchTopic);

        if (config.getNumTopics() <= 20)
        matchingTopics = matchingTopics.stream().map( id -> topicGenerator.getTopicString(id)).collect(
                Collectors.toList());
        if (matchingTopics.size() > 20) {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics.size()));
        } else {
            log.info(String.format("Search: %16s matches: %s", searchTopic, matchingTopics));
        }
    }

}
