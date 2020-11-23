package com.solace.maas.topicmatcher.carlstitching;

import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

@Data
@Builder
public class CriteriaSubscription {
    @NonNull
    private String matchCriteria;
    private Object subscriptionObject;

    public <T> T getSubscriptionObject() {
        return (T) subscriptionObject;
    }
}
