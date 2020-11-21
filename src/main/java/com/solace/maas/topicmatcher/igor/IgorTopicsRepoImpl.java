package com.solace.maas.topicmatcher.igor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IgorTopicsRepoImpl implements TopicsRepo {

    private static final Level ROOT = new Level((short) 0, "<empty>", null, false);

    // if we want to save space, we can consider using unsigned byte instead of short
    // <depth, <parentLvlName, siblingLvlNames>>
    private static final Map<Short, Map<String, Set<Level>>> LEVELS_BY_DEPTH = new HashMap<>();

    static {
        Map<String, Set<Level>> zeroDepth = new HashMap<>();
        Set<Level> root = new HashSet<>();
        root.add(ROOT);
        zeroDepth.put("<root_parent>", root);
        LEVELS_BY_DEPTH.put(ROOT.getDepth(), zeroDepth);
    }

    @Override
    public List<String> findMatchingTopics(String subscription) {
        String[] levels = subscription.split("/");
        List<String> matches = new ArrayList<>();
        findMatchingTopics(levels, (short) 1, ROOT, matches, "");
        return matches;
    }

    @Override
    public List<String> findCoveringSubscriptions(String topic) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    @Override
    public Object getTopicTree() {
        short depth = 0;
        List<Level> allLevels = new ArrayList<>();
        while (LEVELS_BY_DEPTH.containsKey(depth)) {
            allLevels.addAll(LEVELS_BY_DEPTH.get(depth).values().stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList()));
            depth++;
        }
        return allLevels;
    }

    // TODO: recursion must be reworked either via appropriate while loop or via queue
    public void findMatchingTopics(String[] levels,
                                   short depth,
                                   Level parent,
                                   List<String> matches,
                                   String topicPath) {
        if (depth > levels.length) {
            return;
        }
        if (!LEVELS_BY_DEPTH.containsKey(depth)) {
            return;
        }
        Map<String, Set<Level>> levelsOnDepth = LEVELS_BY_DEPTH.get(depth);
        // there is nothing to process as there are no matches
        if (!levelsOnDepth.containsKey(parent.getName())) {
            return;
        }
        boolean isLeafLevel = false;
        if (depth == levels.length) {
            isLeafLevel = true;
        }
        Set<Level> siblings = levelsOnDepth.get(parent.getName());
        if (levels[depth - 1].equals("*")) {
            if (isLeafLevel) {
                Set<Level> leafSiblings = siblings.stream()
                        .filter(Level::isLeaf)
                        .collect(Collectors.toSet());
                for (Level leafLevel : leafSiblings) {
                    matches.add(topicPath + leafLevel.getName());
                }
            } else {
                // is not leaf
                Set<Level> nonLeafSiblings = siblings.stream()
                        .filter(lvl -> !lvl.isLeaf())
                        .collect(Collectors.toSet());
                for (Level nonLeafLevel : nonLeafSiblings) {
                    findMatchingTopics(levels, (short) (depth + 1), nonLeafLevel, matches, topicPath + nonLeafLevel.getName() + "/");
                }
            }
        } else if (levels[depth - 1].endsWith("*")) {
            if (isLeafLevel) {
                Set<Level> leafMatchedLevels = getLevelsFilteredByPrefix(levels[depth - 1], siblings, Level::isLeaf);

                for (Level leafLevel : leafMatchedLevels) {
                    matches.add(topicPath + leafLevel.getName());
                }
            } else {
                // is not leaf
                Set<Level> nonLeafMatchedLevels = getLevelsFilteredByPrefix(
                        levels[depth - 1],
                        siblings,
                        lvl -> !lvl.isLeaf());

                for (Level nonLeafLevel : nonLeafMatchedLevels) {
                    findMatchingTopics(levels, (short) (depth + 1), nonLeafLevel, matches, topicPath + nonLeafLevel.getName() + "/");
                }
            }
        } else if (levels[depth - 1].equals(">")) {
            String finalTopicPath = topicPath;
            LinkedList<PathAwareLevel> q = siblings.stream()
                    .map(lvl -> new PathAwareLevel(finalTopicPath, lvl)).collect(Collectors.toCollection(LinkedList::new));
            while (!q.isEmpty()) {
                PathAwareLevel current = q.removeFirst();
                if (current.isLeaf()) {
                    matches.add(current.getPathToLevel() + current.getName());
                } else {
                    // adding children of a current level
                    Set<Level> children = LEVELS_BY_DEPTH.getOrDefault((short) (current.getDepth() + 1), Collections.emptyMap())
                            .getOrDefault(current.getName(), Collections.emptySet());
                    q.addAll(children.stream()
                            .map(childLvl -> new PathAwareLevel(current.getPathToLevel() + current.getName() + "/", childLvl))
                            .collect(Collectors.toList()));
                }
            }
        } else {
            // matching against regular level, no wildcards detected
            Level current = new Level(depth, levels[depth - 1], parent.getName(), isLeafLevel);
            if (siblings.contains(current)) {
                topicPath += current.getName();
                if (depth == levels.length) {
                    matches.add(topicPath);
                } else {
                    topicPath += "/";
                    findMatchingTopics(levels, (short) (depth + 1), current, matches, topicPath);
                }
            }
        }
    }

    private Set<Level> getLevelsFilteredByPrefix(String level,
                                                 Set<Level> siblings,
                                                 Predicate<Level> predicate) {
        String prefix = level.substring(0, level.lastIndexOf("*"));
        return siblings.stream()
                .filter(predicate)
                .filter(lvl -> lvl.getName().startsWith(prefix))
                .collect(Collectors.toSet());
    }

    @Override
    public void registerTopic(String topic) {
        String[] levels = topic.split("/");
        Level parent = ROOT;
        Level current;
        for (int i = 0; i < levels.length; i++) {
            short depth = (short) (i + 1);
            boolean isLeaf = false;
            if (i == levels.length - 1) {
                isLeaf = true;
            }
            current = new Level(depth, levels[i], parent.getName(), isLeaf);
            if (!LEVELS_BY_DEPTH.containsKey(depth)) {
                Map<String, Set<Level>> siblingsByParentLookup = new HashMap<>();
                Set<Level> siblings = new HashSet<>();
                siblings.add(current);
                siblingsByParentLookup.put(parent.getName(), siblings);
                LEVELS_BY_DEPTH.put(depth, siblingsByParentLookup);
            } else {
                Map<String, Set<Level>> siblingsByParentLookup = LEVELS_BY_DEPTH.get(depth);
                if (!siblingsByParentLookup.containsKey(parent.getName())) {
                    Set<Level> siblings = new HashSet<>();
                    siblings.add(current);
                    siblingsByParentLookup.put(parent.getName(), siblings);
                } else {
                    Set<Level> siblings = siblingsByParentLookup.get(parent.getName());
                    siblings.add(current);
                }
            }
            parent = current;
        }
    }

    @Override
    public void deregisterTopic(String topic) {
        String[] levels = topic.split("/");
        for (int i = levels.length - 1; i > 0; i--) {
            short depth = (short) (i + 1);
            boolean isLeaf = false;
            if (i == levels.length - 1) {
                isLeaf = true;
            }
            String parentLevelName = levels[i - 1];
            String currentLevelName = levels[i];
            Level current = new Level(depth, currentLevelName, parentLevelName, isLeaf);
            if (LEVELS_BY_DEPTH.containsKey(depth)) {
                Map<String, Set<Level>> siblingsByParentLookup = LEVELS_BY_DEPTH.get(depth);
                if (siblingsByParentLookup.containsKey(parentLevelName)) {
                    if (isLeaf) {
                        Set<Level> siblings = siblingsByParentLookup.get(parentLevelName);
                        siblings.remove(current);
                    } else {
                        // we cannot simply remove non-leaf level as there might be child levels that depend on it
                        if (!isParent((short) (depth + 1), currentLevelName)) {
                            Set<Level> siblings = siblingsByParentLookup.get(parentLevelName);
                            siblings.remove(current);
                        }
                    }
                }
            }
        }
    }

    private boolean isParent(short depth, String levelName) {
        return LEVELS_BY_DEPTH.containsKey(depth)
                && LEVELS_BY_DEPTH.get(depth).get(levelName) != null
                && !LEVELS_BY_DEPTH.get(depth).get(levelName).isEmpty();
    }

}
