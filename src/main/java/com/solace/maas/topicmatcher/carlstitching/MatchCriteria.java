package com.solace.maas.topicmatcher.carlstitching;

import lombok.Getter;

public class MatchCriteria {
    private String matchPiece;
    @Getter
    private CriteriaType criteriaType;
    @Getter
    private String matchString;

    public MatchCriteria(final String matchPiece) {
        this.matchPiece = matchPiece;
        init();
    }

    private void init() {
        int starIndex = matchPiece.indexOf('*');
        if (">".equals(matchPiece)) {
            criteriaType = CriteriaType.INHERIT;
        } else if ("*".equals(matchPiece)) {
            criteriaType = CriteriaType.WILDCARD;
        } else if (starIndex == -1) {
            criteriaType = CriteriaType.EXACT_MATCH;
            matchString = matchPiece;
        } else {
            criteriaType = CriteriaType.STARTS_WITH;
            matchString = matchPiece.substring(0, starIndex);
        }
    }

    public boolean isMatch(final TopicNode topicNode) {
        switch (criteriaType) {
            case INHERIT:
            case WILDCARD:
                return true;
            case EXACT_MATCH:
                return matchString.equals(topicNode.getPartialTopic());
            case STARTS_WITH:
                return topicNode.getPartialTopic().startsWith(matchString);
            default:
                return false;
        }
    }

    public enum CriteriaType {
        INHERIT,
        WILDCARD,
        STARTS_WITH,
        EXACT_MATCH
    }
}
