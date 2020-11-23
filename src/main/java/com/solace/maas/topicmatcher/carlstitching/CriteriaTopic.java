package com.solace.maas.topicmatcher.carlstitching;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class CriteriaTopic {
    private String topic;
    private List<CriteriaSubscription> subscriptions = new ArrayList<>();
}
