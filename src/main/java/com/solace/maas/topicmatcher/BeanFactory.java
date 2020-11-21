package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.service.AlphabetTopicGenerator;
import com.solace.maas.topicmatcher.service.AbstractTopicGenerator;
import com.solace.maas.topicmatcher.service.TopicGeneratorBeer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanFactory {

    @Autowired
    Config config;

    @Bean
    public AbstractTopicGenerator getTopicGenerator() {
        if (config.isBeer()) {
            return new TopicGeneratorBeer();
        } else {
            return new AlphabetTopicGenerator();
        }
    }
}
