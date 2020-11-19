package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.Config;
import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Application;
import com.solace.maas.topicmatcher.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class TopicService {

    private final Logger log = LoggerFactory.getLogger(TopicService.class);

    @Autowired
    Config config;

    @Autowired
    TopicGenerator topicGenerator;

    private TopicAnalyzer publisherAnalyzer = new TopicAnalyzer();
    private TopicAnalyzer subscriberAnalyzer = new TopicAnalyzer();
    private List<Application> applications = new ArrayList<>();

    @PostConstruct
    public void init() {

        if (config.isLargeDataSet()) {
            publisherAnalyzer.analyze(topicGenerator.getPublisherTopics());
            subscriberAnalyzer.analyze(topicGenerator.getSubscriberTopics());
        } else {
            applications.clear();
            // Generate the applications.
            List<Topic> publisherTopics = topicGenerator.getPublisherTopics();
            List<Topic> subscriberTopics = topicGenerator.getSubscriberTopics();

            publisherAnalyzer.analyze(publisherTopics);
            subscriberAnalyzer.analyze(subscriberTopics);

            // If we have 10-99 apps, each has an id like App-09.
            // If we have 100-999, each has an id like App-009 and so on.
            double sizef = Math.pow(config.getNumApplications(), .10);
            int idLength = (int) Math.round(sizef) + 1;
            String idFormat = String.format("App-%%0%dd", idLength);


            for (int i = 0; i < config.getNumApplications(); i++) {
                Application application = new Application();
                application.setName(String.format(idFormat, i));
                applications.add(application);

                for (Topic pub : publisherTopics) {
                    if (Math.random() < config.getAppToTopicRatio()) {
                        application.addPublishingTopic(pub.getTopicString());
                    }
                }
                for (Topic sub : subscriberTopics) {
                    if (Math.random() < config.getAppToTopicRatio()) {
                        application.addSubscribingTopic(sub.getTopicString());
                    }
                }

                log.debug("Generated app {}", application);
            }
        }
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
