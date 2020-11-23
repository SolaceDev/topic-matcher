package com.solace.maas.topicmatcher.carlstitching;

import com.google.common.base.Objects;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Builder
public class CriteriaNode {
    @NonNull
    @Setter
    @Getter
    private CriteriaType criteriaType;
    @Getter
    @Setter
    private String matchString;
    @Getter
    private final Set<CriteriaSubscription> subscriptions = new HashSet<>();
    @Getter
    private final Map<CriteriaNode, CriteriaNode> children = new HashMap<>();


    public enum CriteriaType {
        INHERIT,
        WILDCARD,
        STARTS_WITH,
        EXACT_MATCH
    }

    public boolean isMatch(final String piece) {
        switch (criteriaType) {
            case INHERIT:
            case WILDCARD:
                return true;
            case EXACT_MATCH:
                return matchString.equals(piece);
            case STARTS_WITH:
                return piece.startsWith(matchString);
            default:
                return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(criteriaType, matchString);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof CriteriaNode)) {
            return false;
        }

        CriteriaNode otherNode = (CriteriaNode) obj;

        if (!criteriaType.equals(otherNode.getCriteriaType())) {
            return false;
        }

        if (matchString == null && otherNode.getMatchString() == null) {
            return true;
        }

        return matchString != null && matchString.equals(otherNode.getMatchString());
    }
}
