package com.solace.maas.topicmatcher.carlstitching;

import com.google.common.base.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Builder
public class TopicNode {
    @Getter
    @Setter
    private String partialTopic;
    @Getter
    private final Set<String> topics = new HashSet<>();
    @Getter
    private final Map<TopicNode, TopicNode> children = new HashMap<>();

    @Override
    public int hashCode() {
        return Objects.hashCode(partialTopic);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TopicNode)) {
            return false;
        }

        TopicNode otherNode = (TopicNode) obj;

        if (partialTopic == null && otherNode.getPartialTopic() == null) {
            return true;
        }

        return partialTopic != null && partialTopic.equals(otherNode.getPartialTopic());
    }
}
