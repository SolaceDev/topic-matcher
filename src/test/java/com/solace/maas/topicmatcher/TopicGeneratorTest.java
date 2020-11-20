package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.model.Topic;
import com.solace.maas.topicmatcher.service.TopicGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class TopicGeneratorTest {


    private final Logger log = LoggerFactory.getLogger(TopicGeneratorTest.class);
    @Autowired
    Config config;

    @Autowired
    TopicGenerator topicGenerator;

    @BeforeEach
    public void init() {
        log.info("BEFORE --------------------------------");
        config.setNumTopics(10);
        config.setMaxLevelLength(2);
        config.setVocabularySize(3);
        config.setHardCodedTopics(true);
    }

    @Test
    void testGeneration() {
        topicGenerator.getTopics(PubOrSub.pub);
    }


}
