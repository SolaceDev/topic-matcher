package com.solace.maas.topicmatcher.igor;

import java.util.List;

public interface TopicsRepo {

    void registerTopic(String topic);

    void deregisterTopic(String topic);

    List<String> findMatchingTopics(String subscription);

    List<String> findCoveringSubscriptions(String topic);

}
