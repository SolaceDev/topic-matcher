package com.solace.maas.topicmatcher;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class TopicAnalyzer {

    private Logger log = LoggerFactory.getLogger(TopicAnalyzer.class);

    List<Map<String, List<Pair<String, Integer>>>> maps = new ArrayList();
    List<String> allTopics = new ArrayList<>();

    public void analyze(List<Topic> topics) {
        allTopics.clear();
        for (Topic topic : topics) {
            allTopics.add(topic.getId());
            int numLevels = topic.getLevels();
            for (int level = 0; level < numLevels; level++) {
                String levelString = topic.getLevel(level);

                if (maps.size() <= level) {
                    int levelsNeeded = level - maps.size() + 1;
                    for (int i = 0; i < levelsNeeded; i++) {
                        maps.add(new HashMap<String, List<Pair<String, Integer>>>());
                    }
                }
                Map<String, List<Pair<String, Integer>>> matchingTopicsAtThisLevel = maps.get(level);

                List<Pair<String, Integer>> listOfTopics = matchingTopicsAtThisLevel.get(levelString);
                if (listOfTopics == null) {
                    listOfTopics = new ArrayList<>();
                    matchingTopicsAtThisLevel.put(levelString, listOfTopics);
                }
                listOfTopics.add(Pair.of(topic.getId(), topic.getLevels()));
            }
        }
    }

    public List<String> match(String topic) {
        Set<Pair<String, Integer>> matching = new HashSet();
        String[] levels = topic.split("/");
        for (int i = 0; i < levels.length; i++) {
            String levelToMatch = levels[i];
            log.debug("Level to match: {} {}", i, levelToMatch);
            boolean isLeafNode = i == levels.length - 1;
            final int lev = i; // for use in lamdas

            // Special case: match all
            if (levelToMatch.equals(">")) {
                if (i == 0) {
                    return allTopics;
                } else {
                    return matching.stream().map(p -> p.getLeft()).collect(Collectors.toList());
                }
            }

            if (levelToMatch.equals("*")) {
                // If it's a leaf node, return the current set where it ends at this level.
                if (isLeafNode) {
                    return matching.stream().filter(p -> p.getRight() == lev + 1).map(p -> p.getLeft()).collect(
                            Collectors.toList());
                }
                // else nothing changes in our set of matches.
            } else {

                if (maps.size() <= i) {
                    return List.of(); // No topics are as long as this one. No matches.
                }

                List<Pair<String, Integer>> matchingTopicsAtThisLevel = maps.get(i).get(levelToMatch);
                if (matchingTopicsAtThisLevel == null) {
                    return List.of();
                }

                matchingTopicsAtThisLevel = new ArrayList<>(matchingTopicsAtThisLevel); // Cloning so we can remove
                // some.

                if (isLeafNode) {
                    matchingTopicsAtThisLevel.removeIf( p -> p.getRight() > lev + 1);
                }

                Set<Pair<String, Integer>> matchingAtThisLevel =
                        matchingTopicsAtThisLevel.stream().collect(Collectors.toSet());
                log.debug("current set: {} matching at this level: {}", matching, matchingAtThisLevel);
                if (i == 0) {
                    matching = matchingAtThisLevel;
                } else {
                    //Set<String> existingTopics = matching.stream().map( p -> p.getLeft()).collect(Collectors.toSet());
                    Set<String> topicsThisLevel =
                            matchingAtThisLevel.stream().map( p -> p.getLeft()).collect(Collectors.toSet());
                    //existingTopics.
                    matching.removeIf( p -> !topicsThisLevel.contains(p.getLeft()));
                }
                log.debug("Resulting set: {}", matching);
            }
        }

        return matching.stream().map(p -> p.getLeft()).collect(Collectors.toList());
    }

    public void dump() {
        log.info("dump: nodes:");
        for (int i = 0; i < maps.size(); i++) {
            log.info("\tlevel {}", i);
            Map<String, List<Pair<String, Integer>>> map = maps.get(i);
            for (String key : map.keySet()) {
                log.info("\t\t{} : {}", key, map.get(key));
            }
        }
    }
}
