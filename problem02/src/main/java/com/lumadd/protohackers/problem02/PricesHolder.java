package com.lumadd.protohackers.problem02;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class PricesHolder {
    private final Map<Integer, Integer> Prices = new HashMap<>();

    public void put(int timestamp, int price) {
        Prices.put(timestamp, price);
    }

    public int get(int minTimestamp, int maxTimestamp) {
        Set<Integer> validTimestamps = Prices.keySet().stream()
                .filter(t -> minTimestamp <= t && t <= maxTimestamp)
                .collect(Collectors.toSet());

        if (validTimestamps.isEmpty()) {
            return 0;
        }

        long sum = validTimestamps.stream()
                .mapToLong(Prices::get)
                .sum();

        return (int) (sum / validTimestamps.size());
    }
}
