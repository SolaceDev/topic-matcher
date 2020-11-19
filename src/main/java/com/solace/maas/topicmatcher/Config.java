package com.solace.maas.topicmatcher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("topicmatcher")
public class Config {

    private double appToTopicRatio = 0.3; // chance that an app will be assigned a particular topic
    private double chanceOfGT = 0.3;  // Chance that a leaf node on a subscription will be >
    private double chanceOfStar = 0.3; // Chance that a level on a subscription will be a *
    private int maxLevels = 6;
    private int numApplications = 20;
    private int numTopics = 40;
    private boolean largeDataSet = true; // If true, we generate a large data set and no applications.
    private int largeDataSetNumTopics = 1_000_000;
    private int largeDataSetMaxLevels = 6;
    private int vocabularySize = 10; // number of letters in the alphabet that we use for topics.

    public double getChanceOfGT() {
        return chanceOfGT;
    }

    public void setChanceOfGT(double chanceOfGT) {
        this.chanceOfGT = chanceOfGT;
    }

    public double getChanceOfStar() {
        return chanceOfStar;
    }

    public void setChanceOfStar(double chanceOfStar) {
        this.chanceOfStar = chanceOfStar;
    }

    public int getMaxLevels() {
        return maxLevels;
    }

    public void setMaxLevels(int maxLevels) {
        this.maxLevels = maxLevels;
    }

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
}
