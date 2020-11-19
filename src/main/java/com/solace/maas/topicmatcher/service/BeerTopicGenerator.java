package com.solace.maas.topicmatcher.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BeerTopicGenerator {
    private final Logger log = LoggerFactory.getLogger(BeerTopicGenerator.class);

    private static final Random random = new Random();

    private static final String[] orderDomains = new String[] {"orders", "billing"};
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

    private final double chanceOfStar;
    private final double chanceOfBiggerThan;

    public BeerTopicGenerator(double chanceOfStar, double chanceOfBiggerThan) {
        this.chanceOfStar = chanceOfStar;
        this.chanceOfBiggerThan = chanceOfBiggerThan;
    }

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

    public String generateOrderTopic() {
        return String.join("/",
                generateOrderParts()
        );
    }

    public String generateDeliveryTopic() {
        return String.join("/",
                generateDeliveryParts()
        );
    }


    private List<String> generateDeliveryParts() {
        return Arrays.asList(
                "deliver",
                pickRandom(vehicleTypes),
                pickRandom(deliveryAction),
                pickRandom(versions),
                randomFloat(90),
                randomFloat(180),
                randomId("order", 5000));
    }

    private List<String> generateOrderParts() {
        return Arrays.asList(
                pickRandom(orderDomains),
                pickRandom(productGroup),
                pickRandom(actions),
                pickRandom(versions),
                pickRandom(area),
                pickRandom(deliveryModes),
                randomId("customer", 50),
                randomId("product", 1000),
                randomId("order", 5000));
    }

    public String generateDeliverySubscription() {
        return generateSubscription(generateDeliveryParts());
    }

    public String generateOrderSubscription() {
        return generateSubscription(generateOrderParts());
    }

    public String generateSubscription(String topic) {
        return generateSubscription(Arrays.asList(topic.split("/")));
    }

    public String generateSubscription(List<String> parts) {
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<parts.size(); i++) {
            if (i != 0 ){
                sb.append("/");
            }
            if (Math.random() < chanceOfBiggerThan) {
                sb.append(">");
                return sb.toString();
            }
            String part = parts.get(i);
            if (Math.random() < chanceOfStar) {
                int pos = random.nextInt(part.length());
                sb.append(part.substring(0, pos) + '*');
            } else {
                sb.append(part);
            }
        }
        return sb.toString();
    }
}
