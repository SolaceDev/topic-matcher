package com.solace.maas.topicmatcher.carlstitching;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class CriteriaRoot {
    @Getter
    private Map<CriteriaNode, CriteriaNode> criteriaRoot = new HashMap<>();
}
