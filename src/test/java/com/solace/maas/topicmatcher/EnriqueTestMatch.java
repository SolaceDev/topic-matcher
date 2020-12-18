package com.solace.maas.topicmatcher;

import com.solace.maas.topicmatcher.eh.SubscriptionSet;
import com.solace.maas.topicmatcher.eh.TopicSet;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class EnriqueTestMatch {


    @Test
    public void matchTopics() {
        var subs = List.of("*/B/*/*/>", "*/*/*");
        SubscriptionSet ss = new SubscriptionSet(subs);
        assertThat(ss.match("BB/B/CAAAD/DD/EEF/GG")).containsExactlyInAnyOrderElementsOf(List.of( "*/B/*/*/>"));
    }


    @Test
    public void matchSubscription() {
        var topics = List.of("as/bg/rewe/bertt", "as/bt/wer");
        var ts = new TopicSet(topics);
        assertThat(ts.match("a*/*/>")).containsExactlyInAnyOrderElementsOf(topics);
    }
}
