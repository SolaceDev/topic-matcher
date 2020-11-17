package com.solace.maas.topicmatcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class TopicGenerator {
    private Logger log = LoggerFactory.getLogger(TopicGenerator.class);

    @Autowired
    Config config;
    private StringBuilder stringBuilder = new StringBuilder();
    private List<Topic> topics = new ArrayList<>();
    private Map<String, Topic> topicHash = new HashMap();

    private Topic generateTopic(String id) {
        stringBuilder.delete(0, stringBuilder.length());
        List<String> topicLevels = new ArrayList<>();
        int levels = 1 + (int) (Math.random() * (config.getMaxLevels()));

        for (int level = 0; level < levels; level++) {
            char c = (char) ('A' + (int) (Math.random() * config.getVocabularySize()));
            topicLevels.add(String.valueOf(c));
            stringBuilder.append(c);
            if (level < levels - 1) {
                stringBuilder.append('/');
            }
        }
        return new Topic(id, levels, stringBuilder.toString(), topicLevels);
    }

    public List<Topic> getTopics() {
        log.info("Generating topics...");
        Set<String> topics = new HashSet<>();
        double sizef = Math.pow(config.getNumTopics(), .10);
        int idLength = (int) Math.round(sizef) + 1;
        String idFormat = String.format("T%%0%dd", idLength);

        for (int i = 0; i < config.getNumTopics(); i++) {
            String id = String.format(idFormat, i);
            Topic topic = generateTopic(id);

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

    public Topic getTopic(String id) {
        return topicHash.get(id);
    }

    public String getTopicString(String id) {
        return topicHash.get(id).getTopicString();
    }

    public List<Topic> getKnownTopics() {
        Topic topic = new Topic("T1", "A/E");
        topics.add(topic);
        topicHash.put(topic.getId(), topic);
        return topics;
    }

}
