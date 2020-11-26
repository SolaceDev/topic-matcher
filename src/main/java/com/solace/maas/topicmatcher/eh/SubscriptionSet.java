package com.solace.maas.topicmatcher.eh;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

public class SubscriptionSet {
    SubscriptionNode root = new SubscriptionNode();

    public SubscriptionSet(List<String> subscriptions) {
        subscriptions.forEach(this::registerSubscription);
    }

    public List<String> match(String topic) {
        String[] parts = topic.split("/");
        List<String> matches = new ArrayList<>();

        Deque<SubscriptionNode> toVisit = new LinkedList<>();
        root.getChildren().forEach(toVisit::push);

        while (!toVisit.isEmpty()) {
            SubscriptionNode node = toVisit.pop();
            int i = node.getDepth() - 1;
            String part = parts[i];
            boolean last = i == parts.length-1;

            if(node.matches(part)) {
                if (node.isRecursive()) {
                    matches.add(getSubscriptionString(node));
                } else if (node.isTerminal() && last) {
                    matches.add(getSubscriptionString(node));
                } else if (!last) {
                    node.getChildren().forEach(toVisit::push);
                }
            }
        }

        return matches;
    }

    private void registerSubscription(String subscription) {
        SubscriptionNode node = root;
        SubscriptionNode newNode;
        String[] parts = subscription.split("/");
        for (int i=0; i<parts.length; i++) {
            boolean last = i == parts.length-1;
            String part = parts[i];

            newNode = SubscriptionNode.create(last, part);
            node = node.addChild(newNode);
        }
    }

    private String getSubscriptionString(SubscriptionNode node) {
        Deque<String> nodes = new ArrayDeque<>();
        var currentNode = node;
        while(currentNode.getParent() != null) {
            nodes.push(currentNode.getValue());
            currentNode = currentNode.getParent();
        }
        return String.join("/", nodes);
    }
}
