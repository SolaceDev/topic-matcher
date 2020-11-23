package com.solace.maas.topicmatcher.carlstitching;

import lombok.Data;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class SubscriptionMatcher {
    private List<CriteriaSubscription> subscriptions;
    private CriteriaRoot criteriaRoot = new CriteriaRoot();

    public void parseCriterias() {
        subscriptions.forEach(this::parseCriteria);
    }

    public void parseCriteria(final CriteriaSubscription subscription) {
        String[] criteriaSplitter = subscription.getMatchCriteria().split("/");
        buildMatchCriteriaTree(criteriaRoot, null, subscription, new LinkedList<>(Arrays.asList(criteriaSplitter)));
    }

    public void buildMatchCriteriaTree(
            final CriteriaRoot root,
            final CriteriaNode parentCriteriaNode,
            final CriteriaSubscription subscription,
            final List<String> criteriaPieces) {

        String criteria = criteriaPieces.get(0);

        CriteriaNode targetNode;
        if (">".equals(criteria)) {
            targetNode = CriteriaNode.builder()
                    .criteriaType(CriteriaNode.CriteriaType.INHERIT)
                    .build();
        } else if ("*".equals(criteria)) {
            targetNode = CriteriaNode.builder()
                    .criteriaType(CriteriaNode.CriteriaType.WILDCARD)
                    .build();
        } else {
            int starIndex = criteria.indexOf('*');
            if (starIndex == -1) {
                targetNode = CriteriaNode.builder()
                        .criteriaType(CriteriaNode.CriteriaType.EXACT_MATCH)
                        .matchString(criteria)
                        .build();
            } else {
                targetNode = CriteriaNode.builder()
                        .criteriaType(CriteriaNode.CriteriaType.STARTS_WITH)
                        .matchString(criteria.substring(0, starIndex))
                        .build();
            }
        }

        AtomicReference<CriteriaNode> thisNode = new AtomicReference<>();
        if (root == null) {
            thisNode.set(parentCriteriaNode.getChildren().computeIfAbsent(targetNode, (v) -> targetNode));
        } else {
            Optional.ofNullable(root.getCriteriaRoot().get(targetNode))
                    .ifPresentOrElse(thisNode::set, () -> {
                        root.getCriteriaRoot().put(targetNode, targetNode);
                        thisNode.set(targetNode);
                    });
        }

        criteriaPieces.remove(0);

        if (criteriaPieces.size() == 0) {
            thisNode.get().getSubscriptions().add(subscription);
        } else {
            buildMatchCriteriaTree(null, thisNode.get(), subscription, criteriaPieces);
        }
    }

    public CriteriaTopic getSubscriptionsForTopic(final String topic) {
        List<String> topicPieces = new LinkedList<>(Arrays.asList(topic.split("/")));

        CriteriaTopic topicObj = new CriteriaTopic();
        topicObj.setTopic(topic);
        topicObj.setSubscriptions(new ArrayList<>());

        String piece = topicPieces.remove(0);

        criteriaRoot.getCriteriaRoot().values().forEach(criteria -> {
            if (criteria.isMatch(piece)) {
                if (criteria.getSubscriptions().size() > 0) {
                    topicObj.getSubscriptions().addAll(criteria.getSubscriptions());
                }

                if (!CriteriaNode.CriteriaType.INHERIT.equals(criteria.getCriteriaType())
                        && topicPieces.size() > 0) {
                    List<String> piecesCopy = new LinkedList<>(topicPieces);
                    findSubscriptionsForTopic(criteria, topicObj, piecesCopy);
                }
            }
        });

        return topicObj;
    }

    public void findSubscriptionsForTopic(final CriteriaNode node, final CriteriaTopic topic, final List<String> pieces) {
        if (pieces.size() == 0) {
            return;
        }
        String piece = pieces.remove(0);

        node.getChildren().values().forEach(criteria -> {
            if (criteria.isMatch(piece)) {
                if (criteria.getSubscriptions().size() > 0) {
                    topic.getSubscriptions().addAll(criteria.getSubscriptions());
                }
                if (!CriteriaNode.CriteriaType.INHERIT.equals(criteria.getCriteriaType())
                        && pieces.size() > 0) {
                    List<String> piecesCopy = new LinkedList<>(pieces);
                    findSubscriptionsForTopic(criteria, topic, piecesCopy);
                }
            }
        });
    }
}
