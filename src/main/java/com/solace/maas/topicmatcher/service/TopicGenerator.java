package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.Config;
import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Topic;
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
    private StringBuilder topicStringBuilder = new StringBuilder();
    private StringBuilder levelStringBuilder = new StringBuilder();
    private Map<String, Topic> topicHash = new HashMap();
    Random random = new Random();

    public List<Topic> getPublisherTopics() {
        return getTopics(PubOrSub.pub);
    }

    public List<Topic> getSubscriberTopics() {
        return getTopics(PubOrSub.sub);
    }

    public List<Topic> getTopics(PubOrSub pub_or_sub) {
        log.info("Generating {} topics...", pub_or_sub);
        topicHash.clear();

        if (config.isHardCodedTopics()) {
            return getKnownTopics(pub_or_sub);
        }

        Set<String> topics = new HashSet<>();

        // If we have 10-99 topics, each has an id like T09.
        // If we have 100-999, each has an id like T009 and so on.
        // So if we have 100 topics our format string will look like T%03d
        double sizef = Math.pow(config.getNumTopics(), .10);
        int idLength = (int) Math.round(sizef) + 1;
        String idFormat = String.format("T%%0%dd", idLength);

        int numTopics = config.isLargeDataSet() ? config.getLargeDataSetNumTopics() : config.getNumTopics();

        for (int i = 0; i < numTopics; i++) {
            String id = String.format(idFormat, i);
            Topic topic = generateTopic(pub_or_sub, id);

            if (!topics.contains(topic.getTopicString())) {
                topics.add(topic.getTopicString());
                topicHash.put(id, topic);
                if (config.getNumTopics() <= 20) {
                    log.info(topic.toString());
                }
            }
        }

        log.info("{} distinct topics generated.", topicHash.size());
        return topicHash.values().stream().collect(Collectors.toList());
    }

    private Topic generateTopic(PubOrSub pub_or_sub, String id) {
        topicStringBuilder.delete(0, topicStringBuilder.length());
        List<String> topicLevels = new ArrayList<>();
        int levels =
                config.getMinLevels() + (int) (Math.random() * (config.getMaxLevels() - config.getMinLevels() + 1));

        for (int level = 0; level < levels; level++) {
            levelStringBuilder.delete(0, levelStringBuilder.length());
            char c = ' '; // to be replaced soon.
            String levelString = null;
            boolean isLeafNode = level == levels - 1;

            // If publishing, we can't have wildcards.
            // If subscribing we can have wildcards, but > must be at the lowest level.
            if (pub_or_sub == PubOrSub.pub) {
                levelString = getLevelString(false);
            } else {
                double chance = random.nextDouble();

                if (isLeafNode) {
                    // here we can have a literal, * or >
                    if (chance < config.getChanceOfGT()) {
                        levelString = ">";
                    } else if (chance <= config.getChanceOfStar() + config.getChanceOfGT()) {
                        levelString = "*";
                    } else {
                        levelString = getLevelString(true);
                    }
                } else {
                    if (chance <= config.getChanceOfStar()) {
                        levelString = "*";
                    } else {
                        levelString = getLevelString(true);
                    }
                }
            }

            topicLevels.add(levelString);
            topicStringBuilder.append(levelString);

            if (!isLeafNode) {
                topicStringBuilder.append('/');
            }
        }
        return new Topic(id, levels, topicStringBuilder.toString(), topicLevels);
    }

    private String getLevelString(boolean allowPrefix) {
        boolean isPrefix = allowPrefix && random.nextDouble() < config.getChanceOfPrefix();
        int maxLength = isPrefix ? config.getMaxLevelLength() - 1 : config.getMaxLevelLength();
        int len = random.nextInt(maxLength) + 1;
        for (int i = 0; i < len; i++) {
            char c = (char) ('A' + random.nextInt(config.getVocabularySize()));
            levelStringBuilder.append(c);
        }
        if (isPrefix) {
            levelStringBuilder.append("*");
        }

        return levelStringBuilder.toString();
    }

    public Topic getTopic(String id) {
        return topicHash.get(id);
    }

    public String getTopicString(String id) {
        return topicHash.get(id).getTopicString();
    }

    public List<Topic> getKnownTopics(PubOrSub pubSub) {
        return pubSub == PubOrSub.pub ? getKnownPublisherTopics() : getKnownSubscriberTopics();
    }

    public List<Topic> getKnownSubscriberTopics() {
        List<Topic> topics = new ArrayList<>();
        addKnownTopic(topics,"T1", "AAA/B*/>");
        addKnownTopic(topics,"T2", "AAA/BB*/>");
        addKnownTopic(topics,"T3", "AAA/BBB/C*");
        addKnownTopic(topics,"T4", "A/*/*");
        addKnownTopic(topics,"T5", "A*/B/C");
        return topics;
    }

    public List<Topic> getKnownPublisherTopics() {
        List<Topic> topics = new ArrayList<>();
        addKnownTopic(topics,"T1", "AAA/BBB/CCC");
        addKnownTopic(topics,"T2", "A/B/C");
        addKnownTopic(topics,"T3", "AAA/F/C");
        addKnownTopic(topics,"T4", "A/BB/CC");
        addKnownTopic(topics,"T5", "AA/B/CCC");
        return topics;
    }


    private void addKnownTopic(List<Topic> topics, String id, String topicString) {
        Topic topic = new Topic(id, topicString);
        topics.add(topic);
        topicHash.put(topic.getId(), topic);
        log.info("Generated {}", topic);
    }

}
