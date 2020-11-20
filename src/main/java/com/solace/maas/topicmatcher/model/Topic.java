package com.solace.maas.topicmatcher.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Topic topic = (Topic) o;
        return numLevels == topic.numLevels &&
                topicString.equals(topic.topicString) &&
                topicLevels.equals(topic.topicLevels);
    }

    @Override
    public int hashCode() {
        return Objects.hash(numLevels, topicString, topicLevels);
    }
}
