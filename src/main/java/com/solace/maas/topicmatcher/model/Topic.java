package com.solace.maas.topicmatcher.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Topic {
    private String id;
    private int numLevels;
    private String topicString;
    private List<String> topicLevels = new ArrayList();

    public Topic(String id, int numLevels, String topicString, List<String> topicLevels) {
        this.id = id;
        this.numLevels = numLevels;
        this.topicString = topicString;
        this.topicLevels = topicLevels;
    }

    public Topic(String id, String topicString) {
        this.id = id;
        this.topicString = topicString;

        String[] levs = topicString.split("/");
        this.numLevels = levs.length;
        this.topicLevels = Arrays.asList(levs);
    }

    public String getId() {
        return id;
    }

    public String getLevel(int level) {
        return topicLevels.get(level);
    }

    public int getNumLevels() { return numLevels; }

    public String getTopicString() {
        return topicString;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id='" + id + '\'' +
                ", levels='" + numLevels + '\'' +
                ", topicString='" + topicString + '\'' +
                '}';
    }
}
