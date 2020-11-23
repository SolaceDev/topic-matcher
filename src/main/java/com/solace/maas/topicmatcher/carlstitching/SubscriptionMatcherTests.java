package com.solace.maas.topicmatcher.carlstitching;

//import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

//import static org.hamcrest.CoreMatchers.equalTo;
//import static org.hamcrest.CoreMatchers.is;
//import static org.hamcrest.MatcherAssert.assertThat;

public class SubscriptionMatcherTests {
//    @Test
    public void simpleTest() {

        List<String> matchCriteria = List.of(
                "users/1/>",
                "users/1/*/stuff",
                "users/1/blah/stuff",
                "users/1/bla*/stuff"
        );
        List<CriteriaSubscription> subscriptions = matchCriteria.stream()
                .map(criteria -> CriteriaSubscription.builder()
                        .matchCriteria(criteria)
                        .build())
                .collect(Collectors.toCollection(LinkedList::new));

        SubscriptionMatcher subscriptionMatcher = new SubscriptionMatcher();
        subscriptionMatcher.setSubscriptions(subscriptions);
        subscriptionMatcher.parseCriterias();

        CriteriaTopic criteriaTopic = subscriptionMatcher.getSubscriptionsForTopic("users/1/blag/stuff");
        List<String> expectedMatches = new LinkedList<>(List.of(
                matchCriteria.get(0),
                matchCriteria.get(1),
                matchCriteria.get(3)
        ));

//        assertThat(criteriaTopic.getSubscriptions().size(), is(equalTo(3)));
//        criteriaTopic.getSubscriptions().forEach(sub -> expectedMatches.remove(sub.getMatchCriteria()));
//        assertThat(expectedMatches.size(), is(equalTo(0)));
    }
}
