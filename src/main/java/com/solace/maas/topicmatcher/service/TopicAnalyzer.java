package com.solace.maas.topicmatcher.service;

import com.solace.maas.topicmatcher.PubOrSub;
import com.solace.maas.topicmatcher.model.Prefix;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class TopicAnalyzer {

    private final Logger log = LoggerFactory.getLogger(TopicAnalyzer.class);
    private Map<Integer, Map<String, List<Pair<String, Integer>>>> maps = new HashMap(); // level -> value ->
    // topicId,length
    private Map<Integer, Prefix> prefixMap = new HashMap(); // Level,
    // length, list
    // of topics.
    // level ->
    // prefixValue etc.
    private List<String> allTopicStrings = new ArrayList<>();
    private Map<String, String> topicIdToTopicString = new HashMap<>();
    private AtomicLong idGenerator = new AtomicLong();

    public void analyze(PubOrSub pubOrSub, List<String> topicStrings) {
        allTopicStrings.clear();
        maps.clear();
        prefixMap.clear();
        topicIdToTopicString.clear();
        int maxLevel = 0;
        List<Topic> topics = new ArrayList<>();

        for (String topicString : topicStrings) {
            String id = "T" + idGenerator.incrementAndGet();
            Topic topic = new Topic(id, topicString);
            allTopicStrings.add(topicString);
            topicIdToTopicString.put(id, topicString);
            topics.add(topic);
            int numLevels = topic.getNumLevels();
            if (numLevels > maxLevel) {
                maxLevel = numLevels;
            }
        }

        int estimatedValuesPerLevel = topics.size() / maxLevel;
        float hashLoadFactor = 0.99f;
        double fudgeFactor = 1.2;
        int hashCapacity = (int) ((estimatedValuesPerLevel * fudgeFactor) / hashLoadFactor);
        log.debug("max level: {} estimatedValuesPerLevel: {} hashCapacity: {}", maxLevel, estimatedValuesPerLevel,
                hashCapacity);

        for (Topic topic : topics) {
            int numLevels = topic.getNumLevels();
            for (int level = 0; level < numLevels; level++) {
                String levelString = topic.getLevel(level);
                boolean handledPrefix = false;

                // If these are subscriptions, we need to put the stars with prefixes into their own list.
                if (pubOrSub == PubOrSub.sub) {
                    int len = levelString.length();
                    if (len > 1) {
                        int starIndex = levelString.indexOf('*');
                        // If it's the first character we'll just put it in the main map.
                        // Otherwise...
                        if (starIndex > 0) {
                            handledPrefix = true;
                            String prefixString = levelString.substring(0, starIndex);
                            int prefixLength = prefixString.length();
                            log.debug("level: {} prefixString {}", level, prefixString);

                            Prefix prefix = prefixMap.get(level);

                            if (prefix == null) {
                                prefix = new Prefix();
                                prefixMap.put(level, prefix);
                            }

                            Map<String, List<Pair<String, Integer>>> topicMapForLength =
                                    prefix.getTopics(prefixLength);
                            if (topicMapForLength == null) {
                                topicMapForLength = new HashMap<>(hashCapacity, hashLoadFactor);
                                prefix.setTopics(prefixLength, topicMapForLength);
                            }


                            List<Pair<String, Integer>> matchingTopics = topicMapForLength.get(prefixString);

                            if (matchingTopics == null) {
                                matchingTopics = new ArrayList<>();
                                topicMapForLength.put(prefixString, matchingTopics);
                            }

                            matchingTopics.add(Pair.of(topic.getId(), topic.getNumLevels()));

                            int largestSoFar = prefix.getMaxPrefixLength();
                            if (prefixLength > largestSoFar) {
                                prefix.setMaxPrefixLength(prefixLength);
                            }
                        }
                    }
                }

                if (!handledPrefix) {
                    Map<String, List<Pair<String, Integer>>> matchingTopicsAtThisLevel = maps.get(level);

                    if (matchingTopicsAtThisLevel == null) {
                        matchingTopicsAtThisLevel = new HashMap<>();
                        maps.put(level, matchingTopicsAtThisLevel);
                    }

                    List<Pair<String, Integer>> listOfTopics = matchingTopicsAtThisLevel.get(levelString);
                    if (listOfTopics == null) {
                        listOfTopics = new ArrayList<>();
                        matchingTopicsAtThisLevel.put(levelString, listOfTopics);
                    }
                    listOfTopics.add(Pair.of(topic.getId(), topic.getNumLevels()));
                }
            }
        }
    }

    public List<String> matchFromSubscriber(String topic) {
        log.debug("Match from subscriber: {}", topic);
        Set<Pair<String, Integer>> matching = new HashSet();
        String[] levels = topic.split("/");
        for (int level = 0; level < levels.length; level++) {
            String levelToMatch = levels[level];
            boolean isLeafNode = level == levels.length - 1;
            log.debug("Level to match: {} {} leaf: {}", level, levelToMatch, isLeafNode);
            final int lev = level; // for use in lamdas

            // Special case: match all
            if (levelToMatch.equals(">")) {
                if (level == 0) {
                    return allTopicStrings;
                } else {
                    break;
                }
            }

            if (levelToMatch.equals("*")) {
                // If it's a leaf node, return the current set where it ends at this level.
                if (isLeafNode) {
                    matching.removeIf(p -> p.getRight() > lev + 1);
                    break;
                }
                // else nothing changes in our set of matches.
            } else {

                Map<String, List<Pair<String, Integer>>> topicsAtThisLevel = maps.get(level);
                if (topicsAtThisLevel == null) {
                    return List.of(); // No topics at this level.
                }

                List<Pair<String, Integer>> matchingTopicsAtThisLevel = new ArrayList<>();

                int prefixIndex = levelToMatch.indexOf('*');
                if (prefixIndex > 0) {
                    String prefix = levelToMatch.substring(0, prefixIndex);
                    for (String topicString : topicsAtThisLevel.keySet()) {
                        if (topicString.startsWith(prefix)) {
                            matchingTopicsAtThisLevel.addAll(topicsAtThisLevel.get(topicString));
                        }
                    }
                }


                List<Pair<String, Integer>> literalMatchingTopicsAtThisLevel = topicsAtThisLevel.get(levelToMatch);
                if (literalMatchingTopicsAtThisLevel != null) {
                    matchingTopicsAtThisLevel.addAll(literalMatchingTopicsAtThisLevel);
                }

                if (matchingTopicsAtThisLevel.isEmpty()) {
                    return List.of();
                }

                matchingTopicsAtThisLevel = new ArrayList<>(matchingTopicsAtThisLevel); // Cloning so we can remove
                // some.

                Set<Pair<String, Integer>> matchingAtThisLevel = new HashSet<>();

                for (Pair<String,Integer> t : matchingTopicsAtThisLevel) {
                    if (!isLeafNode || t.getRight() <= level + 1) {
                        matchingAtThisLevel.add(t);
                    }
                }

                log.debug("current set: {} matching at this level: {}", matching.size(), matchingAtThisLevel.size());
                if (level == 0) {
                    matching = matchingAtThisLevel;
                } else {
                    matching.retainAll(matchingAtThisLevel);
                }
                log.debug("Resulting set: {}", matching.size());
                if (matching.size() == 0) {
                    break;
                }
            }
        }

        List<String> ret = new ArrayList<>(matching.size());

        for (Pair<String, Integer> match : matching) {
            ret.add(topicIdToTopicString.get(match.getLeft()));
        }

        return ret;
    }

    public List<String> matchFromPublisher(String topic) {
        log.debug("Match from publisher: {}", topic);
        final Set<Pair<String, Integer>> matching = new HashSet();
        final List<Pair<String, Integer>> matchingGts = new ArrayList();
        String[] levels = topic.split("/");

        for (int level = 0; level < levels.length; level++) {
            String levelToMatch = levels[level];
            boolean isLeafNode = level == levels.length - 1;
            log.debug("Level to match: {} {} leaf: {}", level, levelToMatch, isLeafNode);
            final int lev = level; // for use in lamdas which need i to be final.

            Prefix prefix = prefixMap.get(level);
            List<Pair<String, Integer>> prefixTopics = new ArrayList<>();
            List<Pair<String, Integer>> matchingTopicsAtThisLevel = new ArrayList<>();

            if (prefix != null) {
                int maxLengthToConsider = Math.min(levelToMatch.length(), prefix.getMaxPrefixLength());
                for (int prefixLength = 1; prefixLength <= maxLengthToConsider; prefixLength++) {
                    Map<String, List<Pair<String, Integer>>> topicMap = prefix.getTopics(prefixLength);
                    if (topicMap != null) {
                        String substring = levelToMatch.substring(0, prefixLength);
                        List<Pair<String, Integer>> topics = topicMap.get(substring);
                        if (topics != null) {
                            prefixTopics.addAll(topics);
                        }
                    }
                }

                if (prefixTopics.size() > 0) {
                    if (level > 0) {
                        prefixTopics = new ArrayList<>(prefixTopics);
                        prefixTopics.removeIf(p -> !matching.contains(p));
                    }
                    matchingTopicsAtThisLevel.addAll(prefixTopics);
                }
            }

            Map<String, List<Pair<String, Integer>>> topicMap = maps.get(level);

            if (topicMap != null) {

                List<Pair<String, Integer>> gtsAtThisLevel = topicMap.get(">");
                log.debug("matching gts: " + gtsAtThisLevel);

                // If there is a > at the 0 level, always add it.
                // Otherwise only add it if the higher levels of the topic already matched.
                if (gtsAtThisLevel != null) {
                    if (level > 0) {
                        // Cloning so we can remove some.
                        gtsAtThisLevel = new ArrayList<>(gtsAtThisLevel);
                        gtsAtThisLevel.removeIf(p -> !matching.contains(p));
                        log.debug("matching gts after filtering: {}", gtsAtThisLevel);
                    }
                    matchingGts.addAll(gtsAtThisLevel);
                }

                List<Pair<String, Integer>> literalMatchingTopicsAtThisLevel = topicMap.get(levelToMatch);
                if (literalMatchingTopicsAtThisLevel != null) {
                    matchingTopicsAtThisLevel.addAll(literalMatchingTopicsAtThisLevel);
                }

                List<Pair<String, Integer>> starsAtThisLevel = topicMap.get("*");

                if (starsAtThisLevel != null) {
                    matchingTopicsAtThisLevel.addAll(starsAtThisLevel);
                }
            }

            if (matchingTopicsAtThisLevel.size() == 0) {
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
            log.debug("current set: {} matching at this level: {}", matching.size(), matchingAtThisLevel.size());
            if (level == 0) {
                matching.addAll(matchingAtThisLevel);
            } else {
                //Set<String> existingTopics = matching.stream().map( p -> p.getLeft()).collect(Collectors.toSet());
                Set<String> topicsThisLevel =
                        matchingAtThisLevel.stream().map(p -> p.getLeft()).collect(Collectors.toSet());
                //existingTopics.
                matching.removeIf(p -> !topicsThisLevel.contains(p.getLeft()));
            }
            log.debug("Resulting set: {}", matching.size());

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

    public static class Topic {
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
}
