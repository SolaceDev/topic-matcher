package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.Config;
import com.solace.maas.topicmatcher.PubOrSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class AlphabetTopicGenerator extends AbstractTopicGenerator {
    @Autowired
    Config config;
    private Logger log = LoggerFactory.getLogger(AlphabetTopicGenerator.class);
    private StringBuilder topicStringBuilder = new StringBuilder();
    private StringBuilder levelStringBuilder = new StringBuilder();
    private Random random = new Random();

    public List<String> getPublisherTopics() {
        return getTopics(PubOrSub.pub);
    }

    public List<String> getSubscriberTopics() {
        return getTopics(PubOrSub.sub);
    }

    public List<String> getTopics() {
        return getTopics(PubOrSub.sub);
    }

    public List<String> getTopics(PubOrSub pub_or_sub) {
        log.info("Generating {} topics...", pub_or_sub);

        if (config.isHardCodedTopics()) {
            return getKnownTopics(pub_or_sub);
        }

        Set<String> topics = new HashSet<>();


        int numTopics = config.isLargeDataSet() ? config.getLargeDataSetNumTopics() : config.getNumTopics();

        for (int i = 0; i < numTopics; i++) {
            String topic = generateTopic(pub_or_sub);

            if (!topics.contains(topic)) {
                topics.add(topic);
                if (config.getNumTopics() <= 20 && !config.isLargeDataSet()) {
                    log.info(topic);
                }
            }
        }

        log.info("{} distinct topics generated.", topics.size());
        return topics.stream().collect(Collectors.toList());
    }

    public String generateTopic() {
        return generateTopic(PubOrSub.sub);
    }

    public String generateTopic(PubOrSub pub_or_sub) {
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
        return topicStringBuilder.toString();
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

    public List<String> getKnownTopics(PubOrSub pubSub) {
        return pubSub == PubOrSub.pub ? getKnownPublisherTopics() : getKnownSubscriberTopics();
    }

    public List<String> getKnownSubscriberTopics() {
        List<String> topics = new ArrayList<>();
        topics.add("*/B*/C*");
        topics.add("A*/BB/CC");
        topics.add("AAA/B*/>");
        topics.add("AAA/BB*/>");
        topics.add("AAA/BBB/C*");
        topics.add("AAA/BBB/CCC");
        topics.add("A/*/*");
        topics.add(">");
        return topics;
    }

    public List<String> getKnownPublisherTopics() {
        List<String> topics = new ArrayList<>();
        topics.add("AAA/BBB/CCC");
        topics.add("A/B/C");
        topics.add("AAA/F/C");
        topics.add("A/BB/CC");
        topics.add("AA/B/CCC");
        return topics;
    }
}
