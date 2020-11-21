package com.solace.maas.topicmatcher.model;

import org.apache.commons.lang3.tuple.Pair;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a level of a topic that has prefix, such as A* or BB*.
 * We don't store the star.
 * We also need to remember the the maximum length of each prefix at this level
 * so that we know when we can stop matching substrings of publisher topics.
 */
public class Prefix {
    private int maxPrefixLength;
    private Map<Integer, Map<String, List<Pair<String, Integer>>>> topics = new HashMap<>();

    public int getMaxPrefixLength() {
        return maxPrefixLength;
    }

    public void setMaxPrefixLength(int maxPrefixLength) {
        this.maxPrefixLength = maxPrefixLength;
    }

    public Map<String, List<Pair<String, Integer>>> getTopics(int length) {
        return topics.get(length);
    }

    public void setTopics(int length,
            Map<String, List<Pair<String, Integer>>> topics) {
        this.topics.put(length, topics);
    }
}
