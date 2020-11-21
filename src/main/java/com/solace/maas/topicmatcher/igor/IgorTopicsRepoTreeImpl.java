package com.solace.maas.topicmatcher.igor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class IgorTopicsRepoTreeImpl implements TopicsRepo {

    private static final AtomicLong ID_GEN = new AtomicLong(-1);

    private static final TreeLevel ROOT = new TreeLevel(ID_GEN.incrementAndGet(), "<root>", null, false);

    @Override
    public void registerTopic(String topic) {
        String[] levels = topic.split("/");
        TreeLevel parent = ROOT;
        TreeLevel current;
        for (int i = 0; i < levels.length; i++) {
            boolean isLeaf = false;
            if (i == levels.length - 1) {
                isLeaf = true;
            }
            TreeLevel toMatch = new TreeLevel(levels[i], parent, isLeaf);
            if (!parent.getChildren().contains(toMatch)) {
                toMatch.setId(ID_GEN.incrementAndGet());
                parent.addChild(toMatch);
                current = toMatch;
            } else {
                current = parent.getChild(toMatch);
            }
            parent = current;
        }
    }

    @Override
    public void deregisterTopic(String topic) {
        String[] levels = topic.split("/");
        LinkedList<TreeLevel> stack = new LinkedList<>();
        TreeLevel parent = ROOT;
        TreeLevel current;
        for (int i = 0; i < levels.length; i++) {
            boolean isLeaf = false;
            if (i == levels.length - 1) {
                isLeaf = true;
            }
            TreeLevel toMatch = new TreeLevel(levels[i], parent, isLeaf);
            if (parent.getChildren().contains(toMatch)) {
                current = parent.getChild(toMatch);
                // add to top of the  stack
                stack.push(current);
                parent = current;
            } else {
                // this level is not even present in a tree, such topic won't be de-registered
                return;
            }
        }
        while (!stack.isEmpty()) {
            TreeLevel tmp = stack.pop();
            TreeLevel tmpParent = tmp.getParent();
            if (tmp.isLeaf()) {
                tmpParent.removeChild(tmp);
            } else {
                if (tmp.getChildren().isEmpty()) {
                    // there are no children for this level, let's  purge it then
                    tmpParent.removeChild(tmp);
                }
            }
        }
    }

    @Override
    public List<String> findMatchingTopics(String subscription) {
        String[] levels = subscription.split("/");
        List<String> matches = new ArrayList<>();
        findMatchingTopics(levels, 0, ROOT, matches);
        return matches;
    }

    // TODO: recursion must be reworked either via appropriate while loop or via queue
    public void findMatchingTopics(String[] levels,
                                   int index,
                                   TreeLevel parent,
                                   List<String> matches) {
        if (index >= levels.length) {
            return;
        }
        boolean isLeafLevel = false;
        if (index == levels.length - 1) {
            isLeafLevel = true;
        }
        if (levels[index].equals("*")) {
            if (isLeafLevel) {
                Set<TreeLevel> leafSiblings = parent.getChildren().stream()
                        .filter(TreeLevel::isLeaf)
                        .collect(Collectors.toSet());
                for (TreeLevel leafLevel : leafSiblings) {
                    matches.add(leafLevel.getFullPath());
                }
            } else {
                // is not leaf
                Set<TreeLevel> nonLeafSiblings = parent.getChildren().stream()
                        .filter(lvl -> !lvl.isLeaf())
                        .collect(Collectors.toSet());
                for (TreeLevel nonLeafLevel : nonLeafSiblings) {
                    findMatchingTopics(levels, index + 1, nonLeafLevel, matches);
                }
            }
        } else if (levels[index].endsWith("*")) {
            if (isLeafLevel) {
                Set<TreeLevel> leafMatchedLevels = getLevelsFilteredByPrefix(levels[index], parent.getChildren(), TreeLevel::isLeaf);
                for (TreeLevel leafLevel : leafMatchedLevels) {
                    matches.add(leafLevel.getFullPath());
                }
            } else {
                // is not leaf
                Set<TreeLevel> nonLeafMatchedLevels = getLevelsFilteredByPrefix(levels[index], parent.getChildren(), lvl -> !lvl.isLeaf());
                for (TreeLevel nonLeafLevel : nonLeafMatchedLevels) {
                    findMatchingTopics(levels, index + 1, nonLeafLevel, matches);
                }
            }
        } else if (levels[index].equals(">")) {
            Queue<TreeLevel> q = new LinkedList<>(parent.getChildren());
            while (!q.isEmpty()) {
                TreeLevel current = q.remove();
                if (current.isLeaf()) {
                    matches.add(current.getFullPath());
                } else {
                    // adding children of a current level
                    q.addAll(current.getChildren());
                }
            }
        } else {
            // matching against regular level, no wildcards detected
            TreeLevel toMatch = new TreeLevel(levels[index], parent, isLeafLevel);
            if (parent.getChildren().contains(toMatch)) {
                TreeLevel current = parent.getChild(toMatch);
                if (isLeafLevel) {
                    matches.add(current.getFullPath());
                } else {
                    findMatchingTopics(levels, index + 1, current, matches);
                }
            }
        }
    }

    private Set<TreeLevel> getLevelsFilteredByPrefix(String level,
                                                     Collection<TreeLevel> siblings,
                                                     Predicate<TreeLevel> predicate) {
        String prefix = level.substring(0, level.lastIndexOf("*"));
        return siblings.stream()
                .filter(predicate)
                .filter(lvl -> lvl.getName().startsWith(prefix))
                .collect(Collectors.toSet());
    }

    @Override
    public List<String> findCoveringSubscriptions(String topic) {
        return null;
    }

    @Override
    public Object getTopicTree() {
        return null;
    }


    private static class TreeLevel {
        private static final long UNSET_ID = -1;
        private long id = UNSET_ID;
        private final String name;
        private final TreeLevel parent;
        private final boolean leaf;
        // by hash lookup
        private final Map<Integer, TreeLevel> children = new HashMap<>();

        TreeLevel(String name, TreeLevel parent, boolean leaf) {
            this.name = name;
            this.parent = parent;
            this.leaf = leaf;
        }

        TreeLevel(long id, String name, TreeLevel parent, boolean leaf) {
            this.id = id;
            this.name = name;
            this.parent = parent;
            this.leaf = leaf;
        }

        private void setId(long id) {
            if (this.id != UNSET_ID) {
                throw new IllegalStateException("Level id can be set only once!");
            }
            this.id = id;
        }

        private long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public TreeLevel getParent() {
            return parent;
        }

        public boolean isLeaf() {
            return leaf;
        }

        public void addChild(TreeLevel child) {
            children.put(child.hashCode(), child);
        }

        public TreeLevel getChild(TreeLevel child) {
            return children.get(child.hashCode());
        }

        public void removeChild(TreeLevel child) {
            children.remove(child.hashCode());
        }

        public Collection<TreeLevel> getChildren() {
            return children.values();
        }

        // DO NOT add other fields to equals() and hashCode()
        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            TreeLevel treeLevel = (TreeLevel) o;
            return leaf == treeLevel.leaf &&
                    Objects.equals(name, treeLevel.name) &&
                    Objects.equals(parent.id, treeLevel.parent.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, parent.id, leaf);
        }

        @Override
        public String toString() {
            return "TreeLevel{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    ", parent.id=" + (parent != null ? parent.id : "null") +
                    ", leaf=" + leaf +
                    ", children.names=" + children.values().stream().map(TreeLevel::getName).collect(Collectors.toList()) +
                    '}';
        }

        private String getFullPath() {
            TreeLevel current = this;
            LinkedList<String> stack = new LinkedList<>();
            do {
                stack.push(current.getName());
                current = current.getParent();
            } while (current.getParent() != null);
            StringBuilder sb = new StringBuilder();
            while (!stack.isEmpty()) {
                sb.append(stack.pop());
                if (!stack.isEmpty()) {
                    sb.append("/");
                }
            }
            return sb.toString();
        }
    }

}
