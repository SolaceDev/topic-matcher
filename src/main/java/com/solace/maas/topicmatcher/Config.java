package com.solace.maas.topicmatcher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("topicmatcher")
public class Config {

    private double appToTopicRatio = 0.3; // chance that an app will be assigned a particular topic
    private boolean beer = false; // If true, we use the Beer topic generator.
    private double chanceOfGT = 0.2;  // Chance that a leaf node on a subscription will be >
    private double chanceOfPrefix = 0.4; // Chance that a level on a subscription will have a prefixed *
    private double chanceOfStar = 0.4; // Chance that a level on a subscription will be a *
    private boolean hardCodedTopics; // If true, the topic generator returns a hard-coded set of topics.
    private int maxLevelLength = 3; // maximum number of chars in a level.
    private int minLevels = 3;
    private int maxLevels = 6;
    private int numApplications = 20;
    private int numTopics = 40;
    private boolean largeDataSet = false; // If true, we generate a large data set and no applications.
    private int largeDataSetNumTopics = 1000; // 15_000_000;
    private int largeDataSetMaxLevels = 6;
    private int vocabularySize = 10; // number of letters in the alphabet that we use for topics.

    public boolean isBeer() { return beer; }

    public void setBeer(boolean beer) { this.beer = beer; }

    public double getChanceOfGT() { return chanceOfGT; }

    public void setChanceOfGT(double chanceOfGT) { this.chanceOfGT = chanceOfGT; }

    public double getChanceOfPrefix() { return chanceOfPrefix; }

    public void setChanceOfPrefix(double chanceOfPrefix) { this.chanceOfPrefix = chanceOfPrefix; }

    public double getChanceOfStar() {
        return chanceOfStar;
    }

    public void setChanceOfStar(double chanceOfStar) {
        this.chanceOfStar = chanceOfStar;
    }

    public int getMaxLevelLength() { return maxLevelLength; }

    public void setMaxLevelLength(int maxLevelLength) { this.maxLevelLength = maxLevelLength; }

    public int getMaxLevels() {
        return maxLevels;
    }

    public void setMaxLevels(int maxLevels) {
        this.maxLevels = maxLevels;
    }

    public int getMinLevels() { return minLevels; }

    public void setMinLevels(int minLevels) { this.minLevels = minLevels; }

    public int getNumTopics() {
        return numTopics;
    }

    public void setNumTopics(int numTopics) {
        this.numTopics = numTopics;
    }

    public int getVocabularySize() {
        return vocabularySize;
    }

    public void setVocabularySize(int vocabularySize) {
        this.vocabularySize = vocabularySize;
    }

    public double getAppToTopicRatio() {
        return appToTopicRatio;
    }

    public void setAppToTopicRatio(double appToTopicRatio) {
        this.appToTopicRatio = appToTopicRatio;
    }

    public int getNumApplications() {
        return numApplications;
    }

    public void setNumApplications(int numApplications) {
        this.numApplications = numApplications;
    }

    public boolean isLargeDataSet() {
        return largeDataSet;
    }

    public void setLargeDataSet(boolean largeDataSet) {
        this.largeDataSet = largeDataSet;
    }

    public int getLargeDataSetNumTopics() {
        return largeDataSetNumTopics;
    }

    public void setLargeDataSetNumTopics(int largeDataSetNumTopics) {
        this.largeDataSetNumTopics = largeDataSetNumTopics;
    }

    public int getLargeDataSetMaxLevels() {
        return largeDataSetMaxLevels;
    }

    public void setLargeDataSetMaxLevels(int largeDataSetMaxLevels) {
        this.largeDataSetMaxLevels = largeDataSetMaxLevels;
    }

    public boolean isHardCodedTopics() {
        return hardCodedTopics;
    }

    public void setHardCodedTopics(boolean hardCodedTopics) {
        this.hardCodedTopics = hardCodedTopics;
    }
}
