package com.solace.maas.topicmatcher.carlstitching;

import lombok.Data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

@Data
public class TopicMatcher {
    private List<String> topics;
    private final Map<TopicNode, TopicNode> topicsRoot = new HashMap<>();

    public void parseTopics() {
        topics.forEach(this::parseTopic);
    }

    private void parseTopic(final String topic) {
        String[] criteriaSplitter = topic.split("/");
        buildMatchCriteriaTree(topicsRoot, null, topic, new LinkedList<>(Arrays.asList(criteriaSplitter)));
    }

    private void buildMatchCriteriaTree(
            final Map<TopicNode, TopicNode> topicsRoot,
            final TopicNode parentTopicNode,
            final String topic,
            final List<String> criteriaPieces) {

        String criteria = criteriaPieces.get(0);

        TopicNode node = TopicNode.builder().partialTopic(criteria).build();

        AtomicReference<TopicNode> thisNode = new AtomicReference<>();
        if (topicsRoot == null) {
            thisNode.set(parentTopicNode.getChildren().computeIfAbsent(node, (v) -> node));
        } else {
            Optional.ofNullable(topicsRoot.get(node))
                    .ifPresentOrElse(thisNode::set, () -> {
                        topicsRoot.put(node, node);
                        thisNode.set(node);
                    });
        }

        criteriaPieces.remove(0);
        thisNode.get().getTopics().add(topic);

        if (criteriaPieces.size() > 0) {
            buildMatchCriteriaTree(null, thisNode.get(), topic, criteriaPieces);
        }
    }

    public Set<String> getTopicsForSubscription(final String matchCriteria) {
        List<String> matchPieces = new LinkedList<>(Arrays.asList(matchCriteria.split("/")));

        Set<String> topics = new HashSet<>();

        String piece = matchPieces.remove(0);
        MatchCriteria matcher = new MatchCriteria(piece);

        topicsRoot.values().forEach(topicNode -> {
            if (matcher.isMatch(topicNode)) {
                if ((matchPieces.size() == 0 || MatchCriteria.CriteriaType.INHERIT.equals(matcher.getCriteriaType()))
                        && topicNode.getTopics().size() > 0) {
                    topics.addAll(topicNode.getTopics());
                }

                if (!MatchCriteria.CriteriaType.INHERIT.equals(matcher.getCriteriaType())
                        && matchPieces.size() > 0) {
                    List<String> piecesCopy = new LinkedList<>(matchPieces);
                    findSubscriptionsForTopic(topicNode, topics, piecesCopy);
                }
            }
        });

        return topics;
    }

    public void findSubscriptionsForTopic(final TopicNode parentTopicNode, final Set<String> topics, final List<String> pieces) {
        if (pieces.size() == 0) {
            return;
        }
        String piece = pieces.remove(0);
        MatchCriteria matcher = new MatchCriteria(piece);

        parentTopicNode.getChildren().values().forEach(topicNode -> {
            if (matcher.isMatch(topicNode)) {
                if ((pieces.size() == 0 || MatchCriteria.CriteriaType.INHERIT.equals(matcher.getCriteriaType()))
                        && topicNode.getTopics().size() > 0) {
                    topics.addAll(topicNode.getTopics());
                }

                if (!MatchCriteria.CriteriaType.INHERIT.equals(matcher.getCriteriaType())
                        && pieces.size() > 0) {
                    List<String> piecesCopy = new LinkedList<>(pieces);
                    findSubscriptionsForTopic(topicNode, topics, piecesCopy);
                }
            }
        });
    }

    public static void main(String... args) {
        TopicMatcher topicMatcher = new TopicMatcher();
        List<String> topics = List.of(
                "11/22/33/44",
                "11/33/4"
        );
        topicMatcher.setTopics(topics);
        topicMatcher.parseTopics();

        topicMatcher.getTopicsForSubscription("11/2*/>");
    }
}
