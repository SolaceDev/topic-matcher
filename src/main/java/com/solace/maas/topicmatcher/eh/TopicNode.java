package com.solace.maas.topicmatcher.eh;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TopicNode {
    private String value;
    private boolean isTerminal;
    private int depth;
    private HashMap<TopicNode, TopicNode> children;
    private List<String> topics = new ArrayList<>();
    private TopicNode parent;

    public TopicNode(String value, boolean isTerminal) {
        this.value = value;
        this.isTerminal = isTerminal;
    }

    public TopicNode addChild(TopicNode node) {
        if (children == null) {
            children = new HashMap<>();
        }
        node.depth = depth+1;
        node.parent = this;
        children.putIfAbsent(node, node);
        return getChild(node);

    }

    public TopicNode getChild(TopicNode child) {
        return children.get(child);
    }

    public TopicNode getParent() {
        return parent;
    }

    public void addTopic(String topic) {
        topics.add(topic);
    }

    public int getDepth() { return depth; }

    public String getValue() { return value; }

    public Collection<String> getTopics() { return topics; }

    public boolean isTerminal() { return isTerminal; }

    public Collection<TopicNode> getChildren() {
        return children != null ? children.values() : Collections.emptySet();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TopicNode topicNode = (TopicNode) o;
        return isTerminal == topicNode.isTerminal &&
                value.equals(topicNode.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, isTerminal);
    }

    @Override
    public String toString() {
        return "TopicNode{" +
                "value='" + value + '\'' +
                ", isTerminal=" + isTerminal +
                '}';
    }
}
