package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Topic;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class TopicAnalyzer {

    private List<Map<String, List<Pair<String, Integer>>>> maps = new ArrayList();
    private List<String> allTopicStrings = new ArrayList<>();
    private Map<String, String> topicIdToTopicString = new HashMap<>();
    private final Logger log = LoggerFactory.getLogger(TopicAnalyzer.class);

    public void analyze(List<Topic> topics) {
        allTopicStrings.clear();
        topicIdToTopicString.clear();
        for (Topic topic : topics) {
            allTopicStrings.add(topic.getTopicString());
            topicIdToTopicString.put(topic.getId(), topic.getTopicString());
            int numLevels = topic.getNumLevels();
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
                listOfTopics.add(Pair.of(topic.getId(), topic.getNumLevels()));
            }
        }
    }

    public List<String> matchFromSubscriber(String topic) {
        log.debug("Match from subscriber: {}", topic);
        Set<Pair<String, Integer>> matching = new HashSet();
        String[] levels = topic.split("/");
        for (int i = 0; i < levels.length; i++) {
            String levelToMatch = levels[i];
            boolean isLeafNode = i == levels.length - 1;
            log.debug("Level to match: {} {} leaf: {}", i, levelToMatch, isLeafNode);
            final int lev = i; // for use in lamdas

            // Special case: match all
            if (levelToMatch.equals(">")) {
                if (i == 0) {
                    return allTopicStrings;
                } else {
                    break; // return matching.stream().map(p -> p.getLeft()).collect(Collectors.toList());
                }
            }

            if (levelToMatch.equals("*")) {
                // If it's a leaf node, return the current set where it ends at this level.
                if (isLeafNode) {
                    matching.removeIf(p -> p.getRight() > lev + 1);
                    break;
//                    return matching.stream().filter(p -> p.getRight() == lev + 1).map(p -> p.getLeft()).collect(
//                            Collectors.toList());
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
                    matchingTopicsAtThisLevel.removeIf(p -> p.getRight() > lev + 1);
                }

                Set<Pair<String, Integer>> matchingAtThisLevel =
                        matchingTopicsAtThisLevel.stream().collect(Collectors.toSet());
                log.debug("current set: {} matching at this level: {}", matching, matchingAtThisLevel);
                if (i == 0) {
                    matching = matchingAtThisLevel;
                } else {
                    Set<String> topicsThisLevel =
                            matchingAtThisLevel.stream().map(p -> p.getLeft()).collect(Collectors.toSet());
                    matching.removeIf(p -> !topicsThisLevel.contains(p.getLeft()));
                }
                log.debug("Resulting set: {}", matching);
            }
        }

        List<String> matchingIds = matching.stream().map(p -> p.getLeft()).collect(Collectors.toList());
        List<String> ret = matchingIds.stream().map(id -> topicIdToTopicString.get(id)).collect(Collectors.toList());
        return ret;
    }

    public List<String> matchFromPublisher(String topic) {
        log.debug("Match from publisher: {}", topic);
        final Set<Pair<String, Integer>> matching = new HashSet();
        final List<Pair<String, Integer>> matchingGts = new ArrayList();
        String[] levels = topic.split("/");

        for (int i = 0; i < levels.length; i++) {
            String levelToMatch = levels[i];
            boolean isLeafNode = i == levels.length - 1;
            log.debug("Level to match: {} {} leaf: {}", i, levelToMatch, isLeafNode);
            final int lev = i; // for use in lamdas which need i to be final.

            if (maps.size() <= i) {
                // No topics are as long as this one. No matches.
                matching.clear();
                break;
            }

            List<Pair<String, Integer>> gtsAtThisLevel = maps.get(i).get(">");
            log.debug("matching gts: " + gtsAtThisLevel);

            // If there is a > at the 0 level, always add it.
            // Otherwise only add it if the higher levels of the topic already matched.
            if (gtsAtThisLevel != null) {
                if (i > 0) {
                    // Cloning so we can remove some.
                    gtsAtThisLevel = new ArrayList<>(gtsAtThisLevel);
                    gtsAtThisLevel.removeIf(p -> !matching.contains(p));
                    log.debug("matching gts after filtering: {}", gtsAtThisLevel);
                }
                matchingGts.addAll(gtsAtThisLevel);
            }

            List<Pair<String, Integer>> matchingTopicsAtThisLevel = maps.get(i).get(levelToMatch);
            if (matchingTopicsAtThisLevel == null) {
                matchingTopicsAtThisLevel = new ArrayList<>();
            }

            List<Pair<String, Integer>> starsAtThisLevel = maps.get(i).get("*");

            if (starsAtThisLevel != null) {
                matchingTopicsAtThisLevel.addAll(starsAtThisLevel);
            }

            if (matchingTopicsAtThisLevel == null) {
                log.debug("No matches at this level.");
                matching.clear();
                break;
            }


            if (isLeafNode) {
                // Cloning so we can remove some.
                matchingTopicsAtThisLevel = new ArrayList<>(matchingTopicsAtThisLevel);
                matchingTopicsAtThisLevel.removeIf(p -> p.getRight() > lev + 1);
            }

            Set<Pair<String, Integer>> matchingAtThisLevel =
                    matchingTopicsAtThisLevel.stream().collect(Collectors.toSet());
            log.debug("current set: {} matching at this level: {}", matching, matchingAtThisLevel);
            if (i == 0) {
                matching.addAll(matchingAtThisLevel);
            } else {
                //Set<String> existingTopics = matching.stream().map( p -> p.getLeft()).collect(Collectors.toSet());
                Set<String> topicsThisLevel =
                        matchingAtThisLevel.stream().map(p -> p.getLeft()).collect(Collectors.toSet());
                //existingTopics.
                matching.removeIf(p -> !topicsThisLevel.contains(p.getLeft()));
            }
            log.debug("Resulting set: {}", matching);
        }

        List<String> matchingIds = matching.stream().map(p -> p.getLeft()).collect(Collectors.toList());
        log.debug("Final gts: {}", matchingGts);
        matchingIds.addAll(matchingGts.stream().map(p -> p.getLeft()).collect(Collectors.toList()));
        List<String> ret = matchingIds.stream().map(id -> topicIdToTopicString.get(id)).collect(Collectors.toList());
        return ret;
    }

    public List<String> match(PubOrSub pubOrSub, String topic) {
        if (pubOrSub == PubOrSub.pub) {
            return matchFromPublisher(topic);
        } else {
            return matchFromSubscriber(topic);
        }
    }

    public void dump() {
        log.info("dump:");
        for (int i = 0; i < maps.size(); i++) {
            log.info("\tlevel {}", i);
            Map<String, List<Pair<String, Integer>>> map = maps.get(i);
            for (String key : map.keySet()) {
                log.info("\t\t{} : {}", key, map.get(key));
            }
        }
    }
}
