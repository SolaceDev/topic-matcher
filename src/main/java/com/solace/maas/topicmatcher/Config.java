package com.solace.maas.topicmatcher;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("topicmatcher")
public class Config {

    private double chanceOfGT = 0.3;
    private double chanceOfStar = 0.3;
    private int maxLevels = 6;
    private int numTopics = 200000; // 10_000_000;
    private int vocabularySize = 26;

    public double getChanceOfGT() { return chanceOfGT; }

    public double getChanceOfStar() { return chanceOfStar; }

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

}
