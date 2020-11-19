package com.solace.maas.topicmatcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TopicService {

    @Autowired
    Config config;

    @Autowired
    TopicGenerator topicGenerator;

    //@Autowired
    //TopicRepository topicRepository;

    private TopicAnalyzer publisherAnalyzer = new TopicAnalyzer();
    private TopicAnalyzer subscriberAnalyzer = new TopicAnalyzer();

    @PostConstruct
    public void init() {
        publisherAnalyzer.analyze(topicGenerator.getPublisherTopics());
        subscriberAnalyzer.analyze(topicGenerator.getSubscriberTopics());
    }

    public List<String> getMatches(PubOrSub pubOrSub, String topicString) {
        if (pubOrSub == PubOrSub.pub) {
            return getSubscribers(topicString);
        } else {
            return getPublishers(topicString);
        }
    }

    public List<String> getPublishers(String subscriber) {
        return publisherAnalyzer.matchFromSubscriber(subscriber);
    }

    public List<String> getSubscribers(String publisher) {
        return subscriberAnalyzer.matchFromPublisher(publisher);
    }

}
