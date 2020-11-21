package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.AbstractTopicGenerator;
import com.solace.maas.topicmatcher.service.AlphabetTopicGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class AlphabetTopicGeneratorTest {


    private final Logger log = LoggerFactory.getLogger(AlphabetTopicGeneratorTest.class);
    @Autowired
    Config config;

    @Autowired
    AbstractTopicGenerator topicGenerator;

    @BeforeEach
    public void init() {
        log.info("BEFORE --------------------------------");
        config.setBeer(true);
        config.setNumTopics(10);
        config.setMaxLevelLength(2);
        config.setVocabularySize(3);
        config.setHardCodedTopics(true);
    }

    @Test
    void testGeneration() {
        log.info("generator: {}" + topicGenerator);
        topicGenerator.getTopics(PubOrSub.pub);
    }


}
