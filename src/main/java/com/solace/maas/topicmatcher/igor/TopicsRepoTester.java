package com.solace.maas.topicmatcher.igor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopicsRepoTester {

    private static void assertEquals(Object o1, Object o2) {
        if (!o1.equals(o2)) {
            throw new AssertionError(o1 + " is not equal to " + o2);
        }
    }

    public static void main(String[] args) {
        TopicsRepo topicsRepo = new IgorTopicsRepoImpl();
        topicsRepo.registerTopic("a/b/c");
        topicsRepo.registerTopic("a/b/ccccccc");
        topicsRepo.registerTopic("a/bvvvvv/c");
        topicsRepo.registerTopic("a/xxxxx/c");
        topicsRepo.registerTopic("a/b");
        topicsRepo.registerTopic("a/b/c/d");
        topicsRepo.registerTopic("a/b/c/d/e");
        topicsRepo.registerTopic("a/x");
        topicsRepo.registerTopic("a");
        topicsRepo.registerTopic("x");

        System.out.println(topicsRepo.getTopicTree());

        List<String> results;

        results = topicsRepo.findMatchingTopics("a/b/c/d/e");
        System.out.println(results);
        assertEquals(results, List.of("a/b/c/d/e"));

        results = topicsRepo.findMatchingTopics("a/*");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("a/b", "a/x"));

        results = topicsRepo.findMatchingTopics("a/*/c");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("a/bvvvvv/c", "a/b/c", "a/xxxxx/c"));

        results = topicsRepo.findMatchingTopics("a/b*/c*");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("a/bvvvvv/c", "a/b/c", "a/b/ccccccc"));

        results = topicsRepo.findMatchingTopics("a/b*/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("a/bvvvvv/c", "a/b/ccccccc", "a/b/c", "a/b/c/d", "a/b/c/d/e"));

        results = topicsRepo.findMatchingTopics("a/b/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("a/b/ccccccc", "a/b/c", "a/b/c/d", "a/b/c/d/e"));

        results = topicsRepo.findMatchingTopics("a/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("a/x", "a/b", "a/bvvvvv/c", "a/b/ccccccc", "a/b/c", "a/xxxxx/c", "a/b/c/d", "a/b/c/d/e"));

        results = topicsRepo.findMatchingTopics(">");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("x", "a", "a/x", "a/b", "a/bvvvvv/c", "a/b/ccccccc", "a/b/c", "a/xxxxx/c", "a/b/c/d", "a/b/c/d/e"));

        // de-register tests
        topicsRepo.registerTopic("q/w/e");
        topicsRepo.registerTopic("q/w/e/r");
        results = topicsRepo.findMatchingTopics("q/w/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("q/w/e", "q/w/e/r"));

        topicsRepo.deregisterTopic("q/w/e/r");
        results = topicsRepo.findMatchingTopics("q/w/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("q/w/e"));

        topicsRepo.deregisterTopic("q/w/e");
        results = topicsRepo.findMatchingTopics("q/w/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of());

        // diff order
        topicsRepo.registerTopic("q/w/e");
        topicsRepo.registerTopic("q/w/e/r");
        results = topicsRepo.findMatchingTopics("q/w/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("q/w/e", "q/w/e/r"));

        topicsRepo.deregisterTopic("q/w/e");
        results = topicsRepo.findMatchingTopics("q/w/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("q/w/e/r"));

        topicsRepo.deregisterTopic("q/w/e/r");
        results = topicsRepo.findMatchingTopics("q/w/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of());

        topicsRepo.registerTopic("q/w");
        topicsRepo.registerTopic("q");
        results = topicsRepo.findMatchingTopics("q/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("q/w"));

        topicsRepo.deregisterTopic("q");
        results = topicsRepo.findMatchingTopics("q/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of("q/w"));

        topicsRepo.deregisterTopic("q/w");
        results = topicsRepo.findMatchingTopics("q/>");
        System.out.println(results);
        assertEquals(new HashSet<>(results), Set.of());
    }

}
