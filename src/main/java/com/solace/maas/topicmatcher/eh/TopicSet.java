package com.solace.maas.topicmatcher.eh;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class TopicSet {
    private boolean cacheTopics;
    private TopicNode root = new TopicNode("", false);

    public TopicSet (List<String> topics) {
        this(topics, false);
    }

    public TopicSet (List<String> topics, boolean cacheTopics) {
        this.cacheTopics = cacheTopics;
        topics.forEach(this::registerTopic);
    }

    public List<String> match(String subscription) {
        List<SubscriptionNode> subNodes = createSubscriptionNodes(subscription);
        List<String> matches = new ArrayList<>();

        Deque<TopicNode> toVisit = new LinkedList<>();
        root.getChildren().forEach(toVisit::push);

        while(!toVisit.isEmpty()) {
            TopicNode node = toVisit.pop();
            int i = node.getDepth() - 1;
            SubscriptionNode subPart = subNodes.get(i);
            boolean last = i == subNodes.size() - 1;

            if (subPart.matches(node.getValue())) {
                if (subPart.isRecursive()) {
                    if (cacheTopics)
                        matches.addAll(node.getTopics());
                    else
                        addAllChildrenStr(node, matches);
                } else if (node.isTerminal() && last) {
                    if (cacheTopics)
                        matches.addAll(node.getTopics());
                    else
                        matches.add(getTopicStr(node));
                } else if (!last) {
                    node.getChildren().forEach(toVisit::push);
                }
            }
        }

        return matches;
    }

    private void addAllChildrenStr(TopicNode root, Collection<String> matches) {
        Deque<TopicNode> toVisit = new LinkedList<>();
        toVisit.push(root);

        while(!toVisit.isEmpty()) {
            TopicNode node = toVisit.pop();
            if (node.isTerminal()) {
                matches.add(getTopicStr(node));
            } else {
                node.getChildren().forEach(toVisit::push);
            }
        }
    }

    private String getTopicStr(TopicNode node) {
        Deque<String> nodes = new ArrayDeque<>();
        var currentNode = node;
        while(currentNode.getParent() != null) {
            nodes.push(currentNode.getValue());
            currentNode = currentNode.getParent();
        }
        return String.join("/", nodes);
    }

    private List<SubscriptionNode> createSubscriptionNodes(String subscription) {
        List<SubscriptionNode> subNodes = new ArrayList<>();
        String[] parts = subscription.split("/");
        for (int i=0; i<parts.length; i++) {
            boolean last = i == parts.length-1;
            String part = parts[i];
            subNodes.add(SubscriptionNode.create(last, part));
        }
        return subNodes;
    }

    private void registerTopic(String topic) {
        TopicNode node = root;
        String parts[] = topic.split("/");
        for (int i=0; i<parts.length; i++) {
            String part = parts[i];
            boolean last = i == parts.length - 1;
            node = node.addChild(new TopicNode(part, last));
            if (cacheTopics)
                node.addTopic(topic);
        }
    }
}
