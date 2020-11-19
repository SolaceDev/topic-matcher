package com.solace.maas.topicmatcher.service;

import java.util.ArrayList;;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public class BeerTopicGenerator {

    private static final Random random = new Random();

    private static final String[] orderDomains = new String[] {"orders", "billing", "returns"};

    private static final String[] productGroup = new String[] {
        "wine",
        "beer",
        "wine",
        "scotch",
        "gin",
        "tequila",
        "cider",
        "spirits"
    };

    private static final String[] actions = new String[] {
            "initialized",
            "updated",
            "processed",
            "canceled"
    };

    private static final String[] versions = new String[] { "v1", "v2", "v3" };

    private static final String[] area = new String[] {
            "Ottawa",
            "Edmonton",
            "Victoria",
            "Winnipeg",
            "Fredericton",
            "StJohns",
            "Halifax",
            "Toronto",
            "Charlottetown",
            "QuebecCity",
            "Regina",
            "Yellowknife",
            "Iqaluit",
            "Whitehorse"
    };

    private static final String[] deliveryModes = new String[] { "car", "truck", "drone", "plane", "submarine" };
    private static final String[] vehicleTypes = deliveryModes;

    private static final String[] deliveryAction = new String[] { "enroute", "delivered", "signatureMissing", "returned", "discpatched", "sentToPostOffice" };


    private String pickRandom(String[] strings) {
        return strings[random.nextInt(strings.length)];
    }

    private String randomId(String prefix, int n) {
        return prefix + random.nextInt(n);
    }

    private String randomFloat(int n) {
        return String.format("%.3f", Math.random() * n *
                (random.nextBoolean() ? -1 : 1));
    }

    public List<String> generateOrderTopics(int n) {
        List<String> topics = new ArrayList<>(n);
        IntStream.range(0, n).forEach(i -> topics.add(String.join("/",
            pickRandom(orderDomains),
            pickRandom(productGroup),
            pickRandom(actions),
            pickRandom(versions),
            pickRandom(area),
            pickRandom(deliveryModes),
            randomId("customer", 50),
            randomId("product", 1000),
            randomId("order", 5000)
        )));
        return topics;
    }

    public List<String> generateDeliveryTopics(int n) {
        List<String> topics = new ArrayList<>(n);
        IntStream.range(0, n).forEach(i -> topics.add(String.join("/",
                "deliver",
                pickRandom(vehicleTypes),
                pickRandom(deliveryAction),
                pickRandom(versions),
                randomFloat(90),
                randomFloat(180),
                randomId("order", 5000)
        )));
        return topics;
    }


}
