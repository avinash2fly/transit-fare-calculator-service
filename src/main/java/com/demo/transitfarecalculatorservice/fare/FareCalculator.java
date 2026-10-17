package com.demo.transitfarecalculatorservice.fare;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FareCalculator {

    /** Fare for each route, keyed by its two stops. A set has no order, so one entry covers both directions. */
    private final Map<Set<String>, BigDecimal> fareByRoute = new HashMap<>();

    /** Highest fare from each stop to any other stop. */
    private final Map<String, BigDecimal> maxFareByStop = new HashMap<>();

    public FareCalculator(List<Route> routes) {
        for (Route route : routes) {
            addRoute(route.stopA(), route.stopB(), route.fare());
        }
    }

    public BigDecimal fareBetween(String fromStop, String toStop) {
        BigDecimal fare = fareByRoute.get(Set.of(fromStop, toStop));
        if (fare == null) {
            throw new IllegalArgumentException("No fare defined between " + fromStop + " and " + toStop);
        }
        return fare;
    }

    /** Charged when a rider taps on but never taps off. */
    public BigDecimal maxFareFrom(String stop) {
        BigDecimal maxFare = maxFareByStop.get(stop);
        if (maxFare == null) {
            throw new IllegalArgumentException("Unknown stop " + stop);
        }
        return maxFare;
    }

    private void addRoute(String stopA, String stopB, BigDecimal fare) {
        fareByRoute.put(Set.of(stopA, stopB), fare);
        keepHigherFare(stopA, fare);
        keepHigherFare(stopB, fare);
    }

    private void keepHigherFare(String stop, BigDecimal fare) {
        BigDecimal currentMax = maxFareByStop.getOrDefault(stop, BigDecimal.ZERO);
        maxFareByStop.put(stop, currentMax.max(fare));
    }
}
