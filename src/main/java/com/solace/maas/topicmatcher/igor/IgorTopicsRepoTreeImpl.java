package com.solace.maas.topicmatcher.igor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IgorTopicsRepoTreeImpl implements TopicsRepo {

    private static long counter = -1;

    private static final TreeLevel ROOT = new TreeLevel(++counter, "<root>", null, false);

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
            current = parent.getChild(toMatch);
            if (current == null) {
                toMatch.setId(++counter);
                parent.addChild(toMatch);
                current = toMatch;
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
            current = parent.getChild(toMatch);
            if (current != null) {
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
        List<String> matches = new LinkedList<>();
        findMatchingTopics(levels, matches);
        return matches;
    }

    public void findMatchingTopics(String[] levels,
                                   List<String> matches) {
        Queue<TreeLevel> parentQ = new LinkedList<>();
        parentQ.add(ROOT);
        while (!parentQ.isEmpty()) {
            TreeLevel parent = parentQ.remove();
            // depth of a parent level matches with the index of the levels array
            int index = parent.getDepth();
            boolean isLeafLevel = false;
            if (index == levels.length - 1) {
                isLeafLevel = true;
            }
            if (levels[index].equals("*")) {
                if (isLeafLevel) {
                    parent.getLeafChildren().forEach(leaf -> matches.add(leaf.getFullPath()));
                } else {
                    // is not leaf
                    parentQ.addAll(parent.getNonLeafChildren());
                }
            } else if (levels[index].endsWith("*")) {
                if (isLeafLevel) {
                    getLevelsFilteredByPrefix(levels[index], parent.getLeafChildren())
                            .forEach(leaf ->
                                    matches.add(leaf.getFullPath()));
                } else {
                    // is not leaf
                    parentQ.addAll(
                            getLevelsFilteredByPrefix(levels[index], parent.getNonLeafChildren())
                                    .collect(Collectors.toSet()));
                }
            } else if (levels[index].equals(">")) {
                matches.addAll(parent.getLeafChildren().stream().map(TreeLevel::getFullPath).collect(Collectors.toList()));
                Queue<TreeLevel> q = new LinkedList<>(parent.getNonLeafChildren());
                while (!q.isEmpty()) {
                    TreeLevel current = q.remove();
                    // adding children of a current level
                    q.addAll(current.getNonLeafChildren());
                    matches.addAll(current.getLeafChildren().stream().map(TreeLevel::getFullPath).collect(Collectors.toList()));
                }
            } else {
                // matching against regular level, no wildcards detected
                TreeLevel toMatch = new TreeLevel(levels[index], parent, isLeafLevel);
                TreeLevel current = parent.getChild(toMatch);
                if (current != null) {
                    if (isLeafLevel) {
                        matches.add(current.getFullPath());
                    } else {
                        parentQ.add(current);
                    }
                }
            }
        }
    }

    private Stream<TreeLevel> getLevelsFilteredByPrefix(String level,
                                                        Collection<TreeLevel> siblings) {
        String prefix = level.substring(0, level.length() - 1);
        return siblings.stream().filter(lvl -> lvl.getName().startsWith(prefix));
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
        private int depth = -1;
        private String fullPath;
        // by hash lookup
        private final Map<Integer, TreeLevel> children = new HashMap<>();

        // since the children collection might be vast, it is wise to have them partitioned by leaf/non-leaf types
        private final Set<TreeLevel> leafChildren = new HashSet<>();
        private final Set<TreeLevel> nonLeafChildren = new HashSet<>();

        TreeLevel(String name, TreeLevel parent, boolean leaf) {
            this.name = name;
            this.parent = parent;
            this.leaf = leaf;
            if (leaf) {
                setFullPath();
            }
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
            if (child.isLeaf()) {
                leafChildren.add(child);
            } else {
                nonLeafChildren.add(child);
            }
        }

        public TreeLevel getChild(TreeLevel child) {
            return children.get(child.hashCode());
        }

        public void removeChild(TreeLevel child) {
            children.remove(child.hashCode());
            nonLeafChildren.remove(child);
            leafChildren.remove(child);
        }

        public Set<TreeLevel> getLeafChildren() {
            return leafChildren;
        }

        public Set<TreeLevel> getNonLeafChildren() {
            return nonLeafChildren;
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

        private void setFullPath() {
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
            fullPath = sb.toString();
        }

        private String getFullPath() {
            return fullPath;
        }

        // lazy calculation
        public int getDepth() {
            if (depth == -1) {
                TreeLevel current = this;
                int counter = 0;
                while (current.getParent() != null) {
                    current = current.getParent();
                    counter++;
                }
                depth = counter;
            }
            return depth;
        }
    }

}
