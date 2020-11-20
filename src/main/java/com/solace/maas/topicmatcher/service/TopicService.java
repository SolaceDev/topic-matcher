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
import java.util.*;
import java.util.stream.Collectors;

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
    private Map<String, Application> applicationsById = new HashMap<>();
    private Map<String, List<Application>> subscribingTopicToApplications = new HashMap<>();
    private Map<String, List<Application>> publishingTopicToApplications = new HashMap<>();
    private Map<String, List<String>> topicsMatchingSubscriptions = new HashMap<>();

    private List<Topic> publisherTopics;
    private List<Topic> subscriberTopics;

    @PostConstruct
    public void init() {

        log.info("init: largeDataSet: {}", config.isLargeDataSet());
        applications.clear();
        applicationsById.clear();
        topicsMatchingSubscriptions.clear();

        publisherTopics = topicGenerator.getPublisherTopics();
        subscriberTopics = topicGenerator.getSubscriberTopics();

        if (!config.isLargeDataSet()) {
            // If we have 10-99 apps, each has an id like App-09.
            // If we have 100-999, each has an id like App-009 and so on.
            createApplications();
            for(Application app : applications) {
                seedAppSubscriptions(app);
            }
        }

        analyze();
    }

    private void seedAppSubscriptions(Application application) {
        Set<String> matchingTopics = new HashSet<>();

        for (Topic pub : publisherTopics) {
            if (Math.random() < config.getAppToTopicRatio()) {
                application.addPublishingTopic(pub.getTopicString());
                List<Application> appsForThisTopic = publishingTopicToApplications.computeIfAbsent(pub.getTopicString(), k -> new ArrayList<>());
                appsForThisTopic.add(application);
            }
        }
        for (Topic sub : subscriberTopics) {
            if (Math.random() < config.getAppToTopicRatio()) {
                application.addSubscribingTopic(sub.getTopicString());
                List<Application> appsForThisTopic = subscribingTopicToApplications.computeIfAbsent(sub.getTopicString(), k -> new ArrayList<>());
                appsForThisTopic.add(application);

                // Find the matching published topics
                List<String> matchingForThisSub = topicsMatchingSubscriptions.get(sub.getTopicString());
                if (matchingForThisSub == null) {
                    matchingForThisSub = publisherAnalyzer.matchFromSubscriber(sub.getTopicString());
                    topicsMatchingSubscriptions.put(sub.getTopicString(), matchingForThisSub);
                }

                matchingTopics.addAll(matchingForThisSub);
            }
        }

        application.setTopicsMatchingSubscriptions(new ArrayList<>(matchingTopics));
        log.debug("seededAppSubs app {}", application);

    }

    private void computeAppSubscriptions(Application application) {
        Set<String> matchingTopics = new HashSet<>();

        for (Topic sub : subscriberTopics) {
            List<Application> appsForThisTopic = subscribingTopicToApplications.computeIfAbsent(sub.getTopicString(), k -> new ArrayList<>());
            appsForThisTopic.add(application);

            // Find the matching published topics
            List<String> matchingForThisSub = topicsMatchingSubscriptions.get(sub.getTopicString());
            if (matchingForThisSub == null) {
                matchingForThisSub = publisherAnalyzer.matchFromSubscriber(sub.getTopicString());
                topicsMatchingSubscriptions.put(sub.getTopicString(), matchingForThisSub);
            }

            matchingTopics.addAll(matchingForThisSub); // next: store in app field.
        }

        application.setTopicsMatchingSubscriptions(new ArrayList<>(matchingTopics));
        log.debug("computeAppSubscriptions app {}", application);
    }

    public List<Application> createApplications() {
        double sizef = Math.pow(config.getNumApplications(), .10);
        int idLength = (int) Math.round(sizef) + 1;
        String idFormat = String.format("App-%%0%dd", idLength);

        for (int i = 0; i < config.getNumApplications(); i++) {
            Application application = new Application();
            String name = String.format(idFormat, i);
            application.setName(name);
            applications.add(application);
            applicationsById.put(name, application);
        }
        return applications;
    }

    public void analyze() {
        publisherAnalyzer.analyze(publisherTopics);
        subscriberAnalyzer.analyze(subscriberTopics);

        for (Application application : applications) {
            computeAppSubscriptions(application);
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

    public List<Application> getApplications() {
        return applications;
    }

    public Application getApplication(String name) {
        return applicationsById.get(name);
    }

    public Application addSubscription(String appName, String sub) {
        Application app = getApplication(appName);
        boolean analyze = false;
        if (!app.getSubscribingTopics().contains(sub)) {
            getApplication(appName).addSubscribingTopic(sub);
            analyze = true;
        }
        Topic t = new Topic("_", sub);
        if (!subscriberTopics.contains(t)) {
            subscriberTopics.add(t);
            analyze = true;
        }
        log.debug("Analyze {}", analyze);
        if (analyze) analyze();
        return getApplication(appName);
    }
}
