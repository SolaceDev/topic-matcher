package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.Config;
import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Application;
import com.solace.maas.topicmatcher.model.Topic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.ConfigurableEnvironment;
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
    AbstractTopicGenerator topicGenerator;

    @Autowired
    private ConfigurableEnvironment configurableEnvironment;

    private final TopicAnalyzer publisherAnalyzer = new TopicAnalyzer();
    private final TopicAnalyzer subscriberAnalyzer = new TopicAnalyzer();
    private final List<Application> applications = new ArrayList<>();
    private final Map<String, Application> applicationsById = new HashMap<>();
    // We're not doing anything with these yet...

    //private Map<String, List<Application>> subscribingTopicToApplications = new HashMap<>();
    //private Map<String, List<Application>> publishingTopicToApplications = new HashMap<>();

    private final Map<String, List<String>> topicsMatchingSubscriptions = new HashMap<>();

    private List<Topic> publisherTopics;
    private List<Topic> subscriberTopics;

    @PostConstruct

    public void postConstruct() {
        String[] activeProfiles = configurableEnvironment.getActiveProfiles();
        Set profiles = Arrays.stream(activeProfiles).collect(Collectors.toSet());
        if (!profiles.contains("test")) {
            init();
        }
    }

    public void init() {
        publisherTopics = topicGenerator.getTopics(PubOrSub.pub);
        subscriberTopics = topicGenerator.getTopics(PubOrSub.sub);
        createApplications();
        analyze();
    }

    private void computeAppSubscriptions(Application application) {
        Set<String> matchingTopics = new HashSet<>();

        for (String sub : application.getSubscribingTopics()) {
            /* We're not using this yet.
            List<Application> appsForThisTopic = subscribingTopicToApplications.computeIfAbsent(sub.getTopicString(),
             k -> new ArrayList<>());
            appsForThisTopic.add(application);
            */

            // Find the matching published topics
            List<String> matchingForThisSub = topicsMatchingSubscriptions.get(sub);
            if (matchingForThisSub == null) {
                matchingForThisSub = publisherAnalyzer.matchFromSubscriber(sub);
                topicsMatchingSubscriptions.put(sub, matchingForThisSub);
            }

            matchingTopics.addAll(matchingForThisSub);
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
        }
        return applications;
    }

    public void analyze() {
        publisherAnalyzer.analyze(PubOrSub.pub, publisherTopics);
        subscriberAnalyzer.analyze(PubOrSub.sub, subscriberTopics);

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
        if (!subscriptionFound(sub)) {
            subscriberTopics.add(new Topic(topicGenerator.getNextId(), sub));
            analyze = true;
        }
        log.debug("Analyze {}", analyze);
        if (analyze) analyze();
        return getApplication(appName);
    }

    private boolean subscriptionFound(String sub) {
        for (Topic t : subscriberTopics) {
            if (t.getTopicString().equals(sub)) {
                return true;
            }
        }
        return false;
    }
}
