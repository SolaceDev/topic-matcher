package com.solace.maas.topicmatcher.eh;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@NoArgsConstructor
public class SubscriptionNode {
    private String value;
    private MatchingType type;
    private boolean isTerminal;
    private int depth;
    private Map<SubscriptionNode, SubscriptionNode> children;
    @Getter
    @Setter
    private SubscriptionNode parent;

    public SubscriptionNode(String value, MatchingType type, boolean isTerminal) {
        this.value = value;
        this.type = type;
        this.isTerminal = isTerminal || type == MatchingType.RECURSIVE;
    }

    enum MatchingType {
        EXACT,
        PREFIX,
        RECURSIVE,
        ALL
    }
    public static SubscriptionNode create(boolean last, String part) {
        SubscriptionNode newNode;
        if (part.equals(">")) {
            newNode = new SubscriptionNode(part, MatchingType.RECURSIVE, last);
        } else {
            int pos = part.indexOf("*");
            if (pos == 0) {
                newNode = new SubscriptionNode(part, MatchingType.ALL, last);
            } else if (pos > 0) {
                newNode = new SubscriptionNode(part.substring(0, pos), MatchingType.PREFIX, last);
            } else {
                newNode = new SubscriptionNode(part, MatchingType.EXACT, last);
            }
        }
        return newNode;
    }

    public boolean matches(String s) {
        switch (type) {
            case EXACT:
                return s.equals(value);
            case PREFIX:
                return s.startsWith(value);
            case RECURSIVE:
            case ALL:
                return true;
            default:
                return false;
        }
    }

    public SubscriptionNode addChild(SubscriptionNode node) {
        if (children == null) {
            children = new HashMap<>();
        }
        node.setDepth(depth+1);
        node.setParent(this);
        children.putIfAbsent(node, node);
        return getChild(node);
    }

    public SubscriptionNode getChild(SubscriptionNode child) {
        return children.get(child);
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int i) {
        depth = i;
    }

    public Collection<SubscriptionNode> getChildren() {
        return children == null ? Collections.emptySet() : children.keySet();
    }

    public boolean isTerminal() { return isTerminal; }
    public boolean isRecursive() { return type == MatchingType.RECURSIVE; }
    public String getValue() {
        if (type == MatchingType.PREFIX) return value + "*";
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SubscriptionNode that = (SubscriptionNode) o;
        return type == that.type &&
               isTerminal == that.isTerminal &&
               value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, isTerminal, value);
    }

    @Override
    public String toString() {
        return "SubscriptionNode{" +
                "value='" + value + '\'' +
                ", type=" + type +
                ", isTerminal=" + isTerminal +
                '}';
    }
}
