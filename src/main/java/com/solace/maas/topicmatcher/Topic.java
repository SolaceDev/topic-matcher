package com.solace.maas.topicmatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Topic {
    private String id;
    private int levels;
    private String topicString;
    private List<String> topicLevels = new ArrayList();

    public Topic(String id, int levels, String topicString, List<String> topicLevels) {
        this.id = id;
        this.levels = levels;
        this.topicString = topicString;
        this.topicLevels = topicLevels;
    }

    public Topic(String id, String topicString) {
        this.id = id;
        this.topicString = topicString;

        String[] levs = topicString.split("/");
        this.levels = levs.length;
        this.topicLevels = Arrays.asList(levs);
    }

    public String getId() {
        return id;
    }

    public String getLevel(int level) {
        return topicLevels.get(level);
    }

    public int getLevels() { return levels; }

    public String getTopicString() {
        return topicString;
    }

    @Override
    public String toString() {
        return "Topic{" +
                "id='" + id + '\'' +
                ", levels='" + levels + '\'' +
                ", topicString='" + topicString + '\'' +
                '}';
    }
}
