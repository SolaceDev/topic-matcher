package com.solace.maas.topicmatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TopicGenerator {
    @Autowired
    Config config;
    private Logger log = LoggerFactory.getLogger(TopicGenerator.class);
    private StringBuilder stringBuilder = new StringBuilder();
    private List<Topic> topics = new ArrayList<>();
    private Map<String, Topic> topicHash = new HashMap();

    public List<Topic> getPublisherTopics() {
        return getTopics(PubOrSub.pub);
    }

    public List<Topic> getSubscriberTopics() {
        return getTopics(PubOrSub.sub);
    }

    public List<Topic> getTopics(PubOrSub pub_or_sub) {
        log.info("Generating {} topics...", pub_or_sub);
        Set<String> topics = new HashSet<>();
        double sizef = Math.pow(config.getNumTopics(), .10);
        int idLength = (int) Math.round(sizef) + 1;
        String idFormat = String.format("T%%0%dd", idLength);

        for (int i = 0; i < config.getNumTopics(); i++) {
            String id = String.format(idFormat, i);
            Topic topic = generateTopic(pub_or_sub, id);

            if (i % 10_000 == 0) {
                System.out.print('.');
                if (i % 1_000_000 == 0) {
                    System.out.println();
                }
            }

            if (!topics.contains(topic.getTopicString())) {
                topics.add(topic.getTopicString());
                topicHash.put(id, topic);
                if (config.getNumTopics() <= 20) {
                    log.info(topic.toString());
                }
            }
        }

        if (config.getNumTopics() >= 10_000) {
            System.out.println();
        }

        log.info("{} distinct topics generated.", topicHash.size());
        return topicHash.values().stream().collect(Collectors.toList());
    }

    private Topic generateTopic(PubOrSub pub_or_sub, String id) {
        stringBuilder.delete(0, stringBuilder.length());
        List<String> topicLevels = new ArrayList<>();
        int levels = 1 + (int) (Math.random() * (config.getMaxLevels()));

        for (int level = 0; level < levels; level++) {
            char c = ' '; // to be replaced soon.

            // If publishing, we can't have wildcards.
            // If subscribing we can have wildcards, but > must be at the lowest level.
            if (pub_or_sub == PubOrSub.pub) {
                c = (char) ('A' + (int) (Math.random() * config.getVocabularySize()));
            } else {
                boolean isLeafNode = level == levels - 1;
                double chance = Math.random();

                if (isLeafNode) {
                    // here we can have a literal, * or >
                    if (chance < config.getChanceOfGT()) {
                        c = '>';
                    } else if (chance <= config.getChanceOfStar() + config.getChanceOfGT()) {
                        c = '*';
                    } else {
                        c = (char) ('A' + (int) (Math.random() * config.getVocabularySize()));
                    }
                } else {
                    if (chance <= config.getChanceOfStar()) {
                        c = '*';
                    } else {
                        c = (char) ('A' + (int) (Math.random() * config.getVocabularySize()));
                    }
                }
            }

            topicLevels.add(String.valueOf(c));
            stringBuilder.append(c);
            if (level < levels - 1) {
                stringBuilder.append('/');
            }
        }
        return new Topic(id, levels, stringBuilder.toString(), topicLevels);
    }

    public Topic getTopic(String id) {
        return topicHash.get(id);
    }

    public String getTopicString(String id) {
        return topicHash.get(id).getTopicString();
    }

    public List<Topic> getKnownTopics() {
        addKnownTopic("T1", "F/*/>");
        addKnownTopic("T2", "A/>");
        addKnownTopic("T3", "A/*/>");
        addKnownTopic("T4", "A/*/*");
        addKnownTopic("T5", "B/>");
        addKnownTopic("T6", "B/*/>");
        addKnownTopic("T7", "B/*/*");
        return topics;
    }

    private void addKnownTopic(String id, String topicString) {
        Topic topic = new Topic(id, topicString);
        topics.add(topic);
        topicHash.put(topic.getId(), topic);
    }

}
