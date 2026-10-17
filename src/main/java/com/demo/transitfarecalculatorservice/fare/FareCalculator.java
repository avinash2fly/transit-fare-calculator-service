package com.demo.transitfarecalculatorservice.fare;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class FareCalculator {

    private static final List<Route> ROUTES = List.of(
            new Route("Stop1", "Stop2", new BigDecimal("3.25")),
            new Route("Stop2", "Stop3", new BigDecimal("5.50")),
            new Route("Stop1", "Stop3", new BigDecimal("7.30")));

    /** The highest fare from this stop to any other stop. Charged when a rider never taps off. */
    public BigDecimal maxFareFrom(String stop) {
        BigDecimal maxFare = BigDecimal.ZERO;
        for (Route route : ROUTES) {
            if (route.stopA().equals(stop) || route.stopB().equals(stop)) {
                maxFare = maxFare.max(route.fare());
            }
        }
        return maxFare;
    }

    public BigDecimal fareBetween(String fromStop, String toStop) {
        for (Route route : ROUTES) {
            if (route.connects(fromStop, toStop)) {
                return route.fare();
            }
        }
        throw new IllegalArgumentException("No fare defined between " + fromStop + " and " + toStop);
    }

    /** A priced route between two stops.
     * The price is the same in either direction. */
    private record Route(String stopA, String stopB, BigDecimal fare) {

        boolean connects(String from, String to) {
            boolean forwards = stopA.equals(from) && stopB.equals(to);
            boolean backwards = stopA.equals(to) && stopB.equals(from);
            return forwards || backwards;
        }
    }
}
